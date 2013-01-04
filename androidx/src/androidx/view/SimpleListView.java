package androidx.view;

import java.util.List;
import java.util.Map;

import org.androidx.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.model.BaseListAdapter;

/**
 * The ListView with SimpleAdapter is not simple actually, so this SimpleListView
 * is born to make it really simple.
 * 
 * I you want to create list view with one icon, inherit me and 
 * override getAdapter() method with SimpleIconListViewAdapter returned.
 * @author 
 *
 */
public class SimpleListView extends SimpleCompositeView {

	public SimpleListView(Context context, ListView lv) {
		super(context, lv);
	}

	public SimpleListView(Context context, ListView lv, String defaultLabel) {
		super(context, lv, defaultLabel);
	}

	@Override
	protected ListAdapter getAdapter(Context context) {
		return new SimpleAdapter(context, data, android.R.layout.simple_list_item_2, keys, new int[] {
				android.R.id.text1, android.R.id.text2 });
	}
	
	/**
	 * Show only information 
	 * @author
	 *
	 */
	public static class SimpleInfoListViewAdapter extends BaseAdapter {
		protected Context context;
		// Label that displayed while no data for this View.
		protected String defaultLabel = "";
		
		public SimpleInfoListViewAdapter(Context context, String info) {
			this.context = context;
			this.defaultLabel = info;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int position) {
			return defaultLabel;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.ax_lv_item_info, null);
			TextView tv = (TextView) layout.findViewById(R.id.tv_alii_label);
			tv.setText(defaultLabel);
			return layout;
		}
	}
	
	
	/**
	 * Adapter for ListView to show icon and text(title and description).
	 * 
	 * @author 
	 *
	 */
	public static class SimpleIconListViewAdapter extends BaseListAdapter {
		
		
		public SimpleIconListViewAdapter(Context context, List<Map<String, ?>> data, String[] keys) {
			super(context, data, keys);
		}

		public SimpleIconListViewAdapter(Context context, List<Map<String, ?>> data, String[] keys, int layoutResId,
				int[] itemResIds) {
			super(context, data, keys, layoutResId, itemResIds);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			inflater = LayoutInflater.from(context);
			LinearLayout layout = (LinearLayout)inflater.inflate(layoutResId, null);
			
			Map row = data.get(position);
			if (row == null) {
				return layout;
			}
			
			// Icon
			ImageView imgView = (ImageView) layout.findViewById(itemResIds[0]);
			Object img = row.get(keys[0]);
			if (img instanceof Drawable) {
				imgView.setImageDrawable((Drawable) img);
			}
			else if (img instanceof Integer) {
				imgView.setImageResource((Integer) img);
			}
			else {
				return layout;
			}
			
			// Title and Description
			if (itemResIds.length > 1 && layout.findViewById(itemResIds[1]) != null) {
				TextView tvTitle = (TextView) layout.findViewById(itemResIds[1]);
				tvTitle.setText(new String(row.get(keys[1]).toString().getBytes()));
			}

			if (itemResIds.length > 2 && layout.findViewById(itemResIds[2]) != null) {
				TextView tvDesc = (TextView) layout.findViewById(itemResIds[2]);
				tvDesc.setText(new String(row.get(keys[1]).toString().getBytes()));
			}
			return layout;
		}

		
	}
}
