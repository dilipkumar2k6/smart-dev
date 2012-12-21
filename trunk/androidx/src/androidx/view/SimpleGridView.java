package androidx.view;

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
	
}
