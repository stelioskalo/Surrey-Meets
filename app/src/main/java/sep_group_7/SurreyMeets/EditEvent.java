package sep_group_7.SurreyMeets;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import es.dmoral.toasty.Toasty;


/**
 * Created by Stelios on 30/04/2019.
 */

public class EditEvent extends AppCompatActivity {
    private ImageView m_back = null;
    private EditText m_name = null;
    private TextView m_category = null;
    private ImageView m_image = null;
    private EditText m_desc = null;
    private TextView m_date = null;
    private ImageView m_host = null;
    private TextView m_current = null;
    private TextView m_max = null;
    private ImageView m_save = null;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event);

        //Initialize variables.
        m_back = (ImageView) findViewById(R.id.edit_event_back);
        m_name = (EditText) findViewById(R.id.edit_event_name);
        m_category = (TextView) findViewById(R.id.edit_event_category);
        m_image = (ImageView) findViewById(R.id.edit_event_image);
        m_desc = (EditText) findViewById(R.id.edit_event_desc);
        m_date = (TextView) findViewById(R.id.edit_event_date);
        m_host = (ImageView) findViewById(R.id.edit_event_hostspic);
        m_current = (TextView) findViewById(R.id.edit_event_current_participants);
        m_max = (TextView) findViewById(R.id.edit_event_participants);
        m_save = (ImageView) findViewById(R.id.save);

        //Set views to the extras gotten by view event.
        m_name.setText(getIntent().getExtras().getString("name"));
        m_category.setText(getIntent().getExtras().getString("category").toString());
        m_desc.setText(getIntent().getExtras().getString("desc").toString());
        m_date.setText(getIntent().getExtras().getString("date").toString());
        m_current.setText(getIntent().getExtras().getString("current").toString());
        m_max.setText(getIntent().getExtras().getString("participants").toString());


        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("events").child(getIntent().getExtras().getString("eventId")).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String image = dataSnapshot.child("eventImage").getValue().toString();
                        try {
                            m_image.setImageBitmap(decodeFromFirebaseBase64(image));
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        dbRef.child("user").child(getIntent().getExtras().getString("owner")).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("image")) {
                            String image = dataSnapshot.child("image").getValue().toString();
                            try {
                                m_host.setImageBitmap(decodeFromFirebaseBase64(image));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

        FirebaseStorage userStorage = FirebaseStorage.getInstance();
        //access our server for the app
        StorageReference userStorageRef = userStorage.getReferenceFromUrl("gs://surreymeets-6a316.appspot.com");
        //get a reference to the location we want to save our data and name it appropriately
        StorageReference userImagesRef = userStorageRef.child("img/users/" + getIntent().getExtras().getString("owner") + ".jpg");
        //load host image.
        userImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString()).fit().centerCrop().into(m_host);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(EditEvent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * If name and description are not null then when save is clicked the event is updated on firebase and an activity of the menu starts.
         */
        m_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_name.getText().toString() != null && m_desc.getText().toString() != null){
                    ref.child("events").child(getIntent().getExtras().getString("eventId")).child("name").setValue(m_name.getText().toString());
                    ref.child("events").child(getIntent().getExtras().getString("eventId")).child("desc").setValue(m_desc.getText().toString());
                    Intent intent = new Intent(EditEvent.this, MenuContainer.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getBaseContext(), "You must provide a name an description",Toast.LENGTH_SHORT).show();
                }
            }
        });




        /**
         * Back to View Event.
         */
        m_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
