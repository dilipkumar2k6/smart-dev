package androidx.view;

import org.androidx.R;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import androidx.model.GridViewIconAdapter;

/**
 * 
 * @author 
 *
 */
public class SimpleGridView extends SimpleCompositeView{

	public SimpleGridView(Context context, GridView gv, boolean isFromResource) {
		super(context, gv);
		if(isFromResource) {
			adapter = new SimpleAdapter(context, data, R.layout.common_gridview_item
        		, keys, new int[]{R.id.cgi_iv_icon, R.id.cgi_tv_label});
		}
		else {
			adapter = new GridViewIconAdapter(context, data, R.layout.common_gridview_item, keys);
		}
	}

	@Override
	protected BaseAdapter getAdapter(Context context) {
		return null;
	}



	/**
	 * 可以自定义Item显示的样式。
	 * @param context
	 * @param gv
	 * @param itemResId 资源ID，包含一个名为cgi_iv_icon的ImageView和一个名为cgi_tv_label的TextView。
	 */
	public SimpleGridView(Context context, GridView gv, int itemResId) {
		super(context, gv);
		adapter = new SimpleAdapter(context, data, itemResId, keys, new int[]{R.id.cgi_iv_icon, R.id.cgi_tv_label});
	}
	
}
