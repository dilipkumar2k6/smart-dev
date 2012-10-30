package androidx.view;

import java.util.ArrayList;

import org.androidx.R;

import android.content.Context;
import android.widget.GridView;
import android.widget.SimpleAdapter;

public class SimpleGridView extends SimpleCompositeView{
	private GridView gv;

	public SimpleGridView(Context context, GridView gv) {
		super(gv);
		data = new ArrayList();
		adapter = new SimpleAdapter(context, data, R.layout.common_gridview_item
        		, keys, new int[]{R.id.cgi_iv_icon, R.id.cgi_tv_label});
		this.gv = gv;
	}
	
	/**
	 * 可以自定义Item显示的样式。
	 * @param context
	 * @param gv
	 * @param itemResId 资源ID，包含一个名为cgi_iv_icon的ImageView和一个名为cgi_tv_label的TextView。
	 */
	public SimpleGridView(Context context, GridView gv, int itemResId) {
		super(gv);
		data = new ArrayList();
		adapter = new SimpleAdapter(context, data, itemResId, keys, new int[]{R.id.cgi_iv_icon, R.id.cgi_tv_label});
		this.gv = gv;
	}
	
}
