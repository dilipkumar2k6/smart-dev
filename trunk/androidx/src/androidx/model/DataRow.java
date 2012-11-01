package androidx.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataRow extends HashMap<String, Object> implements Serializable {

	public DataRow() {
		super();
	}

	public DataRow(Map<? extends String, ? extends Object> map) {
		super(map);
	}

}
