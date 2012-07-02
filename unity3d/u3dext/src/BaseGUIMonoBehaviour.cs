using System;
using System.Text;
using System.Collections.Generic;
using UnityEngine;

namespace u3dext {

	/// <summary>
	/// Base GUI mono behaviour.
	/// </summary>
	public class BaseGUIMonoBehaviour : BaseMonoBehaviour {

		// Use customized GUI skin.
		public GUISkin userGUISkin;

		// == User customized GUI textures ==
		public Texture logoTex;
		public Texture bgMainTex;
		public Texture bgMainMenuTex;
		public Texture bgLevelFinishTex;
		public Texture levelPassTex;
		public Texture levelFailTex;
		public Texture[] levelPassStars;
		public Texture levelFailImageTex;

		public Texture btnPauseTex;
		public Texture btnLevelFinishRestartTex;
		public Texture btnLevelFinishMenuTex;
		public Texture btnLevelFinishNextTex;
		public Texture btnLevelFinishShopTex;
	
		// === DEBUGING ===
	
		// Show FPS in this rectangle.
		protected Rect rectFPS;
		protected Rect rectDebugInputConsole;
		protected Rect rectDebugTouchPoint;
		protected Rect rectDebugConsole;

		// === GUI ===
		protected Rect rectMainMenuWindow;
		protected Rect rectLogo;
		protected Rect rectStageWindow;
		protected Rect rectLevelWindow;
		protected Rect rectPauseButton;
		protected Rect rectPauseMenu;
		protected Rect rectLevelFinishWindow;

		// Control main menu.
		protected bool isShowMainMenu = false;
		protected bool isShowSettingWindow = false;
		protected bool isShowStageWindows = false;
		protected bool isShowLevelWindows = false;
		protected bool isShowMenuButton = false;
		protected bool isPauseMenuOpened = false;
		protected bool isShowQuitDialog = false;

		protected GUIStyle midCenterBox ;

		// 4 calculating FPS.
		private System.DateTime lastFpsTime;
		private int currentFPS = 0;
		private string fpsLabel = "FPS: 0";



		public BaseGUIMonoBehaviour () {
		}

		protected new void Start () {
			base.Start();

			if (debugMode) {
				lastFpsTime = System.DateTime.Now;
				rectDebugInputConsole = new Rect(debugDisplayPosition[0] + 5, debugDisplayPosition[1] + 5, 150, DEFAULT_LINE_HEIGHT);

				rectDebugTouchPoint = new Rect(
					debugDisplayPosition[0] + 5, debugDisplayPosition[1] + DEFAULT_LINE_HEIGHT + 5 * 2, 
					150, DEFAULT_LINE_HEIGHT);

				rectFPS = new Rect(
					debugDisplayPosition[0] + 5, debugDisplayPosition[1] + DEFAULT_LINE_HEIGHT * 2 + (5 * 3), 
					70, DEFAULT_LINE_HEIGHT);

				debug("The screen resolution is : " + sw + " X " + sh);
			}

			rectMainMenuWindow = new Rect(0, 0, sw, sh);
			rectLogo = new Rect(0, 0, sw, 0);
			rectStageWindow = new Rect(5, 5, sw - 10, sh - 10);
			rectLevelWindow = new Rect(5, 5, sw - 10, sh - 10);
			rectPauseButton = new Rect(10, 5, 80, 40);

			rectLevelFinishWindow = new Rect(hsw - 150, hsh - 175, 300, 350);

			rectPauseMenu = new Rect(hsw - 100, hsh - 100, 200, 200);

			midCenterBox = userGUISkin.customStyles[1];
		}

		protected new void OnGUI () {
			base.OnGUI();
			// === DEBUG ===
			if (debugMode) {
				// Show FPS.
				GUI.Box(rectFPS, fpsLabel);
			
				// User input debug.
				String msg = "L: " + Convert.ToString(leftHandBtnFlag) + ", R: " + Convert.ToString(rightHandBtnFlag);
				GUI.Box(rectDebugInputConsole, msg);

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
				for (int i = 0; i < state.mouseFlags.Length; i++) {
					touchText.Append(state.mouseFlags[i] == true ? "O" : "X");
				}
				touchText.Append(" T: ");
				for (int i = 0; i < state.touchFlags.Length; i++) {
					touchText.Append(state.touchFlags[i] == 1 ? "O" : "X");
				}
				touchText.Append(" Z: ");
				touchText.Append(state.zoomMode ? "O" : "X");
			
				GUI.Box(rectDebugTouchPoint, touchText.ToString());
			}

		
			// ==== Select Stage ====
			if (isShowStageWindows == true) {
				// Background
				GUI.Box(new Rect(0, 0, sw, sh), bgMainTex, userGUISkin.customStyles[2]);
				GUILayout.Window(10, rectStageWindow, OnStageWindowCreated, " == Choose Stage == ", userGUISkin.box);
			}
		
			// == Select Level == 
			if (isShowLevelWindows == true) {
				GUI.Box(new Rect(0, 0, sw, sh), bgMainTex, userGUISkin.customStyles[2]);
				GUILayout.Window(11, rectLevelWindow, OnLevelWindowCreated, " == Choose Level == ", userGUISkin.box);
			}

			// ==== Pause Button ====
			if (isShowMenuButton) {
				if (btnPauseTex == null) {
					if (GUI.Button(rectPauseButton, "PAUSE")) {
						isPauseMenuOpened = !isPauseMenuOpened;
						GameState.isGamePausing = isPauseMenuOpened;
						audio.PlayOneShot(beepMenu);
					}
				} else {
					if (GUI.Button(rectPauseButton, btnPauseTex)) {
						isPauseMenuOpened = !isPauseMenuOpened;
						GameState.isGamePausing = isPauseMenuOpened;
						audio.PlayOneShot(beepMenu);
					}
				}
			}

			if (isPauseMenuOpened == true) {
				GUILayout.Window(20, rectPauseMenu, OnPauseMenuCreated, "  == PAUSE == ", userGUISkin.box);
			}
		
			// Show level pass dialog.
			if (GameState.levelPassStatus == 1) {
				GUILayout.Window(21, rectLevelFinishWindow, OnLevelPassDialogCreated, bgLevelFinishTex, userGUISkin.box);
			} else if (GameState.levelPassStatus == 2) {
				// Show level fail dialog. 
				GUILayout.Window(22, rectLevelFinishWindow, OnLevelFailDialogCreated, bgLevelFinishTex, userGUISkin.box);
			}

			// ==== Main Menu ====
			if (isShowMainMenu == true) {
				// Background
				GUI.Box(new Rect(0, 0, sw, sh), bgMainTex, userGUISkin.customStyles[2]);
				// LOGO
				GUI.Box(rectLogo, logoTex, userGUISkin.box);
				// Main Menu Window
				GUILayout.Window(0, rectMainMenuWindow, OnMainMenuCreated, bgMainMenuTex, userGUISkin.box);
			}

			if (isShowQuitDialog == true) {
				GUILayout.Window(90, new Rect(hsw - 100, hsh - 80, 200, 160), delegate(int id) {
					GUILayout.Label("Are you sure to quit?", GUILayout.Width(180));
					if (GUILayout.Button("\r\n Yes") == true) {
						Application.Quit();
					}
					if (GUILayout.Button("\r\n No") == true) {
						isShowQuitDialog = false;
					}
				}, "Quit?");
			}

		}
		
		protected virtual void OnDeviceBackButtonPressed () {

		}

		protected virtual void OnDeviceMenuButtonPressed () {

		}
	
		protected virtual void OnMainMenuCreated (int winId) {
		
		}
	
		protected virtual void OnStageWindowCreated (int winId) {
		
		}
	
		protected virtual void OnLevelWindowCreated (int winId) {
		
		}
	
		/// <summary>
		/// Be called after user clicked the "MENU" button.
		/// </summary>
		/// <param name="windowId"></param>
		protected virtual void OnPauseMenuCreated (int windowId) {
//		GUILayout.Label("Hello World");
		}
	
		/// <summary>
		/// Be invoked when level pass dialog need to be created.
		/// </summary>
		/// <param name='windowId'>
		/// Window identifier.
		/// </param>
		protected virtual void OnLevelPassDialogCreated (int windowId) {
		}
	
		/// <summary>
		/// Be invoked when level fail dialog need to be created.
		/// </summary>
		/// <param name='windowId'>
		/// Window identifier.
		/// </param>
		protected virtual void OnLevelFailDialogCreated (int windowId) {
		}

		protected new void Update () {
			base.Update();
		
			// Calculating FPS.
			if (debugMode) {
				if (System.DateTime.Now.Ticks - lastFpsTime.Ticks > 10000 * 1000) {
					fpsLabel = "FPS: " + this.currentFPS;
					this.currentFPS = 0;
					lastFpsTime = System.DateTime.Now;
				} else {
					this.currentFPS++;
				}
			}

			if (Input.GetKeyDown("escape") == true) {
				debug("Pressed Key Escape");
				isShowQuitDialog = !isShowQuitDialog;
				OnDeviceBackButtonPressed();
			} else if (Input.GetKeyDown(KeyCode.Menu) == true) {
				isPauseMenuOpened = !isPauseMenuOpened;
				OnDeviceMenuButtonPressed();
			}
		}
	}
}

