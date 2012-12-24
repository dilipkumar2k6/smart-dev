package androidx.view;

import java.util.List;
import java.util.Map;

import org.androidx.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.model.BaseListAdapter;

/**
 * Simple grid view with two text blocks.
 * @author 
 *
 */
public class SimpleGridView extends SimpleCompositeView{

	public SimpleGridView(Context context, GridView gv) {
		super(context, gv);
	}

	@Override
	protected ListAdapter getAdapter(Context context) {
		return new SimpleAdapter(context, data, android.R.layout.simple_list_item_2, keys, new int[] {
				android.R.id.text1, android.R.id.text2 });
	}


	/**
	 * Simple grid view with icon and description from default layout file.
	 * @author 
	 *
	 */
	public static class SimpleIconGridView extends SimpleGridView {

		public SimpleIconGridView(Context context, GridView gv) {
			super(context, gv);
		}

		@Override
		protected ListAdapter getAdapter(Context context) {
			return new GridViewIconAdapter(context, data, keys);
		}
		
	}
	
	/**
	 * Show grid view with icon in either Drawable type or resource type.
	 * @author 
	 *
	 */
	public static class GridViewIconAdapter extends BaseListAdapter {
		/**
		 * Use default layout.
		 * @param context
		 * @param data
		 * @param keys
		 */
		public GridViewIconAdapter(Context context, List<Map<String, ?>> data, String[] keys) {
			super(context, data, keys);
		}
		
		/**
		 * Use specified layout definition.
		 * @param context
		 * @param data
		 * @param keys
		 * @param layoutResId
		 * @param itemResIds
		 */
		public GridViewIconAdapter(Context context, List<Map<String, ?>> data, String[] keys, int layoutResId,  int[] itemResIds) {
			super(context, data, keys, layoutResId, itemResIds);
		}
		
		@Override
		public long getItemId(int position) {
			return super.getItemId(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			inflater = LayoutInflater.from(context);
			
			// Layout
			int resLayout = layoutResId == 0 ? R.layout.common_gridview_item : layoutResId;
			LinearLayout layout = (LinearLayout) inflater.inflate(resLayout, null);

			Map row = data.get(position);
			if (row == null) {
				return layout;
			}
			
			// Icon
			int resIcon = layoutResId == 0 ? R.id.cgi_iv_icon : itemResIds[0];
			ImageView imgView = (ImageView) layout.findViewById(resIcon);
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
			
			// Description
			int resDesc = layoutResId == 0 ? R.id.cgi_tv_label : itemResIds[1];
			TextView txtView = (TextView) layout.findViewById(resDesc);
			txtView.setText(new String(row.get(keys[1]).toString().getBytes()));
			return layout;
		}

	}
}
