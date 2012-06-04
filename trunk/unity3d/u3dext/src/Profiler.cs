using System;
using System.Text;
using System.Collections;
using System.Collections.Generic;

namespace u3dext {
	/// <summary>
	/// Profiler for performance.
	/// How to use:
	/// 1. Call start() to start a sample point, call end() to end a sample point.
	/// 2. Each start() must be enclosed by end().
	/// 3. Enable to embed another start() end() pair with same tag.
	/// </summary>
	public class Profiler {
		private static Profiler profiler = new Profiler();
		
//		private Stack samplePointStack;
		private Dictionary<string, long> startTime;
		
		// Tag -> List( seq -> time)
		private SortedDictionary<string, List<long>> profileTable;
		
		private Profiler () {
//			samplePointStack = new Stack();
			startTime = new Dictionary<string, long>();
			profileTable = new SortedDictionary<string, List<long>>();
		}
		
		public static Profiler getInstance () {	
			return profiler;
		}
		
		public void start (string tag) {
			List<long> tagTable;
			if (!profileTable.ContainsKey(tag)) {
				tagTable = new List<long>();
				profileTable.Add(tag, tagTable);
			}
			startTime[tag] = DateTime.Now.Ticks;
		}
		
		public void end (string tag) {
			if (!profileTable.ContainsKey(tag)) {
				return;
			}
			List<long> tagTable = profileTable[tag];
			if (startTime[tag] != 0) {
				tagTable.Add(DateTime.Now.Ticks - startTime[tag]);
			}
		}
		
		public void start () {
			start("DEFAULT");
		}
		
		public void end () {
			end("END");
		}
		
		public void print (RemoteDebug.LocalDebugCallback printDelegate) {
			printDelegate("==========  PROFILING ==========\r\n");
			printDelegate("=" + Utils.paddingRight(" TAG ", 16) 
				+ Utils.paddingRight(" | MAX ", 12)
				+ Utils.paddingRight(" | MIN ", 12)
				+ Utils.paddingRight(" | AVERAGE ", 12)
				+ Utils.paddingRight(" | TOTAL \r\n", 10)
			);
			foreach (KeyValuePair<String, List<long>> tagData in profileTable) {
				long min, max, average, total;
				min = max = average = total = 0;
				foreach (long timeSpan in tagData.Value) {
					if (min == 0 || min > timeSpan) {
						min = timeSpan;
					}
					if (max < timeSpan) {
						max = timeSpan;
					}
					total += timeSpan;
				}
				average = tagData.Value.Count == 0 ? 0 : total / tagData.Value.Count;
				StringBuilder buf = new StringBuilder(100);
				buf.Append("=").Append(Utils.paddingRight(tagData.Key, 16))
					.Append(" | ")
					.Append(Utils.paddingLeft(max / 10000f, 7)).Append("ms | ")
					.Append(Utils.paddingLeft(min / 10000f, 7)).Append("ms | ")
					.Append(Utils.paddingLeft(average / 10000f, 7)).Append("ms | ")
					.Append(Utils.paddingLeft(total / (float)(10000 * 1000), 7)).Append("s \r\n");
				printDelegate(buf.ToString());
			}
			printDelegate("==========  PROFILING ==========\r\n");
		}
	}
}

