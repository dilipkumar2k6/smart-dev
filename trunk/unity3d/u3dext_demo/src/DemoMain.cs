using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;

/// <summary>
/// Description of DemoMain.
/// </summary>
public class DemoMain : RemoteDebug {
	
	public DemoMain () {
	}

	public DemoMain (LocalDebugCallback localDebugCallback) {
		Console.WriteLine ("Constructor");
	}
		
	public static void Main (String[] args) {
		Console.WriteLine("Start demo on thread: " + Thread.CurrentThread.Name);
		Thread t = new Thread(new ThreadStart(delegate{
			Console.WriteLine("Thread: " + Thread.CurrentThread.Name);
			Console.WriteLine("Thread: " + Thread.CurrentThread.ManagedThreadId);
		}));
		t.Name = "t-01";
		t.Start();
		DemoMain ins = new DemoMain(Console.WriteLine);
		ins.startupDemo();
		Console.WriteLine("Demo stoped");
	}
		
	public void startupDemo () {
		this.fork (delegate{
			new Thread (new ThreadStart (processInbound)).Start ();				
		});
		Console.WriteLine ("debug");

		for (;;) {
			String inbound = Console.ReadLine ();
			if (inbound != null && !inbound.Equals ("")) {
				this.log ("demo", inbound);					
			}
		}
	}
		
	private void processInbound () {
		Console.WriteLine ("Wait for client command..");
		NetworkStream ns = this.debugClient.GetStream ();
		Byte[] bytes = new Byte[256];
		String cmd;
		int i = 0;
		int c;
		for (c = ns.ReadByte(); c != -1; c = ns.ReadByte()) {
			if (c == '\n' && bytes [i - 1] == '\r') {
				cmd = System.Text.Encoding.ASCII.GetString (bytes, 0, i).Trim ();
				if ("quit".Equals (cmd)) {
					Console.WriteLine ("Client quit!");
					debugClient.Close ();
					this.isStoped = true;
					WaitHandle.SignalAndWait (waitter, waitter);
					break;
				}
				else {
					Console.WriteLine ("Unknown command: " + cmd);
				}
				bytes = new byte[256];
				i = 0;
			}
			else {
				//Console.WriteLine ((byte)c);
				bytes [i++] = (byte)c;
			}

		}
	}
}
