package sep_group_7.SurreyMeets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import sep_group_7.SurreyMeets.R;

public class MapsActivity extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private String provider = "";
    private MarkerOptions marker;
    private Location location;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private View view;
    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_maps, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);

        if(mapView != null){
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.243370, -0.590547),15f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (marker==null) {
                    marker = new MarkerOptions().position(latLng);
                    mMap.addMarker(marker);
                }else{
                    mMap.clear();
                    marker.position(latLng);
                    mMap.addMarker(marker);
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f));

                Intent returnIntent = new Intent();
                returnIntent.putExtra("latitude",latLng.latitude);
                returnIntent.putExtra("longitude",latLng.longitude);
                getActivity().setResult(Activity.RESULT_OK,returnIntent);
                getActivity().finish();

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("latitude",marker.getPosition().latitude);
                returnIntent.putExtra("longitude",marker.getPosition().longitude);
                getActivity().setResult(Activity.RESULT_OK,returnIntent);
                getActivity().finish();
                return false;
            }
        });



    }
}
