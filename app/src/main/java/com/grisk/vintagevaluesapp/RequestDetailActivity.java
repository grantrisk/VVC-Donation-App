package com.grisk.vintagevaluesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RequestDetailActivity extends AppCompatActivity {

    public static final String DOCID = "docID";
    private String docID;
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private Request request;
    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy", Locale.US);

    private TextView name;
    private TextView bags;
    private TextView description;
    private TextView timeStamp;

    private boolean editReceiptButton;
    private ConstraintLayout mEditGroup;
    private ConstraintLayout mNotEditGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        // Initialize the edit button to false (not in edit mode)
        editReceiptButton = false;
        mEditGroup = findViewById(R.id.edit_group);
        mNotEditGroup = findViewById(R.id.not_edit_group);
        mEditGroup.setVisibility(View.GONE);
        mNotEditGroup.setVisibility(View.VISIBLE);

        name = findViewById(R.id.not_name_label_items);
        bags = findViewById(R.id.not_bags_label_items);
        description = findViewById(R.id.not_location_description_label_items);
        timeStamp = findViewById(R.id.not_time_stamp_label_item);


        Intent intent = getIntent();
        docID = intent.getStringExtra(DOCID);

        DocumentReference docRef = mDb.collection(RequestPickup.REQUESTS).document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                request = documentSnapshot.toObject(Request.class);
                Log.d("Test", request.getFirst());

                String fullName = request.getFirst() + " " + request.getLast();

                name.setText(fullName);
                bags.setText(request.getBags());
                description.setText(request.getPickupDescription());
                timeStamp.setText(format.format(request.getCreatedTime()));

            }
        });
    }
/*
    TODO:
    -add edit text to editing constraint layout
    -fill in edit text when editing from old input
    -add valid form
    -once validated, "put" to firestore
    -make sure it is writing over that request document

*/
    public void editRequest(View view){

        // Updates the UI when the user wants to edit their receipt
        if (editReceiptButton) {
            mEditGroup.setVisibility(View.GONE);
            mNotEditGroup.setVisibility(View.VISIBLE);
            editReceiptButton = false;
        } else {
            mNotEditGroup.setVisibility(View.GONE);
            mEditGroup.setVisibility(View.VISIBLE);
            editReceiptButton = true;
        }
    }

}