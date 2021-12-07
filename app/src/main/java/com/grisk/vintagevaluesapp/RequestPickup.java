package com.grisk.vintagevaluesapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class RequestPickup extends AppCompatActivity {

    public static final String REQUESTS = "requests";
    private static final String TAG = "DisplayActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private RequestRecyclerAdapter mAdapter;
    private FirebaseUser currentUser;
    private String Uid;

    private EditText eFirstName;
    private EditText eLastName;
    private EditText eBags;
    private EditText eLocationDescription;

    private String sFirstName;
    private String sLastName;
    private String sBags;
    private String sLocationDescription;
    private int iBags;

    private ImageView mImageThumbnail;
    private Button submitButton;

    // Upload images from phone storage to the database
    ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    // result will be the file path that the user clicked on
                    if(result != null) {
                        createPictureFromUri(result);
                    }
                }
            }
    );

    private void createPictureFromUri(Uri result){
        ContentResolver cr = getBaseContext().getContentResolver();
        String imageType = cr.getType(result).split("/")[1]; // this will tell us if it is png, jpeg, ... (image/jpeg) and splits the string at "/"

        // Set thumbnail as picture selected
        mImageThumbnail.setImageURI(result);
        // Whenever the user presses the submit button, the image will also be uploaded
        submitButton.setOnClickListener(view -> {

            // We need an id for the picture
            String name = UUID.randomUUID().toString();

            BagPicture newBagPicture = new BagPicture(name, "images/"+name+"."+imageType);
            mDb.collection("bag_pictures").add(newBagPicture)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "Picture successfully added!");
                            mStorageRef.child(newBagPicture.getImageFile())
                                    .putFile(result)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Toast.makeText(RequestPickup.this,
                                                    "New picture saved!",
                                                    Toast.LENGTH_LONG).show();
                                            // Submit rest of fields data to Firestore
                                            addRequest();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RequestPickup.this,
                                                "Error: Could not save the picture!",
                                                Toast.LENGTH_LONG).show();
                                        documentReference.delete();
                                    });
                        }
                    });

        });


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_pickup);

        // Find picture buttons
        mImageThumbnail = findViewById(R.id.thumbnail);

        // Get submit button to make a listener to uplaod images
        submitButton = findViewById(R.id.submit_button);

        // Gallery button
        ImageButton galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v->{
            getContent.launch("image/*"); //MINE type declared in string format
        });

        // Allows us to use camera plus files_path.xml and manifest paths and store files
        File imageFile = new File(getFilesDir(), "my_images");
        Uri uri = FileProvider.getUriForFile(this, "com.grisk.vintagevaluesapp.fileprovider", imageFile);

        // Use camera to take picture
        ActivityResultLauncher<Uri> takePicture = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        // picture has been taken
                        if (result == true){
                            createPictureFromUri(uri);
                        }
                    }
                }
        );

        // Camera button
        ImageButton cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture.launch(uri);
            }
        });





        // Find edit text fields
        eFirstName = findViewById(R.id.firstNameEditText);
        eLastName = findViewById(R.id.lastNameEditText);
        eBags = findViewById(R.id.bagsEditText);
        eLocationDescription = findViewById(R.id.locationDescription);


        // Recycler View
        RecyclerView recyclerView = findViewById(R.id.request_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Uid = currentUser.getUid();

        // Firestore Database sorts data by Uid (if the Uid matches the Uid in Firestore then it will display
        Query query = mDb.collection(REQUESTS)
                .whereEqualTo("uid", Uid);
        FirestoreRecyclerOptions<Request> options = new FirestoreRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class)
                .build();


        // Setup recycler listener
        mAdapter = new RequestRecyclerAdapter(options, new RequestRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // position can be -1 if the user clicks repeatedly super fast.
                if (position >= 0) {
                    String id = mAdapter.getSnapshots().getSnapshot(position).getId();

                    // Gets document id
                    String docID = mDb.collection(REQUESTS).document(id).getId();

                    // Launch RequestDetailActivity when clicking card
                    Intent intent = new Intent(getBaseContext(), RequestDetailActivity.class);
                    intent.putExtra(RequestDetailActivity.DOCID, docID);
                    startActivity(intent);

                }
            }
        });

        // Update recycler with any data from Firestore
        recyclerView.setAdapter(mAdapter);

        // This prevents the app from crashing when returning to this activity from RequestDetailActivity
        recyclerView.setItemAnimator(null);

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    // If the text fields are filled correctly, this returns true
    private boolean validateForm() {
        boolean valid = true;

        // First Name
        if (TextUtils.isEmpty(sFirstName)) {
            eFirstName.setError("Required");
            valid = false;
        } else {
            eFirstName.setError(null);
        }

        // Last Name
        if (TextUtils.isEmpty(sLastName)) {
            eLastName.setError("Required");
            valid = false;
        } else {
            eLastName.setError(null);
        }


        // Location
        if (TextUtils.isEmpty(sLocationDescription)) {
            eLocationDescription.setError("Please Enter Description");
            valid = false;
        } else {
            eLocationDescription.setError(null);
        }

        // Check to make sure there are ONLY numbers in this text entry, made last to allow for other errors to be present
        try {
            iBags = Integer.parseInt(sBags);
        } catch (NumberFormatException ex) {
            eBags.setError("Please Enter Only Numbers");
            return false;
        }

        // Bags
        if (TextUtils.isEmpty(sBags)) {
            eBags.setError("Please Enter Number of Donation Bags");
            valid = false;
        } else if (iBags > 10 || iBags < 1) {
            eBags.setError("Bags Must Be Greater Than 0 And Less Than 10");
            valid = false;
        } else {
            eBags.setError(null);
        }

        return valid;
    }


    private void addRequest() {

        sFirstName = eFirstName.getText().toString();
        sLastName = eLastName.getText().toString();
        sLocationDescription = eLocationDescription.getText().toString();
        sBags = eBags.getText().toString();

        if (!validateForm()) {
            return;
        }


        Request newRequest = new Request(Uid, sFirstName, sLastName, sBags, new Date(), sLocationDescription);

        mDb.collection(REQUESTS)
                .add(newRequest)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Successfully adding " + sFirstName + " " + sLastName, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to add " + sFirstName + " " + sLastName, Toast.LENGTH_LONG).show();
                    }
                });

        // Clear out text fields
        eFirstName.setText("");
        eLastName.setText("");
        eBags.setText("");
        eLocationDescription.setText("");

    }

}
