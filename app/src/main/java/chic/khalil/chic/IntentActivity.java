package chic.khalil.chic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Khalil on 19/07/17.
 */
public abstract class IntentActivity extends AppCompatActivity {

    String email;
    String child;
    int layout;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath(getResources().getString(R.string.font))
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        intent = getIntent();
        email = intent.getStringExtra("email");
        child = intent.getStringExtra("child");
    }
}
