package androidx.model; 
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Base Adapter for any ListView.
 * 
 * @author 
 * @deprecated
 */
public abstract class BaseListViewAdapter extends BaseAdapter {
	protected LayoutInflater inflater;
	protected Context context;
	protected Resources resources;

	public BaseListViewAdapter() {
		super();
	}

	public BaseListViewAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.resources = context.getResources();
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

}
