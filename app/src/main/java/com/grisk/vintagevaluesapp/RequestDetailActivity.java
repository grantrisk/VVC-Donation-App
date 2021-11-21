package com.grisk.vintagevaluesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

                // set time stamp to both views
                TextView otherTime = findViewById(R.id.yes_time_stamp_label_item);
                otherTime.setText(timeStamp.getText().toString());

            }
        });


    }

    // Edit request button to activate editing the request receipt
    public void editRequest(View view){

        // Updates the UI when the user wants to edit their receipt
        if (editReceiptButton) {

            if (!validateForm()) {
                return;
            }

            // Gets called second to save changes
            editReceiptButton = false;
            mEditGroup.setVisibility(View.GONE);
            mNotEditGroup.setVisibility(View.VISIBLE);

        } else {
            // Gets called first to switch into edit mode
            editReceiptButton = true;
            mNotEditGroup.setVisibility(View.GONE);
            populateEditFields();
            mEditGroup.setVisibility(View.VISIBLE);
        }
    }



    String nameValue;
    String bagsValue;
    String locationValue;

    EditText nameSet;
    EditText bagsSet;
    EditText locationSet;

    // integer value of the string in bags text field
    int iBags;

    private void populateEditFields() {
        // Pull values from text fields
        nameValue = name.getText().toString();
        bagsValue = bags.getText().toString();
        locationValue = description.getText().toString();

        // Set those text values to the edit text fields
        nameSet = findViewById(R.id.yes_name_label_items);
        nameSet.setText(nameValue);

        bagsSet = findViewById(R.id.yes_bags_label_items);
        bagsSet.setText(bagsValue);

        locationSet = findViewById(R.id.yes_location_description_label_items);
        locationSet.setText(locationValue);

    }

    private boolean validateForm() {
        boolean valid = true;

        nameValue = nameSet.getText().toString();
        bagsValue = bagsSet.getText().toString();
        locationValue = locationSet.getText().toString();


        // Name
        if (TextUtils.isEmpty(nameValue)) {
            nameSet.setError("Required");
            valid = false;
        } else {
            nameSet.setError(null);
        }


        // Location
        if (TextUtils.isEmpty(locationValue)) {
            locationSet.setError("Please Enter Description");
            valid = false;
        } else {
            locationSet.setError(null);
        }

        // Check to make sure there are ONLY numbers in this text entry, made last to allow for other errors to be present
        try {
            iBags = Integer.parseInt(bagsValue);
        }
        catch (NumberFormatException ex){
            bagsSet.setError("Please Enter Only Numbers");
            return false;
        }

        // Bags
        if (TextUtils.isEmpty(bagsValue)) {
            bagsSet.setError("Please Enter Number of Donation Bags");
            valid = false;
        }
        else if(iBags > 10 || iBags < 1){
            bagsSet.setError("Bags Must Be Greater Than 0 And Less Than 10");
            valid = false;
        }
        else {
            bagsSet.setError(null);
        }

        return valid;
    }

}
    /*
        TODO:
(DONE)        -change text fields to edit fields
(DONE)        -fill in edit text when editing from old input
(DONE)        -add valid form
        -once validated, "put" to Firestore
        -make sure it is writing over that request document
        -save edit text fields when rotating screen

    */
