package sep_group_7.SurreyMeets;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

/**
 * Created by Stelios on 30/04/2019.
 */

public class ViewPerson extends AppCompatActivity {
    TextView m_Statistics;
    //ImageView for User's profile picture.
    ImageView m_ProfilePic;
    //TextView for Name
    TextView m_Name;
    //TextView for Age
    TextView m_Age;
    //TextView for Bio
    TextView m_Bio;
    //Edit Button in the form of ImageView
    ImageView m_Edit;
    TextView m_eventsjoined;
    TextView m_eventsHosted;

    ImageView m_back;
    //The layout that shows after the internet check and retrieval.
    private RelativeLayout m_ShowProfile = null;
    // loading dialog
    private ProgressDialog m_dialog = null;

    TextView m_remove;
    FirebaseUser m_user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();

    /**
     * Class to view the profile of a participant.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_profile);

        //Initialize views.
        m_ProfilePic = (ImageView) findViewById(R.id.view_profilePic);
        m_Name = (TextView) findViewById(R.id.view_profilename);
        m_Age = (TextView) findViewById(R.id.view_profileage);
        m_Bio = (TextView) findViewById(R.id.view_profilebio);
        m_back = (ImageView) findViewById(R.id.view_profileback);
        m_remove = (TextView) findViewById(R.id.removeperson);
        m_eventsjoined = (TextView) findViewById(R.id.eventsJoinedStats);
        m_eventsHosted = (TextView) findViewById(R.id.eventsCreatedStats);

        /**
         * back button to finish activity.
         */
        m_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final String eventid = getIntent().getExtras().getString("eventid");

        /**
         * if the current user is the host, set text of to remove and set onCLickListener
         * to tremove the participant from the event (firebase).
         * finish activity.
         */
        if (m_user.getUid().matches(getIntent().getExtras().getString("host"))) {
            m_remove.setText("REMOVE");
            m_remove.setTextColor(Color.parseColor("#F37E70"));
            m_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    m_ref.child("events").child(getIntent().getExtras().getString("eventid")).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            EventListviewClass event = dataSnapshot.getValue(EventListviewClass.class);
                            if (event.getEventId().toString().matches(eventid)) {
                                int currentpart = getIntent().getExtras().getInt("current_part");
                                m_ref.child("events").child(eventid).child("participant").child(getIntent().getExtras().getString("id")).removeValue();
                                m_ref.child("events").child(eventid).child("current_participants").setValue((currentpart - 1));
                                finish();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }

        //load data using a thread
        ThreadForRetrievingData thread = new ThreadForRetrievingData();
        Thread t = new Thread(thread);
        t.start();

        m_Statistics = (TextView) findViewById(R.id.view_statistics);
        SpannableString content = new SpannableString("Statistics");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        setStatistics();
    }

    /**
     * Method to load the current user's details from firebase
     */
    private void loadUserDetails() {
        // get the current user

        // get a reference to the firebase database.
        // NOTE: null pointer exception warning is not an issue as the user needs to be logged in to access this page
        //.child(fUser.getUid());

        //change a value in the database to invoke event listener below
        m_ref.child("access").setValue(RNG.generateNumber());

        //add an event listener to capture data on change
        m_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get all users
                for (DataSnapshot ds : dataSnapshot.child("user").getChildren()) {
                    //check to see if user id in firebase matches with current user id of logged in user
                    // NOTE: null pointer exception warning is not an issue as the user needs to be logged in to access this page
                    if (ds.getKey().equals(getIntent().getExtras().getString("id"))) {
                        //if so, get the data and set the appropriate fields
                        User u = ds.getValue(User.class);
                        m_Name.setText(u.getName());
                        m_Age.setText(u.getAge());
                        m_Bio.setText(u.getBio());
                        if (u.getImage() != null) {
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


    //Thread class used to get data from firebase.
    class ThreadForRetrievingData implements Runnable {

        @Override
        public void run() {
            loadUserDetails();
        }

    }

    /**
     * Retrieve user's stats from firebase and set the appropriate views.
     */
    public void setStatistics() {
        m_ref.child("user").child(m_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                m_eventsjoined.setText(String.valueOf(user.getEvents_joined()));
                m_eventsHosted.setText(String.valueOf(user.getEvents_hosted()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
