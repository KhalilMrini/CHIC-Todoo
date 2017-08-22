package chic.khalil.chic;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Khalil on 19/07/17.
 */
public class SpinnerArrayAdapter extends ArrayAdapter<String> {

    private Context context;

    public SpinnerArrayAdapter(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        overrideFonts(context, view);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        overrideFonts(context, view);
        return view;
    }

    private void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                System.out.println("We have " + vg.getChildCount() + " children views here.");
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextView) {
                System.out.println("One change! for " + ((TextView) v).getText().toString());
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Bold.ttf"));
            }
        } catch (Exception e) {
        }
    }


}
