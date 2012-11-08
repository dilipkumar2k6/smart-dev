package androidx.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author yuxing
 *
 */
public class DataRow extends HashMap<String, Object> implements Serializable {

	public DataRow() {
		super();
	}

	public DataRow(Map map) {
		super(map);
	}
	
	public long getID() {
		return 0;
	}

}
