package androidx.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.SpinnerItem;
import androidx.model.DataList;
import androidx.model.DataRow;

/**
 * 
 * @author 
 *
 */
public class SimpleSpinner {
	
	protected Activity activity;
	
	protected Context ctx;
	
	protected Spinner spinner;

	public SimpleSpinner(Context ctx) {
		super();
		this.ctx = ctx;
	}

	public SimpleSpinner(Activity activity) {
		super();
		this.activity = activity;
	}

	public SimpleSpinner(Spinner spinner) {
		super();
		this.spinner = spinner;
	}

	/**
	 * Init spinner with key-values which grab from DataList.
	 * 
	 * @param resId
	 * @param data
	 * @param idkey
	 * @param valuekey
	 * @return
	 */
	public Spinner initSpinner(int resId, DataList<DataRow> data, final String idkey, final String valuekey) {
		final List<SpinnerItem> items = new ArrayList<SpinnerItem>();
		data.traverse(new DataList.Callback<DataRow>() {

			@Override
			public boolean invoke(int i, DataRow row) {
				items.add(new SpinnerItem(Long.parseLong(row.get(idkey).toString()), row.get(valuekey).toString()));
				return true;
			}

		});
		return initSpinner(resId, items.toArray());
	}

	/**
	 * Init spinner with key-values which grab from map.
	 * @param resId
	 * @param map
	 * @return
	 */
	public Spinner initSpinner(int resId, Map map) {
		final List<SpinnerItem> items = new ArrayList<SpinnerItem>();
		Iterator<Long> it = map.keySet().iterator();
		while (it.hasNext()) {
			Long key = (Long) it.next();
			Object value = map.get(key);
			items.add(new SpinnerItem(key, value));
		}
		return initSpinner(resId, items.toArray());
	}

	/**
	 * Init spinner with data array which has no ID.
	 * 
	 * @param resId
	 * @param data
	 * @return
	 */
	public Spinner initSpinner(int resId, Object[] data) {
		if(activity == null) {
			throw new RuntimeException("No activity specified for this Spinner");
		}
		spinner = (Spinner) activity.findViewById(resId);
		if (spinner == null) {
			Log.w("androidx", "Failed to load Spinner: " + activity.getResources().getResourceEntryName(resId));
			return null;
		}

		ArrayAdapter adapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, data);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		return spinner;
	}

	public Object setSpinner(Object item) {
		return setSpinner(spinner, item);
	}
	
	/**
	 * Set spinner's selection directly by item object.
	 * 
	 * @param spinner
	 * @param item
	 */
	public Object setSpinner(Spinner spinner, Object item) {
		if (spinner == null || item == null) {
			return null;
		}
		int n = spinner.getAdapter().getCount();
		for (int i = 0; i < n; i++) {
			Object value = spinner.getItemAtPosition(i);
			if (item.equals(value)) {
				spinner.setSelection(i);
				return item;
			}
		}
		return null;
	}
	
	public Object setSpinner(long itemId) {
		return setSpinner(spinner, itemId);
	}

	/**
	 * Set spinner's selection by item ID.
	 * 
	 * @param spinner
	 * @param itemId
	 */
	public SpinnerItem setSpinner(Spinner spinner, long itemId) {
		if (spinner == null || itemId < 0) {
			return null;
		}
		int n = spinner.getAdapter().getCount();
		for (int i = 0; i < n; i++) {
			SpinnerItem si = (SpinnerItem) spinner.getItemAtPosition(i);
			Log.d("", itemId + " -- " + si.getId());
			if (itemId == si.getId()) {
				spinner.setSelection(i);
				return si;
			}
		}
		return null;
	}
	
	public Object getSelectedSpinnerKey() {
		SpinnerItem spitem = (SpinnerItem)spinner.getSelectedItem();
		return spitem.getId();
	}
}
