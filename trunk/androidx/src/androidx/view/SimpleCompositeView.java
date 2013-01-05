package androidx.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.Callback;
import androidx.Utils;
import androidx.view.SimpleListView.SimpleInfoListViewAdapter;

/**
 * The base composite view. 
 * @author 
 *
 */
public abstract class SimpleCompositeView {
	
	public static final int STATE_POSITIVE = 1;
	public static final int STATE_NEGATIVE = 2;
	
	public static int translateState(int value, int posTarget, int negTarget) {
		return (Integer)Utils.quaterLogic(value
				, posTarget, SimpleCompositeView.STATE_POSITIVE
				, negTarget, SimpleCompositeView.STATE_NEGATIVE);
	}
	
	protected Context ctx;
	protected AbsListView alv;
	protected String idkey = "k_id";
	protected String statekey = "k_state";
	
	// keys to extract text line 1 and text line 2 for each ListView item. 
	protected String[] keys = new String[] { "k1", "k2", "k3"};
	
	// Data of rows with k1 and k2.
	protected List<Map<String, ?>> data;
	
	// Label that displayed while no data for this View.
	public String defaultLabel = "[Not set yet]";
	
	// Adapter for data of view.
	private ListAdapter adapter;

	public SimpleCompositeView(Context context, AbsListView alv) {
		this.ctx = context;
		this.alv = alv;
		data = new ArrayList();
		adapter = getAdapter(context);
	}
	
	public SimpleCompositeView(Context context, ListView lv, String defaultLabel) {
		this(context, lv);
		this.defaultLabel = defaultLabel;
		adapter = getAdapter(context);
	}
	
	/**
	 * Inject adapter by overriding this method.
	 * This method will be called while constructing the view.
	 * @param context
	 * @return
	 */
	protected abstract ListAdapter getAdapter(Context context);

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

	/**
	 * Add new item 
	 * @param id
	 * @param values
	 * @return
	 */
	public SimpleCompositeView addItem(Object id, Object[] values) {
		return addItem(id, 0 , values);
	}

	/**
	 * Add new list item, title and description.
	 * 
	 * @param
	 * @param
	 * @return
	 */
	public SimpleCompositeView addItem(Object title, Object desc) {
		return addItem(null, 0, title, desc);
	}
	
	/**
	 * Add new item with id, title and description.
	 * @param id
	 * @param title
	 * @param desc
	 * @return
	 */
	public SimpleCompositeView addItem(Object id, Object title, Object desc) {
		return addItem(id, 0, title, desc);
	}
	
	/**
	 * Add new item with title, description, state.
	 * @param title
	 * @param desc
	 * @param state
	 * @return
	 */
	public SimpleCompositeView addItem(Object title, Object desc, int state) {
		return addItem(null, state, title, desc);
	}
	
	/**
	 * Add new item with id, title, description and state.
	 * @param id
	 * @param state
	 * @param values
	 * @return
	 */
	public SimpleCompositeView addItem(Object id, int state, Object... values) {
		Map m = new HashMap();
		if (id != null) {
			m.put(idkey, id);
		}
		m.put(statekey, state);
		if(values.length > 0)m.put(keys[0], values[0]);
		if(values.length > 1)m.put(keys[1], values[1]);
		if(values.length > 2)m.put(keys[2], values[2]);
		data.add(m);
		return this;
	}
	
	/**
	 * Add all map entries, key as id, value as title and description.
	 * @param m
	 * @return
	 */
	public SimpleCompositeView addAllItems(Map m) {
		Iterator it = m.keySet().iterator();
		while (it.hasNext()) {
			Object k = it.next();
			Object v = m.get(k);
			Log.d("", k + "=" + v);
			if (v != null) {
				addItem(k, v, v);
			}
		}
		return this;
	}
	
	/**
	 * 
	 * @param data
	 * @param k1
	 * @param k2
	 * @return
	 */
	public SimpleCompositeView addAllItems(List<Map> data, Object k1, Object k2) {
		for (int i = 0; i < data.size(); i++) {
			Map m = data.get(i);
			addItem(m.get(k1), m.get(k2), null);
		}
		return this;
	}


	/**
	 * Remove item by item ID.
	 * @param id
	 * @return
	 */
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
	
	/**
	 * Clear all data.
	 */
	public void clear() {
		data.clear();
		// render here will case wrong displaying.
		// this.render();
	}

	/**
	 * Render to display all data for this view.
	 */
	public void render() {
		Log.d("androidx", "render()");
		if (this.alv == null) {
			throw new RuntimeException("The composite view was not init correctly.");
		}
		if (data == null || data.isEmpty()) {
			Log.d("SR", "  No data for ListView, show info");
			this.alv.setAdapter(new SimpleInfoListViewAdapter(ctx, defaultLabel));
		}
		else {
			this.alv.setAdapter(adapter);
		}
	}
	
	/**
	 * Set handler for item clicked event.
	 * @param handler Invoked with ID and displayed contents if ID exists.
	 */
	public void onItemClick(final Callback handler) {
		this.alv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long itemid) {
				Log.d("androidx", "  Item at position " + pos + " was clicked");
				handleClickEvent(pos, handler);
			}
		});
	}
	
	/**
	 * Set handler for item long clicked event.
	 * @param handler Invoked with ID and displayed contents if ID exists.
	 */
	public void onItemLongClick(final Callback handler) {
		this.alv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long itemid) {
				Log.d("androidx", "  Item at position " + pos + " was long clicked");
				handleClickEvent(pos, handler);
				return false;
			}
		});
	}
	
	private void handleClickEvent(int pos, Callback handler) {
		Map item = data.get(pos);
		if (item == null || item.isEmpty()) {
			return;
		}
		Object bizid = item.get(idkey);
		if (bizid == null) {
			Log.d("androidx", "  No business ID there, callback without anything");
			handler.invoke();
		}
		else {
			Log.d("androidx", "  Select business ID " + bizid + "[" + bizid.getClass() + "]");
			handler.invoke(bizid);
			handler.invoke(bizid, item.get(keys[0]), item.get(keys[1]));
		}
	}

	/**
	 * Get inner data as a list with Maps inside.
	 * @return
	 */
	public List<Map<String, ?>> getData() {
		return data;
	}
	
}