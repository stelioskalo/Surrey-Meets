package sep_group_7.SurreyMeets;
/**
 * Last edited by Stelios at 05/03/2019
 */

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

/**
 * The MenuContainer activity simply holds the layout of the menu.
 */
public class MenuContainer extends AppCompatActivity {

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
        setContentView(R.layout.menu_container);

        Discover discoverFragment = new Discover();
        m_Transaction = getFragmentManager().beginTransaction();
        m_Transaction.replace(R.id.display, discoverFragment);
        m_Transaction.commit();
    }
}
