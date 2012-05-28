using System;
using UnityEngine;

/// <summary>
/// Description of Utils.
/// </summary>
public class Utils	{

	public Utils() {
	}
	
//	public static Rect changeRect();
	
	public static Vector2 convert3Dto2D (Vector3 v3) {
		return new Vector2(v3.x, v3.y);	
	}
	
	/// <summary>
	/// Mouses the position to screen position. Because Unity3d Mouse input's y axis direction is defferent from screen, so use
	/// this method to convert.
	/// </summary>
	/// <returns>
	/// The position to screen position.
	/// </returns>
	/// <param name='screenHeight'>
	/// Screen height.
	/// </param>
	/// <param name='mousePos'>
	/// Mouse position.
	/// </param>
	public static Vector2 mousePositionToScreenPosition (float screenHeight, Vector2 mousePos) {
		return new Vector2(mousePos.x, screenHeight - mousePos.y);
	}
	
	public static String asString (int[] arr) {
		String buf = "[";
		for (int i = 0; i < arr.Length; i++) {
			buf += arr[i] + ",";
		}
		buf.Remove(buf.Length - 1);
		return buf + "]";
	}
	
	public static String asString (System.Object[] arr) {
		String buf = "[";
		for (int i = 0; i < arr.Length; i++) {
			buf += arr[i] + ",";
		}
		buf.Remove(buf.Length - 1);
		return buf + "]";
	}

	
	public static bool isMobilePlatform() {
		return Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer;
	}
	
}
