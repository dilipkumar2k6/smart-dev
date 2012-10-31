package androidx.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Represent data recordset.
 * 
 * @author yuxing
 * 
 */
public class DataList extends ArrayList<Map> {

	public DataList(Collection<? extends Map> collection) {
		super(collection);
	}

	public void traverse(Callback handler) {
		for (int i = 0; i < this.size(); i++) {
			handler.invoke(i, (Map)this.get(i));
		}
	}
	
	public static interface Callback {
		public void invoke(int i, Map row);
	}
}
