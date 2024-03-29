using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace u3dext {
	
	public class UserControlState {


		public bool isMousePreesedOnScreen = false; // Flag that mouse pressed on screen.
//		public bool isKeyPressed = false;
		public Vector2 mousePressedPositionOnScreen; // Mouse position when pressed down.
		public Vector2 lastFramePositionOnScreen; // For both mouse and touch to calculate movement delta.
	
		//public bool isMousePressed = false; // Mouse pressed on game object.
		public Vector2 mousePressedPosition; // Store the mouse position when mouse pressed on game object.
		public bool[] mouseFlags = new bool[3];


		// Is touching on screen for multi-fingers. Avoid unneccessary touch and mouse events on screen.
		public bool[] isTouchingScreen = new bool[5];
		public Vector2[] touchDownPosition = new Vector2[5];

		// from first finger to 5th finger.
		public int[] touchFlags = new int[5];


		// Zoom
		public bool zoomMode = false;
		public int[] zoomTouchIds = new int[2]{-1, -1}; // First touch id and second touch id for zooming.
		public Vector2[] zoomPoint = new Vector2[5];
		public int nonZoomId; // A touch ID that doesn't be used for Zoom.

		public UserControlState () {
		}
		
		public float getZoomPointDistance () {
			return Vector2.Distance(
				zoomPoint[zoomTouchIds[0]],
				zoomPoint[zoomTouchIds[1]]
			);
		}
		
		// TODO
//		public static int traverseAllFlags(Callback callback) {
//			callback(0, "BEAT", operateBeat);
//			callback(1, "BEAT", operateBeat);
//			callback(2, "BEAT", operateBeat);
//		}
//		
//		public delegate void Callback(int idx, string name, object flag);
		
		/// <summary>
		/// Detect mouse input and callback.
		/// </summary>
		/// <param name='mdc'>
		/// Mdc.
		/// </param>
		/// <param name='mmc'>
		/// Mmc.
		/// </param>
		/// <param name='muc'>
		/// Muc.
		/// </param>
		/// <param name='orhdc'>
		/// Orhdc.
		/// </param>
		/// <param name='orhuc'>
		/// Orhuc.
		/// </param>
		public void changedByMouseInput (MouseDownCallback mdc, MouseMoveCallback mmc, MouseUpCallback muc, 
			ObjectRayHitDownCallback orhdc, ObjectRayHitUpCallback orhuc) {
			//Debug.Log("Callback to " + mdc.Target + "." + mdc.Method);
			for (int i = 0; i < 2; i++) {
//				if (isMousePreesedOnScreen == false && Input.GetMouseButtonDown(i)) {
				if (Input.GetMouseButtonDown(i)) {
					mousePressedPositionOnScreen = Utils.convert3Dto2D(Input.mousePosition);
					lastFramePositionOnScreen = mousePressedPositionOnScreen;
					isMousePreesedOnScreen = true;
					mouseFlags[i] = true;
					// Callback

					mdc(i, mousePressedPositionOnScreen);			
				}
				//if (isMousePreesedOnScreen == true && Input.GetMouseButtonUp(i)) {
				if (Input.GetMouseButtonUp(i)) {
					isMousePreesedOnScreen = false;
					mouseFlags[i] = false;
					// Callback
					muc(i, Input.mousePosition);
				}
			}
		
			Vector2 thisFrameMousePos = Utils.convert3Dto2D(Input.mousePosition);
			if (thisFrameMousePos != lastFramePositionOnScreen) {
				// Callback Mouse Move
				mmc(
					thisFrameMousePos,
					thisFrameMousePos - lastFramePositionOnScreen
				);
				lastFramePositionOnScreen = thisFrameMousePos;
			}
			
			// Raise ray hits event for mouse input.
			u3dext.Profiler.getInstance().start("Mouse.Ray");
//			if (isMousePressed == false && Input.GetMouseButtonDown(0)) {
			if (Input.GetMouseButtonDown(0)) {
//				isMousePressed = true;
				ScreenDebug.getInstance().log("Press mouse on screen position " + Input.mousePosition);
				String hitObjName = rayHitGameObject(Input.mousePosition);
				if (hitObjName != null) {
					u3dext.Profiler.getInstance().start("Mouse.Ray.Down");
					orhdc(hitObjName);
					u3dext.Profiler.getInstance().end("Mouse.Ray.Down");
				}
//			} else if (isMousePressed == true && Input.GetMouseButtonUp(0)) {
			} else if (Input.GetMouseButtonUp(0)) {
				ScreenDebug.getInstance().log("Release mouse on screen position " + Input.mousePosition);
//				isMousePressed = false;
				String hitObjName = rayHitGameObject(Input.mousePosition);
				if (hitObjName != null) {
					u3dext.Profiler.getInstance().start("Mouse.Ray.Up");
					orhuc(hitObjName);
					u3dext.Profiler.getInstance().end("Mouse.Ray.Up");
				}
			}
			u3dext.Profiler.getInstance().end("Mouse.Ray");
		}
		
		/// <summary>
		/// Detect touch and callback.
		/// </summary>
		/// <param name='tdc'>
		/// Tdc.
		/// </param>
		/// <param name='tmc'>
		/// Tmc.
		/// </param>
		/// <param name='tuc'>
		/// Tuc.
		/// </param>
		/// <param name='orhdc'>
		/// Orhdc.
		/// </param>
		/// <param name='orhuc'>
		/// Orhuc.
		/// </param>
		/// <param name='zioc'>
		/// Zioc.
		/// </param>
		public void changedByTouch (TouchDownCallback tdc, TouchMoveCallback tmc, TouchUpCallback tuc,
		                                   ObjectRayHitDownCallback orhdc, ObjectRayHitUpCallback orhuc, ZoomInAndOutCallback zioc) {
			for (int i=0; i<Input.touches.Length; i++) {
				Touch touch = Input.touches[i];
				Vector2 eachPos = touch.position;
				if (touch.phase == TouchPhase.Began) {
					// Callback(Run before everything to ensure being called)
					if (isTouchingScreen[touch.fingerId] == false) {
						
						// Exclude some non-screen(like button ) touch.
						if (tdc(touch.fingerId, eachPos) == false) {
							nonZoomId = touch.fingerId;
						} else {
							// 1st zoom(or rotate) finger.
							if (zoomTouchIds[0] < 0 && zoomTouchIds[1] < 0) {
								zoomTouchIds[0] = touch.fingerId;
								zoomPoint[touch.fingerId] = touch.position;
							} else { // 2nd finger
								zoomMode = true;
								zoomTouchIds[1] = touch.fingerId;
								zoomPoint[touch.fingerId] = touch.position;
							}
							lastFramePositionOnScreen = eachPos; // Omit non-screen position.
						}
						
					}
					isTouchingScreen[touch.fingerId] = true;
					touchDownPosition[touch.fingerId] = eachPos;
					
					
					// Detect ray hits from screen touch point.
					u3dext.Profiler.getInstance().start("Touch.RayCast");
					String hitObjName = rayHitGameObject(touch.position);
					u3dext.Profiler.getInstance().end("Touch.RayCast");
					
					if (hitObjName != null) {
						u3dext.Profiler.getInstance().start("Touch.Ray.Down");
						orhdc(hitObjName);
						u3dext.Profiler.getInstance().end("Touch.Ray.Down");
					}
					
				} else if (touch.phase == TouchPhase.Ended) {
					// Callback
					if (isTouchingScreen[touch.fingerId] == true) {
						tuc(touch.fingerId, eachPos);
					}
					isTouchingScreen[touch.fingerId] = false;
					
					u3dext.Profiler.getInstance().start("Touch.RayCast");
					String hitObjName = rayHitGameObject(touch.position);
					u3dext.Profiler.getInstance().end("Touch.RayCast");
					
					if (hitObjName != null) {
						u3dext.Profiler.getInstance().start("Touch.Ray.Up");
						// Callback ray up
						orhuc(hitObjName);
						u3dext.Profiler.getInstance().end("Touch.Ray.Up");
					}

				} else if (touch.phase == TouchPhase.Moved) {
					
					if (zoomMode == true) {
						u3dext.Profiler.getInstance().start("Touch.Zoom");
						
						// Distance between 2 touch points at last frame.
						float preDistance = this.getZoomPointDistance();//Vector2.Distance(zoomPoint[zoomTouchIds[0]], zoomPoint[zoomTouchIds[1]]);
			
						float curDistance = 0f; 
						if (touch.fingerId == zoomTouchIds[0]) {
							curDistance = Vector2.Distance(
								touch.position,
								zoomPoint[zoomTouchIds[1]]
							);
						} else if (touch.fingerId == zoomTouchIds[1]) {
							curDistance = Vector2.Distance(
								zoomPoint[zoomTouchIds[0]],
								touch.position
							);
						}
						float delta = curDistance - preDistance;
						zioc(delta);
						
						u3dext.Profiler.getInstance().end("Touch.Zoom");
					} else {
						
						u3dext.Profiler.getInstance().start("Touch.Move");

						Vector2 touchDownPos = touchDownPosition[touch.fingerId];
						
						if (tmc(touch.fingerId, eachPos, eachPos - lastFramePositionOnScreen, eachPos - touchDownPos) == true) {
							if (touch.position != lastFramePositionOnScreen) {
								lastFramePositionOnScreen = eachPos;
							}
						}

						u3dext.Profiler.getInstance().end("Touch.Move");
					}
				}
			}
		}

		protected static String rayHitGameObject (Vector3 screenPos) {
			// Detect by Ray
			//this.debug("Ray at screen position: " + screenPos);
			Ray ray = Camera.main.ScreenPointToRay(screenPos);
			RaycastHit hit;
			//Debug.DrawRay(ray.origin, ray.direction, Color.red);
			if (Physics.Raycast(ray.origin, ray.direction, out hit)) {
				//this.debug("Ray " + ray + " hits " + hit.collider);
				return hit.collider.name;
			}
			return null;
		}

//		public void changedByKeyboard(KeyCallback callback) {
//			if (Input.GetKey("escape") == true) {
//
//			}
//
//			if (Input.GetKey(KeyCode.Menu) == true) {
//
//			}
//		}
		
		public delegate void MouseDownCallback (int button,Vector2 mousePosition);
		
		public delegate void MouseMoveCallback (Vector2 mousePosition,Vector2 deltaPosition);
		
		public delegate void MouseUpCallback (int button,Vector2 mousePosition);
		
		public delegate bool ObjectRayHitDownCallback (String objectName);
		
		public delegate bool ObjectRayHitUpCallback (String objectName);
		
		public delegate bool TouchDownCallback (int touchId,Vector2 touchPosition);
		
		public delegate bool TouchMoveCallback (int touchId,Vector2 touchPosition, Vector2 lastFrameDeltaPos, Vector2 touchDownDeltaPos);
		
		public delegate void TouchUpCallback (int touchId,Vector2 touchPosition);
		
		public delegate void ZoomInAndOutCallback(float delta);

//		public delegate void KeyCallback(KeyCode key);
	}
}

