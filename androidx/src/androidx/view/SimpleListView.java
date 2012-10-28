package androidx.view;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.Callback;

/**
 * The ListView with SimpleAdapter is not simple actually, so this SimpleListView
 * is born to make it really simple.
 * @author yuxing
 *
 */
public class SimpleListView extends SimpleCompositeView {
	private ListView lv;
	
	public SimpleListView(Context context, ListView lv) {
		super(lv);
		data = new ArrayList();
		adapter = new SimpleAdapter(context, data, android.R.layout.simple_list_item_2
        		, keys, new int[]{android.R.id.text1, android.R.id.text2});
		this.lv = lv;
	}

	
//	public void render() {
//		this.lv.setAdapter(adapter);
//	}

}
