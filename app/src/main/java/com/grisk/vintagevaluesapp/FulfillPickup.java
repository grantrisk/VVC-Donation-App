package com.grisk.vintagevaluesapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FulfillPickup extends AppCompatActivity {

    public static final String REQUESTS = "requests";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private RequestRecyclerAdapter mAdapter;
    private String Uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfill_pickup);

        // Recycler View
        RecyclerView recyclerView = findViewById(R.id.fulfill_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getUid();

        // Firestore Database sorts data by time stamp
        // Find requests that have not been accepted and is not from the current user
        Query query = mDb.collection(REQUESTS)
                .whereEqualTo("requestAccepted", false)
                .whereNotEqualTo("uid", Uid);
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
                    Intent intent = new Intent(getBaseContext(), FulfillPickupDetail.class);
                    intent.putExtra(FulfillPickupDetail.DOCID, docID);
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
}