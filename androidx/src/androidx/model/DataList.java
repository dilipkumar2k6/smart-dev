package androidx.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import androidx.Callback;

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

	public void traverse(Callback<Map> handler) {
		for (int i = 0; i < this.size(); i++) {
			handler.invoke((Map)this.get(i));
		}
	}
}
