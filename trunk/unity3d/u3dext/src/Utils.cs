using System;
using UnityEngine;

/// <summary>
/// Description of Utils.
/// </summary>
public class Utils	{

	public Utils() {
	}
	
//	public static Rect changeRect();
	
	public static Vector2 convert3Dto2D(Vector3 v3) {
		return new Vector2(v3.x, v3.z);	
	}

	
}
