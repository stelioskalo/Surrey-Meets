package sep_group_7.SurreyMeets;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.IOException;

import es.dmoral.toasty.Toasty;

/**
 * Class extending fragment representing the profile page
 */
public class Profile extends Fragment {

    //View for the Fragment.
    View m_View;
    //TextView for statistics for each user.
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
    TextView m_Edit;
    TextView m_eventsJoined;
    TextView m_eventsHosted;
    TextView m_logout;
    //context of fragment
    private Context m_context = null;
    DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();
    FirebaseUser m_user = FirebaseAuth.getInstance().getCurrentUser();

    public Profile() {
        // Required empty public constructor
    }

    /**
     * onCreateView inflates the view with the activity_edit_profile layout
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        m_View = inflater.inflate(R.layout.fragment_profile, container, false);

        //set the context field
        m_context = getActivity().getApplicationContext();

        m_ProfilePic = (ImageView) m_View.findViewById(R.id.profilePic);

        m_Name= (TextView) m_View.findViewById(R.id.set_name);
        m_Age= (TextView) m_View.findViewById(R.id.set_age);
        m_Bio= (TextView) m_View.findViewById(R.id.set_bio);

        m_logout = (TextView) m_View.findViewById(R.id.logout_btn);
        m_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(i);
                getActivity().finish();
                Toasty.success(getActivity().getApplicationContext(), "Signed Out Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        //load data using a thread
        ThreadForRetrievingData thread = new ThreadForRetrievingData();
        Thread t = new Thread(thread);
        t.start();

        m_Edit = (TextView) m_View.findViewById(R.id.editProfile);
        m_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move to Edit Profile
                Intent intent = new Intent(getActivity(),EditProfile.class);
                startActivity(intent);
            }
        });

        //Setting the text USING FIREBASE and statistics feature.
        m_Statistics = (TextView) m_View.findViewById(R.id.view_statistics);
        SpannableString content = new SpannableString("Statistics");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        //FIREBASE m_Statistics.setText(content);

        m_eventsJoined = (TextView) m_View.findViewById(R.id.eventsJoinedStats);
        m_eventsHosted = (TextView) m_View.findViewById(R.id.eventsCreatedStats);

        setStatistics();

        return m_View;
    }

    /**
     * Method to load the current user's details from firebase
     */
    private void loadUserDetails(){

        // get a reference to the firebase database.
        // NOTE: null pointer exception warning is not an issue as the user needs to be logged in to access this page
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference();//.child(fUser.getUid());

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
                    if (ds.getKey().equals(m_user.getUid())) {
                        //if so, get the data and set the appropriate fields
                        User u = ds.getValue(User.class);
                            m_Name.setText(u.getName());

                        m_Age.setText(u.getAge());
                        m_Bio.setText(u.getBio());
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

    //Thread class used to get data from firebase
    class ThreadForRetrievingData implements Runnable {

        @Override
        public void run() {
            loadUserDetails();
        }

    }
    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    /**
     * Retrieve user's stats from firebase and set the appropriate views.
     */
    public void setStatistics(){
        m_ref.child("user").child(m_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                m_eventsJoined.setText(String.valueOf(user.getEvents_joined()));
                m_eventsHosted.setText(String.valueOf(user.getEvents_hosted()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}

