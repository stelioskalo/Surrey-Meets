package sep_group_7.SurreyMeets;
/**
 * Last edited by Stelios at 05/03/2019
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;
import static java.lang.Boolean.TRUE;


/**
 * Class extending Fragment representing the AddEvent page
 */
public class AddEvent extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "AddEventFragment";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_LOCATION = 2;
    private static DecimalFormat df2 = new DecimalFormat(".##");


    View m_View;
    private EditText nameEditText = null;
    private EditText descEditText = null;
    private EditText noOfParticipantsEditText = null;
    private EditText locationEditText = null;
    private Button dateButton = null;
    private Button timeButton = null;
    private Spinner categoriesSpinner = null;
    private Button createButton = null;
    private ImageView addImageView = null;
    private StorageTask mUploadTask;
    private StorageReference fileReference;
    private TextView showDate;
    private TextView showTime;

    private String name;
    private String desc;
    private int noOfParticipants;
    private String category;
    private double latitude;
    private double longitude;
    private Bitmap img = null;
    private Uri mImageUri;
    private String imgUrl;
    public static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private int mYear,mMonth,mDay,mHour,mMinute;
    String eventID = null;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    //access our server for the app
    StorageReference storageRef = storage.getReferenceFromUrl("gs://surreymeets-6a316.appspot.com");
    //get a reference to the location we want to save our data and name it appropriately
    StorageReference imagesRef = null;
    Uri downloadUrl;


    int year, month, day, hour, minute;

    // current user logged in
    private FirebaseUser m_user = null;
    // firebase auth object used for the database
    private FirebaseAuth m_auth = null;

    public AddEvent() {
        Log.d(TAG, "Constructor");
    }

    /**
     * onCreateView will inflate the view with the fragment_addevent layout
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        askPermissionStorage();
        // get instance of firebase auth
        m_auth = FirebaseAuth.getInstance();
        // get current logged in user
        m_user = m_auth.getCurrentUser();

        // Inflate the layout for this fragment
        m_View = inflater.inflate(R.layout.fragment_addevent, container, false);

        nameEditText = m_View.findViewById(R.id.editTextName);
        descEditText = m_View.findViewById(R.id.editTextDesc);
        noOfParticipantsEditText = m_View.findViewById(R.id.editTextParticipants);
        locationEditText = m_View.findViewById(R.id.editTextLocation);
        dateButton = m_View.findViewById(R.id.btnSetDate);
        timeButton = m_View.findViewById(R.id.btnSetTime);
        createButton = m_View.findViewById(R.id.btnAddEvent);
        categoriesSpinner = m_View.findViewById(R.id.spinnerCategory);
        addImageView = m_View.findViewById(R.id.addEventImageView);
        showDate = m_View.findViewById(R.id.setDate);
        showTime = m_View.findViewById(R.id.setTime);

        createButton.setOnClickListener(this);
        dateButton.setOnClickListener(this);
        timeButton.setOnClickListener(this);
        addImageView.setOnClickListener(this);
        setSpinner();

        //Check permsisions for the Camera
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        }

        locationEditText.setInputType(InputType.TYPE_NULL);
        locationEditText.setOnClickListener(this);
        //First click on editText is captured by the onFocusChange method
        locationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    Log.d(TAG, "locationEditText onFocusChange ");
                    setupMap();
                }
            }
        });
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return m_View;
    }

    private void setupMap() {
        Intent mapIntent = new Intent(getActivity(), MapContainer.class);
        startActivityForResult(mapIntent, REQUEST_LOCATION);
        mapIntent.putExtra("class", "add");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddEvent:
                if (
                        !noOfParticipantsEditText.getText().toString().matches("") && !locationEditText.getText().toString().matches("")
                                && img != null) {
                    Log.d(TAG, "btnAddEvent onClick");
                    name = nameEditText.getText().toString();
                    desc = descEditText.getText().toString();
                    noOfParticipants = Integer.parseInt(noOfParticipantsEditText.getText().toString());

                    //Do error check, make sure fields arent empty

                    //Note: the following runs on the assumption that all data is validated

                    //get a reference object to access database
                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

                    //generate a unique id for the event
                    eventID = RNG.generateUID();

                    //push all relevant data to the database
                    ref.child("events").child(eventID).child("name").setValue(name);
                    ref.child("events").child(eventID).child("desc").setValue(desc);
                    ref.child("events").child(eventID).child("participants").setValue(noOfParticipants);

                    //note: when a new event is created, nobody has joined yet
                    ref.child("events").child(eventID).child("current_participants").setValue(0);
                    final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    ref.child("events").child(eventID).child("participant").child(currentFirebaseUser.getUid()).setValue(currentFirebaseUser.getUid());
                    ref.child("events").child(eventID).child("current_participants").setValue(1);
                    ref.child("events").child(eventID).child("category").setValue(category);
                    ref.child("events").child(eventID).child("latitude").setValue(latitude);
                    ref.child("events").child(eventID).child("longitude").setValue(longitude);
                    ref.child("events").child(eventID).child("owner").setValue(m_user.getUid());

                    //convert date and time into a suitable form for database entry
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.DAY_OF_MONTH, day);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.HOUR_OF_DAY, hour);
                    c.set(Calendar.MINUTE, minute);

                    String dateAndTime = c.getTime().toString();

                    //push date and time data to database
                    ref.child("events").child(eventID).child("dateAndTime").setValue(dateAndTime);
                    ref.child("events").child(eventID).child("eventId").setValue(eventID);


                    //object to convert image to byte array for storage on firebase
                    ByteArrayOutputStream imgConverted = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.JPEG, 100,imgConverted);
                    //save the image as a .png file
                    String imageEncoded = Base64.encodeToString(imgConverted.toByteArray(), Base64.DEFAULT);
                    ref.child("events").child(eventID).child("eventImage").setValue(imageEncoded);

                    ref.child("user").child(currentFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            ref.child("user").child(currentFirebaseUser.getUid()).child("events_hosted").setValue(user.getEvents_hosted() + 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });




                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), MenuContainer.class);
                            startActivity(intent);
                        }
                    }, 2000);
                } else if (!nameEditText.getText().toString().matches("(^[A-Za-z][A-Za-z0-9]*)") ) {
                    new android.support.v7.app.AlertDialog.Builder(getActivity())
                            .setTitle("Invalid name")
                            .setMessage("Name should start with capital letter and can only include numbers and " +
                                    "letters")
                            .setPositiveButton("OK", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }else if(!descEditText.getText().toString().matches("^(.|\\s)*[a-zA-Z0-9]+(.|\\s)*$") ){
                    new android.support.v7.app.AlertDialog.Builder(getActivity())
                            .setTitle("Invalid Description")
                            .setMessage("Description containts some characters that are not allowed" )
                            .setPositiveButton("OK", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else {
                    new android.support.v7.app.AlertDialog.Builder(getActivity())
                            .setTitle("Empty Fields")
                            .setMessage("All fields must be filled")
                            .setPositiveButton("OK", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                //TODO RESET FIELDS TO EMPTY OR SWITCH PAGE HERE

                break;
            case R.id.addEventImageView:
                Log.d(TAG, "addEventPic onClick");
                takePicture();
                break;
            case R.id.btnSetDate:
                Log.d(TAG, "btnSetDate onClick");
                setupDatePicker();
                break;
            case R.id.btnSetTime:
                Log.d(TAG, "btnSetTime onClick");
                setupTimePicker();
                break;
            case R.id.editTextLocation:
                Log.d(TAG, "editTextLocation onClick");
                setupMap();


        }

    }

    private void takePicture() {
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == false) {
            Toast.makeText(getActivity(), "This device does not have a camera.", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            img = (Bitmap) data.getExtras().get("data");
            mImageUri = getImageUri(getActivity().getApplicationContext(), img);
            addImageView.setImageBitmap(img);

        }
        if (requestCode == REQUEST_LOCATION && resultCode == RESULT_OK) {
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);

            String locationText = df2.format(latitude) + ", " + df2.format(longitude);

            locationEditText.setText(locationText);
        }

    }

    private void setupDatePicker() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDateSetListener = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int yearOfEvent,
                                  int monthOfYear, int dayOfMonth) {
                Log.d(TAG, "onDateSet");
                year = yearOfEvent;
                month = monthOfYear;
                day = dayOfMonth;

                Calendar myCalendar = Calendar.getInstance();
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.YEAR, year);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                showDate.setText(sdf.format(myCalendar.getTime()));
                dateButton.setText("Change the date");
            }
        },mYear,mMonth,mDay);
        mDateSetListener.show();
    }

    private void setupTimePicker() {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog mTimeSetListener = new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {
                Log.d(TAG, "onTimeSet");
                hour = hourOfDay;
                minute = minuteOfDay;
                Calendar myCalendar = Calendar.getInstance();
                myCalendar.set(Calendar.HOUR_OF_DAY, hour);
                myCalendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                String date = sdf.format(myCalendar.getTimeInMillis());
                showTime.setText(date);
                timeButton.setText("Change the time");
            }
        },mHour,mMinute,false);
        mTimeSetListener.show();
    }

    private void setSpinner() {
        Log.d(TAG, "setSpinner");
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        categoriesSpinner.setAdapter(adapter);
        categoriesSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        Log.d(TAG, "onItemSelected");
        //An item in spinner was selected
        category = (String) adapterView.getItemAtPosition(pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d(TAG, "onNothingSelected");
    }

    private String getFileType(Uri uri) {
        MimeTypeMap m = MimeTypeMap.getSingleton();
        ContentResolver c = getActivity().getApplication().getContentResolver();
        return m.getExtensionFromMimeType(c.getType(uri));
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void askPermissionStorage() {
        //for media
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            //insert explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //if you want to explaian to the user

            } else {//if no explanation needed
                ActivityCompat.requestPermissions(getActivity(), new
                                String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            }
        }
    }
}
