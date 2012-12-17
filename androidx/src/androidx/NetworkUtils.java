package androidx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import android.util.Log;

public class NetworkUtils {


	public static boolean isWgetStop = false;
	
	/**
	 * TODO Need re-arrange!
	 * @param downloadUrl
	 * @param localSavePath
	 * @param totalSize Download file size, unknown if <=0
	 * @return
	 */
	public static boolean wget(String downloadUrl, String localSavePath, long totalSize) {
		InputStream inputStream = null;
		FileOutputStream fileOutput = null;
		try {
			URL url = new URL(downloadUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
			File file = new File(localSavePath);
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			fileOutput = new FileOutputStream(file);
			inputStream = urlConnection.getInputStream();
			
			long downloadedSize = 0;
			long downloadSpeed = 0;
			float downloadPercent;
			byte[] buffer = new byte[4096];
			int bufferLength = 0;
			Log.d("wget()", "Download file size: " + totalSize);
			while ((bufferLength = inputStream.read(buffer)) > 0 && !isWgetStop) {
				fileOutput.write(buffer, 0, bufferLength);
				downloadedSize += bufferLength;
//			Log.d(TAG, downloadedSize + " bytes have been downloaded.");
				downloadSpeed = downloadedSize;
				downloadPercent = ((downloadedSize * 100) / totalSize) > 100 ? 100 : ((downloadedSize * 100) / totalSize);
			}
			
			// stop = !stop;
			// if (stop) {
			fileOutput.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				fileOutput.close();
				inputStream.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Retrieve the first found local IP address(No 127.0.0.1)
	 * @return
	 */
	public static String getFirstLocalIPAddress() {
		Enumeration<NetworkInterface> enu = null;
		try {
			enu = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if(enu == null) {
			System.out.println("No network interface found in this machine.");
			return null;
		}
		String appropriateIpAddress = null;
		while(enu.hasMoreElements()) {
			NetworkInterface ni = enu.nextElement();
			Enumeration<InetAddress> addresses = ni.getInetAddresses();
			while(addresses.hasMoreElements()) {
				InetAddress ia = addresses.nextElement();
				// Skip loopback
				if(ia.isSiteLocalAddress() && !"127.0.0.1".equals(ia.getHostAddress())) {
					// Prefer 192.* and 10.*
					if(ia.getHostAddress().startsWith("192") || ia.getHostAddress().startsWith("10")) {
						appropriateIpAddress = ia.getHostAddress();
					}
				}
			}
		}
		if(Utils.isEmpty(appropriateIpAddress)) {
			appropriateIpAddress = "127.0.0.1";
		}
		return appropriateIpAddress;
	}
	
}
