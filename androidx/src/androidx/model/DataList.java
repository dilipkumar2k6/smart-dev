package androidx.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import androidx.Utils;

/**
 * Represent data recordset.
 * 
 * @author 
 * 
 */
public class DataList<T extends DataRow> extends ArrayList<T> {
	public DataList() {
		super();
	}
	
	public DataList(Collection<T> collection) {
		super(collection);
	}
	
	/**
	 * Wrap all elements in collection to DataRow class type.
	 * @param coll
	 * @return
	 */
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
		addRow(this.size() - 1, DataRow.class, keys, values);
	}

	/**
	 * 
	 * @param idx
	 * @param keys
	 * @param values
	 */
	public void addRow(int idx, String[] keys, Object[] values) {
		addRow(idx, DataRow.class, keys, values);
	}

	/**
	 * 
	 * @param rowType
	 * @param keys
	 * @param values
	 */
	public void addRow(Class rowType, String[] keys, Object[] values) {
		addRow(this.size() - 1, rowType, keys, values);
	}

	/**
	 * 添加一行，存储为指定的类型，此类型必须与T一致
	 * @param rowType
	 * @param keys
	 * @param values
	 */
	public void addRow(int idx, Class rowType, String[] keys, Object[] values) {
		T row;
		try {
			Constructor cst = rowType.getConstructor(Map.class);
			row = (T)cst.newInstance(Utils.arrays2map(keys, values));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}		
		if(idx <0) {
			this.add(row);
		}
		else {
			this.add(idx, row);	
		}
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public DataRow getRow(final long id) {
		DataRow ret = null; 
		for (int i = 0; i < this.size(); i++) {
			DataRow row = this.get(i);
			if(row.getID() == id) {
				ret = row;
				break;
			}			
		}
		return ret;
	}

	/**
	 * 遍历表中所有的数据行（DataRow）
	 * @param handler
	 */
	public void traverse(Callback handler) {
		for (int i = 0; i < this.size(); i++) {
//			Log.d("androidx", "Traverse DataList's items with type: " + this.get(i).getClass().toString());
//			Log.d("androidx", this.get(i).toString());
			handler.invoke(i, (T)this.get(i));
		}
	}
	
	/**
	 * 将某个字段纵向导出为数组。
	 * @param keyName
	 * @return
	 */
	public Object[] getColumn(String colName) {
		Object[] ret = new Object[this.size()];
		for(int i = 0; i < this.size(); i++) {
			ret[i] = this.get(i).get(colName);
		}
		return ret;
	}
	
	/**
	 * 将某个字段纵向导出为数组。
	 * @param colName
	 * @return
	 */
	public String[] getColumnAsString(String colName) {
		String[] ret = new String[this.size()];
		for(int i = 0; i < this.size(); i++) {
			ret[i] = this.get(i).get(colName).toString();
		}
		return ret;
	}
	
	/**
	 * Callback for traverse DataList.
	 * @author 
	 *
	 * @param <T>
	 */
	public static interface Callback<T extends DataRow> {
		
		/**
		 * 
		 * @param i
		 * @param row
		 * @return true to continue; false to break the following operations.
		 */
		public boolean invoke(int i, T row);
	}

}
