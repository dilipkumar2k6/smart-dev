package androidx.view;

import android.content.Context;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * The ListView with SimpleAdapter is not simple actually, so this SimpleListView
 * is born to make it really simple.
 * @author 
 *
 */
public class SimpleListView extends SimpleCompositeView {

	public SimpleListView(Context context, ListView lv) {
		super(context, lv);
	}

	@Override
	protected ListAdapter getAdapter(Context context) {
		return new SimpleAdapter(context, data, android.R.layout.simple_list_item_2
        		, keys, new int[]{android.R.id.text1, android.R.id.text2});
	}
}
