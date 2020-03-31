package sep_group_7.SurreyMeets;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;

/**
 * @author Riz
 *
 * RegisterActivity.java
 */
public class RegisterActivity extends AppCompatActivity {

    //name field
    private EditText m_name = null;
    //age field
    private EditText m_age = null;
    //email field
    private EditText m_email = null;
    //password field
    private EditText m_pass = null;
    //confirm password field
    private EditText m_cPass = null;
    //register button
    private Button m_submit = null;
    //login button from register
    private Button m_login = null;
    //regex to check email is only surrey email
    private static final String REGEX = "^[a-zA-Z0-9]*+\\@surrey.ac.uk$";
    // firebase auth
    private FirebaseAuth m_auth = null;
    //loading dialog
    private ProgressDialog m_dialog = null;
    //gdpr checkbox
    private CheckBox m_check = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //inflate the widgets
        m_name = (EditText) findViewById(R.id.name_edit_register);
        m_age = (EditText) findViewById(R.id.age_edit_register);
        m_email = (EditText) findViewById(R.id.email_edit_register);
        m_pass = (EditText) findViewById(R.id.password_edit_register);
        m_cPass = (EditText) findViewById(R.id.c_password_edit_register);
        m_submit = (Button) findViewById(R.id.btn_submit);
        m_login = (Button) findViewById(R.id.btn_login_from_reg);
        m_check = (CheckBox) findViewById(R.id.data_check);

        //get firebase reference
        m_auth = FirebaseAuth.getInstance();

        //set onclick for sign up button
        m_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if fields are empty
                if(getName().isEmpty() || getAge().isEmpty() || getEmail().isEmpty() || getPass().isEmpty() || getCPass().isEmpty()){
                    //if so, display message
                    Toasty.warning(RegisterActivity.this, "Some field(s) are empty!", Toast.LENGTH_SHORT).show();
                } else if (!m_check.isChecked()) {
                    Toasty.warning(RegisterActivity.this, "You need to accept the terms to create an account!", Toast.LENGTH_SHORT).show();
                }
                else {
                    //if fields are present, try to register
                    register();
                }
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //set on click for login from register button
        m_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to login
                finish();
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }

    /**
     * Method to register a new account with firebase
     *
     * @return: true if registered, false otherwise
     */
    private void register(){
        this.m_dialog = new ProgressDialog(RegisterActivity.this);
        this.m_dialog.setMessage("Registering Account");
        this.m_dialog.show();

        // check to see if password meets the firebase requirements of at least 6 characters long
        if(getPass().length() >= 6) {
            // check if the passwords match
            if (matchPass()) {
                //check if the email is valid
                if(validateEmail()) {
                    // if so, register an account
                    m_auth.createUserWithEmailAndPassword(getEmail(), getPass()).
                            addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // check to see if task is successful
                                    if (task.isSuccessful()) {
                                        //dismiss dialog
                                        m_dialog.dismiss();
                                        //add details to firebase
                                        addToFirebase();
                                        //sign out user
                                        m_auth.signOut();
                                        //display message of success
                                        Toasty.success(RegisterActivity.this, "Account Registered!", Toast.LENGTH_SHORT).show();
                                        //redirect to login page
                                        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        //if task fails, stop dialog and display error message
                                        m_dialog.dismiss();
                                        Toasty.error(RegisterActivity.this, "Task failed!" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else {
                    //display error if email does not match regex
                    m_dialog.dismiss();
                    Toasty.warning(RegisterActivity.this, "Email is not valid!", Toast.LENGTH_SHORT).show();
                }
            } else {
                //display error if passwords do not match
                m_dialog.dismiss();
                Toasty.warning(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            }
        }else{
            //display error if password is less than 6 characters long
            m_dialog.dismiss();
            Toasty.warning(RegisterActivity.this, "Password should be at least 6 characters long!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to add user details to firebase
     */
    private void addToFirebase(){
        // get db reference
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        // get current user who registered
        final FirebaseUser fUser = m_auth.getCurrentUser();
        // add the email of user
        reference.child("user").child(fUser.getUid()).child("email").setValue(fUser.getEmail());

        // when the email is added, add the other relevant info
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    // add the name, age, id, default events_joined and default events_hosted to the user
                    reference.child("user").child(fUser.getUid()).child("name").setValue(getName());
                    reference.child("user").child(fUser.getUid()).child("age").setValue(getAge());
                    reference.child("user").child(fUser.getUid()).child("id").setValue(fUser.getUid());
                    reference.child("user").child(fUser.getUid()).child("events_joined").setValue(0);
                    reference.child("user").child(fUser.getUid()).child("events_hosted").setValue(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //display error if any occur
                Toasty.error(RegisterActivity.this, "Error" +  databaseError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Getter method for name
     * @return: name
     */
    private String getName(){ return this.m_name.getText().toString(); }

    /**
     * Getter method for age
     * Note: Return type is string as  integer field is defined in xml so no need to check. This also
     * saves us from creating an extra method to validate presence of age
     * @return: age
     */
    private String getAge(){ return this.m_age.getText().toString(); }

    /**
     * Getter method for email
     * @return: email
     */
    private String getEmail(){ return this.m_email.getText().toString(); }

    /**
     * Getter method for password
     * @return: password
     */
    private String getPass(){ return this.m_pass.getText().toString(); }

    /**
     * Getter method for confirm password
     * @return: confirm password
     */
    private String getCPass(){ return this.m_cPass.getText().toString(); }

    /**
     * Method to check if password fields match
     * @return: true if match, false otherwise
     */
    private boolean matchPass(){
        return this.getPass().equals(this.getCPass());
    }

    /**
     * Method to check if email matches REGEX i.e. only surrey email
     * @return: true if match, false otherwise
     */
    private boolean validateEmail() {
        return this.getEmail().matches(REGEX);
    }

}
