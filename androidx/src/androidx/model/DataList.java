package androidx.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;
import androidx.Utils;

/**
 * Represent data recordset.
 * 
 * @author yuxing
 * 
 */
public class DataList<T extends DataRow> extends ArrayList<T> {
	public DataList() {
		super();
	}
	
	public DataList(Collection<T> collection) {
		super(collection);
	}
	
	public static DataList wrap(Collection coll) {
		return wrap(coll, DataRow.class);
	}
	
	/**
	 * Wrap all elements in collection to specified type.
	 * @param coll
	 * @param elType
	 * @return
	 */
	public static DataList wrap(Collection coll, Class elType) {
		DataList ret = new DataList();
		if(coll == null || coll.size() == 0) {
			return ret;
		}		
		Constructor cst;
		try {
			cst = elType.getConstructor(Map.class);
			if(cst == null) {
				return ret;
			}	
			Iterator it = coll.iterator();
			while(it.hasNext()) {
				ret.add(cst.newInstance((Map)it.next()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ret;
		}
		return ret;
	}

	/**
	 * 以默认DataRow类型添加一行。
	 * @param keys
	 * @param values
	 */
	public void addRow(String[] keys, Object[] values) {
//		DataRow m = new DataRow(Utils.arrays2map(keys, values));
//		this.add((T)m);
		addRow(DataRow.class, keys,  values);
	}
	
	/**
	 * 添加一行，存储为指定的类型，此类型必须与T一致
	 * @param rowType
	 * @param keys
	 * @param values
	 */
	public void addRow(Class rowType, String[] keys, Object[] values) {
		T row;
		try {
			
			Constructor cst = rowType.getConstructor(Map.class);
			row = (T)cst.newInstance(Utils.arrays2map(keys, values));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}		
//		DataRow m = new DataRow(Utils.arrays2map(keys, values));
		this.add(row);
	}

	/**
	 * 
	 * @param handler
	 */
	public void traverse(Callback handler) {
		for (int i = 0; i < this.size(); i++) {
//			Log.d("androidx", this.get(i).getClass().toString());
			Log.d("androidx", this.get(i).toString());
			handler.invoke(i, (T)this.get(i));
		}
	}
	
	/**
	 * Callback for traverse DataList.
	 * @author yuxing
	 *
	 * @param <T>
	 */
	public static interface Callback<T extends DataRow> {
		public void invoke(int i, T row);
	}

}
