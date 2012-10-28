package androidx.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import androidx.Callback;

public class SimpleCompositeView {
	protected AbsListView alv;
	protected SimpleAdapter adapter;
	protected String idkey = "k_id";
	protected String[] keys = new String[] { "k1", "k2" };
	protected List<Map<String, ?>> data;

	public SimpleCompositeView(AbsListView alv) {
		this.alv = alv;
	}

	/**
	 * Add new list item, title and description.
	 * 
	 * @param values
	 *            Must with 2 elements in it.
	 * @return
	 */
	public SimpleCompositeView addItem(Object[] values) {
		if (values == null || values.length != 2) {
			throw new IllegalArgumentException();
		}
		return addItem(values[0], values[1]);
	}

	public SimpleCompositeView addItem(Object id, Object[] values) {
		if (values == null || values.length != 2) {
			throw new IllegalArgumentException();
		}
		return addItem(id, values[0], values[1]);
	}

	/**
	 * Add new list item, title and description.
	 * 
	 * @param
	 * @param
	 * @return
	 */
	public SimpleCompositeView addItem(Object title, Object desc) {
		return addItem(null, title, desc);
	}

	public SimpleCompositeView addItem(Object id, Object title, Object desc) {
		Map m = new HashMap();
		if (id != null) {
			m.put(idkey, id);
		}
		m.put(keys[0], title);
		m.put(keys[1], desc);
		data.add(m);
		return this;
	}

	public boolean removeItem(Object id) {
		if (id == null) {
			return false;
		}
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).get(idkey) == id) {
				data.remove(i);
				this.render();
				return true;
			}
		}
		return false;
	}

	public void render() {
		this.alv.setAdapter(adapter);
	}
	
	public void onItemClick(final Callback handler) {
		this.alv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long itemid) {
				Log.d("androidx", "Item at position " + pos  + " was clicked");
				handleClickEnvent(pos, handler);
			}
		});
	}
	
	public void onItemLongClick(final Callback handler) {
		this.alv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long itemid) {
				Log.d("androidx", "Item at position " + pos  + " was long clicked");
				handleClickEnvent(pos, handler);
				return false;
			}
		});
	}
	
	private void handleClickEnvent(int pos, Callback handler) {
		Map item = data.get(pos);
		Object bizid = item.get(idkey);
		if(bizid == null) {
			handler.invoke();
		}
		else {
			handler.invoke(bizid);
			handler.invoke(bizid, item.get(keys[0]), item.get(keys[1]));
		}
	}

}