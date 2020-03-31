package sep_group_7.SurreyMeets;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;


/**
 * Created by Stelios on 23/04/2019.
 */

/**
 * Activity that will allow the user to view information about a specific event and join
 */
public class ViewEvent extends AppCompatActivity implements UserListAdapter.UserActionListener {
    private TextView m_name = null;
    private TextView m_category = null;
    private TextView m_description = null;
    private TextView m_currentParticipants = null;
    private TextView m_participants = null;
    private TextView m_dateAndTime = null;
    private Button m_joinCancel = null;
    private ImageView m_back = null;
    private ImageView m_eventimage = null;
    private String m_eventId = null;
    private String m_owner = null;
    private int m_current_participants = 0;
    private ImageView m_host = null;
    private ImageView m_edit = null;
    DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();
    FirebaseUser m_currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView m_participant_list = null;
    private UserListAdapter m_adapter  = null;
    private List<User> userList = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_event);
        m_name = (TextView) findViewById(R.id.view_event_name);
        m_category = (TextView) findViewById(R.id.view_event_category);
        m_description = (TextView) findViewById(R.id.view_event_desc);
        m_host = (ImageView) findViewById(R.id.hostspic);
        m_currentParticipants = (TextView) findViewById(R.id.view_event_current_participants);
        m_participants = (TextView) findViewById(R.id.view_event_participants);
        m_dateAndTime = (TextView) findViewById(R.id.view_event_date);
        m_joinCancel = (Button) findViewById(R.id.view_event_join);
        m_back = (ImageView) findViewById(R.id.view_event_back);
        m_eventimage = (ImageView) findViewById(R.id.view_event_image);
        m_edit = (ImageView) findViewById(R.id.edit_event);
        m_participant_list = (RecyclerView) findViewById(R.id.participantphotos);
        userList = new ArrayList<User>();
        /**
         * Go m_back to discover activity or evensmap activity, depending from where you viewed the event
         */
        m_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getIntent().getExtras().getString("class") != null) {
                    Intent intent = new Intent(ViewEvent.this, EventsMap.class);
                    if(getIntent().getExtras().getString("events").toString().matches("all")){
                        intent.putExtra("events", "all");
                    }
                    else {
                        intent.putExtra("events","nearby");
                    }
                    startActivity(intent);
                }
                finish();
            }
        });
        m_owner = getIntent().getExtras().getString("owner");
        Log.v("OWNER",m_owner);
        m_current_participants = getIntent().getExtras().getInt("current_participants");

        /**
         * Get m_host's Image
         */
        if(m_owner != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            dbRef.child("user").child(m_owner).addListenerForSingleValueEvent(new ValueEventListener() {
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
            });
        }

        m_eventId = getIntent().getExtras().getString("eventID");

        /**
         * On click view Host's profile
         */
        m_host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m_currentFirebaseUser.getUid().matches(m_owner)) {
                    Intent intent = new Intent(ViewEvent.this, ViewPerson.class);
                    intent.putExtra("host", m_owner);
                    intent.putExtra("id",m_owner);
                    startActivity(intent);
                }
            }
        });
        /**
         * Set event's image
         */
        if(m_eventId != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            dbRef.child("events").child(m_eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("eventImage")) {
                        String image = dataSnapshot.child("eventImage").getValue().toString();
                        try {
                            m_eventimage.setImageBitmap(decodeFromFirebaseBase64(image));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        /**
         * If you are the user the button should say cancel.
         */
        Log.v("USER:", m_currentFirebaseUser.getUid().toString());
        if(m_currentFirebaseUser.getUid().toString().matches(m_owner)){
            m_joinCancel.setText("Cancel");
            m_joinCancel.setBackgroundResource(R.drawable.buttoncancel);
            m_edit.setVisibility(View.VISIBLE);
        }
        else {
            joinOrLeave();
        }

        /**
         * Join or leave and event on button pressed and go m_back to discover or Eventsmap depending from where the user
         * viewed the event. Cancel event if you are the m_host.
         */
        m_joinCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_currentFirebaseUser.getUid().toString().matches(m_owner)){
                    m_ref.child("events").child(m_eventId).removeValue();
                    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                    Discover discoverFragment = new Discover();
                    FragmentTransaction m_Transaction = getFragmentManager().beginTransaction();
                    m_Transaction.replace(R.id.display, discoverFragment);
                    m_Transaction.commit();
                }
                else {
                    joinLeaveEvent();
                }

//                if(getIntent().getExtras().getString("class") != null) {
//                    Intent intent = new Intent(ViewEvent.this, EventsMap.class);
//                    if(getIntent().getExtras().getString("events").toString().matches("all")){
//                        intent.putExtra("events", "all");
//                    }
//                    else {
//                        intent.putExtra("events","nearby");
//                    }
//                    startActivity(intent);
//                }
//                else {
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent intent = new Intent(ViewEvent.this, MenuContainer.class);
//                            startActivity(intent);
//                        }
//                    }, 500);
//                }
            }
        });
        /**
         * Set all textviews to event's information
         */
        m_name.setText(getIntent().getExtras().getString("name"));
        m_category.setText(getIntent().getExtras().getString("category"));
        m_description.setText(getIntent().getExtras().getString("desc"));
        m_currentParticipants.setText(String.valueOf(getIntent().getExtras().getInt("current_participants") + "/"));
        m_participants.setText(String.valueOf(getIntent().getExtras().getInt("participants")));
        m_dateAndTime.setText(getIntent().getExtras().getString("dateAndTime"));

        /**
         * If you are not the m_host you are not able to m_edit the event. If you are the m_host EditEvent activity will start.
         */
        m_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m_currentFirebaseUser.getUid().toString().matches(m_owner)){
                    new AlertDialog.Builder(ViewEvent.this)
                            .setTitle("Edit Event")
                            .setMessage("You do not have access to edit the event")
                            .setPositiveButton("OK", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else {
                    Intent intent = new Intent(ViewEvent.this, EditEvent.class);
                    intent.putExtra("name", m_name.getText().toString());
                    intent.putExtra("category", m_category.getText().toString());
                    intent.putExtra("desc", m_description.getText().toString());
                    intent.putExtra("current", m_currentParticipants.getText().toString());
                    intent.putExtra("participants", m_participants.getText().toString());
                    intent.putExtra("date", m_dateAndTime.getText().toString());
                    intent.putExtra("eventId", m_eventId);
                    intent.putExtra("owner", m_owner.toString());
                    startActivity(intent);
                }
            }
        });
        //viewParticipants();
    }

    /**
     * On resume fetch data from firebase and update views.
     */
    @Override
    public void onResume(){
        super.onResume();
        userList.clear();
        m_participant_list.removeAllViews();
        m_ref.child("events").child(m_eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EventListviewClass event = dataSnapshot.getValue(EventListviewClass.class);
                m_currentParticipants.setText(String.valueOf(event.getCurrent_participants() + "/"));
                viewParticipants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //OnResume Fragment
    }

    /**
     * Functionality for user to join an event or leave.
     */
    public void joinLeaveEvent(){
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(m_joinCancel.getText().toString().matches("JOIN")){
            int participants = getIntent().getExtras().getInt("participants");
            if (participants != m_current_participants) {
                m_ref.child("events").child(m_eventId).child("participant").child(currentFirebaseUser.getUid()).setValue(currentFirebaseUser.getUid());
                m_ref.child("events").child(m_eventId).child("current_participants").setValue(m_current_participants + 1);
                m_currentParticipants.setText(String.valueOf(m_current_participants + 1) + "/");
                m_ref.child("user").child(m_currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        m_ref.child("user").child(m_currentFirebaseUser.getUid()).child("events_joined").setValue(user.getEvents_joined() + 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                m_current_participants += 1;
                m_joinCancel.setText("Leave");
                m_joinCancel.setBackgroundResource(R.drawable.buttoncancel);
                Toast.makeText(ViewEvent.this, "EVENT JOINED", Toast.LENGTH_LONG).show();
            }
            else {
                new AlertDialog.Builder(ViewEvent.this)
                        .setTitle("Full")
                        .setMessage("The event is full, you cannot join")
                        .setPositiveButton("OK", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        }
        else {
            m_ref.child("events").child(m_eventId).child("participant").child(currentFirebaseUser.getUid()).removeValue();
            m_ref.child("events").child(m_eventId).child("current_participants").setValue(m_current_participants - 1);
            m_currentParticipants.setText(String.valueOf(m_current_participants - 1) + "/");
            m_current_participants -= 1;
            m_joinCancel.setText("Join");
            m_joinCancel.setBackgroundResource(R.drawable.buttoncircle);
            Toast.makeText(ViewEvent.this, "YOU LEFT THE EVENT", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Determine button's text and colour.
     */
    public void joinOrLeave(){
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        m_ref.child("events").child(m_eventId).child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(currentFirebaseUser.getUid())){
                    m_joinCancel.setText("Leave");

                    m_joinCancel.setBackgroundResource(R.drawable.buttoncancel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * if you are the host of an event.Populate recycler view with the picture of the participants.
     */
    public void viewParticipants(){
        if(m_currentFirebaseUser.getUid().toString().matches(m_owner)){
            m_ref.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        final User user = ds.getValue(User.class);
                        m_ref.child("events").child(m_eventId).child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshots) {
                                if(user.getId()!=null) {
                                    if (dataSnapshots.hasChild(user.getId())) {
                                        userList.add(user);
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        final Handler handler = new Handler();
                        final int delay = 1000; //milliseconds

                        handler.postDelayed(new Runnable(){
                            public void run(){
                                if(!userList.isEmpty())//checking if the data is loaded or not
                                {
                                    Log.v("WOOOO", String.valueOf(userList.size()));
                                    m_adapter = new UserListAdapter(getApplicationContext(), userList, ViewEvent.this);
                                    //set the adapter
                                    m_participant_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                    m_participant_list.setAdapter(m_adapter);

                                }
                                else
                                    handler.postDelayed(this, delay);
                            }
                        }, delay);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });


        }

    }

    /**
     * On click the host can view the participant's profile and ability to remove the participant from the event.
     * @param position
     */
    @Override
    public void onUserClick(int position) {
        if(!m_currentFirebaseUser.getUid().matches(userList.get(position).getId())) {
            Intent intent = new Intent(ViewEvent.this, ViewPerson.class);
            intent.putExtra("id", userList.get(position).getId());
            intent.putExtra("host", m_owner);
            intent.putExtra("eventid", m_eventId);
            m_current_participants = getIntent().getExtras().getInt("current_participants");
            Log.v("GAMOTO", String.valueOf(m_current_participants));
            intent.putExtra("current_part", m_current_participants);
            startActivity(intent);
        }
    }
    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
