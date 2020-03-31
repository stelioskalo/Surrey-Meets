package sep_group_7.SurreyMeets;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * @author Riz
 * <p>
 * Note to members: this activity will be replaced by the splash screen. Link the splash screen to the
 * LoginActivity and all is good.
 */
public class MainActivity extends AppCompatActivity {

    // login button
    private Button m_loginBtn = null;
    //register button
    private Button m_registerBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(sep_group_7.SurreyMeets.R.layout.activity_main);

        //Necessary permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
        }

        //inflate the widgets
        m_loginBtn = (Button) findViewById(R.id.login_btn);
        m_registerBtn = (Button) findViewById(R.id.register_btn);

        //set on click listener for login button
        m_loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when button pressed, go to login activity
                finish();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        //set on click listener for register button
        m_registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when button pressed, go to register activity
                finish();
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

}
