package sep_group_7.SurreyMeets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DiscoverListAdapter extends RecyclerView.Adapter<DiscoverListAdapter.SearchViewHolder> {

    //context object
    private Context m_context = null;
    //list of events
    private List<EventListviewClass> m_events = null;
    //custom click listener for events
    private EventActionListener m_eventListener = null;

    /**
     * Constructor for adapter
     * @param context: context of app
     * @param events: list of events
     * @param eventActionListener: custom  listener for our events list
     */
    public DiscoverListAdapter(Context context, List<EventListviewClass> events, EventActionListener eventActionListener) {
        super();
        this.m_context = context;
        this.m_events = events;
        this.m_eventListener = eventActionListener;
    }

    @Override
    public DiscoverListAdapter.SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate item layout for list items
        View view = LayoutInflater.from(m_context).inflate(R.layout.item_layout, parent, false);
        return new DiscoverListAdapter.SearchViewHolder(view, m_eventListener);
    }

    @Override
    public void onBindViewHolder(final SearchViewHolder holder, int position) {
        //set the event details
        holder.m_name.setText(m_events.get(position).getName());
        holder.m_category.setText(m_events.get(position).getCategory());

        //get the eventId of the event
        String id = m_events.get(position).getEventId();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("events").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("eventImage").getValue().toString();
                try {
                    holder.m_image.setImageBitmap(decodeFromFirebaseBase64(image));
                }catch (IOException e){
                    e.printStackTrace();
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
     * Method to return number of events
     * @return: number of events
     */
    @Override
    public int getItemCount() {
        return m_events.size();
    }


    //search view holder class
    class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //event name
        private TextView m_name = null;
        //event category
        private TextView m_category = null;
        //event image
        private ImageView m_image = null;
        //on event click listener
        private EventActionListener m_listener = null;

        private SearchViewHolder(View itemView, EventActionListener eventActionListener) {
            super(itemView);

            //inflate widgets
            m_name = (TextView) itemView.findViewById(R.id.eventname);
            m_category = (TextView) itemView.findViewById(R.id.eventcategory);
            m_image = (ImageView) itemView.findViewById(R.id.eventimage);

            m_listener = eventActionListener;

            //set onclick listener
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //call our on click listener
            m_listener.onEventClick(getAdapterPosition());
        }
    }

    /**
     * Interface for all event list actions
     */
    public interface EventActionListener{
        /** on event click */
        void onEventClick(int position);
    }
}
