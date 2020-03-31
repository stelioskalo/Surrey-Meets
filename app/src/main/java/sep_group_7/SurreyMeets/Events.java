package sep_group_7.SurreyMeets;
/**
 * Created by Stelios on 05/03/2019
 */
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Class extending Fragment representing the Events page
 */
public class Events extends Fragment implements DiscoverListAdapter.EventActionListener{
    View m_View= null;
    Button m_myevents = null;
    Button m_upcomingevents = null;
    RecyclerView m_list = null;
    List<EventListviewClass> m_eventList = null;
    DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();
    FirebaseUser m_user = FirebaseAuth.getInstance().getCurrentUser();
    DiscoverListAdapter m_adapter  = null;
    TextView m_order = null;

    public Events() {
        // Required empty public constructor
    }

    /**
     * onCreateView will inflate the view with the fragment_events layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        m_View = inflater.inflate(R.layout.fragment_events, container, false);
        //initialize variables
        m_myevents = (Button) m_View.findViewById(R.id.myevents);
        m_upcomingevents = (Button) m_View.findViewById(R.id.upcomingevents);
        m_order = (TextView) m_View.findViewById(R.id.order);

        //initialise m_list and configure
        m_list = (RecyclerView) m_View.findViewById(R.id.myeventlist);
        m_eventList = new ArrayList<EventListviewClass>();
        m_list.setHasFixedSize(true);
        m_list.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        m_list.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL) {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

            }
        });

        //call initMyEvents function
        initMyEvents();

        /**
         * When m_myevents button is clicked initialize list with user's hosted events
         */
        m_myevents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMyEvents();
            }
        });

        /**
         * When m_upcomingevents button is clicked initialize list with events the user has joined.
         */
        m_upcomingevents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initUpcoming();
            }
        });

        return m_View;

    }

    /**
     * Function that will populate the view with the events the user has hosted.
     * if m_order textview is pressed the events will be ordered in ascending order by
     * the number of current participants.
     */
    public void initMyEvents(){
        m_eventList.clear();
        m_list.removeAllViews();

        m_ref.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    EventListviewClass event = ds.getValue(EventListviewClass.class);
                    if(m_user.getUid().matches(event.getOwner())){
                        m_eventList.add(event);
                    }
                }
                m_adapter = new DiscoverListAdapter(getActivity().getApplicationContext(), m_eventList, Events.this);
                //set the adapter
                m_list.setAdapter(m_adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        m_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_eventList.clear();
                m_list.removeAllViews();

                m_ref.child("events").orderByChild("current_participants").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            EventListviewClass event = ds.getValue(EventListviewClass.class);
                            if(m_user.getUid().matches(event.getOwner())){
                                m_eventList.add(event);
                            }
                        }
                        m_adapter = new DiscoverListAdapter(getActivity().getApplicationContext(), m_eventList, Events.this);
                        //set the adapter
                        m_list.setAdapter(m_adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    /**
     * Function that will populate the view with the events the user has joined.
     * If m_order textview is pressed the events will be ordered in ascending order by
     * number of participants.
     */
    public void initUpcoming(){
        m_eventList.clear();
        m_list.removeAllViews();
        m_ref.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(final DataSnapshot ds: dataSnapshot.getChildren()){
                    final EventListviewClass event = ds.getValue(EventListviewClass.class);
                    m_ref.child("events").child(event.getEventId()).child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!m_user.getUid().matches(event.getOwner()) && dataSnapshot.hasChild(m_user.getUid())){
                                    m_eventList.add(event);
                                }
                            m_adapter = new DiscoverListAdapter(getActivity().getApplicationContext(), m_eventList, Events.this);
                            //set the adapter
                            m_list.setAdapter(m_adapter);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        m_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_eventList.clear();
                m_list.removeAllViews();
                m_ref.child("events").orderByChild("current_participants").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(final DataSnapshot ds: dataSnapshot.getChildren()){
                            final EventListviewClass event = ds.getValue(EventListviewClass.class);
                            m_ref.child("events").child(event.getEventId()).child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!m_user.getUid().matches(event.getOwner()) && dataSnapshot.hasChild(m_user.getUid())){
                                        m_eventList.add(event);
                                    }
                                    m_adapter = new DiscoverListAdapter(getActivity().getApplicationContext(), m_eventList, Events.this);
                                    //set the adapter
                                    m_list.setAdapter(m_adapter);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    /**
     * Start ViewEvent activity to view the event pressed.
     * @param position
     */
    @Override
    public void onEventClick(int position) {
        Intent intent = new Intent(getActivity(), ViewEvent.class);
        intent.putExtra("name", m_eventList.get(position).getName());
        intent.putExtra("desc", m_eventList.get(position).getDesc());
        intent.putExtra("category", m_eventList.get(position).getCategory());
        intent.putExtra("participants", m_eventList.get(position).getParticipants());
        intent.putExtra("current_participants", m_eventList.get(position).getCurrent_participants());
        intent.putExtra("dateAndTime", m_eventList.get(position).getDateAndTime());
        intent.putExtra("owner", m_eventList.get(position).getOwner());
        intent.putExtra("eventID", m_eventList.get(position).getEventId());
        startActivity(intent);
    }
}
