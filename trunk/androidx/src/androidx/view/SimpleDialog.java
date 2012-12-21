package androidx.view;

import java.util.Map;

import org.androidx.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.Callback.CallbackAdapter;

public class SimpleDialog {
	// == 字符串资源 ==
	protected String tagOk = "OK";
	protected String tagCancel = "Cancel";
	protected String tagYes = "Yes";
	protected String tagNo = "No";
	protected String tagSave = "Save";
	protected String tagClose = "Close";
	
	protected int dialogBgColor = Color.LTGRAY;
	
	protected int dialogTxtColor = Color.BLACK;
	
	// 处理UI更新
	protected final Handler handler = new Handler();

	// 信息对话框
	protected AlertDialog infoDialog;

	// 确认对话框
	protected AlertDialog confirmDialog;

	// 进度条对话框
	protected AlertDialog progressDialog;

	// 单项输入对话框
	protected AlertDialog searchDialog;

	// 单选组对话框
	protected AlertDialog radioGroupDialog;

	// 列表选择对话框
	protected AlertDialog listSelectDialog;
	
	Context context;
	Resources rs;

	public SimpleDialog(Context context) {
		super();
		this.context = context;
		this.rs = context.getResources();
		
		tagOk = rs.getString(android.R.string.ok);
		tagCancel = rs.getString(android.R.string.cancel);
		tagClose = rs.getString(R.string.common_close);
		tagYes = rs.getString(android.R.string.yes);
		tagNo = rs.getString(android.R.string.no);
	}

	/**
	 * Show dialog with message to confirm something.
	 * 
	 * @param msg
	 * @param callback
	 */
	public void showConfirmDialog(String msg, final DialogCallback callback) {
		AlertDialog.Builder dBuilder = new Builder(context);
		dBuilder.setMessage(msg);
		dBuilder.setIcon(android.R.drawable.ic_menu_help);
		dBuilder.setPositiveButton(tagOk, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("Confirm Dialog", "OK clicked");
				callback.onPositive(dialog);
				dialog.dismiss();
				callback.afterSelected();
			}
		});
		dBuilder.setNegativeButton(tagCancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("Confirm Dialog", "Calcel clicked");
				callback.onNegative(dialog);
				dialog.dismiss();
				callback.afterSelected();
			}
		});

		confirmDialog = dBuilder.create();
		confirmDialog.setTitle(rs.getString(R.string.common_dialog_confirm_title));
		confirmDialog.show();
	}

	/**
	 * Show un-interrupted progress dialog with message.
	 * 
	 * @param msg
	 * @param callback
	 */
	public void showProgressDialog(String msg, final DialogCallback callback) {
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
		progressDialog.setTitle(rs.getString(R.string.common_dialog_progress_title));
		progressDialog.show();
	}

	/**
	 * Show dialog with single input.
	 * 
	 * @param title
	 * @param msg
	 * @param inputInit
	 *            Init the input edit text.
	 * @param callback
	 *            Callback with user inputs when click OK button.
	 * @return
	 */
	public AlertDialog showInputDialog(String title, String msg, String inputInit, final DialogCallback callback) {
		View inputView = LayoutInflater.from(context).inflate(R.layout.common_dialog_single_input, null);
		final EditText txtInput = (EditText) inputView.findViewById(R.id.editTxtInput);
		AlertDialog.Builder dBuilder = new Builder(context);
		dBuilder.setView(inputView);
		dBuilder.setIcon(android.R.drawable.ic_dialog_info);
		dBuilder.setMessage(msg);
		dBuilder.setPositiveButton(tagYes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onPositive(txtInput.getText().toString().trim());
				searchDialog.dismiss();
			}
		});
		dBuilder.setNegativeButton(tagNo, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onNegative(dialog);
				searchDialog.dismiss();
			}
		});

		txtInput.setText(inputInit);
		searchDialog = dBuilder.create();
		searchDialog.setTitle(title);
		searchDialog.show();
		return searchDialog;
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
	public AlertDialog showRadioGroupDialog(String title, String msg, String[] labels, int checked,
			final DialogCallback callback) {
		View inputView = LayoutInflater.from(context).inflate(R.layout.common_dialog_radiogroup, null);
		inputView.setBackgroundColor(dialogBgColor);
		final RadioGroup radioGroup = (RadioGroup) inputView.findViewById(R.id.cdr_rg_selection);
		// radioGroup.setBackgroundColor(dialogBgColor);
		radioGroup.removeAllViews();
		for (int i = 0; i < labels.length; i++) {
			// Log.d("", "Add new radio to group " + labels[i]);
			RadioButton radio = new RadioButton(context);
			radio.setId(i);
			radio.setText(labels[i]);
			radio.setChecked(checked == i ? true : false);
			radio.setTextColor(dialogTxtColor);
			radioGroup.addView(radio);
		}

		AlertDialog.Builder dBuilder = new Builder(context);
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
	 * 
	 * @param title
	 * @param msg
	 * @param labels
	 * @param checkboxListViewAdapter
	 *            Adapter to init the list view with checkboxs.
	 * @param callback
	 *            Callback to invoker.
	 * @return
	 */
	public AlertDialog showCheckBoxsDialog(String title, BaseAdapter checkboxListViewAdapter,
			final DialogCallback callback) {
		View inputView = LayoutInflater.from(context).inflate(R.layout.common_dialog_list_select, null);
		final ListView listView = (ListView) inputView.findViewById(R.id.cdr_rg_selection);

		listView.setAdapter(checkboxListViewAdapter);

		AlertDialog.Builder dBuilder = new Builder(context);
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
	 * 
	 * @param msg
	 */
	public void showInfoDialog(final String msg) {
		AlertDialog.Builder dBuilder = new Builder(context);
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
	 * 
	 * @param title
	 * @param items
	 * @param callback
	 *            列表选中一项时调用，参数为选项位置。
	 */
	public void showListSelectDialog(final String title, final String[] items, final DialogCallback callback) {
		View fileActionView = LayoutInflater.from(context).inflate(R.layout.common_dialog_list_select, null);
		ListView listSelect = (ListView) fileActionView.findViewById(R.id.cdl_list);
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
		listSelectDialog.show();
	}
	
	/**
	 * Show dialog with key-value list, when click, callback with selected key.
	 * @param title
	 * @param items
	 * @param callback
	 */
	public void showListSelectDialog(final String title, final Map items, final DialogCallback callback) {
		View fileActionView = LayoutInflater.from(context).inflate(R.layout.common_dialog_list_select, null);
		ListView listSelect = (ListView) fileActionView.findViewById(R.id.cdl_list);
		SimpleListView slv = new SimpleListView(context, listSelect);
		slv.addAllItems(items);
		slv.onItemClick(new CallbackAdapter() {
			@Override
			public void invoke(Object key) {
				callback.onPositive(key);
			}
		});
		slv.render();

		AlertDialog.Builder dBuilder = new Builder(context);
		dBuilder.setTitle(title);
		dBuilder.setIcon(android.R.drawable.ic_menu_more);
		dBuilder.setView(fileActionView);
		listSelectDialog = dBuilder.create();
		listSelectDialog.show();
	}
	
	
	/**
	 * Callback for dialog.
	 * 
	 * @author 
	 * 
	 */
	public static class DialogCallback<T> {
		/**
		 * Positive button clicked.
		 * @param value
		 */
		public void onPositive(T value){};
		
		/**
		 * Positive button clicked with multi-values returned.
		 * @param values
		 */
		public void onPositive(T... values) {};

		/**
		 * Negative button clicked.
		 */
		public void onNegative(T value){};
		
		/**
		 * Invoked after any choices that make.
		 */
		public void afterSelected(){}
	}
}