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
 * Show grid view with icon in Drawable type, not resource type.
 * @author 
 *
 */
public class GridViewIconAdapter implements ListAdapter {
	protected LayoutInflater inflater;
	protected List<Map<String, ?>> data;
	protected String[] keys;
	protected Context context;
	protected int layoutResId;

	public GridViewIconAdapter(Context context, List<Map<String, ?>> data, int layoutResId,  String[] keys) {
		super();
		this.keys = keys;
		this.data = data;
		this.context = context;
		this.layoutResId = layoutResId;
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
		
		LinearLayout layout = (LinearLayout)inflater.inflate(layoutResId, null);
		
		ImageView imgView = (ImageView)layout.findViewById(R.id.cgi_iv_icon);
		Map row = data.get(position);
		imgView.setImageDrawable((Drawable)row.get(keys[0]));
		
		TextView txtView = (TextView)layout.findViewById(R.id.cgi_tv_label);
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
