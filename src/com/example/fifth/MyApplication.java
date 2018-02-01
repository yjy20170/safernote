package com.example.fifth;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MyApplication extends Application{
	public static Context context;
	public static SQLiteDatabase db;
	private static final int dbVersion = 1;
	@Override
	public void onCreate(){
		context = getApplicationContext();
		DbHelper dbHelper = new DbHelper(context, context.getString(R.string.database_name), null, dbVersion);
		db = dbHelper.getWritableDatabase();
	}
}
