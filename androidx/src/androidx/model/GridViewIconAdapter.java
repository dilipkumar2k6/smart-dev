package androidx.model;

import java.util.List;
import java.util.Map;

import org.androidx.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Show grid view with icon in either Drawable type or resource type.
 * @author 
 *
 */
public class GridViewIconAdapter implements ListAdapter {
	protected LayoutInflater inflater;
	protected List<Map<String, ?>> data;
	protected String[] keys;
	protected Context context;
	protected int layoutResId;
	protected int[] itemResIds;

	/**
	 * Use default layout.
	 * @param context
	 * @param data
	 * @param keys
	 */
	public GridViewIconAdapter(Context context, List<Map<String, ?>> data, String[] keys) {
		super();
		this.keys = keys;
		this.data = data;
		this.context = context;
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
		super();
		this.keys = keys;
		this.data = data;
		this.context = context;
		this.layoutResId = layoutResId;
		this.itemResIds = itemResIds;
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
//		return data.get(position).get(idkey);
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		inflater = LayoutInflater.from(context);
		
		// Layout
		int resLayout = layoutResId == 0 ? R.layout.common_gridview_item : layoutResId;
		LinearLayout layout = (LinearLayout)inflater.inflate(resLayout, null);
		
		Map row = data.get(position);
		if (row == null) {
			return layout;
		}
		
		// Icon
		int resIcon = layoutResId == 0 ? R.id.cgi_iv_icon : itemResIds[0];
		ImageView imgView = (ImageView)layout.findViewById(resIcon);
		Object img = row.get(keys[0]);
		if(img instanceof Drawable) {
			imgView.setImageDrawable((Drawable)img);	
		}
		else if(img instanceof Integer) {
			imgView.setImageResource((Integer)img);
		}
		else {
			return layout;
		}
				
		
		// Description
		int resDesc = layoutResId == 0 ? R.id.cgi_tv_label : itemResIds[1];
		TextView txtView = (TextView)layout.findViewById(resDesc);
		txtView.setText(new String(row.get(keys[1]).toString().getBytes()));
		return layout;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

}
