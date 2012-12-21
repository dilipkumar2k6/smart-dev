using System;
using System.IO;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

using LitJson;

namespace u3dext {
	public class SimpleJsonReader {

		public delegate void Log (string msg);

		public Log log = Console.WriteLine;

		public SimpleJsonReader () {
		}

		bool inDictionary = false;
		bool inList = false;


		public Dictionary<string, object> Read (TextReader textReader) {
			Dictionary<string, object> result = new Dictionary<string, object>();
			String txt = textReader.ReadToEnd();
//			log(txt);

			JsonReader jsonReader = new JsonReader(txt);
			string currentKey = null;
			Stack stack = new Stack();

			Dictionary<string, object> currentDic = null;
			IList currentList = null;

			while (jsonReader.Read()) {
//				Debug.Log(jsonReader.Token + "=" + jsonReader.Value);
//				log(jsonReader.Token + "=" + jsonReader.Value);
				switch (jsonReader.Token) {
				case JsonToken.ObjectStart:
					// first node
					Dictionary<string, object> newDic = new Dictionary<string, object>();
					if (currentDic == null) {
						currentDic = newDic;
					} else { // Other sub key-value node
						if (inDictionary) {
							currentDic.Add(currentKey, newDic);
						} else if (inList) {
							currentList.Add(newDic);
						}
					}
					stack.Push(newDic);
					currentDic = newDic;
					togglePosition(true);
					break;
				case JsonToken.ObjectEnd:
					stack.Pop();
					if (stack.Count == 0) {
						break;
					} else {
						if (stack.Peek().GetType() == typeof(Dictionary<string, object>)) {
							currentDic = (Dictionary<string, object>)stack.Peek();
							togglePosition(true);
						} else if (stack.Peek().GetType() == typeof(ArrayList)) {
							currentList = (ArrayList)stack.Peek();
							togglePosition(false);
						} else {
							log("Unknow type: " + stack.Peek().GetType());
						}
					}
					break;
				case JsonToken.ArrayStart:
					IList newList = new ArrayList();
					if (inDictionary) {
						currentDic.Add(currentKey, newList);
					} else if (inList) {
						currentList.Add(newList);
					}
					stack.Push(newList);
					currentList = newList;
					togglePosition(false);
					break;
				case JsonToken.ArrayEnd:
					stack.Pop();
					if (stack.Peek().GetType() == typeof(Dictionary<string, object>)) {
						currentDic = (Dictionary<string, object>)stack.Peek();
						togglePosition(true);
					} else if (stack.Peek().GetType() == typeof(ArrayList)) {
						currentList = (ArrayList)stack.Peek();
						togglePosition(false);
					}
					break;
				case JsonToken.PropertyName:
					currentKey = jsonReader.Value.ToString();
					break;
				default:
					object value = jsonReader.Value;
					if(value!=null && value.GetType() == typeof(Int64)) {
						value = (Int32)value;
					}
					if (inDictionary) {
						currentDic.Add(currentKey, value);
					} else if (inList) {
						currentList.Add(value);
					}
					break;
				}
			}
			jsonReader.Close();
			return currentDic == null ? new Dictionary<string, object>() : (Dictionary<string, object>)currentDic;
		}

		// Deprecated
		public Dictionary<string,object> list2arrayWrapper(Dictionary<string, object> dic) {
			Dictionary<string,object> newDic = new Dictionary<string,object>();
			foreach(KeyValuePair<string, object> m in dic) {
				string key = m.Key;
				object value = m.Value;
//				log("Key=" + key + ", Value=" + value);
				if(value.GetType() == typeof(Dictionary<string, object>)) {
//					log("Found a dic: " + key);
					newDic.Add(key, value);
					list2arrayWrapper((Dictionary<string, object>)newDic[key]);
				}
				else if (value.GetType() == typeof(ArrayList)) {
//					log("Found a list");
					IList list = (ArrayList)value;
					Dictionary<string, object>[] array = new Dictionary<string, object>[list.Count];
					newDic.Add(key, array);
					for(int i=0; i<list.Count; i++) {
//						log("Type for: " + i + " "+ list[i].GetType());
						if(list[i]!=null && list[i].GetType() == typeof(Dictionary<string, object>)) {
							array[i] = list2arrayWrapper((Dictionary<string, object>)list[i]);
//						}
//						else {
//							array[i] = list[i];
						}
					}
				}
				else {
					newDic.Add(key, value);
				}
			}
			return newDic;
		}

		private void togglePosition (bool isInDictionary) {
			inDictionary = isInDictionary;
			inList = !isInDictionary;
		}
	}


}

