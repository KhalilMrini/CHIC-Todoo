package chic.khalil.chic;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Khalil on 11/05/17.
 */
public abstract class DrawerActivity extends IntentActivity implements NavigationView.OnNavigationItemSelectedListener, ItemArrayAdapter.ItemArrayAdapterInterface {


    final static int IMAGE_SIZE = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ItemArrayAdapter menuAdapter = new ItemArrayAdapter(this, new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.menu_items))), false);
        menuAdapter.setCallback(this);
        ListView menuItemListView =  (ListView) navigationView.findViewById(R.id.menuItemList);
        menuItemListView.setAdapter(menuAdapter);

        TextView logOut = (TextView) findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DrawerActivity.this, LoginActivity.class);
                UserDatabaseHelper userDb = new UserDatabaseHelper(DrawerActivity.this);
                userDb.updateSettings(email, userDb.COL_6, false);
                startActivity(i);
                DrawerActivity.this.finish();
            }
        });

        ImageButton menuButton = (ImageButton) findViewById(R.id.btn_menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(Gravity.RIGHT)) {
                    drawer.closeDrawer(Gravity.RIGHT);
                } else {
                    drawer.openDrawer(Gravity.RIGHT);
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void wasClicked(String text, boolean center, int position) {
        if (center){
            Intent i = new Intent(this, ActivitiesActivity.class);
            i.putExtra("email", email);
            i.putExtra("child", child);
            i.putExtra("plan", getResources().getString(R.string.init_day) + text);
            i.putExtra("page", getResources().getString(R.string.page_act));
            startActivity(i);
        } else {
            if (position == 0) {
                Intent i = new Intent(this, ActivitiesActivity.class);
                i.putExtra("email", email);
                i.putExtra("child", child);
                i.putExtra("page", getResources().getString(R.string.page_plan));
                startActivity(i);
            } else if (position == 2) {
                Intent i = new Intent(this, ActivitiesActivity.class);
                i.putExtra("email", email);
                i.putExtra("child", child);
                i.putExtra("page", getResources().getString(R.string.page_child));
                startActivity(i);
            } else if (position == 3) {
                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtra("email", email);
                i.putExtra("child", child);
                startActivity(i);
            }
        }
        this.finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
