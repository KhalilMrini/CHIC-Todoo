package chic.khalil.chic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Khalil on 11/04/17.
 */
public class UserDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "User.db";
    public static final String TABLE_NAME = "user_table";
    public static final String COL_1 = "EMAIL";
    public static final String COL_2 = "PASSWORD";
    public static final String COL_3 = "VISUAL_COUNTDOWN";
    public static final String COL_4 = "NUMERIC_COUNTDOWN";
    public static final String COL_5 = "ICON_SWITCH";
    public static final String COL_6 = "LOGGED_IN";

    public static final String T = " TEXT, ";
    public static final String I = " INTEGER, ";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" + COL_1 + " TEXT PRIMARY KEY," + COL_2 + T + COL_3 + I + COL_4 + I + COL_5 + I + COL_6 + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, username);
        contentValues.put(COL_2, password);
        contentValues.put(COL_3, 1);
        contentValues.put(COL_4, 1);
        contentValues.put(COL_5, 1);
        contentValues.put(COL_6, 1);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1){
            return false;
        } else {
            return true;
        }
    }

    public Cursor query(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where " + COL_1 + " = \"" + username + "\"", null);
        return res;
    }

    public boolean login(String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select " + COL_2 + " from " + TABLE_NAME + " where " + COL_1 + " = \"" + username +"\"", null);
        if (res.getCount() != 1){
            return false;
        }
        res.moveToFirst();
        String thisPassword = res.getString(0);
        if (thisPassword.equals(password)){
            updateSettings(username, COL_6, true);
            return true;
        }
        return false;
    }

    public void updateSettings(String email, String column, boolean isChecked){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(column, isChecked ? 1 : 0);
        db.update(TABLE_NAME, cv, COL_1+"=\""+email+"\"", null);
    }

    public Cursor queryOnline(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where " + COL_6 + " = \"1\"", null);
        return res;
    }
}
