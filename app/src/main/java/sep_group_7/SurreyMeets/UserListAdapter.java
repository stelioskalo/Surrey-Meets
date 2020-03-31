package sep_group_7.SurreyMeets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

/**
 * Recyvler view adapter to view participants.
 * Created by Stelios on 02/05/2019.
 */

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.SearchViewHolder> {
    //context object
    private Context m_context = null;
    //list of events
    private List<User> m_users = null;
    //custom click listener for events
    private UserListAdapter.UserActionListener m_userListener = null;

    /**
     * Constructor for adapter
     *
     * @param context:            context of app
     * @param users:              list of participants
     * @param userActionListener: custom  listener for our participant list
     */
    public UserListAdapter(Context context, List<User> users, UserListAdapter.UserActionListener userActionListener) {
        super();
        this.m_context = context;
        this.m_users = users;
        this.m_userListener = userActionListener;
    }

    @Override
    public UserListAdapter.SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate item layout for list items
        View view = LayoutInflater.from(m_context).inflate(R.layout.participant_item, parent, false);
        return new UserListAdapter.SearchViewHolder(view, m_userListener);
    }

    @Override
    public void onBindViewHolder(final SearchViewHolder holder, int position) {

        //get the userId of the participant
        final String id = m_users.get(position).getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();//.child(fUser.getUid());

        //change a value in the database to invoke event listener below
        ref.child("user").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //if so, get the data and set the appropriate fields
                User u = dataSnapshot.getValue(User.class);
                if (u.getImage() != null) {
                    try {
                        holder.m_image.setImageBitmap(decodeFromFirebaseBase64(u.getImage()));
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

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    /**
     * Method to return number of participants
     *
     * @return: number of events
     */
    @Override
    public int getItemCount() {
        return m_users.size();
    }


    //search view holder class
    class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //user image
        private ImageView m_image = null;
        //on participant click listener
        private UserListAdapter.UserActionListener m_listener = null;

        private SearchViewHolder(View itemView, UserListAdapter.UserActionListener userActionListener) {
            super(itemView);

            m_image = (ImageView) itemView.findViewById(R.id.participant_image);

            m_listener = userActionListener;

            //set onclick listener
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //call our on click listener
            m_listener.onUserClick(getAdapterPosition());
        }
    }

    /**
     * Interface for all user list actions
     */
    public interface UserActionListener {
        /**
         * on participant click
         */
        void onUserClick(int position);
    }
}
