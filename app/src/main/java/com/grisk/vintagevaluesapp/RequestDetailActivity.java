package com.grisk.vintagevaluesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RequestDetailActivity extends AppCompatActivity {

    public static final String DOCID = "docID";
    private String docID;
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private Request request;
    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy", Locale.US);

    private TextView name;
    private TextView bags;
    private TextView description;
    private TextView timeStamp;
    private ImageView imageView;

    private boolean editReceiptButton;
    private ConstraintLayout mEditGroup;
    private ConstraintLayout mNotEditGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);


        // Initialize the edit button to false (not in edit mode)
        mEditGroup = findViewById(R.id.edit_group);
        mNotEditGroup = findViewById(R.id.not_edit_group);
        mEditGroup.setVisibility(View.GONE);
        mNotEditGroup.setVisibility(View.VISIBLE);


        name = findViewById(R.id.not_name_label_items);
        bags = findViewById(R.id.not_bags_label_items);
        description = findViewById(R.id.not_location_description_label_items);
        timeStamp = findViewById(R.id.not_time_stamp_label_item);
        imageView = findViewById(R.id.bag_image);


        Intent intent = getIntent();
        docID = intent.getStringExtra(DOCID);

        DocumentReference docRef = mDb.collection(RequestPickup.REQUESTS).document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                request = documentSnapshot.toObject(Request.class);

                String fullName = request.getFirst() + " " + request.getLast();
                name.setText(fullName);

                bags.setText(request.getBags());
                description.setText(request.getPickupDescription());
                timeStamp.setText(format.format(request.getCreatedTime()));

                String imageFileLocation = request.getImageFile();
                StorageReference image = mStorageRef.child(imageFileLocation);

                // Glide is a 3rd party library that simplifies image downloading, caching,
                // and injection into your UI. This tells Glide to load the image from Storage and
                // put it in the ImageView when complete.
                GlideApp.with(RequestDetailActivity.this)
                        .load(image)
                        .into(imageView);

                // set time stamp to both views
                TextView otherTime = findViewById(R.id.yes_time_stamp_label_item);
                otherTime.setText(timeStamp.getText().toString());

            }
        });

    }

    public void onDeleteRequest (View view){

        // Delete the request receipt
        mDb.collection(RequestPickup.REQUESTS).document(docID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error deleting document", e);
                    }
                });

        // Go back to previous page
        finish();
    }


    // Edit request button used to activate editing the request receipt
    public void editRequest(View view) {

        // Updates the UI when the user wants to edit their receipt
        if (editReceiptButton) {
            // Gets called second to validate form, write data, and save changes

            // Make sure edittext fields are valid and remove any unnecessary white space in name
            if (!validateForm()) {
                return;
            }

            // Write new data to DB
            DocumentReference docReference = mDb.collection(RequestPickup.REQUESTS).document(docID);
            docReference.update(
                    "bags", bagsValue,
                    "pickupDescription", locationValue,
                    "first", firstNameValue,
                    "last", lastNameValue
            ).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getApplicationContext(), "Saved Changes", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Changes were not saved. Retry later.", Toast.LENGTH_SHORT).show();
                }
            });

            // Store new data in text fields
            name.setText(firstNameValue + " " + lastNameValue);
            bags.setText(bagsValue);
            description.setText(locationValue);

            // Switching view back to normal display
            editReceiptButton = false;
            mEditGroup.setVisibility(View.GONE);
            mNotEditGroup.setVisibility(View.VISIBLE);


        } else {

            // Gets called first to switch into edit mode (edit display)
            editReceiptButton = true;
            mNotEditGroup.setVisibility(View.GONE);
            populateEditFields();
            mEditGroup.setVisibility(View.VISIBLE);

        }
    }


    // String of values in DB
    String firstNameValue;
    String lastNameValue;
    String bagsValue;
    String locationValue;

    EditText firstNameSet;
    EditText lastNameSet;
    EditText bagsSet;
    EditText locationSet;

    // integer value of the string in bags text field
    int iBags;

    private void populateEditFields() {
        // Pull values from text fields
        String nameValue = name.getText().toString();
        String[] split = nameValue.split(" ");
        firstNameValue = split[0];
        lastNameValue = split[1];

        bagsValue = bags.getText().toString();
        locationValue = description.getText().toString();

        // Set those text values to the edit text fields
        firstNameSet = findViewById(R.id.yes_name_label_items);
        firstNameSet.setText(firstNameValue);
        lastNameSet = findViewById(R.id.yes_last_name_label_items);
        lastNameSet.setText(lastNameValue);

        bagsSet = findViewById(R.id.yes_bags_label_items);
        bagsSet.setText(bagsValue);

        locationSet = findViewById(R.id.yes_location_description_label_items);
        locationSet.setText(locationValue);

    }

    private boolean validateForm() {
        boolean valid = true;

        firstNameValue = firstNameSet.getText().toString().trim();
        lastNameValue = lastNameSet.getText().toString().trim();
        bagsValue = bagsSet.getText().toString();
        locationValue = locationSet.getText().toString();


        // First Name
        if (TextUtils.isEmpty(firstNameValue)) {
            firstNameSet.setError("Required");
            valid = false;
        } else {
            firstNameSet.setError(null);
            firstNameValue.trim();
        }

        // Last Name
        if (TextUtils.isEmpty(lastNameValue)) {
            lastNameSet.setError("Required");
            valid = false;
        } else {
            lastNameSet.setError(null);
            lastNameValue.trim();
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
        } catch (NumberFormatException ex) {
            bagsSet.setError("Please Enter Only Numbers");
            return false;
        }

        // Bags
        if (TextUtils.isEmpty(bagsValue)) {
            bagsSet.setError("Please Enter Number of Donation Bags");
            valid = false;
        } else if (iBags > 10 || iBags < 1) {
            bagsSet.setError("Bags Must Be Greater Than 0 And Less Than 10");
            valid = false;
        } else {
            bagsSet.setError(null);
        }

        return valid;
    }

}
