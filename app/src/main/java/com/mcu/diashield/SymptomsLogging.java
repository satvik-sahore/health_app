package com.mcu.diashield;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class SymptomsLogging extends AppCompatActivity {
    Database dbHelper = new Database(this);
    Button symptomUpload;
    RatingBar ratingBar;
    Spinner symptomList;
    ArrayList<String> symptomArr;
    float[] rating;
    CustomAdaptor c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange)));

        initializeViews();

        symptomUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Insert symptoms and ratings into the database
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("nausea", rating[0]);
                values.put("headache", rating[1]);
                values.put("diarrhea", rating[2]);
                values.put("sore_throat", rating[3]);
                values.put("fever", rating[4]);
                values.put("muscle_ache", rating[5]);
                values.put("loss_of_smell_or_taste", rating[6]);
                values.put("cough", rating[7]);
                values.put("shortness_of_breath", rating[8]);
                values.put("feeling_tired", rating[9]);

                long rowId = db.insert("symptoms_ratings", null, values);
                if (rowId != -1) {
                    // Insert successful
                    Toast.makeText(SymptomsLogging.this, "Symptoms and ratings saved successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    // Insert failed
                    Toast.makeText(SymptomsLogging.this, "Failed to save symptoms and ratings.", Toast.LENGTH_SHORT).show();
                }

                // Close the database
                db.close();

                // Navigate back to the main activity or perform any other desired action
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        c = new CustomAdaptor(symptomArr,getApplicationContext(),rating);
        symptomList.setAdapter(c);
    }

    @Override
    protected void onResume() {
        super.onResume();
        c = new CustomAdaptor(symptomArr,getApplicationContext(),rating);
        symptomList.setAdapter(c);
    }

    private void initializeViews()
    {
        symptomUpload = findViewById(R.id.upload_symptoms);
        ratingBar = findViewById(R.id.ratingBar);
        symptomList = findViewById(R.id.symptoms_list);
        if(rating == null)
            rating = getIntent().getExtras().getFloatArray("rating");
        Log.d("satvik","in 2nd page");
        loadSpinner();
    }

    private void loadSpinner()
    {

        // Load symptoms
        if(symptomArr == null) {
            symptomArr = new ArrayList<>();
            symptomArr.add("Nausea");
            symptomArr.add("Headache");
            symptomArr.add("Diarrhea");
            symptomArr.add("Soar Throat");
            symptomArr.add("Fever");
            symptomArr.add("Muscle Ache");
            symptomArr.add("Loss of smell or taste");
            symptomArr.add("Cough");
            symptomArr.add("Shortness of Breath");
            symptomArr.add("Feeling Tired");
        }

        c =new CustomAdaptor(symptomArr,getApplicationContext(),rating);
        rating = c.getRating();
        symptomList.setAdapter(c);
    }
}