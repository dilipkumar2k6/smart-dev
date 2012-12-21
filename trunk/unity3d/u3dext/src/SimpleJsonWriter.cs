using System;
using System.Text;
using System.IO;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using LitJson;

namespace u3dext {
	public class SimpleJsonWriter {
		JsonWriter jsonWriter;
		StringBuilder sb;

		public delegate void Log (string msg);

		public Log log = Console.WriteLine;

		public SimpleJsonWriter () {
			sb = new StringBuilder();
			jsonWriter = new JsonWriter(sb);
		}


		// Write object to file with JSON format.
		public void write (string filePath, Dictionary<string, object> jsonData) {
			log(" === Start to write JSON to file ===");

			// Generate JSON string from object.
			writeDictionary(jsonData);

			// Flush to file.
//			FileInfo fi = new FileInfo(filePath);
			FileStream fs = new FileStream(filePath, FileMode.Create);
			byte[] byteArray = System.Text.Encoding.Default.GetBytes(sb.ToString());
			fs.Write(byteArray, 0, byteArray.Length);
			fs.Close();
		}


		// Write key-map object to JSON object.
		public void writeDictionary (Dictionary<string, object> jsonData) {
			jsonWriter.WriteObjectStart();
			foreach (KeyValuePair<string, object> m in jsonData) {
				string key = m.Key;
				object value = m.Value;
//				log("Key=" + key + ", Value=" + value);
				jsonWriter.WritePropertyName(key);
				if (value.GetType() == typeof(Dictionary<string, object>)) {
					writeDictionary((Dictionary<string, object>)value);
				} else if (value.GetType() == typeof(ArrayList)) {
					writeList((ArrayList)value);
				} else {
					writeObject(value);
				}
			}
//			log("}");
			jsonWriter.WriteObjectEnd();
		}

		// Write list object to JSON object.
		private void writeList (IList list) {
			jsonWriter.WriteArrayStart();
			for (int i=0; i<list.Count; i++) {
				object item = list[i];
				if (item != null && item.GetType() == typeof(Dictionary<string, object>)) {
					writeDictionary((Dictionary<string, object>)item);
				} else if (item != null && item.GetType() == typeof(IList)) {
					writeList((IList)item);
				}
				else {
					writeObject(item);
				}
			}
			jsonWriter.WriteArrayEnd();
		}

		// Write any object to JSON object.
		private void writeObject (object value) {
			if(value == null) {
				log("Null object.");
				return;
			}
			Type type = value.GetType();
			if (type == typeof(Int32)) {
				jsonWriter.Write((Int32)value);
			}else if (type == typeof(Int64)) {
				jsonWriter.Write((Int64)value);
			} else if (type == typeof(string)) {
				jsonWriter.Write((string)value);
			} else if (type == typeof(Boolean)) {
				jsonWriter.Write((Boolean)value);
			}else {
				log("Unkown: " + type);
			}
		}
	}
}

