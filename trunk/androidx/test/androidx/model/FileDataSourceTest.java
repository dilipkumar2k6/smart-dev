package androidx.model;

import java.util.Calendar;

import junit.framework.Assert;
import android.content.ContentValues;
import android.test.AndroidTestCase;

public class FileDataSourceTest extends AndroidTestCase {

	public void testInsertData() {
		FileDataSource ds = new FileDataSource("/sdcard/temp/unit_test.db");
		
		ds.connect();
		ds.createTable(FileDataSource.SQL_CREATE_TABLE_USER_META);
		
		ContentValues values = new ContentValues();
		
		values.put("META_NAME", "name_" + Calendar.getInstance().getTimeInMillis());
		values.put("META_VALUE", "value_0");
		values.put("MODIFY_TIME", Calendar.getInstance().getTimeInMillis());
		long id = ds.getDb().insert(FileDataSource.TABLE_NAME_USER_META, null, values);
		if(id == -1) {
			Assert.fail("Not inserted.");
		}
		
		System.out.println("Data " + id + " inserted");
		
		ds.deleteRow(FileDataSource.TABLE_NAME_USER_META, id);
		
		ds.connect();
		id = ds.getDb().insert(FileDataSource.TABLE_NAME_USER_META, null, values);
		if(id == -1) {
			Assert.fail("Not inserted.");
		}
		System.out.println("Data " + id + " inserted");
		ds.disconnect();
	}
}
