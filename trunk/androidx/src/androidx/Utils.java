package androidx; 

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Utils {
	
	public static SimpleDateFormat TIME_FORMATTER_DATA  = new SimpleDateFormat("yyyy-MM-dd");
	
	public static SimpleDateFormat TIME_FORMATTER_TIME  = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public static String quaterLogicStr(Object value, Object op0, String ret0, Object op1, String ret1) {
		return (String)quaterLogic(value, op0, ret0, op1, ret1);
	}
	
	public static Object quaterLogic(Object value, Object op0, Object ret0, Object op1, Object ret1) {
		return quaterLogic(value.equals(op0), ret0, value.equals(op1), ret1);
	}
	
//	public static <TI, TO> Object quaterLogic(TI value, TI op0, TO ret0, TI op1, TO ret1) {
//		return quaterLogic(value.equals(op0), ret0, value.equals(op1), ret1);
//	}
	
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
	
	/**
	 * Return default string value if not exist.
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getStringFrom(Map map, String key, String defaultValue) {
		return (isEmpty(map.get(key)) ? defaultValue : map.get(key).toString());
	}
	
	public static boolean isEmpty(Object str) {
		return str == null || str.toString().length() == 0;
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
	
	public static Object[] append(Object[] arr, Object... newEl){
		Object[] ret = new Object[arr.length + newEl.length];
		for(int i=0; i<arr.length; i++) {
			ret[i] = arr[i];
		}
		for(int j=0;j<newEl.length;j++) {
			ret[arr.length + j] = newEl[j];	
		}		
		return ret;
	}

	public static String stringifyDate(Calendar date) {
		return TIME_FORMATTER_DATA.format(date.getTime());
	}

	public static String stringifyTime(Date time) {
		return TIME_FORMATTER_TIME.format(time);
	}
	
	public static String stringifyTime(Calendar time) {
		return TIME_FORMATTER_TIME.format(time.getTime());
	}
	
	public static String stringifyTime(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return TIME_FORMATTER_TIME.format(cal.getTime());
	}
	
	public static String rightPad(String str, int count, char c) {
		char[] pad = new char[count];
		for(int i=0;i<count;i++) {
			pad[i] = c;
		}
		return str.concat(new String(pad));
	}

}
