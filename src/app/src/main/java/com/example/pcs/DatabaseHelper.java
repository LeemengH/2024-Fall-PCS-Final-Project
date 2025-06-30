package com.example.pcs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String databaseName = "SignLog.db";
    private static final int DataBaseVersion = 1;

    private static final String TABLE_NAME = "smsUser";   //change name
    private static final String SM_TABLE = "smKeeper";
    public DatabaseHelper(@Nullable Context context) {
        super(context, databaseName, null, DataBaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {
        //MyDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        //MyDatabase.execSQL("DROP TABLE IF EXISTS "+SM_TABLE);
        String SQLTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + "username TEXT primary key, password TEXT, role TEXT, phone TEXT, provider TEXT, quota INTEGER, task INTEGER" + ");";
        MyDatabase.execSQL(SQLTable);

        // initial test
        //this.insertData("111", "2222", "provider", "090061234", 0);
        //this.insertData("222", "2222", "provider", "090001234", 1);
        //this.insertData("333", "2222", "provider", "090101234", 2);

        String SMTable = "CREATE TABLE IF NOT EXISTS " + SM_TABLE + "(" + "ID INTEGER PRIMARY KEY   AUTOINCREMENT,phone TEXT, message TEXT" + ");";
        MyDatabase.execSQL(SMTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int i, int i1) {
        MyDB.execSQL("drop Table if exists users");
    }

    public void initializeTestData() {
        insertData("111", "2222", "provider", "090061234", 0);
        insertData("222", "2222", "provider", "090001234", 1);
        insertData("333", "2222", "provider", "090101234", 2);
    }


    //TODO: 新增role, phone, quota
    public void insertData(String username, String passwd, String role, String phone, int quota){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", passwd);
        values.put("role", role);
        values.put("phone", phone);
        values.put("task", 0);

        if(role.equals("shop")){
            values.put("provider", ""); //TODO: 如果有shop 要拿掉
            values.put("quota", quota);
        }
        else{
            values.put("provider", checkProvider(phone)); //TODO: 如果有shop 要拿掉
            values.put("quota", quota);
        }

        MyDB.insert(TABLE_NAME, null, values);
        Log.d("Database", "insert");
        // MyDB.close();
    }

    public long saveSM(String phone, String message){
        // save the short message and return the message id
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phone", phone);
        values.put("message", message);
        return MyDB.insert(SM_TABLE, null, values);
    }

    public Boolean checkUsername(String username){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        //Cursor cursor = MyDatabase.rawQuery("Select * from users where username = ?", new String[]{username});
        Cursor cursor = MyDatabase.rawQuery("Select * from smsUser where username = ?", new String[]{username});
        if(cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }
    public Boolean checkUsernamePassword(String username, String password){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from smsUser where username = ? and password = ?", new String[]{username, password});
        if (cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }

    // TODO: 確認是shop or provider
    public String checkUsage(String username){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from smsUser where username = ?", new String[]{username});
        String role = null;
        if (cursor.moveToFirst()) { // 檢查是否有資料
            role = cursor.getString(2); // 獲取 "role" 欄位的值
        }
        cursor.close();
        return role; // 回傳 "provider" 或 "shop"，若找不到則回傳 null
    }

    public static String checkProvider(String phoneNumber){
        switch(phoneNumber.substring(0, 5)){
            case "09006":
                return "CHT";
            case "09000":
                return "FET";
            case "09010":
                return "TWM";
            default:
                Log.d("Database", phoneNumber+ " CHTTT");
                return "CHT";
        }
    }

    public  boolean findCandidate(String phone, String msg){
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("Database", "findCandidate");
        Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_NAME + " WHERE provider = ?", new String[] {checkProvider(phone)});
        c.moveToFirst();
        if (c.getCount() > 0){
            try {
                long id = this.saveSM(phone, msg);
                ContentValues contentValues = new ContentValues();
                contentValues.put("task", id);
                db.update(TABLE_NAME, contentValues, "username = ?", new String[]{c.getString(0)});
                return true;

            }catch (Exception e) {
                Log.d("Database", e.toString());
                return false;
            }
        }
        return false;

    }

    public int getQuota(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT quota FROM "+ TABLE_NAME + " WHERE username = ?", new String[] {id});
        c.moveToFirst();
        if (c.getCount() > 0){
            return c.getInt(0);
        }return 0;

    }

    public ArrayList<Integer> getTaskQuota(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor task = db.rawQuery("SELECT task, quota FROM "+ TABLE_NAME + " WHERE username = ?", new String[] {id});
        task.moveToFirst();

        ArrayList<Integer> return_list = new ArrayList<>();
        return_list.add(task.getInt(0));
        return_list.add(task.getInt(1));
        return return_list;
    }

    public ArrayList<String> getPhoneMessage(long sm_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor smInfo = db.rawQuery("SELECT * FROM "+ SM_TABLE + " WHERE ID = ?", new String[] {sm_id+""});
        smInfo.moveToFirst();

        String number = smInfo.getString(1);
        String msg = smInfo.getString(2);

        ArrayList<String> return_list = new ArrayList<>();
        return_list.add(number);
        return_list.add(msg);
        return return_list;
    }

    public void updateContentValues(String user_id, int quota){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("task", 0);
        contentValues.put("quota", quota-1);
        db.update(TABLE_NAME, contentValues, "username = ?", new String[]{user_id});
        return;
    }


    
}

