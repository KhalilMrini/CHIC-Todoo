package chic.khalil.chic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Khalil on 11/04/17.
 */
public class ChildrenDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Children.db";
    public static final String TABLE_NAME = "child_table";
    public static final String COL_0 = "_id";
    public static final String COL_1 = "PARENT";
    public static final String COL_2 = "CHILD_NAME";

    public static final String T = " TEXT,";

    public ChildrenDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COL_0 + " TEXT PRIMARY KEY, " + COL_1 + T + COL_2 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String parentId, String childName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_0, parentId + "_" + childName);
        contentValues.put(COL_1, parentId);
        contentValues.put(COL_2, childName);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1){
            return false;
        } else {
            return true;
        }
    }

    public Cursor query(String parentId){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where " + COL_1 + " = \"" + parentId + "\"", null);
        return res;
    }

    public Cursor queryByChildName(String childName){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where " + COL_2 + " = \"" + childName + "\"", null);
        return res;
    }
}
