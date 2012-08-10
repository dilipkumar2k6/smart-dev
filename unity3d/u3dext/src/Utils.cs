using System;
using System.IO;
using System.Collections;
using System.Collections.Generic;
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

	public static String asString (Dictionary<string, object> dic) {
		String buf = "{";
		foreach(KeyValuePair<string, object> m in dic) {
			buf += m.Key + "=";
			if(m.Value.GetType() == typeof(Dictionary<string, object>)) {
				buf += asString((Dictionary<string, object>)m.Value);
			}
			else {
				buf += m.Value;
			}
			buf +=  ",";
		}
		return buf + "}";
	}

	public static String asString(IList list) {
		String buf = "{";
		for(int i=0;i<list.Count;i++) {
			if(list[i].GetType() == typeof(Dictionary<string, object>)) {
				buf += asString((Dictionary<string, object>)list[i]);
			}
			else {
				buf += list[i];
			}
			buf +=  ",";
		}
		return buf + "}";
	}

	public static String asString (Hashtable dic) {
		String buf = "{";
		foreach(DictionaryEntry m in dic) {
			buf += m.Key + "=" + m.Value;
		}
		return buf + "}";
	}
	
	public static string paddingLeft (object txt, int length) {
		char[] after = new char[length];
		char[] before = txt.ToString().ToCharArray();
		int i = 0;
		int center = Math.Min(length - before.Length, length);
		for (; i< center; i++) {
			after[i] = ' ';
		}
		for (int j = 0; i< length; i++, j++) {
			after[i] = before[j];
		}
		return new string(after);
	}
	
	public static string paddingRight (object txt, int length) {
		char[] after = new char[length];
		char[] before = txt.ToString().ToCharArray();
		int i = 0;
		int center = Math.Min(before.Length, length);
		for (; i< center; i++) {
			after[i] = before[i];
		}
		for (; i< length; i++) {
			after[i] = ' ';
		}
		return new string(after);
	}
	
	public static bool isMobilePlatform() {
		return Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer;
	}
	
	/// <summary>
	/// Create file and it's directory in path if not exists.
	/// </summary>
	/// <returns>
	/// The and create file.
	/// </returns>
	/// <param name='filePath'>
	/// File path.
	/// </param>
	public static FileInfo checkAndCreateFile(string filePath) {
		//
		FileInfo fi = new FileInfo(filePath);
		if(fi.Directory.Exists == false) {
			DirectoryInfo di = Directory.CreateDirectory(fi.DirectoryName);
		}
		if(fi.Exists == false) {
			FileStream fs = File.Create(fi.FullName);
			fs.Close();
		}
		return new FileInfo(filePath);
	}
	
}
