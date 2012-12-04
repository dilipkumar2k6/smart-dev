package androidx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * Utilities for Android specified.
 * @author 
 *
 */
public class AndroidUtils {
	
	private static final String GLOBAL_SETTING = "org.androidx";

	/**
	 * Compare target version with APP version.
	 * @param ctx
	 * @param packageName
	 * @param targetVersion
	 * @return Return 1 if target version higher than app version, return -1 if lower, return 0 if equals.
	 */
	public static int compareAppVersion(Context ctx, String packageName, String targetVersion) {
		int[] appVersion = getAppVersion(ctx, packageName);
		StringTokenizer token = new StringTokenizer(targetVersion, ".");
		for (int i = 0; token.hasMoreTokens(); i++) {
			int v = Integer.parseInt(token.nextToken().toString());
			if(v > appVersion[i]) {
				return 1;
			}
			else if(v < appVersion[i]) {
				return -1;
			}
			else {
				continue;
			}
		}
		return 0;
	}
	
	/**
	 * 获取应用程序的版本号
	 * @param ctx
	 * @param packageName
	 * @return 版本数组（长度取决于版本信息）
	 */
	public static int[] getAppVersion(Context ctx, String packageName) {
		List<PackageInfo> pkgs = ctx.getPackageManager().getInstalledPackages(0);
		for (Iterator it = pkgs.iterator(); it.hasNext();) {
			PackageInfo pi = (PackageInfo) it.next();
			if (pi.packageName.equals(packageName)) {
				StringTokenizer token = new StringTokenizer(pi.versionName, ".");
				int[] result = new int[token.countTokens()];
				for (int i = 0; token.hasMoreTokens(); i++) {
					result[i] = Integer.parseInt(token.nextToken().toString());
				}
				return result;
			}
		}
		return null;
	}
	
	public static String getAppVersionString(Context ctx, String packageName) {
		List<PackageInfo> pkgs = ctx.getPackageManager().getInstalledPackages(0);
		for (Iterator it = pkgs.iterator(); it.hasNext();) {
			PackageInfo pi = (PackageInfo) it.next();
			if (pi.packageName.equals(packageName)) {
				return pi.versionName;
			}
		}
		return null;
	}
	
	public static int splitScreenWidth(Context ctx, int gridHeight, int skipHeight) {
		//TODO
		Log.d("AndroidUtils", "Screen: " + AndroidUtils.getScreenWidth(ctx) + "X" + AndroidUtils.getScreenHeight(ctx));
		return 0;
	}
	
	/**
	 * Split screen height to grid.
	 * @param ctx
	 * @param gridHeight
	 * @param skipHeight
	 * @return
	 */
	public static int splitScreenHeight(Context ctx, int gridHeight, int skipHeight) {
		Log.d("AndroidUtils", "Screen: " + AndroidUtils.getScreenWidth(ctx) + "X" + AndroidUtils.getScreenHeight(ctx));
		int screenH = AndroidUtils.getScreenHeight(ctx);
		int contentHeight = screenH - skipHeight;
		double spacing = ((contentHeight / 160.0) * (contentHeight / 160.0));
		return (int) Math.round(contentHeight / (gridHeight + spacing));
	}
	
	public static int splitScreen(Context ctx, int gridWidth, int skipWidth, int gridHeight, int skipHeight, int spacing){
		Log.d("AndroidUtils", "Screen: " + AndroidUtils.getScreenWidth(ctx) + "X" + AndroidUtils.getScreenHeight(ctx));
		int result = (int) Math.round(AndroidUtils.getScreenWidth(ctx) / gridWidth)
				* (int) Math.round((AndroidUtils.getScreenHeight(ctx) - skipHeight) / gridHeight);
		
		Log.d("AndroidUtils", "Cols: " + Math.round(AndroidUtils.getScreenWidth(ctx) / gridWidth) + ", Rows: "
				+ (int) Math.round((AndroidUtils.getScreenHeight(ctx) - skipHeight) / gridHeight));
		return result;
	}
	
	public static int getScreenWidth(Context ctx) {
		DisplayMetrics dm = ctx.getApplicationContext().getResources().getDisplayMetrics();
		return dm.widthPixels;
	}
	
	public static int getScreenHeight(Context ctx) {
		DisplayMetrics dm = ctx.getApplicationContext().getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	/**
	 * 保存全局设置
	 * 
	 * @param ctx
	 * @param name
	 * @param value
	 */
	public static void saveGlobalSetting(Context ctx, String name, Object value) {
		SharedPreferences setting = ctx.getSharedPreferences(GLOBAL_SETTING, 0);
		setting.edit().putString(name, value.toString()).commit();
	}

	/**
	 * 取出全局设置，如果不存在，返回默认值
	 * 
	 * @param ctx
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static String getGlobalSetting(Context ctx, String name, Object defaultValue) {
		SharedPreferences setting = ctx.getSharedPreferences(GLOBAL_SETTING, 0);
		if (setting == null) {
			return defaultValue.toString();
		}
		if (setting.getString(name, defaultValue.toString()) == null) {
			return null;
		}
		else {
			return setting.getString(name, defaultValue.toString()).trim();
		}
	}

	/**
	 * 取出全局设置 
	 * @param ctx
	 * @param name
	 * @return
	 */
	public static String getGlobalSetting(Context ctx, String name) {
		SharedPreferences setting = ctx.getSharedPreferences(GLOBAL_SETTING, 0);
		if(setting.getString(name, null) == null) {
			return null;
		}
		else {
			return setting.getString(name, null).trim();
		}
	}
	
//	public static String getGlobalSettingInt()
	
	/**
	 * 
	 * @param ctx
	 * @param prefix
	 * @return
	 */
	public static Map<String, Object> getGlobalSettingsWithPrefix(Context ctx, String prefix) {
		SharedPreferences setting = ctx.getSharedPreferences(GLOBAL_SETTING, 0);
		Map result = new HashMap<String, Object>();
		Map m = setting.getAll();
		for (Iterator it = m.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			if (key.startsWith(prefix) && m.get(key) != null) {
				result.put(key, m.get(key));
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param ctx
	 * @param key
	 * @return 
	 */
	public static boolean removeGlobalSetting(Context ctx, String key) {
		Log.d("", "Try to remove setting: " + key);
		SharedPreferences setting = ctx.getSharedPreferences(GLOBAL_SETTING, 0);
		return setting.edit().remove(key).commit();
	}
	
	
	public static void tileBackground(Context ctx, View view, int resourceId) {
		Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), resourceId);
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		bd.setDither(true);
		view.setBackgroundDrawable(bd);
	}
	
	public static void mirrorBackground(Context ctx, View view, int resourceId) {
		Bitmap bmHead = BitmapFactory.decodeResource(ctx.getResources(), resourceId);
		BitmapDrawable bdHead = new BitmapDrawable(bmHead);
		bdHead.setTileModeXY(TileMode.MIRROR , TileMode.MIRROR);
		bdHead.setDither(true);		
		view.setBackgroundDrawable(bdHead);
	}
	
	/**
	 * 
	 * @param ctx
	 * @param serviceClass
	 * @return true if service is running, false otherwise
	 */
	public static boolean checkServiceStatus(Context ctx, Class serviceClass) {
		ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//	    	Log.d("Running Service:", service.service.getClassName());
	        if (service.service.getClassName().equals(serviceClass.getName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
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
	 * 在状态栏显示提示消息。
	 * @param context
	 * @param id Notification ID
	 * @param icon
	 * @param title
	 * @param msg
	 * @param activity 点击后调用的Activity
	 * @param sticky 是否常驻状态栏
	 */
	public static void showNotification(Context context, int id, int icon,String title, String msg, Class activity, boolean sticky) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon,
				msg, System.currentTimeMillis());
		Intent notificationIntent = new Intent(context, activity);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, title, msg, contentIntent);
		if(sticky) {
			notification.flags = Notification.FLAG_ONGOING_EVENT;
		}
		notificationManager.notify(id, notification);
	}
}
