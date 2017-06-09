package chic.khalil.chic;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.regex.Pattern;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddTaskActivity extends AppCompatActivity {

    private static final int SELECTED_PICTURE = 1;

    String email;
    String child;
    String plan;
    String page;
    String task;

    EditText editName;
    EditText editStartTime;
    EditText editEndTime;
    ImageView imageView;

    TaskDatabaseHelper myDb;
    String filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath(getResources().getString(R.string.font))
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        child = intent.getStringExtra("child");
        plan = intent.getStringExtra("plan");
        page = intent.getStringExtra("page");
        task = intent.getStringExtra("task");

        editName = (EditText) findViewById(R.id.editname);
        editStartTime = (EditText) findViewById(R.id.editstarttime);
        editEndTime = (EditText) findViewById(R.id.editendtime);
        imageView = (ImageView) findViewById(R.id.activityImage);

        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveBack();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.putExtra("email", email);
                i.putExtra("child", child);
                i.putExtra("plan", plan);
                startActivityForResult(i, SELECTED_PICTURE);
            }
        });

        myDb = new TaskDatabaseHelper(this);

        Button deleteButton = (Button) findViewById(R.id.delete);
        Button saveButton = (Button) findViewById(R.id.save);

        Cursor cursor = null;

        if (task.length() > 0 && (cursor = myDb.query(email, child, plan, task)).getCount() > 0){
            cursor.moveToFirst();
            editName.setText(cursor.getString(cursor.getColumnIndexOrThrow(myDb.COL_5)));
            editStartTime.setText(cursor.getString(cursor.getColumnIndexOrThrow(myDb.COL_6)));
            editEndTime.setText(cursor.getString(cursor.getColumnIndexOrThrow(myDb.COL_7)));
            filePath = cursor.getString(cursor.getColumnIndexOrThrow(myDb.COL_8));
            Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
            if (selectedImage != null) {
                imageView.setImageBitmap(selectedImage);
            }
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDb.delete(email, child, plan, task);
                    moveBack();
                }
            });
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myDb.delete(email, child, plan, task);
                    tryToInsert();
                }
            });
        } else {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveBack();
                }
            });
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tryToInsert();
                }
            });
        }

    }

    public boolean isHoursAndMinutes(String time){
        String timePattern = "\\d?\\d:\\d\\d";
        return Pattern.matches(timePattern, time) && Integer.parseInt(time.split(":")[0]) < 24 && Integer.parseInt(time.split(":")[1]) < 60;
    }

    public void tryToInsert(){
        String name = editName.getText().toString();
        String startTime = editStartTime.getText().toString();
        String endTime = editEndTime.getText().toString();
        boolean start = isHoursAndMinutes(startTime);
        boolean end = isHoursAndMinutes(endTime);
        if (!start){
            editStartTime.setError("Respect the time format!");
        }
        if (!end){
            editEndTime.setError("Respect the time format!");
        }
        if (start && end){
            boolean inserted = myDb.insertData(email, child, plan, name, startTime, endTime, filePath);
            if (inserted){
                moveBack();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case SELECTED_PICTURE:
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    filePath = cursor.getString(columnIndex);
                    cursor.close();
                    Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    imageView.setImageBitmap(selectedImage);
                }
                break;
            default:
                break;
        }
    }

    public void moveBack(){
        Intent i = new Intent(AddTaskActivity.this,ActivitiesActivity.class);
        i.putExtra("email", email);
        i.putExtra("child", child);
        i.putExtra("plan", plan);
        i.putExtra("page", page);
        startActivity(i);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed(){
        moveBack();
    }
}
