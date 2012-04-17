using System;
using System.Text;

/// <summary>
/// Show debug information on screen.
/// </summary>
public class ScreenDebug {
	
	public const int MESSAGE_LENGTH = 10;
	
	string separator = "\r\n";
	
	string[] messages = new string[MESSAGE_LENGTH];
	
	public ScreenDebug () {
	}
	
	public void log(object msg) {
		for (int i = 0; i < messages.Length - 1; i++) {
			messages[i] = messages[i+1];
		}
		messages[messages.Length - 1] = msg.ToString();
	}
	
	public string contatDebugInfo() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < messages.Length; i++) {
			buf.Append(messages[i]).Append(separator);
		}
		return buf.ToString();
	}
	
}


