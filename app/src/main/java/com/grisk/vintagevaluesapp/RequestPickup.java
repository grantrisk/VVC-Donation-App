package com.grisk.vintagevaluesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.Date;

public class RequestPickup extends AppCompatActivity {

    public static final String REQUESTS = "requests";
    private static final String TAG = "DisplayActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private RequestRecyclerAdapter mAdapter;
    private FirebaseUser currentUser;

    private EditText eFirstName;
    private EditText eLastName;
    private EditText eBags;
    private EditText eLocationDescription;

    private String sFirstName;
    private String sLastName;
    private String sBags;
    private String sLocationDescription;
    private int iBags;

    private String Uid;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_pickup);

        eFirstName = findViewById(R.id.firstNameEditText);
        eLastName = findViewById(R.id.lastNameEditText);
        eBags = findViewById(R.id.bagsEditText);
        eLocationDescription = findViewById(R.id.locationDescription);


        // Recycler View
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
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
        // Allow for deletion
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

                    // Make sure user can't come back to another request activity, this makes sure when
                    // the user deletes a receipt that they don't return to old request pickup activity
                    // (they will return to the MainActivity)
                    //finish();
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

    public void addRequest(View v) {

        sFirstName = eFirstName.getText().toString();
        sLastName = eLastName.getText().toString();
        sLocationDescription = eLocationDescription.getText().toString();
        sBags = eBags.getText().toString();

        if (!validateForm()) {
            return;
        }


        Request newRequest = new Request(Uid, sFirstName, sLastName, sBags, new Date(), sLocationDescription);

        Toast.makeText(this, "Adding " + sFirstName + " " + sLastName, Toast.LENGTH_LONG).show();
        mDb.collection(REQUESTS)
                .add(newRequest)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "User added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding user", e);
                    }
                });

    }

}
