package com.mcu.diashield;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class SymptomsLogging extends AppCompatActivity {
    Button sym_u2;
    RatingBar ratingBar;
    Spinner symptom_list;
    ArrayList<String> symps;
    float[] rating;
    CustomAdaptor c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);
        initialize_views();

        sym_u2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* two tasks
                2. Get back to main page after giving confirmation of database entry
                 */

                //2.  Get back to main page after giving confirmation of database entry
                Toast.makeText(SymptomsLogging.this, "Succesfull datase Entry", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                Output.message(getApplicationContext(),""+rating[0]+" size = "+rating.length);
                intent.putExtra("rating_r",rating);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        c = new CustomAdaptor(symps,getApplicationContext(),rating);
        symptom_list.setAdapter(c);
    }

    @Override
    protected void onResume() {
        super.onResume();
        c = new CustomAdaptor(symps,getApplicationContext(),rating);
        symptom_list.setAdapter(c);
    }

    private void initialize_views()
    {
        sym_u2 = findViewById(R.id.upload_symptoms);
        ratingBar = findViewById(R.id.ratingBar);
        symptom_list = findViewById(R.id.symptoms_list);
        if(rating == null)
            rating = getIntent().getExtras().getFloatArray("rating");
        Log.d("sarthak","in 2nd page");
        load_spinner();
    }

    private void load_spinner()
    {

        //load the symptoms
        if(symps == null) {
            symps = new ArrayList<>();
            symps.add("Nausea");
            symps.add("Headache");
            symps.add("Diarrhea");
            symps.add("Soar Throat");
            symps.add("Fever");
            symps.add("Muscle Ache");
            symps.add("Loss of smell or taste");
            symps.add("Cough");
            symps.add("Shortness of Breath");
            symps.add("Feeling Tired");
        }



        c =new CustomAdaptor(symps,getApplicationContext(),rating);
        rating = c.getRating();
        Output.message(getApplicationContext(),""+rating[0]);
        symptom_list.setAdapter(c);
    }



}