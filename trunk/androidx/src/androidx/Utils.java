package androidx; 

import java.util.HashMap;
import java.util.Map;


public class Utils {
	
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Convert key-value arrays to map.
	 * @param keys
	 * @param values
	 * @return
	 */
	public static Map arrays2map(String[] keys, Object[] values) {
		if(keys==null || values == null|| keys.length != values.length) {
			return null;
		}
		Map m = new HashMap();
		for(int i=0; i<keys.length; i++) {
			m.put(keys[i], values[i]);
		}
		return m;
	}
}
