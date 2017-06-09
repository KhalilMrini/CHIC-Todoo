package chic.khalil.chic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Khalil on 11/04/17.
 */
public class PlanDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Planning.db";
    public static final String TABLE_NAME = "plan_table";
    public static final String COL_0 = "_id";
    public static final String COL_1 = "EMAIL";
    public static final String COL_2 = "CHILD";
    public static final String COL_3 = "PLAN";

    public static final String T = " TEXT,";

    public PlanDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" + COL_0 + " TEXT PRIMARY KEY, " + COL_1 + T + COL_2 + T + COL_3 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String email, String child, String planning){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_0, email + "_" + child +"_"+ planning);
        contentValues.put(COL_1, email);
        contentValues.put(COL_2, child);
        contentValues.put(COL_3, planning);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1){
            return false;
        } else {
            return true;
        }
    }

    public Cursor query(String email, String child){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where " + COL_1 + " = \"" + email + "\" and " + COL_2 + " = \"" + child + "\"", null);
        return res;
    }
}
