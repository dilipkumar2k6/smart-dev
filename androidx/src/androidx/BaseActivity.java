package androidx;

import org.androidx.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * 提供常用功能的基础Activity类<td/>
 * 文字资源；各种标准对话框；手势支持
 * 
 * @author yxwang
 * 
 */
public abstract class BaseActivity extends Activity {

	public static final String SYS_PROP_DEBUG_MODE = "androidx.debug";

	protected Context context;
	
	// DEBUG模式（默认）
	protected boolean debugMode = true; 
	
	// 资源(deprecated to rs)
	protected Resources resources;
	
	protected Resources rs;
	
	// 处理UI更新
	protected final Handler handler = new Handler();

	// 信息对话框
	protected AlertDialog infoDialog;
	
	// 确认对话框
	protected AlertDialog confirmDialog;

	// 进度条对话框
	protected AlertDialog progressDialog;

	// 单项输入对话框
	protected AlertDialog inputDialog;

	// 单选组对话框
	protected AlertDialog radioGroupDialog;
	
	// 列表选择对话框
	protected AlertDialog listSelectDialog;
	
	// == Tabs == 
	protected TabsController tabsController;
	
	// == 字符串资源 ==
	protected String tagOk = "OK";
	protected String tagCancel = "Cancel";
	protected String tagYes = "Yes";
	protected String tagNo = "No";
	protected String tagSave = "Save";
	
	protected DisplayMetrics dm;
	protected int sw;
	protected int sh;
	
	
	// == 需要的话覆盖变量值进行参数调整 ==
	
	protected int animationSpeed = 500; // ms, Larger is faster
	
	// Gesture
	protected int gestureThrottleVelocityX = 500;
	protected int gestureThrottleVelocityY = 30;
	
	protected final GestureDetector gestureDetector = new GestureDetector(new SimpleOnGestureListener() {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.d("Fling...", e1 + "__" + e2 + "__" + velocityX + "__" + velocityY);
			if (velocityX > gestureThrottleVelocityX && velocityY < gestureThrottleVelocityY) {
				Log.d(this.getClass().getSimpleName(), "Fling to right");
				onGesture(tabsController.currentTabIndex, tabsController.nextTabIndex());
			}

			else if (velocityX < -gestureThrottleVelocityX && velocityY > -gestureThrottleVelocityY) {
				Log.d(this.getClass().getSimpleName(), "Fling to left");
				onGesture(tabsController.currentTabIndex, tabsController.previousTabIndex());
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	});
	
	protected void onGesture(int oldTabIdx, int newTabIdx){
		// DO NOTHING
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		
		// 没设置参数的情况为true，只有明确设置不是debug模式的情况才是false。
		debugMode = !"false".equals(System.getProperty(SYS_PROP_DEBUG_MODE));
		
		resources = this.getResources();
		rs = this.getResources();

		tagOk = resources.getString(android.R.string.ok);
		tagCancel = resources.getString(android.R.string.cancel);
		
		dm = context.getApplicationContext().getResources().getDisplayMetrics();
		sw = dm.widthPixels;
		sh = dm.heightPixels;
	}

	public void startActivityByName(String actName) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		ComponentName cn = new ComponentName("com.easytime.easytools", actName);
		intent.setComponent(cn);
		startActivity(intent);
	}
	

	/**
	 * Show dialog with message to confirm something.
	 * 
	 * @param msg
	 * @param callback
	 */
	protected void showConfirmDialog(String msg, final DialogCallback callback) {
		AlertDialog.Builder dBuilder = new Builder(context);
		dBuilder.setMessage(msg);
		dBuilder.setIcon(android.R.drawable.ic_menu_help);
		dBuilder.setPositiveButton(tagOk, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("Confirm Dialog", "OK clicked");
				callback.onPositive(dialog);
				dialog.dismiss();
			}
		});
		dBuilder.setNegativeButton(tagCancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("Confirm Dialog", "Calcel clicked");
				callback.onNegative(dialog);
				dialog.dismiss();
			}
		});

		confirmDialog = dBuilder.create();
		confirmDialog.setTitle(resources.getString(R.string.common_dialog_confirm_title));
		confirmDialog.show();
	}
	
	/**
	 * Show un-interrupted progress dialog with message.
	 * 
	 * @param msg
	 * @param callback
	 */
	protected void showProgressDialog(String msg, final DialogCallback callback) {
		final View progressView = LayoutInflater.from(context).inflate(R.layout.common_progress_dialog, null);
		TextView textView = (TextView) progressView.findViewById(R.id.textViewMsg);
		textView.setText(msg);

		AlertDialog.Builder dBuilder = new Builder(context);
		dBuilder.setView(progressView);
		dBuilder.setIcon(android.R.drawable.ic_dialog_info);
		dBuilder.setNegativeButton(tagCancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("ProgressDialog", "Calcel clicked");
				callback.onNegative(dialog);
				dialog.dismiss();
			}
		});

		progressDialog = dBuilder.create();
		progressDialog.setTitle(resources.getString(R.string.common_dialog_progress_title));
		progressDialog.show();
	}

	/**
	 * Show dialog with single input.
	 * 
	 * @param title
	 * @param msg
	 * @param inputInit Init the input edit text.
	 * @param callback Callback when click OK button.
	 * @return
	 */
	protected AlertDialog showInputDialog(String title, String msg, String inputInit, final DialogCallback callback) {
		View inputView = LayoutInflater.from(this).inflate(R.layout.common_dialog_single_input, null);
		final EditText txtInput = (EditText) inputView.findViewById(R.id.editTxtInput);
		AlertDialog.Builder dBuilder = new Builder(this);
		dBuilder.setView(inputView);
		dBuilder.setIcon(android.R.drawable.ic_dialog_info);
		dBuilder.setMessage(msg);
		dBuilder.setPositiveButton(tagYes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onPositive(txtInput.getText().toString().trim());
				inputDialog.dismiss();
			}
		});
		dBuilder.setNegativeButton(tagNo, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onNegative(dialog);
				inputDialog.dismiss();
			}
		});

		txtInput.setText(inputInit);
		inputDialog = dBuilder.create();
		inputDialog.setTitle(title);
		inputDialog.show();
		return inputDialog;
	}

	/**
	 * Show radio group dialog, return selected index in group.
	 * 
	 * @param title
	 * @param msg
	 * @param labels
	 * @param checked
	 * @param callback
	 * @return
	 */
	protected AlertDialog showRadioGroupDialog(String title, String msg, String[] labels, int checked,
			final DialogCallback callback) {
		View inputView = LayoutInflater.from(this).inflate(R.layout.common_dialog_radiogroup, null);
		final RadioGroup radioGroup = (RadioGroup) inputView.findViewById(R.id.radioGroup);
		radioGroup.removeAllViews();
		for (int i = 0; i < labels.length; i++) {
//			Log.d("", "Add new radio to group " + labels[i]);
			RadioButton radio = new RadioButton(this);
			radio.setId(i);
			radio.setText(labels[i]);
			radio.setChecked(checked == i ? true : false);
			radioGroup.addView(radio);
		}

		AlertDialog.Builder dBuilder = new Builder(this);
		dBuilder.setView(inputView);
		dBuilder.setIcon(android.R.drawable.ic_menu_more);
		dBuilder.setMessage(msg);
		dBuilder.setPositiveButton(tagYes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onPositive(radioGroup.getCheckedRadioButtonId());
				radioGroupDialog.dismiss();
			}
		});
		dBuilder.setNegativeButton(tagNo, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onNegative(dialog);
				radioGroupDialog.dismiss();
			}
		});

		radioGroupDialog = dBuilder.create();
		radioGroupDialog.setTitle(title);
		radioGroupDialog.show();
		return radioGroupDialog;
	}

	/**
	 * Show dialog with Checkbox list view
	 * @param title
	 * @param msg
	 * @param labels
	 * @param checkboxListViewAdapter Adapter to init the list view with checkboxs.
	 * @param callback Callback to invoker.
	 * @return
	 */
	protected AlertDialog showCheckBoxsDialog(String title, BaseAdapter checkboxListViewAdapter, final DialogCallback callback) {
		View inputView = LayoutInflater.from(this).inflate(R.layout.common_dialog_list_select, null);
		final ListView listView = (ListView) inputView.findViewById(R.id.listViewSelect);
		
		listView.setAdapter(checkboxListViewAdapter);		

		AlertDialog.Builder dBuilder = new Builder(this);
		dBuilder.setView(inputView);
		dBuilder.setIcon(android.R.drawable.ic_menu_more);
		dBuilder.setPositiveButton(tagYes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onPositive(listView);
				listSelectDialog.dismiss();
			}
		});
		dBuilder.setNegativeButton(tagNo, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onNegative(dialog);
				listSelectDialog.dismiss();
			}
		});

		listSelectDialog = dBuilder.create();
		listSelectDialog.setTitle(title);
		listSelectDialog.show();
		return listSelectDialog;
	}

	/**
	 * 显示信息对话框。
	 * @param msg
	 */
	protected void showInfoDialog(final String msg) {
		AlertDialog.Builder dBuilder = new Builder(this);
		dBuilder.setMessage(msg);
		dBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("showInfoDialog", "Cancel clicked");
				infoDialog.dismiss();
			}
		});
		infoDialog = dBuilder.create();
		infoDialog.setTitle(android.R.string.dialog_alert_title);
		infoDialog.setIcon(android.R.drawable.ic_menu_info_details);
		infoDialog.show();
	}
	
	/**
	 * 显示一个列表对话框，比如“对话框式弹出菜单“。不自动关闭对话框，需要调用者手动关闭。
	 * @param title
	 * @param items
	 * @param callback 列表选中一项时调用，参数为选项位置。
	 */
	protected void showListSelectDialog(final String title, final String[] items, final DialogCallback callback) {
		View fileActionView = LayoutInflater.from(context).inflate(R.layout.common_dialog_list_select, null);
		ListView listSelect = (ListView) fileActionView.findViewById(R.id.listViewSelect);
		listSelect.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, items));
		listSelect.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				callback.onPositive(position);
			}
			
		});
		
		AlertDialog.Builder dBuilder = new Builder(context);
		dBuilder.setTitle(title);
		dBuilder.setIcon(android.R.drawable.ic_menu_more);
		dBuilder.setView(fileActionView);
		listSelectDialog = dBuilder.create();
		listSelectDialog .show();
	}
	
	/**
	 * 
	 * @param icon
	 * @param msg
	 * @param activity
	 */
	protected void showNotification(int icon, String msg, Class activity) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon,
				msg, System.currentTimeMillis());
		Intent notificationIntent = new Intent(context, activity);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, msg, "", contentIntent);
		notificationManager.notify(1, notification);
	}
	
	/**
	 * 
	 * @param lastView
	 * @param nextView
	 * @param isLeftToRight
	 */
	protected void playAnimation(View lastView, View nextView, boolean isLeftToRight) {
		if (lastView == null || nextView == null) {
			return;
		}
		AnimationSet aset = new AnimationSet(true);

		int distanceTo = isLeftToRight ? -sw : sw;
		int distanceFrom = isLeftToRight ? sw : -sw;

		TranslateAnimation ta1 = new TranslateAnimation(0, distanceTo, 0, 0);
		ta1.setDuration(animationSpeed);
		lastView.setAnimation(ta1);

		TranslateAnimation ta2 = new TranslateAnimation(distanceFrom, 0, 0, 0);
		ta2.setDuration(animationSpeed);
		nextView.setAnimation(ta2);

		aset.addAnimation(ta1);
		aset.addAnimation(ta2);
		aset.start();

		lastView.startAnimation(ta1);
		lastView.setVisibility(View.INVISIBLE);
		nextView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 获取LinearLayout
	 * @param resourceId
	 * @return
	 */
	protected LinearLayout getLinearyLayout(int resourceId) {
		return (LinearLayout)this.findViewById(resourceId);
	}
	
	/**
	 * 
	 * @param resourceId
	 * @return
	 */
	protected TextView getTextView(int resourceId) {
		return (TextView)this.findViewById(resourceId);
	}
	
	/**
	 * 
	 * @param resourceId
	 * @return
	 */
	protected Button getButton(int resourceId) {
		return (Button)this.findViewById(resourceId);
	}
	
	/**
	 * 取出并处理嵌入式的字符资源，嵌入格式: {编号}
	 * @param sentence
	 * @param words 字符串值或者字符串资源ID可以混合使用
	 * @return
	 */
	protected String getNestedString(int sentence, Object... words){
		String resource = rs.getString(sentence);
		for (int i = 0; i < words.length; i++) {
			if(words[i] instanceof Integer) {
				resource = resource.replace("{" + i + "}", rs.getString((Integer)words[i]));	
			}
//			else if(words[i] instanceof String) {
			else{
				resource = resource.replace("{" + i + "}", words[i].toString());
			}
		}
		return resource;
	}


	/**
	 * 用于对话框的回调。
	 * 
	 * @author yuxing
	 * 
	 */
	public static class DialogCallback {
		/**
		 * Positive button clicked.
		 * @param value
		 */
		public void onPositive(Object value){};
		
		/**
		 * Positive button clicked with multi-values returned.
		 * @param values
		 */
		public void onPositive(Object... values) {};

		/**
		 * Negative button clicked.
		 */
		public void onNegative(Object value){};
	}
}
