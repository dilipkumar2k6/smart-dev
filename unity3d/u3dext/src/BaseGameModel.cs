using System;
using System.IO;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using System.Runtime;
using JsonFx;
using JsonFx.Json;
using JsonFx.IO;
using UnityEngine;
using u3dext;

namespace u3dext {
	public abstract class BaseGameModel {

		protected string userSettingFile ;
		// Reader and writer for settings.
		private JsonWriter jWriter ;
		private JsonReader jReader ;
		protected Dictionary<String, object> modelCache = new Dictionary<String, object>();

		public BaseGameModel () {
			jWriter = new JsonWriter();
			jReader = new JsonReader();

		}

		public bool isMusicOn () {
			if (!modelCache.ContainsKey("music")) {
				object d = this.getSetting("music");
				modelCache.Add("music", d == null ? true : (bool)d);
			}
			return (bool)modelCache["music"];
		}
	
		public bool isSoundOn () {
			if (!modelCache.ContainsKey("sound")) {
				object d = this.getSetting("sound");
				modelCache.Add("sound", d == null ? true : (bool)d);
			}
			return (bool)modelCache["sound"];
		}

		public object getSetting (String name) {
			// Read exist settings from storage file.
			FileInfo fi = Utils.checkAndCreateFile(userSettingFile);
			FileStream inStream = File.OpenRead(userSettingFile);  //fi.OpenRead();
			TextReader tr = new StreamReader(inStream);
			Dictionary<String, System.Object> data = jReader.Read<Dictionary<String, System.Object>>(tr);
			inStream.Close();
			if (data == null) {
				return null;
			}
			if (data.ContainsKey(name)) {
				return data[name];
			} else {
				return null;
			}
		}
	
		public bool saveSetting (String name, System.Object value) {
			FileInfo fi = Utils.checkAndCreateFile(userSettingFile);
		
			// Read exist settings from storage file.
			FileStream inStream = File.OpenRead(userSettingFile);  //fi.OpenRead();
			TextReader tr = new StreamReader(inStream);
			Dictionary<String, System.Object> data = jReader.Read<Dictionary<String, System.Object>>(tr);
			if (data == null) {
				data = new Dictionary<string, object>();
			}
			if (data.ContainsKey(name)) {
				data[name] = value;
			} else {
				data.Add(name, value);
			}
			inStream.Close();

			// Write modified or new setting to strage file.
			String json = jWriter.Write(data);
			FileStream fs = File.OpenWrite(userSettingFile);  //fi.OpenWrite();
			fs.Write(Encoding.Default.GetBytes(json), 0, json.Length);
			fs.Close();
		
			// update cache
			modelCache[name] = value;
		
			Debug.Log("Setting " + name + " saved to local storage");
			return true;
		}


	}
}

