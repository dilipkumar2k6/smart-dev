package androidx;

import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;


/**
 * 需要激活操作的Activity基础类。
 * TODO
 * @author yxwang
 * 
 */
public abstract class BasePreActivity extends BaseActivity {

	private static final String SETTING_KEY_APP_ACTIVE_KEY = "app_active_key";
	private static final String SETTING_KEY_APP_ACTIVED = "app_actived";

	@Override
	protected void onStart() {
		super.onStart();
		String activeFlag = AndroidUtils.getGlobalSetting(context, SETTING_KEY_APP_ACTIVED);
		// 需要激活先
		if (activeFlag == null || "false".equals(activeFlag)) {
			String activeKey = AndroidUtils.getGlobalSetting(context, SETTING_KEY_APP_ACTIVE_KEY);
			super.showInputDialog("Activate this tool",
					"Before using this tool, you must activate it by your phone number first", "",
					new DialogCallback() {

						@Override
						public void onPositive(Object value) {
							super.onPositive(value);
							// Retrieve active code from server.
							
							// Notify use to activate app.
							String phoneNumber = value.toString().trim();
							SmsManager sms = SmsManager.getDefault();
							PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
							sms.sendTextMessage(phoneNumber, null, "156234", pi, null);
							
							showInputDialog("Input activate code", "Please input the activate code from SMS", "", new DialogCallback(){

								@Override
								public void onPositive(Object value) {
									super.onPositive(value);
									if(true) {
										AndroidUtils.saveGlobalSetting(context, SETTING_KEY_APP_ACTIVED, "true");
										AndroidUtils.saveGlobalSetting(context, SETTING_KEY_APP_ACTIVE_KEY, value);
									}
								}

								@Override
								public void onNegative(Object value) {
									super.onNegative(value);
									finish();
								}
								
							});
							
						}

						@Override
						public void onNegative(Object value) {
							super.onNegative(value);
							finish();
						}
					});
		}
		else {
			return;
		}
	}
	
	private void sendSmsToMySelf() {
		
		
	}

}
