﻿using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Collections.Generic;
//using u3dext;
using System.Threading;
using UnityEngine;
using u3dext;

/// <summary>
/// Provides:
/// 	Main menu displaying and handling.
/// 	Screen touch pre-handling.
/// 	Mouse events pre-handling.
/// 	Displaying FPS, Screen Touch Points.
///		Debug (remote and local).
/// Approaches:
/// * Create menu: 
/// 	Overide and implement method OnMenuCreated(windowId).
/// * Game Object Touching: 
/// 	Override and implement methods OnGameObjectHitDown() and OnGameObjectHitUp(), 
/// 	Either touch(click) down or up, a ray will cast from where you touched(clicked). 
/// 	OnGameObjectHitDown() will be called when the ray hit game object.
/// 	OnGameObjectHitUp() will be called when 
/// @Author: Yuxing Wang
/// @Version: 0.1
/// </summary>
public abstract class BaseMonoBehaviour : MonoBehaviour	{
	
	public const int DEFAULT_BUTTON_WIDTH = 60;
	public const int DEFAULT_BUTTON_HEIGHT = 60;

	// ==== Settings in Unity Editor ==== 
	public bool debugMode = false;
	
	public bool remoteDebugMode = false;
	
	public bool profilingMode = false;
	
	public float turnSpeed = 20;
	
	public float walkSpeed = 10;
	
	public float runSpeed = 20;
	
	// Beep audio for menu operation.
	public AudioClip beepMenu;
	
	
	// Controller for current game object.
	protected CharacterController controller;
	
	// === GUI ===
	protected Rect dialogRect;
	protected Rect menuRect;
	protected Rect menuButtonRect;

	// Control main menu.
	protected bool isShowMenuButton = false;
	protected bool isMenuOpend = false;

	// Character moving status 
	protected bool isAccelerate = false;
	
	// For level-based game, 0=Playing, 1=Passed, 2=Failed
	protected int levelPassStatus = 0;
	
	// Screen width and height.
	protected float sw;
	protected float sh;
	
	// Half of screen width and height.
	protected float hsw;
	protected float hsh;
	
	// Screen button flags for left hand and right hand.
	protected int leftHandBtnFlag = 0;
	protected int rightHandBtnFlag = 0;
	
	// === DEBUGING ===
	
	// Show FPS in this rectangle.
	protected Rect rectFPS;
	protected Rect rectDebugInputConsole;
	protected Rect rectDebugTouchPoint;
	protected Rect rectDebugConsole;
	
	// 4 calculating FPS.
	private System.DateTime lastFpsTime;
	private int currentFPS = 0;
	private string fpsLabel = "FPS: 0";
//	private int[] touchFlags = new int[5];
//	private bool[] mouseFlags = new bool[3];
	
	// Profiling
	private float profileSummaryTime = 0;
	
	// Remote debug shared instance.
	protected RemoteDebug debugConsole;
	
	// Screen debug.
//	protected static ScreenDebug screenDebug = new ScreenDebug();
	
	protected Dictionary<String, Boolean> flagDebug = new Dictionary<String, Boolean>();
	
	// ===  ===	
	// Use this for initialization
	protected void Start () {
		
		Debug.Log("Script '" + this.GetType().Name + "' runs on thread " + Thread.CurrentThread.ManagedThreadId);
		
		System.Object[] attrs = this.GetType().GetCustomAttributes(true);
		
		if (debugMode) {
			Debug.Log("Script" + this.GetType() + " runs on DEBUG mode");

			lastFpsTime = System.DateTime.Now;
			
			rectDebugInputConsole = new Rect(5, 5, 150, 25);
			
			rectDebugTouchPoint = new Rect(5, 35, 150, 25);
			
			rectFPS = new Rect(5, 65, 70, 25);
			
			
			// Remote Debugger
			if (debugConsole == null) {
				debugConsole = RemoteDebug.getInstance(Debug.Log);
			}
			
			if (remoteDebugMode) {
				try {
					debugConsole.startTcpListener();
					debugConsole.fork(delegate {
						Debug.Log("debug client connected");
					}
					);
					debug(this.GetType() + " is ready to send debug info");
				} catch (Exception e) {
					Debug.Log(e.StackTrace);
				}
			}
			
//			flagDebug.Add("MPS", isMousePreesedOnScreen);
//			flagDebug.Add("TS", isTouchingScreen);

		}

		sw = Screen.width;
		sh = Screen.height;
		
		hsw = Screen.width / 2;
		hsh = Screen.height / 2;
		
		controller = (CharacterController)this.GetComponent(typeof(CharacterController));

		menuButtonRect = new Rect(Screen.width - 90, 5, 80, 40);
		dialogRect = new Rect(hsw - 100, hsh - 100, 200, 200);
		menuRect = new Rect(hsw - 100, hsh - 100, 200, 200);
	}
	
	protected Camera getCamera(String name) {
		if(name == null) {
			return null;
		}
		UnityEngine.Object[] allCameras = GameObject.FindObjectsOfType(typeof(Camera));
		for (int i = 0; i < allCameras.Length; i++) {
			Camera c = (Camera)allCameras[i];
//			debug(c.name);
			if(name.Equals(c.name)) {
				return c;
			}
		}
		return null;
	}
	
	protected bool isMobilePlatform() {
		return Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer;
	}
	
	// === OnGUI Private ===
	
	protected void OnGUI () {
		// === DEBUG ===
		if (debugMode) {
			// Show FPS.
			GUI.Box(rectFPS, fpsLabel);
			
			// User input debug.
			String msg = "L: " + Convert.ToString(leftHandBtnFlag) + ", R: " + Convert.ToString(rightHandBtnFlag);
			GUI.Box(rectDebugInputConsole, msg);
			
			//  Screen debug console.
//			GUI.TextArea(new Rect(10, 100, 350, 17 * ScreenDebug.MESSAGE_LENGTH), screenDebug.contatDebugInfo());
			
			// Show FLAGs
			int left = 10;
			foreach (KeyValuePair<String, Boolean> item in flagDebug) {
				Rect rect = new Rect(left, sh - 30, 50, 20);
				left += 60;
				if (item.Value == true) {
					GUI.Box(rect, item.Key);
				} else {
					GUI.Box(rect, "-");	
				}
			}
			
			// Show Touch Points
			StringBuilder touchText = new StringBuilder();		
			touchText.Append("M: ");
			for (int i = 0; i < BaseState.mouseFlags.Length; i++) {
				touchText.Append(BaseState.mouseFlags[i] == true ? "O" : "x");
			}
			touchText.Append(", T: ");
			for (int i = 0; i < BaseState.touchFlags.Length; i++) {
				touchText.Append(BaseState.touchFlags[i] == 1 ? "O" : "x");
			}
			GUI.Box(rectDebugTouchPoint, touchText.ToString());
		}

		// ==== Menu Handling ====
		if (isShowMenuButton) {
			if (GUI.Button(menuButtonRect, "MENU")) {
				isMenuOpend = !isMenuOpend;
				audio.PlayOneShot(beepMenu);
			}
		}

		if (isMenuOpend == true) {
			GUILayout.Window(0, menuRect, OnMenuCreated, "  == MENU == ");
		}
		
		// Show level pass dialog.
		if (levelPassStatus == 1) {
			GUILayout.Window(0, dialogRect, OnLevelPassDialogCreated, "  ==  == ");
		} else if (levelPassStatus == 2) {
			// Show level fail dialog. 
			GUILayout.Window(0, dialogRect, OnLevelFailDialogCreated, "  ==  == ");
		}

		// ==== User Input Touch ====
//		if(isDebugTouch) {
//			GUIStyle style = new GUIStyle();
//			GUI.TextArea(new Rect(10, 100, 350, 17 * infos.Length), contatDebugInfo());
//		}
		
		// Mouse events on screen. (Mobile devices will get mouse event when touchs screen, so...)
		if (isMobilePlatform() == false) {
			u3dext.Profiler.getInstance().start("GUI.Mouse");
			BaseState.changedByMouseInput(
				OnScreenMouseDown,
				OnScreenMouseOver,
				OnScreenMouseUp,
				OnGameObjectHitDown,
				OnGameObjectHitUp
			);
			u3dext.Profiler.getInstance().end("GUI.Mouse");

//			for (int i = 0; i < 2; i++) {
//				if (isMousePreesedOnScreen == false && Input.GetMouseButtonDown(i)) {
//					mousePressedPositionOnScreen = Utils.convert3Dto2D(Input.mousePosition);
//					mouseLastFramePositionOnScreen = mousePressedPositionOnScreen;
//					isMousePreesedOnScreen = true;
//					mouseFlags[i] = true;
//					// Callback
//					this.OnScreenMouseDown(i, mousePressedPositionOnScreen);			
//				}
//				if (isMousePreesedOnScreen == true && Input.GetMouseButtonUp(i)) {
//					isMousePreesedOnScreen = false;
//					mouseFlags[i] = false;
//					// Callback
//					this.OnScreenMouseUp(i, Input.mousePosition);
//				}
//			}
//		
//			Vector2 thisFrameMousePos = Utils.convert3Dto2D(Input.mousePosition);
//			if (thisFrameMousePos != mouseLastFramePositionOnScreen) {
//				// Callback
//				this.OnScreenMouseOver(thisFrameMousePos, thisFrameMousePos - mouseLastFramePositionOnScreen);
//				mouseLastFramePositionOnScreen = thisFrameMousePos;
//			}
//			
//			// Raise ray hits event for mouse input.
//			u3dext.Profiler.getInstance().start("GUI.Mouse.Ray");
//			if (isMousePressed == false && Input.GetMouseButtonDown(0)) {
//				isMousePressed = true;
//				screenDebug.log("Press mouse on screen position " + Input.mousePosition);
//				String hitObjName = this.rayHitGameObject(Input.mousePosition);
//				if (hitObjName != null) {
//					u3dext.Profiler.getInstance().start("GUI.Mouse.Ray.Down");
//					this.OnGameObjectHitDown(hitObjName);
//					u3dext.Profiler.getInstance().end("GUI.Mouse.Ray.Down");
//				}
//			} else if (isMousePressed == true && Input.GetMouseButtonUp(0)) {
//				screenDebug.log("Release mouse on screen position " + Input.mousePosition);
//				isMousePressed = false;
//				String hitObjName = this.rayHitGameObject(Input.mousePosition);
//				if (hitObjName != null) {
//					u3dext.Profiler.getInstance().start("GUI.Mouse.Ray.Up");
//					this.OnGameObjectHitUp(hitObjName);
//					u3dext.Profiler.getInstance().end("GUI.Mouse.Ray.Up");
//				}
//			}
//			u3dext.Profiler.getInstance().end("GUI.Mouse.Ray");
		}

		// Raise touch events and ray hits events for touch screen devices.
		if (isMobilePlatform() == true) {
			u3dext.Profiler.getInstance().start("GUI.Touch");
			BaseState.changedByTouch(
				OnTouchDown,
				OnTouchMove,
				OnTouchUp,
				OnGameObjectHitDown,
				OnGameObjectHitUp,
				OnZoomInAndOut
			);
//			for (int i=0; i<Input.touches.Length; i++) {
//				Touch touch = Input.touches[i];
////				int touchId = touch.fingerId;
//				//debug("##" + touch.fingerId + ", " + touch.phase);
//				Vector2 eachPos = touch.position;
//				if (touch.phase == TouchPhase.Began) {
//					// Callback(Run before everything to ensure being called)
//					if (isTouchingScreen[touch.fingerId] == false) {
//						
//						// Exclude some non-zoom(like button ) touch.
//						//this.OnTouchDown(touch.fingerId, eachPos);
//						if (this.OnTouchDown(touch.fingerId, eachPos) == false) {
//							BaseState.nonZoomId = touch.fingerId;
//						} else {
//							if (BaseState.zoomTouchIds[0] < 0 && BaseState.zoomTouchIds[1] < 0) {
//								BaseState.zoomTouchIds[0] = touch.fingerId;
//								BaseState.zoomPoint[touch.fingerId] = touch.position;
//							} else {
//								BaseState.zoomMode = true;
//								BaseState.zoomTouchIds[1] = touch.fingerId;
//								BaseState.zoomPoint[touch.fingerId] = touch.position;
//							}
//						}
//					}
//					isTouchingScreen[touch.fingerId] = true;
//					
//					// Detect ray hits from screen touch point.
//					String hitObjName = this.rayHitGameObject(touch.position);
//					if (hitObjName != null) {
//						this.OnGameObjectHitDown(hitObjName);
//					}
//					
//				} else if (touch.phase == TouchPhase.Ended) {
//					// Callback
//					if (isTouchingScreen[touch.fingerId] == true) {
//						this.OnTouchUp(touch.fingerId, eachPos);
//					}
//					isTouchingScreen[touch.fingerId] = false;
//					String hitObjName = this.rayHitGameObject(touch.position);
//					if (hitObjName != null) {
//						this.OnGameObjectHitUp(hitObjName);
//					}
//					
//				} else if (touch.phase == TouchPhase.Moved) {
//					// Callback
//					if (BaseState.zoomMode == true) {
//						// Distance between 2 touch points at last frame.
//						float preDistance = BaseState.getZoomPointDistance();//Vector2.Distance(zoomPoint[zoomTouchIds[0]], zoomPoint[zoomTouchIds[1]]);
//			
//						float curDistance = 0f; 
//						if (touch.fingerId == BaseState.zoomTouchIds[0]) {
//							curDistance = Vector2.Distance(touch.position, BaseState.zoomPoint[BaseState.zoomTouchIds[1]]);
//						} else if (touch.fingerId == BaseState.zoomTouchIds[1]) {
//							curDistance = Vector2.Distance(BaseState.zoomPoint[BaseState.zoomTouchIds[0]], touch.position);
//						}
//
//						float delta = curDistance - preDistance;
//						float zoomDistance = (delta / 50) * Time.deltaTime;
//						this.OnZoomInAndOut(zoomDistance);
//					} else {
//						this.OnTouchMove(touch.fingerId, eachPos, touch.deltaPosition);						
//					}
//				}
//			}
			u3dext.Profiler.getInstance().end("GUI.Touch");
		}// Touch
	}
	
	
	/// <summary>
	/// Be called after user clicked the "MENU" button.
	/// </summary>
	/// <param name="windowId"></param>
	protected virtual void OnMenuCreated (int windowId) {
//		GUILayout.Label("Hello World");
	}
	
	protected virtual void OnLevelPassDialogCreated(int windowId) {}
	
	protected virtual void OnLevelFailDialogCreated(int windowId) {}

	// TBD?
	protected void OnMouseDown () {
		BaseState.isMousePressed = true;
		BaseState.mousePressedPosition = Utils.convert3Dto2D(Input.mousePosition);
	}
	
	// TBD?
	protected void OnMouseUp () {
		BaseState.isMousePressed = false;
		BaseState.mousePressedPosition = new Vector2 ();
	}
	
	/// <summary>
	/// Raises the screen mouse down event.
	/// </summary>
	/// <param name='button'>
	/// Button.
	/// </param>
	/// <param name='mousePosition'>
	/// Mouse position. Start from bottom to top.
	/// </param>
	protected virtual void OnScreenMouseDown (int button, Vector2 mousePosition) {
		this.debug("Mouse Button " + button + " pressed down at position " + mousePosition);
	}
	
	/// <summary>
	/// Raises the screen mouse over event.
	/// </summary>
	/// <param name='mousePosition'>
	/// Mouse position.
	/// </param>
	/// <param name='deltaPosition'>
	/// Delta position. Start from bottom to top.
	/// </param>
	protected virtual void OnScreenMouseOver (Vector2 mousePosition, Vector2 deltaPosition) {
		this.debug("Mouse moved over " + deltaPosition + " to position " + mousePosition);
	}
	
	/// <summary>
	/// Be called each time a mouse button up at screen position.
	/// </summary>
	/// <param name='button'>
	/// Button.
	/// </param>
	/// <param name='mousePosition'>
	/// Mouse position. Start from bottom to top.
	/// </param>
	protected virtual void OnScreenMouseUp(int button, Vector2 mousePosition) {
		this.debug ("Mouse Button " + button + " released at position " + mousePosition);
	}
	
	/// <summary>
	/// Be called every time user touched screen.
	/// </summary>
	/// <param name="touchId"></param>
	/// <param name="touchPosition"></param>
	/// <returns>True if touched down for zoom, false if not touched down for zoom</returns>
	protected virtual bool OnTouchDown (int touchId, Vector2 touchPosition) {
		this.debug("Finger " + touchId + " touched down at position: " + touchPosition);
		BaseState.touchFlags[touchId] = 1;
		return true;
	}

	/// <summary>
	/// Be called every time user move on touch screen.
	/// </summary>
	/// <param name="touchId"></param>
	/// <param name="touchPosition"></param>
	protected virtual void OnTouchMove (int touchId, Vector2 touchPosition, Vector2 deltaPosition) {
		this.debug("Finger " + touchId + " touched move at position: " + touchPosition);
	}
	
	/// <summary>
	/// Be called every time user released touching screen.
	/// </summary>
	/// <param name="touchId"></param>
	/// <param name="touchPosition"></param>
	protected virtual void OnTouchUp (int touchId, Vector2 touchPosition) {
		this.debug("Finger " + touchId + " touched up at position: " + touchPosition + "  " + System.DateTime.Now.Ticks / 10000f * 1000f);
		BaseState.touchFlags[touchId] = 0;
		if (touchId == BaseState.nonZoomId) {
			BaseState.nonZoomId = -1;
		} else {
			if (BaseState.zoomMode == true) {
				BaseState.zoomMode = false;
			} 
			if (BaseState.zoomTouchIds[0] == touchId) {
				BaseState.zoomTouchIds[0] = -1;
			} else if (BaseState.zoomTouchIds[1] == touchId) {
				BaseState.zoomTouchIds[1] = -1;
			}
		}
	}
	
	protected virtual void OnZoomInAndOut (float delta) {
		debug("ZOOM: " + delta);
	}
		
	//
//	protected String rayHitGameObject (Vector3 screenPos) {
//		// Detect by Ray
//		//this.debug("Ray at screen position: " + screenPos);
//		Ray ray = Camera.main.ScreenPointToRay(screenPos);
//		RaycastHit hit;
//		Debug.DrawRay(ray.origin, ray.direction, Color.red);
//		if (Physics.Raycast(ray.origin, ray.direction, out hit)) {
//			//this.debug("Ray " + ray + " hits " + hit.collider);
//			return hit.collider.name;
//		}
//		return null;
//	}
	
	/// <summary>
	/// Be called when touchs or clicks down on screen with ray hit a game object.
	/// </summary>
	/// <param name='objectName'>
	/// Object name.
	/// </param>
	protected virtual bool OnGameObjectHitDown (String objectName) {
		this.debug("Game object " + objectName + " be hit down on screen");
		return true;
	}
	
	/// <summary>
	/// Be called when touchs or clicks up on screen with ray hit a game object.
	/// </summary>
	/// <param name='objectName'>
	/// If set to <c>true</c> object name.
	/// </param>
	protected virtual bool OnGameObjectHitUp (string objectName) {
		this.debug("Game object " + objectName + " be hit up on screen");
		return true;
	}

	
	protected void Update () {
		// Calculating FPS.
		if (System.DateTime.Now.Ticks - lastFpsTime.Ticks > 10000 * 1000) {
			fpsLabel = "FPS: " + this.currentFPS;
			this.currentFPS = 0;
			lastFpsTime = System.DateTime.Now;
		} else {
			this.currentFPS++;
		}
		
		// Profiling
		if (profilingMode == true) {
			if (profileSummaryTime > 30) {
				u3dext.Profiler.getInstance().print(debug);
				profileSummaryTime = 0;
			} else {
				profileSummaryTime += Time.deltaTime; 
			}
		}
	}
	
	protected void Destroy () {
		Debug.Log("Destroying...");
//		remoteDebug("Destroying...");
//		if(debugMode) {
//			remoteDebug.stop();
//		}
	}

	public void debug(System.Object msg) {
		if(debugConsole != null) {
			debugConsole.log(DateTime.Now.ToLongTimeString() + " DEBUG ", msg.ToString());
		}
	}	
	
	public void info(System.Object msg) {
		if(debugConsole != null) {
			debugConsole.log(DateTime.Now.ToLongTimeString() + " INFO ", msg.ToString());
		}
	}
	
	public void error(System.Object msg) {
		if(debugConsole != null) {
			debugConsole.log(DateTime.Now.ToLongTimeString() + " ERROR ", msg.ToString());
		}
	}
}

