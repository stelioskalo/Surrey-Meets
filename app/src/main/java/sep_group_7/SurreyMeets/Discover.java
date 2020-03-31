package sep_group_7.SurreyMeets;
/**
 * Last edited by Stelios at 05/03/2019
 */

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
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

import static android.content.Context.LOCATION_SERVICE;


/**
 * Class extending Fragment representing the Discover page
 */
public class Discover extends Fragment implements DiscoverListAdapter.EventActionListener {
    //edittext representing the search bar
    private EditText m_searchBar = null;
    //radio button for name search
    private RadioButton m_radioName = null;
    //radio button for category search
    private RadioButton m_radioCategory = null;
    //database reference object
    private DatabaseReference m_ref = null;
    //firebase user object
    private FirebaseUser m_user = null;
    //m_list of events
    List<EventListviewClass> eventList = new ArrayList<EventListviewClass>();
    //view object
    private View m_View = null;
    //recycler view widget
    private RecyclerView m_list = null;
    //custom list adapter
    private DiscoverListAdapter m_adapter = null;
    //TODO event on maps button
    private TextView m_eventMapBtn = null;
    private LocationManager locationManager;
    private LocationListener listener;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
    private CheckBox m_nearby = null;
    // private Spinner m_sortSpinner = null;
    //private String m_selection = null;

    public Discover() {
        // Required empty public constructor
    }

    /**
     * onCreateView will inflate the view with the fragment_discover layout
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        m_View = inflater.inflate(R.layout.fragment_discover, container, false);
        //initialise search elements
        m_searchBar = (EditText) m_View.findViewById(R.id.search_text);
        m_radioName = (RadioButton) m_View.findViewById(R.id.name_btn);
        m_radioCategory = (RadioButton) m_View.findViewById(R.id.category_btn);
        m_nearby = (CheckBox) m_View.findViewById(R.id.nearby);
        //TODO initialise our map button
        m_eventMapBtn = (TextView) m_View.findViewById(R.id.view_map_btn);
        //TODO map button on click functionality
        /**
         * switches to map view, puts extra if all events should be shown or only nearby.
         */
        m_eventMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EventsMap.class);
                if (!m_nearby.isChecked()) {
                    intent.putExtra("events", "all");
                } else {
                    intent.putExtra("events", "nearby");
                }
                startActivity(intent);
            }
        });

        //Necessary permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
        }

        //initialise our references to db and user
        m_ref = FirebaseDatabase.getInstance().getReference();
        m_user = FirebaseAuth.getInstance().getCurrentUser();

        //initialise m_list and configure
        m_list = (RecyclerView) m_View.findViewById(R.id.discoverlist);
        eventList = new ArrayList<EventListviewClass>();

        m_list.setHasFixedSize(true);
        m_list.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL) {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

            }
        });
        m_list.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        //initialise list
        nearbyOrAll("", 3);

        m_radioName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set the appropriate radio buttons
                m_radioName.setChecked(true);
                m_radioCategory.setChecked(false);
            }
        });

        //on click for category radio
        m_radioCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set the appropriate radio buttons
                m_radioName.setChecked(false);
                m_radioCategory.setChecked(true);
            }
        });


        //search functionality
        m_searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    //check which option is selected and search by that choice i.e. 1 for name, 2 for category
                    if (m_radioName.isChecked()) {
                        nearbyOrAll(editable.toString(), 1);
                    } else {
                        nearbyOrAll(editable.toString(), 2);
                    }
                } else {
                    //if the search is empty, load the entire list by re-initialising it
                    nearbyOrAll("", 3);
                }
            }
        });
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        m_nearby.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                nearbyOrAll("", 3);
            }
        });

        /**
         * Listener to get current location of the user and set the values to firebase.
         */
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                ref.child("latitude").setValue(location.getLatitude());
                ref.child("longitude").setValue(location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        //Location manager that listens to location changes every 5 seconds
        locationManager.requestLocationUpdates("gps", 5000, 0, listener);

        return m_View;
    }

    /**
     * on event click start new activity to view event's information.
     *
     * @param position
     */
    @Override
    public void onEventClick(int position) {
        Intent intent = new Intent(getActivity(), ViewEvent.class);
        intent.putExtra("name", eventList.get(position).getName());
        intent.putExtra("desc", eventList.get(position).getDesc());
        intent.putExtra("category", eventList.get(position).getCategory());
        intent.putExtra("participants", eventList.get(position).getParticipants());
        intent.putExtra("current_participants", eventList.get(position).getCurrent_participants());
        intent.putExtra("dateAndTime", eventList.get(position).getDateAndTime());
        intent.putExtra("owner", eventList.get(position).getOwner());
        intent.putExtra("eventID", eventList.get(position).getEventId());
        startActivity(intent);
    }


    /**
     * Request perimissions.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                                , 10);
                    }
                    return;
                }
                locationManager.requestLocationUpdates("gps", 2000, 0, listener);
                break;
            default:
                break;
        }
    }

    /**
     * Method to search events list and determine which events to search, nearby or all.
     * If find nearby checkbox is checked the listview will only show events nearby.
     *
     * @param searchQuery: query to search
     * @param searchType:  what to search i.e. 1 for name, 2 for  category, 3 for initialisation
     */
    private void nearbyOrAll(final String searchQuery, final int searchType) {
        final String lowercaseSearchQuery = searchQuery.toLowerCase();
        if (m_nearby.isChecked()) {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    if (dataSnapshot.hasChild("longitude") && dataSnapshot.hasChild("latitude")) {
                        m_ref.child("events").orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                eventList.clear();
                                m_list.removeAllViews();

                                //loop through all events
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    //get the relevant details
                                    EventListviewClass e = ds.getValue(EventListviewClass.class);
                                    final Location eventLoc = new Location("event");
                                    eventLoc.setLatitude(e.getLatitude());
                                    eventLoc.setLongitude(e.getLongitude());
                                    final Location userLoc = new Location("user");
                                    userLoc.setLatitude(user.getLatitude());
                                    userLoc.setLongitude(user.getLongitude());


                                    if (userLoc.distanceTo(eventLoc) <= 1000) {
                                        if (searchType == 1 && e.getName().toLowerCase().contains(lowercaseSearchQuery)) {
                                            //if so, add to the list
                                            eventList.add(e);
                                        }
                                        //if we're searching by category, check if the event category contains the query
                                        //NOTE: toLowercase is used to increase search results
                                        else if (searchType == 2 && e.getCategory().toLowerCase().contains(lowercaseSearchQuery)) {
                                            //if so, add to the list
                                            eventList.add(e);
                                        }
                                        //search type 3 is initialising the whole list
                                        else if (searchType == 3) {
                                            eventList.add(e);
                                        }
                                    }

                                }
                                m_adapter = new DiscoverListAdapter(getActivity().getApplicationContext(), eventList, Discover.this);
                                //set the adapter
                                m_list.setAdapter(m_adapter);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        //Do nothing because the user has no location
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    m_ref.child("events").orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            eventList.clear();
                            m_list.removeAllViews();

                            //loop through all events
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                //get the relevant details
                                EventListviewClass e = ds.getValue(EventListviewClass.class);
                                if (searchType == 1 && e.getName().toLowerCase().contains(lowercaseSearchQuery)) {
                                    //if so, add to the list
                                    eventList.add(e);
                                }
                                //if we're searching by category, check if the event category contains the query
                                //NOTE: toLowercase is used to increase search results
                                else if (searchType == 2 && e.getCategory().toLowerCase().contains(lowercaseSearchQuery)) {
                                    //if so, add to the list
                                    eventList.add(e);
                                }
                                //search type 3 is initialising the whole list
                                else if (searchType == 3) {
                                    eventList.add(e);
                                }

                            }
                            m_adapter = new DiscoverListAdapter(getActivity(), eventList, Discover.this);
                            //set the adapter
                            m_list.setAdapter(m_adapter);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
