package sep_group_7.SurreyMeets;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

/**
 * @author Riz
 *
 * TempHomeActivity.java
 *
 * Replace with main screen of app when logged in.
 */
public class TempHomeActivity extends AppCompatActivity {

    // email text view
    private TextView m_text = null;
    // sign out button
    private Button m_btn = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_home);

        //inflate the widgets
        m_text = (TextView) findViewById(R.id.text_home);
        m_btn = (Button) findViewById(R.id.sign_out_btn);

        //get the intent that called the activity
        Intent i = getIntent();

        //get email and save as welcome text
        String welcomeText = "Welcome, " + i.getStringExtra("email");

        //set text to display email
        m_text.setText(welcomeText);

        //on click action for sign out
        m_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sign out user and redirect
                FirebaseAuth a = FirebaseAuth.getInstance();
                a.signOut();
                Toasty.info(TempHomeActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                finish();
                Intent i = new Intent(TempHomeActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}
