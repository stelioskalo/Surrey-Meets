package sep_group_7.SurreyMeets;
/**
 * Last edited by Stelios at 05/03/2019
 */
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Class extending fragment for navigation bar
 */
public class Navigation extends Fragment {
    ImageView m_Profile;
    ImageView m_Events;
    ImageView m_Discover;
    ImageView m_Add;

    /**
     * onCreateView will inflate the view with the fragment_navigation layout
     * will initialize the 4 pictures and add onClickListeners to them that when pressed
     * Will change the view to the view associated with the picture.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        m_Profile = (ImageView)view.findViewById(R.id.profile);
        m_Events = (ImageView)view.findViewById(R.id.list);
        m_Discover = (ImageView)view.findViewById(R.id.discover);
        m_Add = (ImageView)view.findViewById(R.id.add);


        m_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getFragmentManager().beginTransaction().replace(R.id.display, new Profile()).commit();
            }
        });
        m_Events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.display, new Events()).commit();
            }
        });
        m_Discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.display, new Discover()).commit();
            }
        });
        m_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.display, new AddEvent()).commit();
            }
        });
        return view;
    }
}
