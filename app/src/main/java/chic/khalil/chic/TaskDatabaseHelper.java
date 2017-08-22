package chic.khalil.chic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Khalil on 11/04/17.
 */
public class TaskDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Task.db";
    public static final String TABLE_NAME = "task_table";
    public static final String COL_1 = "_id";
    public static final String COL_2 = "EMAIL";
    public static final String COL_3 = "CHILD";
    public static final String COL_4 = "PLAN";
    public static final String COL_5 = "TASK";
    public static final String COL_6 = "START_TIME";
    public static final String COL_7 = "END_TIME";
    public static final String COL_8 = "IMAGE_PATH";

    public static final String IMAGE_NAME = "image_table";

    public static final String T = " TEXT,";

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" + COL_1 + " TEXT PRIMARY KEY," + COL_2 + T + COL_3 + T + COL_4 + T + COL_5 + T + COL_6 + T + COL_7 + T + COL_8 + " TEXT)");
        db.execSQL("create table " + IMAGE_NAME + " (" + COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_8 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String email, String child, String plan, String task, String start, String end, String image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, email + "_" + child + "_" + plan + "_" + task);
        contentValues.put(COL_2, email);
        contentValues.put(COL_3, child);
        contentValues.put(COL_4, plan);
        contentValues.put(COL_5, task);
        contentValues.put(COL_6, start);
        contentValues.put(COL_7, end);
        contentValues.put(COL_8, image);
        ContentValues imageValues = new ContentValues();
        imageValues.put(COL_8, image);
        db.insert(IMAGE_NAME, null, imageValues);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor query(String email, String child, String plan){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where " + COL_2 + " = \"" + email + "\" and " + COL_3 + " = \"" + child + "\" and " + COL_4 + " = \"" + plan + "\" order by \"" + COL_6 + "\" ASC", null);
        return res;
    }

    public Cursor query(String email, String child, String plan, String task){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where " + COL_2 + " = \"" + email + "\" and " + COL_3 + " = \"" + child + "\" and " + COL_4 + " = \"" + plan + "\" and " + COL_5 + " = \"" + task + "\"", null);
        return res;
    }

    public void delete(String email, String child, String plan, String task){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME + " where " + COL_2 + " = \"" + email + "\" and " + COL_3 + " = \"" + child + "\" and " + COL_4 + " = \"" + plan + "\" and " + COL_5 + " = \"" + task + "\"");
    }

    public void delete(String email, String child, String plan){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME + " where " + COL_2 + " = \"" + email + "\" and " + COL_3 + " = \"" + child + "\" and " + COL_4 + " = \"" + plan + "\"");
    }


}
