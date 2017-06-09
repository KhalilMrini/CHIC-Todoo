package chic.khalil.chic;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Khalil on 25/04/17.
 */
public class ItemArrayAdapter extends ArrayAdapter<String> {

    private ItemArrayAdapterInterface callback;

    boolean center;

    Typeface face;

    public ItemArrayAdapter(Context context, ArrayList<String> strings, boolean center){
        super(context, 0, strings);
        this.center = center;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final String text = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        int layoutId = center ? R.layout.day_item : R.layout.menu_item;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null){
                    callback.wasClicked(text, center, position);
                }
            }
        };
        // Lookup view for data population
        TextView itemView = (TextView) convertView.findViewById(R.id.itemName);
        // Populate the data into the template view using the data object
        itemView.setText(text);
        itemView.setOnClickListener(listener);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.itemImage);
        imageView.setOnClickListener(listener);

        if (center){
            RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.dayLayout);
            layout.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.custom_shape));
        }
        // Return the completed view to render on screen
        return convertView;
    }

    public void setCallback(ItemArrayAdapterInterface callback){
        this.callback = callback;
    }

    public interface ItemArrayAdapterInterface{

        void wasClicked(String text, boolean center, int position);

    }
}
