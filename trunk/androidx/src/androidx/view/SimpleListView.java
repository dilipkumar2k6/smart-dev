package androidx.view;

import android.content.Context;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * The ListView with SimpleAdapter is not simple actually, so this SimpleListView
 * is born to make it really simple.
 * @author yuxing
 *
 */
public class SimpleListView extends SimpleCompositeView {
//	private ListView lv;
	
	public SimpleListView(Context context, ListView lv) {
		super(lv);
		adapter = new SimpleAdapter(context, data, android.R.layout.simple_list_item_2
        		, keys, new int[]{android.R.id.text1, android.R.id.text2});
//		this.lv = lv;
	}

	
//	public void render() {
//		this.lv.setAdapter(adapter);
//	}

}
