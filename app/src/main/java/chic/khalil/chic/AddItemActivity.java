package chic.khalil.chic;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddItemActivity extends IntentActivity {

    String page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.activity_add_item;
        super.onCreate(savedInstanceState);
        page = intent.getStringExtra("page");

        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveBack();
            }
        });

        Button saveButton = (Button) findViewById(R.id.button);

        if (isMyChildren()){
            TextView title = (TextView) findViewById(R.id.textViewPlan);
            EditText editText = (EditText) findViewById(R.id.editTextPlan);
            title.setText("Add Child");
            editText.setHint("Child Name");

            final ChildrenDatabaseHelper childDb = new ChildrenDatabaseHelper(this);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText editName = (EditText) findViewById(R.id.editTextPlan);
                    String name = editName.getText().toString();
                    boolean inserted = childDb.insertData(email, name);
                    if (inserted) {
                        moveBack();
                    } else {
                        editName.setError("Child name already exists!");
                    }
                }
            });
        } else if (isMyDayPlans() || isActivities()){
            final PlanDatabaseHelper planDb = new PlanDatabaseHelper(this);

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText editName = (EditText) findViewById(R.id.editTextPlan);
                    String name = editName.getText().toString();
                    boolean inserted = planDb.insertData(email, child, name);
                    if (inserted) {
                        if (isActivities()){
                            TaskDatabaseHelper taskDb = new TaskDatabaseHelper(AddItemActivity.this);
                            String plan = getIntent().getStringExtra("plan");
                            Cursor cursor = taskDb.query(email, child, plan);
                            while (cursor.moveToNext()){
                                taskDb.insertData(email, child, name,
                                        cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_5)),
                                        cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_6)),
                                        cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_7)),
                                        cursor.getString(cursor.getColumnIndexOrThrow(taskDb.COL_8)));
                            }
                            Intent i = new Intent(AddItemActivity.this, ActivitiesActivity.class);
                            i.putExtra("email", email);
                            i.putExtra("child", child);
                            i.putExtra("page", page);
                            i.putExtra("plan", plan);
                            startActivity(i);
                            AddItemActivity.this.finish();
                        } else {
                            moveBack();
                        }
                    } else {
                        editName.setError("Day Plan already exists for this "+ child + "!");
                    }
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed(){
        moveBack();
    }

    public void moveBack(){
        Intent i = new Intent(AddItemActivity.this, ActivitiesActivity.class);
        i.putExtra("email", email);
        i.putExtra("child", child);
        i.putExtra("page", page);
        startActivity(i);
        finish();
    }
}
