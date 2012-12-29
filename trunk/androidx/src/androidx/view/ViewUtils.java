package androidx.view;

import android.text.InputType;
import android.widget.EditText;

public class ViewUtils {

	public static EditText initEditTextByType(EditText et, Class clazz) {
		if(clazz == Integer.class || clazz == Long.class) {
			et.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
		}
		else if(clazz == Float.class || clazz == Double.class) {
			et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}
		else if(clazz == String.class) {
			et.setInputType(InputType.TYPE_CLASS_TEXT);
		}
		else {
			et.setInputType(InputType.TYPE_CLASS_TEXT);
		}
		return et;
	}
}
