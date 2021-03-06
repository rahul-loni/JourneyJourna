package com.example.journey_journal;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.journey_journal.db.DbHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class EditJourney extends AppCompatActivity {
    public static final String TAG = EditJourney.class.getSimpleName();
    public static final String ID = "Id";
    public static final int PICK_IMAGE = 100;

    int id = 0;
    ModelClass journey;
    Bundle intentData;
    DbHelper dbHelper;
    Uri imageUri;

    TextView txtTitle, txtDesc;
    ImageView imShowImage, imUploadImage, imAddLocation;
    Button btnSubmit;

    public static Intent getIntent(Context context, int id) {
        Intent intent = new Intent(context, EditJourney.class);
        intent.putExtra(ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journey);
        // init object fields
        dbHelper = new DbHelper(this);
        // retrieve intent data
        intentData = getIntent().getExtras();

        // init views
        txtTitle = findViewById(R.id.txtTitle);
        txtDesc = findViewById(R.id.txtDesc);
        imShowImage = findViewById(R.id.showImage);
        imUploadImage = findViewById(R.id.imUploadImage);
        imAddLocation = findViewById(R.id.imAddLocation);
        btnSubmit = findViewById(R.id.btn_submit);

        // setup click listeners
        // Here, listeners are function with specific signature or simply - structure.
        // These methods are called method reference which it is as its name applies;
        // means methods are not called here, their names are simply referenced respectively.
        imUploadImage.setOnClickListener(this::promptToChooseImage);
        imAddLocation.setOnClickListener(this::promptToAddLocation);
        btnSubmit.setOnClickListener(this::onSubmitButtonClick);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        populateData();
    }

    // check journey object, fetch if it is null and populate the views
    private void populateData() {
        id = intentData.getInt(ID);
        Log.d(TAG, "ID=" + id);

        //if journey is null, fetch data
        if (journey == null) {
            Cursor cursor = dbHelper.getElementById(id);
            if (cursor != null) {
                cursor.moveToFirst();
                journey = new ModelClass(
                        id,
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getBlob(4)
                );
            } else {
                Toast.makeText(getApplicationContext(), "Could not load data",
                        Toast.LENGTH_SHORT).show();
            }
        }
        // populate views
        txtTitle.setText(journey.getJTitle());
        // txtLocation.setText(journey.getJLocation());
        txtDesc.setText(journey.getJDis());
        // if imageUri is obtained run this block
        if (imageUri == null){
            byte[] imgBlob = journey.getImage();
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgBlob, 0, imgBlob.length);
            imShowImage.setImageBitmap(bitmap);
        }
    }

    private void onSubmitButtonClick(View view) {
        String title = txtTitle.getText().toString();
        Log.d(TAG, "Title=" + title);
        String desc = txtDesc.getText().toString();
        Log.d(TAG, "Description=" + desc);
        String location = "Hard Coded Location";
        Log.d(TAG, "Location=" + location);
        byte[] imgBlob = imageViewToByte(imShowImage);
        Log.d(TAG, "Image Blob=" + Arrays.toString(imgBlob));
        if (validateData(title, desc, location, imgBlob)) {
            saveEditData(title, desc, location, imgBlob);
        }
    }

    // validate
    private boolean validateData(String title, String desc, String location, byte[] imageBlob) {
        if (title.isEmpty()) {
            txtTitle.setError("Title is required!");
            txtTitle.requestFocus();
            return false;
        }
        if (desc.isEmpty()) {
            txtDesc.setError("Title is required!");
            txtDesc.requestFocus();
            return false;
        }
        if (location.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please choose a location",
                    Toast.LENGTH_SHORT).show();
        }
        if (imageBlob.length == 0) {
            Toast.makeText(getApplicationContext(), "Please select an image with valid format!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveEditData(String title, String desc, String location, byte[] imageBlob) {
        int id = getIntent().getExtras().getInt(ID);
        if (dbHelper.update(
                id,
                title,
                desc,
                location,
                imageBlob
        )) {
            Toast.makeText(getApplicationContext(), "Updated Successfully",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("IntentReset")
    private void promptToChooseImage(View view) {
        // open gallery to select image
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    private void promptToAddLocation(View view) {
        // open map to select location
        Toast.makeText(getApplicationContext(), "Coming Soon",
                Toast.LENGTH_SHORT).show();
    }


    /*
     * Override onActivityResult method: it checks if this activity
     * was paused requesting any type of result from other activities
     * by matching its request code with a known request code of this activity.
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            try {
                // create a buffer from the image at given uri
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                // decode buffer into bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                // set bitmap for the ImageView
                imShowImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Could not pick image!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Convert image drawable in an imageview to byte[]
     * Takes ImageView object as param
     * */
    public static byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}