package chic.khalil.chic;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Khalil on 25/04/17.
 */
public class ItemCursorAdapter extends CursorAdapter{

    private ItemCursorAdapterInterface callback;

    String columnName;

    public ItemCursorAdapter(Context context, Cursor cursor, String columnName){
        super(context, cursor, 0);
        this.columnName = columnName;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.day_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final String text = cursor.getString(cursor.getColumnIndexOrThrow(columnName));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null){
                    callback.wasClicked(text);
                }
            }
        };

        TextView textView = (TextView) view.findViewById(R.id.itemName);
        textView.setText(text);
        textView.setOnClickListener(listener);

        ImageView imageView = (ImageView) view.findViewById(R.id.itemImage);
        imageView.setOnClickListener(listener);
    }

    public void setCallback(ItemCursorAdapterInterface callback){
        this.callback = callback;
    }

    public interface ItemCursorAdapterInterface {

        void wasClicked(String text);

    }
}
