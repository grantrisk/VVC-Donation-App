package com.grisk.vintagevaluesapp;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RequestPickup extends AppCompatActivity {

    private static final String REQUESTS = "requests";
    private static final String USERREQUESTS = "user_requests";
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

        // Firestore Database
        Query query = mDb.collection(REQUESTS)
                .orderBy("createdTime", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Request> options = new FirestoreRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class)
                .build();


        // Setup recycler listener
        mAdapter = new RequestRecyclerAdapter(options, new RequestRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // position can be -1 if the user clicks repeatedly super fast.
                if(position >= 0) {
                    Request request = mAdapter.getSnapshots().getSnapshot(position).toObject(Request.class);
                    String id = mAdapter.getSnapshots().getSnapshot(position).getId();
                    mDb.collection(REQUESTS).document(id).delete();

                    Toast.makeText(getApplicationContext(), "Deleting " + request.getFirst(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update recycler with any data from Firestore
        recyclerView.setAdapter(mAdapter);

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

        if (TextUtils.isEmpty(sFirstName)) {
            eFirstName.setError("Required");
            valid = false;
        } else {
            eFirstName.setError(null);
        }

        if (TextUtils.isEmpty(sLastName)) {
            eLastName.setError("Required");
            valid = false;
        } else {
            eLastName.setError(null);
        }

        if (TextUtils.isEmpty(sBags)) {
            eBags.setError("Please Enter Number of Donation Bags");
            valid = false;
        }
        else if(iBags > 10 || iBags < 1){
            eBags.setError("Bags Must Be Greater Than 0 And Less Than 10");
            valid = false;
        }
        else {
            eBags.setError(null);
        }

        if (TextUtils.isEmpty(sLocationDescription)) {
            eLocationDescription.setError("Please Enter Description");
            valid = false;
        } else {
            eLocationDescription.setError(null);
        }

        return valid;
    }

    public void addRequest(View v) {

        sFirstName = eFirstName.getText().toString();
        sLastName = eLastName.getText().toString();
        sBags = eBags.getText().toString();
        sLocationDescription = eLocationDescription.getText().toString();
        iBags = Integer.parseInt(sBags);


        if (!validateForm()) {
            return;
        }

        Request newRequest = new Request(sFirstName, sLastName, sBags, new Date(), sLocationDescription);

        Toast.makeText(this, "Adding " + sFirstName + " " + sLastName, Toast.LENGTH_SHORT).show();
//        mDb.collection(REQUESTS)
//                .add(newRequest)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "User added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding user", e);
//                    }
//                });

        Log.d(TAG, Uid);

        mDb.collection(REQUESTS)
                .document(Uid)
                .set(newRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });;

    }

}
