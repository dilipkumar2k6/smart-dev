package androidx.model; 
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Tab按钮
 * @author yxwang
 *
 */
public class ImageTabAdapter extends BaseAdapter {
	Context ctx ;
	LinearLayout[] layouts = new LinearLayout[2];
	int curTab;

	public ImageTabAdapter(Context ctx, int curTab) {
		super();
		this.ctx = ctx;
		this.curTab = curTab;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public Object getItem(int arg0) {
		return layouts[arg0];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout tabLayout;
		tabLayout = new LinearLayout(ctx);
		tabLayout.setFocusable(false);
		tabLayout.setClickable(false);
		tabLayout.setOrientation(LinearLayout.HORIZONTAL);
		tabLayout.setGravity(Gravity.CENTER);
		tabLayout.setMinimumHeight(60);
		tabLayout.setFadingEdgeLength(0);
		tabLayout.setHorizontalFadingEdgeEnabled(false);
//		layout.setVerticalScrollBarEnabled(false);
//		layout.setHorizontalScrollBarEnabled(false);
//		tabLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT));

		
		if (curTab == position) {
			tabLayout.setBackgroundColor(Color.parseColor("#333333"));
		}
		else {
			tabLayout.setBackgroundColor(Color.parseColor("#666666"));
		}

		ImageView imgView = new ImageView(ctx);
		imgView.setPadding(0, 0, 5, 0);

		TextView txtView = new TextView(ctx);
		txtView.setGravity(Gravity.CENTER);

		switch (position) {
		case 0:
			imgView.setImageResource(android.R.drawable.ic_menu_manage);
			txtView.setText(ctx.getResources().getString(android.R.drawable.menu_frame));
			break;
		case 1:
			imgView.setImageResource(android.R.drawable.ic_menu_more);
			txtView.setText(ctx.getResources().getString(android.R.drawable.menu_full_frame));
			break;
//		case 2:
//			imgView.setImageResource(R.drawable.filesync_setting);
//			txtView.setText(ctx.getResources().getString(R.string.menu_setting));
//			break;
		default:
			break;
		}
		tabLayout.addView(imgView);
		tabLayout.addView(txtView);
		layouts[position] = tabLayout;
		return tabLayout;
	}

}
