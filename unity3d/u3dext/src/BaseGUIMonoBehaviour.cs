using System;
using System.Text;
using System.Collections.Generic;
using UnityEngine;

namespace u3dext {

	/// <summary>
	/// Base GUI mono behaviour.
	/// </summary>
	public class BaseGUIMonoBehaviour : BaseMonoBehaviour {

	
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

		public BaseGUIMonoBehaviour () {
		}

		protected new void Start () {
			base.Start();

			if (debugMode) {

				lastFpsTime = System.DateTime.Now;
				
				rectDebugInputConsole = new Rect(debugDisplayPosition[0] + 5, debugDisplayPosition[1] + 5, 150, DEFAULT_LINE_HEIGHT);
				
				rectDebugTouchPoint = new Rect(
					debugDisplayPosition[0] + 5,
					debugDisplayPosition[1] + DEFAULT_LINE_HEIGHT + 5 * 2, 150, DEFAULT_LINE_HEIGHT);
				
				rectFPS = new Rect(
					debugDisplayPosition[0] + 5,
					debugDisplayPosition[1] + DEFAULT_LINE_HEIGHT * 2 + 5 * 3, 70, DEFAULT_LINE_HEIGHT);
				
			}

			rectMainMenuWindow = new Rect(5, 5, sw - 10, sh - 10);
			rectStageWindow = new Rect(5, 5, sw - 10, sh - 10);
			rectLevelWindow = new Rect(5, 5, sw - 10, sh - 10);
			rectMenuButton = new Rect(10, 5, 80, 40);
			rectMenu = new Rect(hsw - 100, hsh - 100, 200, 200);
			rectDialog = new Rect(hsw - 100, hsh - 100, 200, 200);
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

		
			// ==== Main Menu ====
			if (isShowMainMenu == true) {
				GUILayout.Window(0, rectMainMenuWindow, OnMainMenuCreated, " == Main Menu ==");
			}
		
			// ==== First Stage and Level Choosing ====
			if (isShowStageWindows == true) {
				GUILayout.Window(10, rectStageWindow, OnStageWindowCreated, " == Choose Stage == ");
			}
		
			if (isShowLevelWindows == true) {
				GUILayout.Window(11, rectLevelWindow, OnLevelWindowCreated, " == Choose Level == ");
			}

			// ==== Menu Handling ====
			if (isShowMenuButton) {
				if (GUI.Button(rectMenuButton, "MENU")) {
					isMenuOpened = !isMenuOpened;
					audio.PlayOneShot(beepMenu);
				}
			}

			if (isMenuOpened == true) {
				GUILayout.Window(20, rectMenu, OnMenuCreated, "  == MENU == ");
			}
		
			// Show level pass dialog.
			if (levelPassStatus == 1) {
				GUILayout.Window(21, rectDialog, OnLevelPassDialogCreated, "  == Pass == ");
			} else if (levelPassStatus == 2) {
				// Show level fail dialog. 
				GUILayout.Window(22, rectDialog, OnLevelFailDialogCreated, "  == Fail == ");
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
				isMenuOpened = !isMenuOpened;
				OnDeviceMenuButtonPressed();
			}
		}
	}
}

