package chic.khalil.chic;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MenuActivity extends DrawerActivity {

    UserDatabaseHelper userDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.activity_menu;
        super.onCreate(savedInstanceState);

        userDb = new UserDatabaseHelper(this);

        ItemArrayAdapter dayAdapter = new ItemArrayAdapter(this, new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.days))), true);
        dayAdapter.setCallback(this);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(dayAdapter);

    }
}
