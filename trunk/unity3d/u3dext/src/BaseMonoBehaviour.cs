using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using u3dext;

namespace u3dext {
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
	public const int DEFAULT_LINE_HEIGHT = 25;

	// ==== Settings in Unity Editor ==== 
	public bool debugMode = false;

	// 0 is left, 1 is top.
	public int[] debugDisplayPosition = new int[]{0, 150};

	public bool remoteDebugMode = false;
	
	public bool profilingMode = false;
	
	public float turnSpeed = 20;
	
	public float walkSpeed = 10;
	
	public float runSpeed = 20;
	
	public float zoomSpeed = 10;
	
	// Beep audio for menu operation.
	public AudioClip beepMenu;
	
	// Controller for current game object.
	protected CharacterController controller;


	// Character moving status 
	protected bool isAccelerate = false;

	
	// Screen diagonal size
	protected float sd;
	
	// Screen width and height.
	protected float sw;
	protected float sh;
	
	// Half of screen width and height.
	protected float hsw;
	protected float hsh;
	
	// Screen button flags for left hand and right hand.
	protected int leftHandBtnFlag = 0;
	protected int rightHandBtnFlag = 0;

	
	// How long it will print the profiling data.
	private float profileSummaryTime = 0;
	
	
	protected UserControlState state;
	
	// Remote debug shared instance.
	protected RemoteDebug debugConsole;

	protected Profiler profiler;


	protected delegate void Closure0();

	protected delegate void Closure1(System.Object param);
	
	
	// Screen debug.
//	protected static ScreenDebug screenDebug = new ScreenDebug();
	
	protected Dictionary<String, Boolean> flagDebug = new Dictionary<String, Boolean>();
	
	// ===  ===	
	// Use this for initialization
	protected void Start () {
		
		Debug.Log("Script '" + this.GetType().Name + "' runs on thread " + Thread.CurrentThread.ManagedThreadId);

		profiler = u3dext.Profiler.getInstance();

		state = new UserControlState();
		
		if (debugMode) {
			Debug.Log("Script" + this.GetType() + " runs on DEBUG mode");

			
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
		
		sd = (float)Math.Sqrt(sw * sw + sh * sh);
		
		hsw = Screen.width / 2;
		hsh = Screen.height / 2;
		
		controller = (CharacterController)this.GetComponent(typeof(CharacterController));

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

	// === OnGUI Private ===
	
	protected void OnGUI () {

	}

	protected void Update () {
//		debug (this.GetType().Name + ".Update()");

		if(GameState.isGamePausing == true) {
			return;
		}

		// Mouse events and ray hits events on screen. (Mobile devices will get mouse event when touchs screen, so...)
		if (Utils.isMobilePlatform() == false) {
			u3dext.Profiler.getInstance().start("Mouse");
			state.changedByMouseInput(
				OnScreenMouseDown,
				OnScreenMouseOver,
				OnScreenMouseUp,
				OnGameObjectHitDown,
				OnGameObjectHitUp
			);
			u3dext.Profiler.getInstance().end("Mouse");
		}
		
		// Raise touch events and ray hits events for touch screen devices.
		if (Utils.isMobilePlatform() == true) {
			u3dext.Profiler.getInstance().start("Touch");
			state.changedByTouch(
				OnTouchDown,
				OnTouchMove,
				OnTouchUp,
				OnGameObjectHitDown,
				OnGameObjectHitUp,
				OnZoomInAndOut
			);
			u3dext.Profiler.getInstance().end("Touch");
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
	/// <returns>True if touched down for zoom or rotate</returns>
	protected virtual bool OnTouchDown (int touchId, Vector2 touchPosition) {
		this.debug("Finger " + touchId + " touched down at position: " + touchPosition);
		state.touchFlags[touchId] = 1;
		return true;
	}

	/// <summary>
	/// Be called every time user move on touch screen.
	/// </summary>
	/// <param name="touchId"></param>
	/// <param name="touchPosition"></param>
	/// <returns>True if touched down for zoom or rotate</returns>
	protected virtual bool OnTouchMove (int touchId, Vector2 touchPosition, Vector2 deltaPosition) {
		this.debug("Finger " + touchId + " touched move at position: " + touchPosition);
		return true;
	}
	
	/// <summary>
	/// Be called every time user released touching screen.
	/// </summary>
	/// <param name="touchId"></param>
	/// <param name="touchPosition"></param>
	protected virtual void OnTouchUp (int touchId, Vector2 touchPosition) {
		this.debug("Finger " + touchId + " touched up at position: " + touchPosition + "  " + System.DateTime.Now.Ticks / 10000f * 1000f);
		state.touchFlags[touchId] = 0;
		if (touchId == state.nonZoomId) {
			state.nonZoomId = -1;
		} else {
			if (state.zoomMode == true) {
				state.zoomMode = false;
			} 
			if (state.zoomTouchIds[0] == touchId) {
				state.zoomTouchIds[0] = -1;
			} else if (state.zoomTouchIds[1] == touchId) {
				state.zoomTouchIds[1] = -1;
			}
		}
	}
	
	/// <summary>
	/// Be invoked every time Zoom In or Zoom out.
	/// </summary>
	/// <param name='delta'>
	/// How many distance the 2 fingers totaly passed through.
	/// </param>
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

	
	protected void Destroy () {
		Debug.Log("Destroying...");
//		remoteDebug("Destroying...");
//		if(debugMode) {
//			remoteDebug.stop();
//		}
	}

	public void debug(System.Object msg) {
		if(debugConsole != null) {
			debugConsole.log(DateTime.Now.ToShortTimeString() + " DEBUG " + this.GetType().Name, msg.ToString());
		}
	}	
	
	public void info(System.Object msg) {
		if(debugConsole != null) {
			debugConsole.log(DateTime.Now.ToShortTimeString() + " INFO " + this.GetType().Name, msg.ToString());
		}
	}
	
	public void error(System.Object msg) {
		if(debugConsole != null) {
			debugConsole.log(DateTime.Now.ToShortTimeString() + " ERROR " + this.GetType().Name, msg.ToString());
		}
	}
}

}