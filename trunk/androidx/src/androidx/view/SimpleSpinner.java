package androidx.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.SpinnerItem;

/**
 * 
 * @author
 * 
 */
public class SimpleSpinner {

	protected Context ctx;

	protected Spinner spinner;

	public SimpleSpinner(Context ctx) {
		super();
		this.ctx = ctx;
	}

	public SimpleSpinner(Spinner spinner) {
		super();
		this.spinner = spinner;
	}

	/**
	 * Init spinner with key-value data in Map.
	 * @param spinner
	 * @param map
	 * @return
	 */
	public Spinner initSpinner(Spinner spinner, Map map) {
		return initSpinner(spinner, mapToList(map).toArray());
	}

	/**
	 * Init spinner with data array.
	 * @param spinner
	 * @param data
	 * @return
	 */
	public Spinner initSpinner(Spinner spinner, Object[] data) {
		if (ctx == null) {
			throw new RuntimeException("No context specified for this Spinner");
		}
		if (spinner == null) {
			throw new RuntimeException("No spinner found");
		}
		this.spinner = spinner;
		if (data == null || data.length == 0) {
			Log.d("androidx", "Nothing to init for Spinner");
			return spinner;
		}
		Log.d("androidx", "" + data.length);
		ArrayAdapter adapter = new ArrayAdapter(ctx, android.R.layout.simple_spinner_item, data);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		return spinner;
	}

	
	public Object setSelection(Object item) {
		return setSelection(spinner, item);
	}

	/**
	 * Set spinner's selection directly by item object.
	 * @deprecated
	 * @param spinner
	 * @param item
	 */
//	public SpinnerItem setSelection(Spinner spinner, SpinnerItem item) {
//		if (spinner == null || item == null) {
//			return null;
//		}
//		int n = spinner.getAdapter().getCount();
//		for (int i = 0; i < n; i++) {
//			Object value = spinner.getItemAtPosition(i);
//			if (item.equals(value)) {
//				spinner.setSelection(i);
//				return item;
//			}
//		}
//		return null;
//	}

	public Object setSelection(long itemId) {
		return setSelection(spinner, itemId);
	}

	/**
	 * Set spinner's selection by item ID.
	 * 
	 * @param spinner
	 * @param itemId
	 */
	public Object setSelection(Spinner spinner, Object itemId) {
		if (spinner == null || itemId == null) {
			return null;
		}
		int n = spinner.getAdapter().getCount();
		for (int i = 0; i < n; i++) {
			Object value = spinner.getItemAtPosition(i);
			if(value instanceof SpinnerItem) {
				SpinnerItem si = (SpinnerItem) value;
				Log.d("", itemId + " -- " + si.getId());
				if (itemId.equals(si.getId())) {
					spinner.setSelection(i);
					return si;
				}				
			}
			else {
				if (itemId.equals(value)) {
					spinner.setSelection(i);
					return itemId;
				}
			}
		}
		return null;
	}

	public Object getSelectedSpinnerKey() {
		SpinnerItem spitem = (SpinnerItem) spinner.getSelectedItem();
		return spitem.getId();
	}

	/**
	 * Convert key-value map too SpinnerItem list.
	 * @param map
	 * @return
	 */
	protected List<SpinnerItem> mapToList(Map map) {
		final List<SpinnerItem> items = new ArrayList<SpinnerItem>();
		Iterator<Long> it = map.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			Object value = map.get(key);
			items.add(new SpinnerItem(key, value));
		}
		return items;
	}
}
