using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Collections;
using System.Threading;

/// <summary>
/// Accept remote socket client to receive log information, all log messages 
/// will be queued until new connection established.
/// @Author: Yuxing Wang
/// @Version: 0.1
/// </summary>
public class RemoteDebug {
	protected int DEFAULT_PORT = 5000;
	
	protected int MAX_MSG_QUEUE_SIZE = 1000;

	// Listener for clients.
	protected TcpListener tcpListener;

	// Single client instance for every connection.
	protected TcpClient debugClient;
	
	// Queue messages here.
	protected Queue msgQueue;
	
	// Main thread for listening new connection.
	protected Thread mainThread;
	
	// Delegate the flushing method.
	protected delegate void ProcessDelegate();
	
	public delegate void ConnectionCallback();
	protected ConnectionCallback connectedCallback;
	
	public delegate void LocalDebugCallback(String msg);
	LocalDebugCallback localDebugCallback;
	
	protected Thread parentThread;
	
	// Waitter for incoming msg notification.
	protected WaitHandle waitter;
	
	protected bool isStarting = false;
	protected bool isStoped = false;
	
	private DateTime keepAliveTime;
	
	private static RemoteDebug ins;
	private RemoteDebug () {
		this.msgQueue = new Queue();
		this.mainThread = null;
	}
	
	/// <summary>
	/// Only one remote debug instance allowed.
	/// </summary>
	/// <returns>
	/// The instance.
	/// </returns>
	/// <param name='localDebugCallback'>
	/// Local debug callback.
	/// </param>
	public static RemoteDebug getInstance(LocalDebugCallback localDebugCallback) {
		if(ins == null) {
			ins = new RemoteDebug();
			ins.localDebugCallback = localDebugCallback;
			localDebugCallback ("RemoteDebug constructed");
		}
		return ins;
	}
	
	/// <summary>
	/// Initializes a new instance of the <see cref="RemoteDebug"/> class.
	/// </summary>
	private RemoteDebug (LocalDebugCallback localDebugCallback) {
		this.localDebugCallback = localDebugCallback;
	}
	
	public void startTcpListener () {
		// Avoid multi starting.
		if (!isStarting) {
			isStarting = true;
			try {
				IPHostEntry hostEntry = Dns.GetHostEntry(Dns.GetHostName());
				if (hostEntry == null || hostEntry.AddressList == null || hostEntry.AddressList.Length == 0) {
					localDebugCallback("Failed to resolve local IP address");
					return;
				}
				
				tcpListener = new TcpListener(hostEntry.AddressList[0], DEFAULT_PORT);
				tcpListener.Start();
			} catch (Exception ex) {
				localDebugCallback(ex.Message + ex.StackTrace);
				localDebugCallback("RemoteDebug construct failed");
				return;
			}
			localDebugCallback("Remote debug listening on port " + DEFAULT_PORT);
			
			// Test thread.
			new Thread(new ThreadStart(delegate{
				while (true) {
//					if (parentThread == null || !parentThread.IsAlive) {
//						break;
//					}
					localDebugCallback("Running...");
					Thread.Sleep(1000);
				}
			})).Start();
		}
	}

	
	public void fork(ConnectionCallback connectedCallback) {
		this.connectedCallback = connectedCallback;
		if(mainThread == null) {
			
			localDebugCallback("Listener started");
			
			mainThread = new Thread(new ThreadStart(startListen));
			mainThread.Start();
			localDebugCallback("Main thread " + mainThread.Name + " started");
		} 
		else {
			// localDebugCallback("The listener thread is already started.");
		}
	}

	/// <summary>
	/// Listen in a new thread.
	/// </summary>
	private void startListen () {

		ProcessDelegate processDelegate = processRemoteDebug;
		
		while (true) {

			log ("INIT", "Remote debug listen on: " + tcpListener.LocalEndpoint);
			
			// Only one client allowed, this single client instance take over remote connection, all debug info will be forward to new client console.
			debugClient = tcpListener.AcceptTcpClient(); // ## Block here.
			
			log ("INIT", "A remote debug console connected: " + debugClient.Client.RemoteEndPoint);
			if (connectedCallback != null) {
				connectedCallback();
			}

			while (true) {
				if (isStoped) {
					break;
				}
				processDelegate.GetType ().GetMethods ();
				IAsyncResult ar = processDelegate.BeginInvoke (null, null);
				waitter = ar.AsyncWaitHandle;
				waitter.WaitOne (); // ## Block here.
			}
		}
	}
	
	/// <summary>
	/// Processes the remote debug. Run in a new thread.
	/// </summary>
	protected void processRemoteDebug() {
		if(debugClient == null) {
			return;
		}
		
		for(String msg = (String)msgQueue.Dequeue(); msg!=null; msg = (String)msgQueue.Dequeue()) {
			debugClient.GetStream().Write(Encoding.Default.GetBytes(msg.ToString()), 0, msg.Length);
		}
	}

	public void log (String prefix, String msg) {
		String logInfo = prefix + " " + ": " + msg + "\r\n";
		// Callback log.
		localDebugCallback(logInfo);
		
		// Not ready for remote debug.
		if (tcpListener == null) {
			return;
		}
		
		// Log to queue for remote displaying.
		if (msgQueue.Count > MAX_MSG_QUEUE_SIZE) {
			msgQueue.Dequeue();
		}
		msgQueue.Enqueue(logInfo);
		if (waitter != null) {
//			localDebugCallback("Signal and wait");
			WaitHandle.SignalAndWait(waitter, waitter);
		}
	}
	
	public void logErr(String prefix, String msg) {
		// TODO
	}

	public void stop() {
		localDebugCallback("Stoping remote debug...");
		waitter.Close();
		// Close connection to client.
		debugClient.GetStream().Close();
		debugClient.Close();
		
		tcpListener.Stop();
		mainThread.Abort();
		localDebugCallback("Remote debug aborted.");
	}

}
