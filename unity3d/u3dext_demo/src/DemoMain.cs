﻿using System;
using System.Net;
using System.Net.Sockets;
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using u3dext;

/// <summary>
/// Description of DemoMain.
/// </summary>
public class DemoMain {
	RemoteDebug remoteDebug;
	public string originalStr;
	public Object originalInt;
	
	public DemoMain () {
		remoteDebug = RemoteDebug.getInstance(Console.WriteLine);
	}

	public DemoMain (RemoteDebug.LocalDebugCallback localDebugCallback) {
		Console.WriteLine("Constructor");
		remoteDebug = RemoteDebug.getInstance(Console.WriteLine);
	}
		
	public static void Main (String[] args) {
		DemoMain ins = new DemoMain(Console.WriteLine);
		
		ins.testInteger();
		
		ins.testFloatNumber();
		
		ins.testObjectReferece();
		
//		ins.startupDemo();
		
		ins.testDelegateVirtualMethod();

		ins.testTheme();
		
		Profiler.getInstance().print(Console.WriteLine);

		ins.originalStr = "original";
		ins.originalInt = new object();
		Console.WriteLine(ins.originalStr);
		ins.testReferenceAndValueType(ins.originalStr, ins.originalInt);
		
		Console.WriteLine("Demo stoped");
	}
		
	public void startupDemo () {
		remoteDebug.fork(delegate {
//			new Thread(new ThreadStart(processInbound)).Start();				
		}
		);
		Console.WriteLine("debug");

		for (;;) {
			String inbound = Console.ReadLine();
			if (inbound != null && !inbound.Equals("")) {
				remoteDebug.log("demo", inbound);					
			}
		}
	}
	
	public void testInteger () {
		Console.WriteLine("testInteger");
		long large = DateTime.Now.Ticks;
		int small = 7;
		
		Console.WriteLine(large);
		Console.WriteLine(large / small);
		
		Console.WriteLine(large % small);
		int n = (int)(large % small);
		Console.WriteLine(n);
		
	}
	
	public void testFloatNumber () {
		Profiler.getInstance().start("Test");
		Console.WriteLine(3 / 2);
		Console.WriteLine(3.0f / 2.0f);
		Console.WriteLine(3 / 2f);
		Console.WriteLine(Math.PI / 4f);
		
		Thread.Sleep(260);
		Profiler.getInstance().end("Test");
	}
	
	public void testObjectReferece () {
//		Profiler.getInstance().start("Test");
		Console.WriteLine("======");
		Boolean flag = false;
		IList list = new ArrayList();
		list.Add(flag);
		flag = true;
		Console.WriteLine(list[0]);
		Thread.Sleep(1380);
		Profiler.getInstance().end("Test");
	}
	
	public void testProfiling () {
		Profiler.getInstance().start("Test Profiling");
		Thread.Sleep(2350);
		Profiler.getInstance().end("Test Profiling");
	}
	
	public void testDelegateVirtualMethod() {
//		SubClass ins = new SubClass();
//		ins.testDelegate();
//		SubClass2 ins2 = new SubClass2();
//		ins2.testDelegate();
	}


	public void testTheme(){
		Theme theme = new Theme800x480();
		Console.WriteLine(theme.W(320));
		Console.WriteLine(theme.H(480));
		Console.WriteLine(theme.W(481));
	}

	public void testReferenceAndValueType(string value, Object number) {

		Console.WriteLine(value);

		Console.WriteLine("Is the same String object? " + (originalStr == value));

		Console.WriteLine("Is the same object? " + (originalInt == number));

		value = "changed";
		Console.WriteLine(value);
	}


	
		
// Comment Tempararily!!
//	private void processInbound () {
//		Console.WriteLine ("Wait for client command..");
//		NetworkStream ns = remoteDebug.debugClient.GetStream ();
//		Byte[] bytes = new Byte[256];
//		String cmd;
//		int i = 0;
//		int c;
//		for (c = ns.ReadByte(); c != -1; c = ns.ReadByte()) {
//			if (c == '\n' && bytes [i - 1] == '\r') {
//				cmd = System.Text.Encoding.ASCII.GetString (bytes, 0, i).Trim ();
//				if ("quit".Equals (cmd)) {
//					Console.WriteLine ("Client quit!");
//					debugClient.Close ();
//					remoteDebug.isStoped = true;
//					WaitHandle.SignalAndWait (waitter, waitter);
//					break;
//				}
//				else {
//					Console.WriteLine ("Unknown command: " + cmd);
//				}
//				bytes = new byte[256];
//				i = 0;
//			}
//			else {
//				//Console.WriteLine ((byte)c);
//				bytes [i++] = (byte)c;
//			}
//
//		}
//	}
}
