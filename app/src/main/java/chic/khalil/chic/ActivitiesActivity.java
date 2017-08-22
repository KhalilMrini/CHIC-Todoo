package chic.khalil.chic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ActivitiesActivity extends IntentActivity implements ItemCursorAdapter.ItemCursorAdapterInterface {

    String page;
    String plan;
    boolean isWeekDayPlan;

    Typeface type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        plan = getIntent().getStringExtra("plan");
        isWeekDayPlan = plan != null && plan.startsWith(getResources().getString(R.string.init_day));
        layout = (isWeekDayPlan && getIntent().getStringExtra("page").equals(getResources().getString(R.string.page_act))) ?
                R.layout.activity_day_tasks : R.layout.activity_activities;

        super.onCreate(savedInstanceState);

        type = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf");
        page = intent.getStringExtra("page");

        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final ListView modularListView = (ListView) findViewById(R.id.modularListView);

        Cursor cursor = null;
        String columnName = "";

        if (isActivities()){
            plan = intent.getStringExtra("plan");
            TextView title = (TextView) findViewById(R.id.custom_title);
            title.setText(plan.replace(getResources().getString(R.string.init_day),""));
            title.setTypeface(type);
            TaskDatabaseHelper taskDb = new TaskDatabaseHelper(this);
            cursor = taskDb.query(email, child, plan);
            TaskCursorAdapter adapter = new TaskCursorAdapter(this, cursor);
            modularListView.setAdapter(adapter);
            modularListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView activityView = (TextView) view.findViewById(R.id.activityName);
                    Intent i = new Intent(ActivitiesActivity.this, AddTaskActivity.class);
                    i.putExtra("plan", plan);
                    i.putExtra("task", activityView.getText().toString());
                    start(i);
                }
            });
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ActivitiesActivity.this, AddTaskActivity.class);
                    i.putExtra("plan", plan);
                    i.putExtra("task", "");
                    start(i);
                }
            });
            if (isWeekDayPlan){
                Button savePlan = (Button) findViewById(R.id.savePlan);
                savePlan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ActivitiesActivity.this, AddItemActivity.class);
                        i.putExtra("plan", plan);
                        start(i);
                    }
                });
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                PlanDatabaseHelper planDb = new PlanDatabaseHelper(this);
                Cursor planCursor = planDb.query(email, child);

                ArrayList<String> mArrayList = new ArrayList<>();
                mArrayList.add(cursor.getCount() > 0 ? getResources().getString(R.string.spinner_header_non_empty_list) : getResources().getString(R.string.spinner_header_empty_list));
                for(planCursor.moveToFirst(); !planCursor.isAfterLast(); planCursor.moveToNext()) {
                    mArrayList.add(planCursor.getString(planCursor.getColumnIndex(planDb.COL_3)));
                }
                planDb.close();
                SpinnerArrayAdapter spinnerAdapter = new SpinnerArrayAdapter(this, R.layout.simple_spinner_item, mArrayList);
                spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                spinner.setDropDownHorizontalOffset(0);
                spinner.setDropDownVerticalOffset(40);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String text = ((TextView) view).getText().toString();
                        if (!text.equals(getResources().getString(R.string.spinner_header_non_empty_list)) && !text.equals(getResources().getString(R.string.spinner_header_empty_list))) {
                            TaskDatabaseHelper taskDb = new TaskDatabaseHelper(ActivitiesActivity.this);
                            taskDb.delete(email, child, plan);
                            Cursor cursor = taskDb.query(email, child, text);
                            while (cursor.moveToNext()) {
                                taskDb.insertData(email, child, plan,
                                        cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_5)),
                                        cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_6)),
                                        cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_7)),
                                        cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_8)));
                            }
                            cursor = taskDb.query(email, child, plan);
                            TaskCursorAdapter adapter = new TaskCursorAdapter(ActivitiesActivity.this, cursor);
                            modularListView.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        } else if (isMyDayPlans()){
            PlanDatabaseHelper planDb = new PlanDatabaseHelper(this);
            cursor = planDb.query(email, child);
            columnName = planDb.COL_3;
            planDb.close();
        } else if (isMyChildren()){
            ChildrenDatabaseHelper childDb = new ChildrenDatabaseHelper(this);
            cursor = childDb.query(email);
            columnName = childDb.COL_2;
        }

        if (isMyChildren() || isMyDayPlans()){
            ItemCursorAdapter adapter = new ItemCursorAdapter(this, cursor, columnName);
            adapter.setCallback(this);
            modularListView.setAdapter(adapter);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(ActivitiesActivity.this, AddItemActivity.class);
                    if (isWeekDayPlan){
                        i.putExtra("plan", plan);
                    }
                    start(i);
                }
            });
        }
        overrideFonts(this, findViewById(R.id.largerLayout));
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
            } else if (v instanceof TextView ) {
                System.out.println("One change! for " + ((TextView) v).getText().toString());
                ((TextView) v).setTypeface(type);
            }
        } catch (Exception e) {
        }
    }

    public boolean isActivities(){
        return page.equals(getResources().getString(R.string.page_act));
    }

    public boolean isMyChildren(){
        return page.equals(getResources().getString(R.string.page_child));
    }

    public boolean isMyDayPlans(){
        return page.equals(getResources().getString(R.string.page_plan));
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ActivitiesActivity.this, MenuActivity.class);
        start(i);
    }

    @Override
    public void wasClicked(String text) {
        Intent i = new Intent(ActivitiesActivity.this, isMyChildren() ? MenuActivity.class : ActivitiesActivity.class);
        if (isMyDayPlans()) {
            i.putExtra("plan", isWeekDayPlan ? plan : text);
            page = getResources().getString(R.string.page_act);
        } else {
            child = text;
        }
        start(i);
    }

    public void start(Intent i){
        i.putExtra("email", email);
        i.putExtra("child", child);
        i.putExtra("page", page);
        startActivity(i);
        ActivitiesActivity.this.finish();
    }
}
