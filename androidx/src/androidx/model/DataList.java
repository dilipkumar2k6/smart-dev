package androidx.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
		if(keys==null || values == null|| keys.length != values.length) {
			return;
		}
		Map m = new HashMap();
		for(int i=0; i<keys.length; i++) {
			m.put(keys[i], values[i]);
		}
		this.add((T)m);
	}
	
	public static interface Callback<T extends Map> {
		public void invoke(int i, T row);
	}
}
