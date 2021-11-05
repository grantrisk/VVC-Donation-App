package com.grisk.vintagevaluesapp;

import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class RequestPickup extends AppCompatActivity {

    private static final String REQUESTS = "requests";
    private static final String TAG = "DisplayActivity";

    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private RequestRecyclerAdapter mAdapter;

    private EditText firstName;
    private EditText lastName;
    private EditText bags;
    private EditText locationDescription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_pickup);

        firstName = findViewById(R.id.firstNameEditText);
        lastName = findViewById(R.id.lastNameEditText);
        bags = findViewById(R.id.bagsEditText);
        locationDescription = findViewById(R.id.locationDescription);

        // Recycler View
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

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
//t
    public void addUser(View v) {
        String first = firstName.getText().toString();
        String last = lastName.getText().toString();
        String bags = this.bags.getText().toString();
        String description = locationDescription.getText().toString();

        Request newRequest = new Request(first, last, bags, new Date(), description);

        Toast.makeText(this, "Adding " + first + " " + last, Toast.LENGTH_SHORT).show();
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
