package sep_group_7.SurreyMeets;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import es.dmoral.toasty.Toasty;

/**
 * Class extending fragment representing the profile page
 */
public class EditProfile extends AppCompatActivity {
    //Default values needed for the permissions
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    //ImageView for User's profile picture.
    ImageView m_ProfilePic;
    //TextView used as a button to add picture.
    TextView m_ChangePic;
    //EditText for changing Name
    EditText m_ChangeName;
    //EditText for changing Age
    EditText m_ChangeAge;
    //EditText for changing Bio
    EditText m_ChangeBio;
    //TextView for when the user is done
    TextView m_DoneButton;
    // File Uri used for retrieving the photo.
    Uri m_FileUri;
    //A bitmap for putting the image together.
    Bitmap m_Map;
    //Fragment transaction for when the user clicks done.
    FragmentTransaction m_Transaction;
    //context of fragment
    private Context m_context = null;


    public EditProfile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Checking for the permissions that are granted.
        checkPermissionsCamera();

        m_context = getApplicationContext();

        //inflate the widgets
        m_ProfilePic = (ImageView) findViewById(R.id.view_profilePic);
        m_ChangeName = (EditText) findViewById(R.id.edit_name);
        m_ChangeAge = (EditText) findViewById(R.id.edit_age);
        m_ChangeBio = (EditText) findViewById(R.id.edit_bio);

        //set the initial values retrieved from firebase
        loadInitialValues();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        m_ChangePic = (TextView) findViewById(R.id.changePic);
        m_ChangePic.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //Options for the dialog that pops up
                final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                builder.setTitle("Select an option:");

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (items[i].equals("Camera")) {

                            // Calling the camera via intent.
                            Intent picIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // Starting Camera.
                            startActivityForResult(picIntent, 0);

                        } else if (items[i].equals("Gallery")) {
                            // Calling the gallery via intent.
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            galleryIntent.setType("image/*");
                            startActivityForResult(galleryIntent, 3);

                        } else if (items[i].equals("Cancel")) {
                            // Nothing happens.
                            dialogInterface.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        m_DoneButton = (TextView) findViewById(R.id.done);
        m_DoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items = {"Save", "Exit without saving", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                builder.setTitle("Select an option:");

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (items[i].equals("Save")) {
                            //check if name and/or age are empty
                            if (m_ChangeName.getText().toString().isEmpty() || m_ChangeAge.getText().toString().isEmpty()) {
                                //display error message
                                Toasty.error(m_context, "Name and Age must be present", Toast.LENGTH_SHORT).show();
                            } else {
                                //if not, then proceed with saving the datas
                                //use a thread to upload the data to firebase
                                ThreadForUpload threadForUpload = new ThreadForUpload();
                                Thread t = new Thread(threadForUpload);
                                t.start();

                                AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                                setContentView(R.layout.menu_container);
                                Profile profileFragment = new Profile();
                                m_Transaction = getFragmentManager().beginTransaction();
                                m_Transaction.replace(R.id.display, profileFragment);
                                m_Transaction.commit();
                                finish();
                            }

                        } else if (items[i].equals("Exit without saving")) {
                            //Nothing is saved and a user is redirected to his profile page
                            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                            setContentView(R.layout.menu_container);
                            Profile profileFragment = new Profile();
                            m_Transaction = getFragmentManager().beginTransaction();
                            m_Transaction.replace(R.id.display, profileFragment);
                            m_Transaction.commit();
                            finish();
                        } else if (items[i].equals("Cancel")) {
                            dialogInterface.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    /**
     * Method that is executed after the camera intent for retrieving the image. It then
     * sets the profile pic to be the given one.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Checking if the result came from Camera or Gallery
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // If its from Camera we retrieve the image and save it to firebase
            m_Map = (Bitmap) data.getExtras().get("data");


            // Setting the picture.
            m_ProfilePic.setImageBitmap(m_Map);
        } else if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            // If its from Gallery we get the picture as Uri.
            m_FileUri = data.getData();
            try {
                //Converting it as a BitMap.
                m_Map = MediaStore.Images.Media.getBitmap(getContentResolver(), m_FileUri);

                // Setting the picture.
                m_ProfilePic.setImageBitmap(m_Map);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Method for checking the permissions for accessing camera and writing in external Storage.
     */
    public void checkPermissionsCamera() {
        PackageManager packageManager = EditProfile.this.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Checking the permissions for using the camera.
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            } else if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == false) {
                Toast.makeText(EditProfile.this, "This device does not have a camera.", Toast.LENGTH_LONG)
                        .show();
                return;
            }
        }
    }

    /**
     * Overridden method works as onBackPressed. Works when the user wants to
     * move to another fragment. A dialog is shown advising the user to chose
     * if he/she wants to save current changes
     */
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            final CharSequence[] items = {"Save", "Exit without saving", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select an option:");

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (items[i].equals("Save")) {
                        //check if age and/or name is  empty
                        if (m_ChangeAge.getText().toString().isEmpty() || m_ChangeName.getText().toString().isEmpty()) {
                            //display error message
                            Toasty.error(m_context, "Name and Age must be present", Toast.LENGTH_SHORT).show();
                        } else {
                            //if not, proceed with saving data
                            //use a thread to upload the data to firebase
                            ThreadForUpload threadForUpload = new ThreadForUpload();
                            Thread t = new Thread(threadForUpload);
                            t.start();

                            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                            setContentView(R.layout.menu_container);
                            Profile profileFragment = new Profile();
                            m_Transaction = getFragmentManager().beginTransaction();
                            m_Transaction.replace(R.id.display, profileFragment);
                            m_Transaction.commit();
                            finish();
                        }

                    } else if (items[i].equals("Exit without saving")) {
                        //Nothing is saved and a user is redirected to his profile page
                        //Move to Profile
                        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                        setContentView(R.layout.menu_container);

                        Profile profileFragment = new Profile();
                        m_Transaction = getFragmentManager().beginTransaction();
                        m_Transaction.replace(R.id.display, profileFragment);
                        m_Transaction.commit();
                        finish();

                    } else if (items[i].equals("Cancel")) {
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();

        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    /**
     * Method to load the user's current information from firebase
     */
    private void loadInitialValues() {
        // get the current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // get a reference to the firebase database.
        // NOTE: null pointer exception warning is not an issue as the user needs to be logged in to access this page
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();//.child(fUser.getUid());

        //change a value in the database to invoke event listener below
        ref.child("access").setValue(RNG.generateNumber());

        //add an event listener to capture data on change
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get all users
                for (DataSnapshot ds : dataSnapshot.child("user").getChildren()) {
                    //check to see if user id in firebase matches with current user id of logged in user
                    // NOTE: null pointer exception warning is not an issue as the user needs to be logged in to access this page
                    if (ds.getKey().equals(fUser.getUid())) {
                        //if so, get the data and set the appropriate fields
                        User u = ds.getValue(User.class);
                        m_ChangeName.setText(u.getName());
                        m_ChangeAge.setText(u.getAge());
                        m_ChangeBio.setText(u.getBio());
                        if(u.getImage()!=null) {
                            try {
                                m_ProfilePic.setImageBitmap(decodeFromFirebaseBase64(u.getImage()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    /**
     * Method used to save updated data to firebase
     */
    private void saveDetails() {
        //get the editted data
        String name = m_ChangeName.getText().toString();
        final String age = m_ChangeAge.getText().toString();
        final String bio = m_ChangeBio.getText().toString();

        //get the image from the image view
        final Bitmap img = ((BitmapDrawable) m_ProfilePic.getDrawable()).getBitmap();

        //get a reference to firebase database
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        //get the current logged in user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        // add the new name of the user
        ref.child("user").child(fUser.getUid()).child("name").setValue(name);

        // when the name is updated, update the other relevant info
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update the age and bio
                ref.child("user").child(fUser.getUid()).child("age").setValue(age);
                ref.child("user").child(fUser.getUid()).child("bio").setValue(bio);
                //object to convert image to byte array for storage on firebase
                ByteArrayOutputStream imgConverted = new ByteArrayOutputStream();

                //save the image as a .jpg file
                img.compress(Bitmap.CompressFormat.JPEG, 100, imgConverted);

                String imageEncoded = Base64.encodeToString(imgConverted.toByteArray(), Base64.DEFAULT);
                ref.child("user").child(fUser.getUid()).child("image").setValue(imageEncoded);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //display error if any occur
                Toasty.error(m_context, "Error" + databaseError, Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Thread class used to push data to firebase.
    class ThreadForUpload implements Runnable {

        @Override
        public void run() {
            saveDetails();
        }

    }
}


