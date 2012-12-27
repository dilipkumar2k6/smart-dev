package androidx.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.util.Log;
import android.widget.Spinner;
import androidx.SpinnerItem;
import androidx.model.DataList;
import androidx.model.DataRow;

public class MoreSimpleSpinner extends SimpleSpinner{

	protected Activity activity;

	public MoreSimpleSpinner(Activity activity) {
		super(activity);
		this.activity = activity;
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
	 * 
	 * @param resId
	 * @param map
	 * @return
	 */
	public Spinner initSpinner(int resId, Map map) {
		return initSpinner(resId, mapToList(map).toArray());
	}

	/**
	 * Init spinner with data array which has no ID.
	 * 
	 * @param resId
	 * @param data
	 * @return
	 */
	public Spinner initSpinner(int resId, Object[] data) {
		if (activity == null) {
			throw new RuntimeException("No activity specified for this Spinner");
		}
		spinner = (Spinner) activity.findViewById(resId);
		if (spinner == null) {
			Log.w("androidx", "Failed to load Spinner: " + activity.getResources().getResourceEntryName(resId)
					+ " from Activity " + activity.getClass().getName());
			return null;
		}
		return initSpinner(spinner, data);
	}
}
