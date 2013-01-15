package androidx;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.androidx.R;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

/**
 * Utilities for Android.
 * @author 
 *
 */
public class AndroidUtils {
	
	private static final String GLOBAL_SETTING = "org.androidx";
	
	/**
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getIMEI(Context ctx) {
		TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		return imei;
	}

	public static int getProcessId(Context ctx, String pkgName) {
		ActivityManager actManager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
		
		List<RunningAppProcessInfo> processes = actManager.getRunningAppProcesses();
		Iterator<RunningAppProcessInfo> it = processes.iterator();
		while(it.hasNext()) {
			RunningAppProcessInfo psinfo = it.next();
//			Log.d("androidx", psinfo.pid + "");
//			Log.d("androidx", psinfo.processName);
			for(int i=0; i<psinfo.pkgList.length; i++) {
//				Log.d("androidx", "  " + psinfo.pkgList[i]);
				if(psinfo.pkgList[i].equals(pkgName)) {
					return psinfo.pid;
				}
			}
		}
		return 0;
	}

	
	public static void killProcess(Context ctx,String pkgName) {
		ActivityManager actManager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
		// version 1.5 - 2.1
		if(android.os.Build.VERSION.SDK_INT <= 7) {
			actManager.restartPackage(pkgName);
		}
		// version 2.2+
		else {
			actManager.killBackgroundProcesses(pkgName);
		}
	}
	
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
	
	public static Drawable getAppIcon(Context ctx, String pkgName) {
//		PackageInfo pi = ctx.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
		try {
			return ctx.getPackageManager().getApplicationIcon(pkgName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return ctx.getResources().getDrawable(R.drawable.ic_launcher);
		}
	}
	
	/**
	 * Retrieve APP displaying name by it's package name.
	 * @param ctx
	 * @param pkgName
	 * @return
	 */
	public static String getAppName(Context ctx, String pkgName) {
		try {
			ApplicationInfo appinfo = ctx.getPackageManager().getApplicationInfo(pkgName, PackageManager.GET_ACTIVITIES);
			if(appinfo == null) {
				return pkgName;
			}
			return ctx.getPackageManager().getApplicationLabel(appinfo).toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return pkgName;
		}
	}
	
	/**
	 * Retrieve all installed APPs.
	 * @param ctx
	 * @return
	 */
	public static List getInstalledApps(Context ctx) {
		List<PackageInfo> pkgs = ctx.getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES);
		List ret = new ArrayList();
		for (Iterator it = pkgs.iterator(); it.hasNext();) {
			PackageInfo pkg = (PackageInfo) it.next();
			String packageName = pkg.packageName;
			String appName;
			Drawable icon;
			try {
				if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					continue;
				}
				appName = ctx.getPackageManager().getApplicationLabel(pkg.applicationInfo).toString();
				icon = ctx.getPackageManager().getApplicationIcon(packageName);
				ret.add(new Object[]{packageName, appName, icon});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	/**
	 * 获取应用程序的版本号
	 * @param ctx
	 * @param packageName
	 * @return 版本数组（长度取决于版本信息�?
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
	
	/**
	 * 
	 * @param ctx
	 * @param packageName
	 * @return
	 */
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
	
	/**
	 * Version of current APP.
	 * @param ctx
	 * @return
	 */
	public static String getAppVersion(Context ctx) {
		try {
			PackageInfo pkgInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			return pkgInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "0.0";
		}
	}
	
	/**
	 * Divide screen height to grid.
	 * @param ctx
	 * @param gridHeight
	 * @param skipHeight
	 * @return
	 */
	public static int divideScreenHeight(Context ctx, int gridHeight, int skipHeight) {
		Log.d("AndroidUtils", "Screen: " + AndroidUtils.getScreenWidth(ctx) + "X" + AndroidUtils.getScreenHeight(ctx));
		int screenH = AndroidUtils.getScreenHeight(ctx);
		int contentHeight = screenH - skipHeight;
		double spacing = ((contentHeight / 160.0) * (contentHeight / 160.0));
		return (int) Math.round(contentHeight / (gridHeight + spacing));
	}
	
	/**
	 * Divide screen width to grid.
	 * @param ctx
	 * @param gridWidth
	 * @param skipWidth
	 * @param gridHeight
	 * @param skipHeight
	 * @param spacing
	 * @return
	 */
	public static int divideScreenWidth(Context ctx, int gridWidth, int skipWidth, int gridHeight, int skipHeight, int spacing){
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
		SharedPreferences setting = ctx.getSharedPreferences(ctx.getPackageName(), 0);
		setting.edit().putString(name, value.toString()).commit();
	}

	/**
	 * Get global setting from system, return specified default value if not exists.
	 * 
	 * @param ctx
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static String getGlobalSetting(Context ctx, String name, Object defaultValue) {
		SharedPreferences setting = ctx.getSharedPreferences(ctx.getPackageName(), 0);
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
	 * Get global setting.
	 * @param ctx
	 * @param name
	 * @return
	 */
	public static String getGlobalSetting(Context ctx, String name) {
		SharedPreferences setting = ctx.getSharedPreferences(ctx.getPackageName(), 0);
		if(setting.getString(name, null) == null) {
			return null;
		}
		else {
			return setting.getString(name, null).trim();
		}
	}

	/**
	 * 
	 * @param ctx
	 * @param prefix
	 * @return
	 */
	public static Map<String, Object> getGlobalSettingsWithPrefix(Context ctx, String prefix) {
		SharedPreferences setting = ctx.getSharedPreferences(ctx.getPackageName(), 0);
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
		SharedPreferences setting = ctx.getSharedPreferences(ctx.getPackageName(), 0);
		return setting.edit().remove(key).commit();
	}

	
	
	/**
	 * 在状态栏显示提示消息�?
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
	
//	protected int findNotifiation() {
//		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		notificationManager.
//		return 0;
//	}
	
	
	/**
	 * 取消状态栏提示消息�?
	 * @param id
	 */
	public static void cancelNotification(Context context, int id) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
	}
	
	public static void showToast(Context context,String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	public static void showToast(Context context,String msg, Object... params) {
		// TODO
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isSDCardAvailable() {
		File sdcard = new File("/sdcard/");
		return sdcard.exists() && sdcard.canWrite();
	}
		
	public static String getContactName(Context context, String phone) {
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.NUMBER + " = " + phone, null, null);
		if (cursor == null)
			return null;
		while (cursor.moveToNext()) {
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

			Cursor cursor2 = cr.query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts._ID + "="
					+ contactId, null, null);
			if (cursor2 == null)
				return null;
			while (cursor2.moveToNext()) {
				return cursor2.getString(cursor2.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			}
		}
		return null;
	}

	/**
	 * Read contacts names from device storage.
	 * @param context
	 * @return Map with phone number to contact name.
	 */
	public static Map<String, String> getContactsNamesFromDevice(Context context) {
		Map<String, String> ret = new HashMap();
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		while (cursor.moveToNext()) {
			int idxName = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			String contact = cursor.getString(idxName);
			if(Utils.isEmpty(contact)) {
				continue;
			}
			Log.d("", "" + contact);

			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			Cursor phoneNums = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
			ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "	+ contactId, null, null);// 第一个参数是确定查询电话号，第三个参数是查询具体某个人的过滤�

			while (phoneNums.moveToNext()) {
				String phoneNum = phoneNums.getString(phoneNums.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				ret.put(phoneNum.trim(), contact);
			}
			phoneNums.close();
		}
		cursor.close();
		return ret;
	}
}
