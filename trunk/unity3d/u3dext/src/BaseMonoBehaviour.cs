using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
//using u3dext;
using UnityEngine;


/// <summary>
/// Provides:
/// 	Main menu displaying and handling.
/// 	Screen touch pre-handling.
/// 	Mouse events pre-handling.
/// 	Displaying FPS.
///		Debug (remote and local).
/// @Author: Yuxing Wang
/// @Version: 0.1
/// </summary>
public abstract class BaseMonoBehaviour : MonoBehaviour	{
	
	public const int DEFAULT_BUTTON_WIDTH = 60;
	public const int DEFAULT_BUTTON_HEIGHT = 60;

	// ==== Settings in Unity Editor ==== 
	public bool debugMode = false;
	
	public float turnSpeed = 20;
	
	public float walkSpeed = 10;
	
	public float runSpeed = 20;
	
	// Beep audio for menu operation.
	public AudioClip beepMenu;
	
	
	// Controller for current game object.
	protected CharacterController controller;
	
	// Show FPS in this rectangle.
	protected Rect rectFPS;
	protected Rect rectDebugInputConsole;
	protected Rect rectDebugConsole;

	// Control main menu.
	protected bool isShowMenuButton = false;
	protected bool isMenuOpend = false;
	
	// Avoid unneccessary touch and mouse events on screen.
	protected bool isTouchingScreen = false;
	
	protected bool isMousePreesedOnScreen = false; // Flag that mouse pressed on screen.
	protected Vector2 mousePressedPositionOnScreen; // Mouse position when pressed down.
	private Vector2 mouseLastFramePositionOnScreen; // 
	
	protected bool isMousePressed = false; // Mouse pressed on game object.
	protected Vector2 mousePressedPosition; // Store the mouse position when mouse pressed on game object.
	
	// 
	protected bool isAccelerate = false;
	
	// Screen width and height.
	protected float sw;
	protected float sh;
	
	// Half of screen width and height.
	protected float hsw;
	protected float hsh;
	
	// Screen button flags for left hand and right hand.
	protected int leftHandBtnFlag = 0;
	protected int rightHandBtnFlag = 0;

	// 4 calculating FPS.
	private System.DateTime lastFpsTime;
	private int currentFPS = 0;
	private string fpsLabel = "FPS: 0";
	
	// Remote debug shared instance.
	protected static RemoteDebug remoteDebug ;
	
	// Screen debug.
	protected static ScreenDebug screenDebug = new ScreenDebug();
		
	// Use this for initialization
	protected void Start () {
		
		System.Object[] attrs = this.GetType().GetCustomAttributes(true);
		
		Debug.Log("annotations:" + attrs);
		
		if (debugMode) {
			Debug.Log("This script" + this.GetType() + " runs on DEBUG mode");
			
			lastFpsTime = System.DateTime.Now;
			
			rectDebugInputConsole = new Rect(10, 10, 200, 30);
			
			rectFPS = new Rect(10, 50, 80, 30);
			
			// Remote Debugger
			if (remoteDebug == null) {
				remoteDebug = new RemoteDebug(Debug.Log);
			}

			try {
				remoteDebug.startTcpListener();
				remoteDebug.fork(delegate {
					Debug.Log("debug client connected");
				});
				debug(this.GetType() + " is ready to send debug info");
			} catch (Exception e) {
				Console.WriteLine(e.StackTrace);
			}
		}

		
		sw = Screen.width;
		sh = Screen.height;
		
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
	
	protected bool isMobilePlatform() {
		return Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer;
	}
	
	protected void OnGUI () {
		
		if (debugMode) {
			// Show FPS.
			GUI.Box (rectFPS, fpsLabel);
			
			// User input debug.
			String msg = "L: " + Convert.ToString (leftHandBtnFlag) + ", R: " + Convert.ToString (rightHandBtnFlag);
			GUI.Box (rectDebugInputConsole, msg);
			
			//  Screen debug console.
			GUI.TextArea (new Rect (10, 100, 350, 17 * ScreenDebug.MESSAGE_LENGTH), screenDebug.contatDebugInfo ());
		}

		// ==== Menu Handling ====
		if (isShowMenuButton) {
			if (GUI.Button (new Rect (Screen.width - 90, 10, 80, 40), "MENU")) {
				isMenuOpend = !isMenuOpend;
				audio.PlayOneShot (beepMenu);
			}
		}

		if (isMenuOpend == true) {
			Rect menuRect = new Rect (Screen.width / 2 - 100, Screen.height / 2 - 100, 200, 200);
			GUILayout.Window (0, menuRect, OnMenuCreated, "  == MENU == ");
		}

		// ==== User Input Touch ====
//		if(isDebugTouch) {
//			GUIStyle style = new GUIStyle();
//			GUI.TextArea(new Rect(10, 100, 350, 17 * infos.Length), contatDebugInfo());
//		}
		
		// Mouse press events on screen.
		for (int i = 0; i < 2; i++) {
			if (isMousePreesedOnScreen == false && Input.GetMouseButtonDown (i)) {
				mousePressedPositionOnScreen = Utils.convert3Dto2D (Input.mousePosition);
				mouseLastFramePositionOnScreen = mousePressedPositionOnScreen;
				isMousePreesedOnScreen = true;
				this.OnScreenMouseDown (i, mousePressedPositionOnScreen);				
			}
			if (isMousePreesedOnScreen == true && Input.GetMouseButtonUp (i)) {
				isMousePreesedOnScreen = false;
				this.OnScreenMouseUp (i, Input.mousePosition);
			}
		}
		
		Vector2 thisFrameMousePos = Utils.convert3Dto2D (Input.mousePosition);
		this.OnScreenMouseOver (thisFrameMousePos, thisFrameMousePos - mouseLastFramePositionOnScreen);
		mouseLastFramePositionOnScreen = thisFrameMousePos;

		// Raise touch events or ray hits event for touch screen devices.
		if (isMobilePlatform () == true) {
			for (int i=0; i<Input.touches.Length; i++) {
				Touch touch = Input.touches [i];
				Vector2 eachPos = touch.position;
				if (isTouchingScreen == false && touch.phase == TouchPhase.Began) {
					isTouchingScreen = true;
					this.OnTouchDown (touch.fingerId, eachPos);
				}
				else if (isTouchingScreen == true && touch.phase == TouchPhase.Ended) {
					this.OnTouchUp (touch.fingerId, eachPos);
					isTouchingScreen = false;
				}
				else if (touch.phase == TouchPhase.Moved) {
					this.OnTouchMove (touch.fingerId, eachPos, touch.deltaPosition);
				}
			}
			// Detect ray hits from screen touch point.
			for (int i=0; i<Input.touches.Length; i++) {
				Touch touch = Input.touches [i];
				this.rayHitGameObjectAndCallback (touch.position);
			}
		}
		
		// Raise ray hits event for mouse input.
		if (isMousePressed == false && Input.GetMouseButtonDown (0)) {
			isMousePressed = true;
			screenDebug.log ("Press mouse on screen position " + Input.mousePosition);
			this.rayHitGameObjectAndCallback (Input.mousePosition);
		}
		else if (isMousePressed == true && Input.GetMouseButtonUp (0)) {
			screenDebug.log ("Release mouse on screen position " + Input.mousePosition);
			isMousePressed = false;
		}
	}
	
	
	/// <summary>
	/// Be called after user clicked the "MENU" button.
	/// </summary>
	/// <param name="windowId"></param>
	protected virtual void OnMenuCreated (int windowId) {
		
	}

	protected void OnMouseDown () {
		isMousePressed = true;
		mousePressedPosition = Utils.convert3Dto2D(Input.mousePosition);
	}
	
	protected void OnMouseUp () {
		isMousePressed = false;
		mousePressedPosition = new Vector2 ();
	}
	
	protected virtual void OnScreenMouseDown (int button, Vector2 mousePosition) {
		this.debug ("Button " + button + " pressed down at position " + mousePosition);
	}
	
	protected virtual void OnScreenMouseOver (Vector2 mousePosition, Vector2 deltaPosition) {
		this.debug ("Mouse moved over " + deltaPosition + " to position " + mousePosition);
	}
	
	protected virtual void OnScreenMouseUp(int button, Vector2 mousePosition) {
		this.debug ("Button " + button + " released at position " + mousePosition);
	}
	
	/// <summary>
	/// Be called every time user touched screen.
	/// </summary>
	/// <param name="touchId"></param>
	/// <param name="touchPosition"></param>
	protected virtual void OnTouchDown(int touchId, Vector2 touchPosition){
		this.debug (touchId + " touched down at position: " + touchPosition);
	}

	/// <summary>
	/// Be called every time user released touching screen.
	/// </summary>
	/// <param name="touchId"></param>
	/// <param name="touchPosition"></param>
	protected virtual void OnTouchUp(int touchId, Vector2 touchPosition){
		this.debug (touchId + " touched up at position: " + touchPosition + "  " + System.DateTime.Now.Ticks / 10000f * 1000f);
	}

	/// <summary>
	/// Be called every time user move on touch screen.
	/// </summary>
	/// <param name="touchId"></param>
	/// <param name="touchPosition"></param>
	protected virtual void OnTouchMove(int touchId, Vector2 touchPosition, Vector2 deltaPosition){
		this.debug (touchId + " touched move at position: " + touchPosition);
	}
		
	//
	protected bool rayHitGameObjectAndCallback(Vector3 screenPos) {
		// Detect by Ray
		this.debug("Ray at position: " + screenPos);
		Ray ray = Camera.main.ScreenPointToRay(screenPos);
		RaycastHit hit;
		Debug.DrawRay(ray.origin, ray.direction, Color.red);
		if(Physics.Raycast(ray.origin, ray.direction, out hit)) {
			this.debug("Ray " + ray + " hits " + hit.collider);
			return this.OnGameObjectTouchedOnScreen(hit.collider.name);
		}
		return false;
	}
	
	/// <summary>
	/// Be called when user touchs or clicks on screen with ray hit a game object.
	/// </summary>
	/// <param name='objectName'>
	/// Object name.
	/// </param>
	protected virtual bool OnGameObjectTouchedOnScreen(String objectName) {
		this.debug ("Game object " + objectName + " be touched on screen");
		return true;
	}

	
	protected void Update() {
		// Calculating FPS.
		if (System.DateTime.Now.Ticks - lastFpsTime.Ticks > 10000 * 1000) {
			fpsLabel = "FPS: " + this.currentFPS;
			this.currentFPS = 0;
			lastFpsTime = System.DateTime.Now;
		}
		else {
			this.currentFPS++;
		}
	}
	
	protected void Destroy() {
		Debug.Log("Destroying...");
//		remoteDebug("Destroying...");
//		if(debugMode) {
//			remoteDebug.stop();
//		}
	}

	public void debug(String msg) {
		if(remoteDebug != null) {
			remoteDebug.log(DateTime.Now.ToLongTimeString() + " DEBUG ", msg);
		}
	}	
	
	public void info(String msg) {
		if(remoteDebug != null) {
			remoteDebug.log(DateTime.Now.ToLongTimeString() + " INFO ", msg);
		}
	}
	
	public void error(String msg) {
		if(remoteDebug != null) {
			remoteDebug.log(DateTime.Now.ToLongTimeString() + " ERROR ", msg);
		}
	}
}

