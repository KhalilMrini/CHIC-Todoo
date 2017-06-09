package chic.khalil.chic;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class ActivitiesActivity extends DrawerActivity implements ItemCursorAdapter.ItemCursorAdapterInterface {

    String page;
    String plan;
    boolean isWeekDayPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        plan = getIntent().getStringExtra("plan");
        isWeekDayPlan = plan != null && plan.startsWith(getResources().getString(R.string.init_day));
        layout = (isWeekDayPlan && getIntent().getStringExtra("page").equals(getResources().getString(R.string.page_act))) ?
                R.layout.activity_day_tasks : R.layout.activity_activities;
        super.onCreate(savedInstanceState);
        page = intent.getStringExtra("page");

        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        ListView modularListView = (ListView) findViewById(R.id.modularListView);

        Cursor cursor = null;
        String columnName = "";

        if (isActivities()){
            plan = intent.getStringExtra("plan");
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
                Button chosePlan = (Button) findViewById(R.id.chosePlan);
                chosePlan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ActivitiesActivity.this, ActivitiesActivity.class);
                        page = getResources().getString(R.string.page_plan);
                        i.putExtra("plan", plan);
                        start(i);
                    }
                });
            }
        } else if (isMyDayPlans()){
            PlanDatabaseHelper planDb = new PlanDatabaseHelper(this);
            cursor = planDb.query(email, child);
            columnName = planDb.COL_3;
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
            if (isWeekDayPlan){
                TaskDatabaseHelper taskDb = new TaskDatabaseHelper(this);
                taskDb.delete(email, child, plan);
                Cursor cursor = taskDb.query(email, child, text);
                while (cursor.moveToNext()){
                    taskDb.insertData(email, child, plan,
                            cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_5)),
                            cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_6)),
                            cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_7)),
                            cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_8)));
                }
            }
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
