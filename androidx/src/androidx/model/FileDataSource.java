package androidx.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Access SQLite database from local file system(usually SDCard).
 * Extend me to do more works on database.
 * Override initTables() method to create database schema.
 * Call connect() before invoking any database operation method.
 * @author yuxing
 *
 */
public class FileDataSource {
	
	public static final String TABLE_NAME_USER_META = "USER_META";

	public static final String SQL_CREATE_TABLE_USER_META = "create table "
			+ TABLE_NAME_USER_META
			+ " (ID integer not null PRIMARY KEY autoincrement, "
			+ " META_NAME text not null UNIQUE, " 
			+ " META_VALUE text not null, "
			+ " DEFAULT_VALUE text, "
			+ " MODIFY_TIME long not null, "
			+ " EXT_DATA text)";
	
	protected String dbFilePath;
	
	protected SQLiteDatabase db ;
	
	protected final String SQL_DROP_TABLE = "drop table ${tableName}";

	protected final String SQL_FIND_ALL = "select * from ${tableName}";

	/**
	 * Constructor.
	 * @param dbFilePath
	 */
	public FileDataSource(String dbFilePath) {
		super();
		this.dbFilePath = dbFilePath;
	}
	
	/**
	 * Connect to DB file, create it if not exist.
	 */
	public void connect() {
		if(db != null && db.isOpen()) {
			return;
		}
		File dbFile = null;
		dbFile = new File(dbFilePath);
		if (!dbFile.exists()) {
			try {
				if (!dbFile.getParentFile().exists()) {
					if (dbFile.getParentFile().mkdirs() == false) {
						Log.e("db", "Failed to create db directory : " + dbFile.getParent());
						return;
					}
					else {
						Log.i("db", "Black list db directory created: " + dbFile.getParent());
					}
				}
				if(dbFile.createNewFile() == false) {
					Log.d("db", "Failed to create db file: " + dbFile);
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
	}
	
	public void disconnect() {
		if (db != null && db.isOpen()) {
			db.close();
		}
	}
	
	/**
	 * Init tables by default: user meta infomation table.
	 * Override me if more tables to be created.
	 */
	public void initTables() {
		createTable(SQL_CREATE_TABLE_USER_META);
	}
	
	/**
	 * Get meta data by name. No need to open and close connection explicitly.
	 * @param metaName
	 * @return
	 */
	public String getMeta(String metaName) {
		if (!db.isOpen()) {
			this.connect();
		}
		try {
			Cursor cursor = db.query(TABLE_NAME_USER_META, null, " META_NAME = ? ", new String[] { metaName }, null, null, null);
			if (cursor.moveToNext()) {
				return cursor.getString(cursor.getColumnIndex("META_VALUE"));
			}
			else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Set meta data by name. No need to open and close connection explicitly.
	 * @param metaName
	 * @param metaValue
	 * @return
	 */
	public boolean setMeta(String metaName, String metaValue) {
		if(getMeta(metaName) == null) {
			if (!db.isOpen()) {
				this.connect();
			}
			try {
				ContentValues values = new ContentValues();
				values.put("META_NAME", metaName);
				values.put("META_VALUE", metaValue);
				values.put("MODIFY_TIME", Calendar.getInstance().getTimeInMillis());
				db.insert(TABLE_NAME_USER_META, null, values);
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
//			} finally {
//				this.disconnect();
			}
		}
		else {
			if (!db.isOpen()) {
				this.connect();
			}
			try {
				db.execSQL("UPDATE " + TABLE_NAME_USER_META + " SET META_VALUE=? WHERE META_NAME=?", new String[]{metaValue, metaName});
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
//			} finally {
//				this.disconnect();
			}			
		}
		return true;
	}
	
	/**
	 * Create table by sql if not exist.
	 * @param sql
	 */
	public void createTable(String sql) {
		if(db == null || !db.isOpen()) {
			Log.e("db", "Database instance is not correctly initilized.");
			return;
		}
		try {
			db.execSQL(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Log.i("db", "Table created");
//			db.close();
		}
	}
	
	public void dropTable(String tableName) {
		if (db == null || !db.isOpen()) {
			Log.e("db", "Database instance is not correctly initilized.");
			return;
		}
		try {
			db.execSQL(SQL_DROP_TABLE.replace("${tableName}", tableName));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Log.i("db", "Table " + tableName + " has been droped");
			this.disconnect();
		}
	}
	
	public boolean isExists(String tableName, String uniqueCol, String colValue) {
		String sql = "select * from " + tableName + " where " + uniqueCol + "='" + colValue + "'";
//		Log.d("db", "SQL:" + sql);
		Cursor cur = null;
		try {
			cur = db.rawQuery(sql, null);
			if(cur.moveToNext()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally{
			if(cur != null )cur.close();	
		}
		return false;
	}
	
	public Map findUnique(String tableName, String uniqueColName, String value) {
		List<Map> data = findAll(tableName, uniqueColName + "=" + value);
		if(data == null || data.size() == 0) {
			return null;
		}
		return data.get(0);
	}
	
	public List<Map> findAll(String tableName) {
		return findAll(tableName, null,  null);
	}
	
	public List<Map> findAll(String tableName, String whereClause) {
		return findAll(tableName, whereClause,  null);
	}
	
	/**
	 * 查询指定表的所有记录。
	 * @param tableName
	 * @param orderBy
	 * @return 一个List，每一项表示一条记录，每项用Map表示，key为字段名，value为字段值。
	 */
	public List<Map> findAll(String tableName, String whereClause,  String orderByClause) {
		if(db == null || !db.isOpen()) {
			Log.e("db", "Database instance is not correctly initilized.");
			return null;
		}
		connect();
		Log.d("db", "Find all in table " + tableName);
		List<Map> result = new ArrayList();
//		String sql = SQL_FIND_ALL.replace("${tableName}", tableName);
		Cursor cursor = db.query(tableName, null, whereClause, null, null, null, orderByClause);
		int n = 0;
		for(;cursor.moveToNext();) {
//			Log.d("db", "Row" + n++);
			int count = cursor.getColumnCount();
			Map row = new HashMap();
			for (int i = 0; i < count; i++) {
				String colName = cursor.getColumnName(i);
				row.put(colName, cursor.getString(i));
			}
			result.add(row);
		}
		Log.d("db", "Result with " + result.size() + " records.");
		this.disconnect();
		return result;
	}
	
	public long countTableAll(String table) {
		return countTable(table, null, null);
	}
	
	public long countTable(String table, String filter, String[] values) {
		//TODO 暂时用query替代实现，如果有性能问题，再改成用rawQuery实现
		Cursor cursor = db.query(table, null, filter, values, null, null, null, null);
		long i=0;
		for(;cursor.moveToNext();i++){}
		cursor.close();
		return i;
	}
	
	/**
	 * 
	 * @param tbName
	 * @param pkID
	 * @return
	 */
	public boolean deleteRow(String tbName, long pkID) {
		if (this.db == null) {
			return false;
		}
		try {
			int rows = db.delete(tbName, "ID=?", new String[]{"" + pkID});
			Log.i("db", "" + rows  + " rows deleted.");
			return (rows > 0);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
//		} finally {
//			this.disconnect();
		}
	}

	public SQLiteDatabase getDb() {
		return db;
	}

	
}
