package sep_group_7.SurreyMeets;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

/**
 * Created by Stelios on 10/04/2019.
 */

public class MapContainer extends AppCompatActivity {

        FragmentTransaction m_Transaction;

        /**
         * onCreate will set the view to menu_container layout
         * and begin a transaction between fragments and give functionality
         * to change the view of the relative layout when something is pressed
         *
         * @param savedInstanceState
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
            setContentView(R.layout.map_container);
            MapsActivity mapFragment = new MapsActivity();
            m_Transaction = getFragmentManager().beginTransaction();
            m_Transaction.replace(R.id.displaymap, mapFragment);
            m_Transaction.commit();


        }



}

