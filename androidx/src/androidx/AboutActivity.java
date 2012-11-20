package androidx;

import org.androidx.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class AboutActivity extends BaseActivity {
	
	public static final String KEY_APP_NAME = "app.name";
	public static final String KEY_APP_VERSION = "app.version";
	public static final String KEY_APP_LOGO = "app.logo";
	public static final String KEY_HOME_URL = "home.url";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final String appName = getArgStrFromPreActivity(KEY_APP_NAME);
		final String appVersion = getArgStrFromPreActivity(KEY_APP_VERSION);
		final String homePageUrl = getArgStrFromPreActivity(KEY_HOME_URL);

		this.setContentView(R.layout.common_about_us);

		View logoView = this.findViewById(R.id.cau_tv_logo_img);

		logoView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(homePageUrl != null) {
					Uri uri = Uri.parse(homePageUrl);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);					
				}
			}
		});
		
		setTextViewText(R.id.cau_tv_app_name, appName);
		setTextViewText(R.id.cau_tv_version, appVersion);
	}

}
