package chic.khalil.chic;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Khalil on 25/04/17.
 */
public class TaskCursorAdapter extends CursorAdapter{

    private Context context;

    public TaskCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listview_activity, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView activityView = (TextView) view.findViewById(R.id.activityName);
        TextView startView = (TextView) view.findViewById(R.id.startingTime);
        TextView endView = (TextView) view.findViewById(R.id.endTime);

        Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Bold.ttf");
        activityView.setTypeface(type);
        startView.setTypeface(type);
        endView.setTypeface(type);

        String activityName = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_5));
        String startTime = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_6));
        String endTime = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_7));

        activityView.setText(activityName);
        startView.setText(startTime);
        endView.setText(endTime);

        ImageView imageView = (ImageView) view.findViewById(R.id.activityImage);
        Bitmap image = BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_8)));
        if (image != null){
            imageView.setImageBitmap(image);
        }
    }


}
