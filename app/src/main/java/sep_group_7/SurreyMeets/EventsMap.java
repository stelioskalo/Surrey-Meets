package sep_group_7.SurreyMeets;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 *Activity to show all events on a map.
 */
public class EventsMap extends Activity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnMarkerClickListener {

    private static final String TAG = "EventsMap";
    private GoogleMap mMap;
    private String provider = "";
    private MarkerOptions marker;
    private Location location;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //event listener for firebase
    private ChildEventListener m_childEventListener;
    // used to refer to the database
    //marker to display on the map
    Marker markers;
    // store last location of user
    private Location lastLocation;
    // mark current location of user
    private Marker currentLocationMarker;
    // google API client
    private GoogleApiClient client;
    //used to request location
    private LocationRequest locationRequest;
    // constant used to request location
    public static final int REQUEST_LOCATION_CODE = 99;
    View view;
    MapView mapView;
    DatabaseReference ref =FirebaseDatabase.getInstance().getReference().child("events");
    String name = null;
    String desc = null;
    String category = null;
    String owner = null;
    int participants = 0;
    int current_participants = 0;
    String dateAndTime = null;
    String eventID = null;
    TextView back = null;
    private LocationManager locationManager;
    private android.location.LocationListener listener;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_map);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermission();
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapView = (MapView) findViewById(R.id.eventsmap);
        back = (TextView) findViewById(R.id.back_to_list);
        /**
         * Go back to listview.
         */
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventsMap.this, MenuContainer.class);
                startActivity(intent);
            }
        });
        if(mapView != null){
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        /**
         * Listener to get current location of the user. Set the values to firebase and add a marker.
         */
        listener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userRef.child("latitude").setValue(location.getLatitude());
                userRef.child("longitude").setValue(location.getLongitude());

                lastLocation = location;

                // remove marker if there is already a loaction set
                if(currentLocationMarker != null){
                    currentLocationMarker.remove();
                }

                // set new location coordinates
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Location");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                // set current location marker
                currentLocationMarker = mMap.addMarker(markerOptions);

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

        /**
         * necessary permissions
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
        }
        /**
         * Location manager to get location of the user every 5 seconds
         */
        locationManager.requestLocationUpdates("gps", 5000, 0, listener);


    }


    /**
     * checks permission and if permission granted builds a google API client if there isnt one already, and sets location.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                                ,10);
                    }
                    return;
                }
                locationManager.requestLocationUpdates("gps", 5000, 0, listener);
                break;
            default:
                break;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final DatabaseReference m_Events = FirebaseDatabase.getInstance().getReference().child("events");
        mMap.setOnMarkerClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.243370, -0.590547),15f));
        /**
         * Populates the map with the events.
         * If the checkbox for finding nearby events was checked in the discover activity,
         * the map will show only events in a 1000 meter radius from the user.
         */
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);

                m_Events.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot s : dataSnapshot.getChildren()){
                            // retrieve event information and place markers
                            EventListviewClass event = s.getValue(EventListviewClass.class);
                            LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                            MarkerOptions marker = new MarkerOptions().position(location).title(event.getEventId());
                            Location eventLoc = new Location("event");
                            eventLoc.setLatitude(event.getLatitude());
                            eventLoc.setLongitude(event.getLongitude());
                            Location userLoc = new Location("user");
                            userLoc.setLatitude(user.getLatitude());
                            userLoc.setLongitude(user.getLongitude());
                            if(getIntent().getExtras().getString("events").toString().matches("nearby") ){
                                if(userLoc.distanceTo(eventLoc) <= 1000) {
                                    mMap.addMarker(marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                }
                            }
                            else {
                                mMap.addMarker(marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //failed to read from database
                        Log.w(TAG, "Failed to retrieve data from firebase.", databaseError.toException());

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    /**
     * Build google API client
     */
    protected synchronized void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }




    /**
     * Method to request location in order to find current location.
     * check for location every 1000ms.
     * @param bundle
     */
    //@Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, (com.google.android.gms.location.LocationListener) this);}

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Check if app has permission to request the user's location, if not app must explicitly ask for permission.
     * @return
     */
    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);

            } else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            return false;
        } else
            return true;
    }

    /**
     * On marker click open the viewEvent activity to view event information.
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!marker.getTitle().toString().matches("Current Location")) {
            ref.child(marker.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    EventListviewClass event = dataSnapshot.getValue(EventListviewClass.class);
                    name = event.getName();
                    desc = event.getDesc();
                    category = event.getCategory();
                    participants = event.getParticipants();
                    current_participants = event.getCurrent_participants();
                    dateAndTime = event.getDateAndTime();
                    owner = event.getOwner();
                    eventID = event.getEventId();
                    Intent intent = new Intent(EventsMap.this, ViewEvent.class);
                    intent.putExtra("name", name);
                    intent.putExtra("desc", desc);
                    intent.putExtra("category", category);
                    intent.putExtra("participants", participants);
                    intent.putExtra("current_participants", current_participants);
                    intent.putExtra("dateAndTime", dateAndTime);
                    intent.putExtra("owner", owner);
                    intent.putExtra("eventID", eventID);
                    intent.putExtra("class", "map");
                    if(getIntent().getExtras().getString("events").toString().matches("all")){
                        intent.putExtra("events","all");
                    }
                    else {
                        intent.putExtra("events","nearby");
                    }
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        return false;
        }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
