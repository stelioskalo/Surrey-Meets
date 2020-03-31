package sep_group_7.SurreyMeets;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

/**
 * @author Riz
 *
 * LoginActivity.java
 */
public class LoginActivity extends AppCompatActivity {

    //email field
    private EditText m_email = null;
    //password field
    private EditText m_pass = null;
    //login button
    private Button m_login = null;
    //sign up text
    private Button m_signup = null;
    // firebase auth
    private FirebaseAuth m_auth = null;
    // loading dialog
    private ProgressDialog m_dialog = null;
    // current user logged in
    private FirebaseUser m_user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //inflate the widgets
        m_email = (EditText) findViewById(R.id.email_edit_login);
        m_pass = (EditText) findViewById(R.id.password_edit_login);
        m_login = (Button) findViewById(R.id.btn_login);
        m_signup = (Button) findViewById(R.id.btn_register);

        // get instance of firebase auth
        m_auth = FirebaseAuth.getInstance();
        // get current logged in user
        m_user = m_auth.getCurrentUser();

        //Necessary permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
        }

        //check if user is not logged in already
        if (m_user == null) {
            // attempt to sign in when login button is clicked
            m_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check to see if fields are not empty
                    if(getEmail().isEmpty() || getPassword().isEmpty()){
                        //display toast if they are
                        Toasty.warning(LoginActivity.this, "Some field(s) are empty!", Toast.LENGTH_SHORT).show();
                    }else{
                        //check if user is already logged in
                        if(m_user != null){
                            //if so, redirect to home
                            finish();
                            Intent i = new Intent(LoginActivity.this, MenuContainer.class);
                            startActivity(i);
                        }else {
                            //if not, try to login
                            login();
                        }
                    }
                }
            });

            //on click action for sign up text
            m_signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //go to the register activity
                    finish();
                    Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(i);
                }
            });
        } // else log in the user straight away
        else {
            //if so, redirect to home
            finish();
            Intent i = new Intent(LoginActivity.this, MenuContainer.class);
            startActivity(i);
        }
    }

    /**
     * Method to login a user
     */
    private void login(){
        //display dialog
        m_dialog = new ProgressDialog(LoginActivity.this);
        m_dialog.setMessage("Logging in");
        m_dialog.show();
        // log into the app with the credentials provided
        m_auth.signInWithEmailAndPassword(getEmail(), getPassword()).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            //display error, if any
                            m_dialog.dismiss();
                            Toasty.warning(LoginActivity.this, "A problem occurred!", Toast.LENGTH_SHORT).show();
                        }else{
                            // if successful, go to home activity
                            m_dialog.dismiss();
                            Toasty.success(LoginActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                            finish();
                            Intent i = new Intent(LoginActivity.this, MenuContainer.class);
                            // send email to activity for display purpose
                            i.putExtra("email", getEmail());
                            startActivity(i);
                        }
                    }
                });
    }

    /**
     * Method to get the email entered
     * @return: email
     */
    private String getEmail(){
        return m_email.getText().toString();
    }

    /**
     * Method to get the password entered
     * @return: password
     */
    private String getPassword(){
        return m_pass.getText().toString();
    }
}
