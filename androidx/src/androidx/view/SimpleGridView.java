package androidx.view;

import org.androidx.R;

import android.content.Context;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import androidx.model.GridViewIconAdapter;

/**
 * Simple grid view with two text blocks.
 * @author 
 *
 */
public class SimpleGridView extends SimpleCompositeView{

	public SimpleGridView(Context context, GridView gv) {
		super(context, gv);
//		if(isFromResource) {
//			adapter = new SimpleAdapter(context, data, R.layout.common_gridview_item
//        		, keys, new int[]{R.id.cgi_iv_icon, R.id.cgi_tv_label});
//		}
//		else {
//			adapter = new GridViewIconAdapter(context, data, R.layout.common_gridview_item, keys);
//		}
	}

	@Override
	protected ListAdapter getAdapter(Context context) {
		return new SimpleAdapter(context, data, android.R.layout.simple_list_item_2, keys, new int[] {
				android.R.id.text1, android.R.id.text2 });
	}


	/**
	 * Simple grid view with icon and description from defaul layout file.
	 * @author allen
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
	 * 可以自定义Item显示的样式。
	 * @param context
	 * @param gv
	 * @param itemResId 资源ID，包含一个名为cgi_iv_icon的ImageView和一个名为cgi_tv_label的TextView。
	 */
//	public SimpleGridView(Context context, GridView gv, int itemResId) {
//		super(context, gv);
//		adapter = new SimpleAdapter(context, data, itemResId, keys, new int[]{R.id.cgi_iv_icon, R.id.cgi_tv_label});
//	}
	
}
