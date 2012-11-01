package androidx.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import androidx.Utils;

/**
 * Represent data recordset.
 * 
 * @author yuxing
 * 
 */
public class DataList<T extends Map> extends ArrayList<T> {

	public DataList(Collection<T> collection) {
		super(collection);
	}

	public void traverse(Callback handler) {
		for (int i = 0; i < this.size(); i++) {
			handler.invoke(i, (T)this.get(i));
		}
	}
	
	public void addRow(String[] keys, Object[] values) {
		Map m = Utils.arrays2map(keys, values);
		this.add((T)m);
	}
	
	public static interface Callback<T extends Map> {
		public void invoke(int i, T row);
	}
}
