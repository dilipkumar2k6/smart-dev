package androidx; 

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class Utils {
	
	public static String quaterLogicStr(boolean firstCond, String firstResult, boolean secondCond, String secondResult) {
		return (String)quaterLogic(firstCond, firstResult, secondCond, secondResult);
	}
	
	/**
	 * Just used for simplify code to be less if - else statements.
	 * @param firstCond
	 * @param firstResult
	 * @param secondCond
	 * @param secondResult
	 * @return
	 */
	public static Object quaterLogic(boolean firstCond, Object firstResult, boolean secondCond, Object secondResult) {
		if (firstCond) {
			return firstResult;
		}
		else if (secondCond) {
			return secondResult;
		}
		else {
			return "";
		}
	}
	
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
	
	static SimpleDateFormat TIME_FORMATTER  = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	
	public static String stringify(Calendar time) {
		return TIME_FORMATTER.format(time.getTime());
	}
	
	public static String stringify(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return TIME_FORMATTER.format(cal.getTime());
	}
	
	public static String rightPad(String str, int count, char c) {
		char[] pad = new char[count];
		for(int i=0;i<count;i++) {
			pad[i] = c;
		}
		return str.concat(new String(pad));
	}
}
