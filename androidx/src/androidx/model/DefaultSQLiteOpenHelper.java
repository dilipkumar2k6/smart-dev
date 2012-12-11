package androidx.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A default SQLite helper which did nothing.
 * @author 
 *
 */
public class DefaultSQLiteOpenHelper extends SQLiteOpenHelper {

	public DefaultSQLiteOpenHelper(Context context, String name) {
		super(context, name, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


	}

}
