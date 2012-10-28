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
	
	

}
