using System;
using System.IO;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using System.Runtime;
using LitJson;
using UnityEngine;
using u3dext;

namespace u3dext {
	public abstract class BaseGameModel {

		protected string userSettingFile ;
		// Reader and writer for settings.
		public Dictionary<String, System.Object> dMeta;

		protected Dictionary<String, object> modelCache = new Dictionary<String, object>();
		
		public BaseGameModel () {
		}

		public bool isMusicOn () {
			object d = this.getSetting("music", true);
			return d == null ? true : (bool)d;
		}
	
		public bool isSoundOn () {
			object d = this.getSetting("sound", true);
			return d == null ? true : (bool)d;
		}

		public object getSetting (String name) {
			return getSetting(name, null);
		}

		public object getSetting (String name, object defaultValue) {
			if (!modelCache.ContainsKey(name)) {

				// Read exist settings from storage file.
				Debug.Log(" === Start To Read JSON from File: " + userSettingFile);

				FileInfo fi = Utils.checkAndCreateFile(userSettingFile);
				FileStream inStream = File.OpenRead(userSettingFile);  //fi.OpenRead();
				TextReader tr = new StreamReader(inStream);

				SimpleJsonReader jReader = new SimpleJsonReader();
				Dictionary<String, object> data = jReader.Read(tr);

				tr.Close();
				inStream.Close();

				if (data == null || !data.ContainsKey(name)) {
//					if(defaultValue != null) {
						modelCache[name] = defaultValue;
//					}
				}
				else {
					modelCache[name] = data[name];
				}

			}
			return modelCache[name];
		}
	
		public bool saveSetting (String name, object value) {
			Debug.Log(" === Start To Read JSON from File: " + userSettingFile);
			FileInfo fi = Utils.checkAndCreateFile(userSettingFile);
			// Read exist settings from storage file.
			FileStream inStream = File.OpenRead(userSettingFile);  //fi.OpenRead();
			TextReader tr = new StreamReader(inStream);

			SimpleJsonReader jReader = new SimpleJsonReader();
			Dictionary<String, object> data = jReader.Read(tr);

			if (data == null) {
				data = new Dictionary<string, object>();
			}
			if (data.ContainsKey(name)) {
				data[name] = value;
			} else {
				data.Add(name, value);
			}
			tr.Close();
			inStream.Close();

			// Write modified or new setting to strage file.
			SimpleJsonWriter jWriter = new SimpleJsonWriter();
			jWriter.log = Debug.Log;
			jWriter.write(userSettingFile, data);
		
			// update cache
			modelCache[name] = value;
			Debug.Log("Setting " + name + " = " + value + " saved to local storage");
			return true;
		}
	
		public virtual bool loadGameMetaData (string metaDataName) {
			Debug.Log(" === Start To Read JSON from File: " + metaDataName);
			TextAsset metaFile = (TextAsset)Resources.Load(metaDataName, typeof(TextAsset));
			if (metaFile == null) {
				Debug.LogError("Failed to load game metadata resource : " + metaFile);
				return false;
			}
			TextReader tr = new StringReader(metaFile.text);

			SimpleJsonReader jReader = new SimpleJsonReader();
			jReader.log = Debug.Log;
			dMeta = jReader.Read(tr);

			if (dMeta.Count == 0) {
				Debug.LogError("Failed to read metadata to JSON object");
				return false;
			}
			return true;
		}
	}
}

