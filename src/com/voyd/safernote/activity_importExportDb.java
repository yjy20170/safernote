package com.voyd.safernote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import com.voyd.safernote.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class activity_importExportDb extends SafeActivity implements OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_import_export_db);
        ((Button) findViewById(R.id.finish)).setOnClickListener(this);
        ((Button) findViewById(R.id.export_db)).setOnClickListener(this);
        ((Button) findViewById(R.id.import_db)).setOnClickListener(this);
        ((Button) findViewById(R.id.export_db)).setText(
                "将数据导出至 /" + getString(R.string.app_name)
                + "/" + getString(R.string.database_name));
        //titlebar设置
        ((TextView) findViewById(R.id.titlebarText)).setText("导入与导出");
        findViewById(R.id.save).setVisibility(View.INVISIBLE);
    }
    @Override
    public void onClick(View v){
        switch(v.getId()){
        case R.id.finish:
            onBackPressed();
            break;
        case R.id.import_db:
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");//设置类型
            intent.addCategory(Intent.CATEGORY_OPENABLE);  
            //由于在文件选择器界面进入后台时不能上锁，因此将文件选择器视为不安全 startActivityForResult
            //体验不好，改回
            startSafeActivityForResult(intent,1);
            break;
        case R.id.export_db:
            //若不存在，创建新文件夹
            File direct = new File(Environment.getExternalStorageDirectory() + "/"+getString(R.string.app_name));
            if(!direct.exists())
            {
                if(direct.mkdir()) 
                {
                    //directory is created
                }
            }
            exportDB();
            break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        //需执行SfaActivity的onActivityResult，以设置isFromStack
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                //首先importDB()检验文件是否符合规范，
                //是则看能否得到MD5Password(此处同MyApplication
                String MD5Password="";
                DbHelper dbHelper;
                SQLiteDatabase db;
                File toDb;
                toDb = importDB(uri.getPath().toString(),"temp.db");//getString(R.string.database_name)
                if(toDb != null){
                    dbHelper = new DbHelper(MyApp.context, "temp.db", null, MyApp.dbVersion);
                    db = dbHelper.getWritableDatabase();
                    try{
                        Cursor cursor = db.rawQuery("select * from settings", null);
                        cursor.moveToFirst();
                        MD5Password = cursor.getString(cursor.getColumnIndex("md5password"));
                        db.close();
                        dbHelper.close();
                        new alert("数据正常");
                    }catch(Exception e){
                        new alert("数据异常: "+e.toString(),"long");
                        toDb.delete();
                        return;//不继续执行后面的操作
                    }
                }else{
                    return;
                }
                
                //输入密码校验MD5值，若通过，提示是否导出原数据，然后覆盖原数据库
                Intent intent = new Intent(this, activity_testNewDb.class);
                intent.putExtra("MD5Password", MD5Password);
                startSafeActivityForResult(intent, 1);//在返回该activity时强制不lock
                
            }
        }
    }
    public static File importDB(String importFilePath, String dbName){
        try{
            File data  = Environment.getDataDirectory();
            String  currentDBPath= "//data//" + MyApp.context.getString(R.string.package_name)
                    + "//databases//" + dbName;
            File backupDB = new File(importFilePath);
            File currentDB  = new File(data, currentDBPath);
            if(!currentDB.exists()){
                currentDB.createNewFile();
            }
            FileInputStream inputStream = new FileInputStream(backupDB);
            FileOutputStream outputStream = new FileOutputStream(currentDB);
            FileChannel src = inputStream.getChannel();
            FileChannel dst = outputStream.getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            inputStream.close();
            outputStream.close();
            return currentDB;
        }catch(Exception e){
            new alert("数据异常: "+e.toString(),"long");
            return null;
        }
    }

    public static void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String  currentDBPath= "//data//" + MyApp.context.getString(R.string.package_name)
                        + "//databases//" + MyApp.context.getString(R.string.database_name);
                String backupDBPath  = "/" + MyApp.context.getString(R.string.app_name)
                        + "/" + MyApp.context.getString(R.string.database_name);
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                if(!backupDB.exists()){
                    backupDB.createNewFile();
                }
                FileInputStream inputStream = new FileInputStream(currentDB);
                FileOutputStream outputStream = new FileOutputStream(backupDB);
                FileChannel src = inputStream.getChannel();
                FileChannel dst = outputStream.getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                inputStream.close();
                outputStream.close();
                new alert("导出成功");
            }
        } catch (Exception e) {
            new alert(e.toString());
        }
    }

}