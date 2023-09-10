package com.mcu.diashield;
import android.content.Context;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorEventListener;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Button symptomButton, heartRateButton, repirationRateButton, uploadSignsButton;
    TextView heartRateTextView, respiratoryRateTextView;
    private static final int PERMISSION_SENSORS_REQUEST_CODE = 1;
    private static final int PERMISSION_STORAGE_REQUEST_CODE = 2;
    private Uri videoUri;
    private static final int VIDEO_CAPTURE = 101;
    float[] rating;
    private SensorManager sensorManager;
    private Sensor accSensor;
    private List<Float> accValX = new ArrayList<>();
    private List<Float> accValY = new ArrayList<>();
    private List<Float> accValZ = new ArrayList<>();
    private CameraManager camManager;
    private String camId;
    private boolean isTorchOn = false;
    private boolean isRecording = false;
    Database dbHelper = new Database(MainActivity.this);
    private ActivityResultLauncher<Intent> videoCaptureLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange)));

        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        dbHelper = new Database(this);

        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            for (String id : camManager.getCameraIdList()) {
                CameraCharacteristics characteristics = camManager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    camId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Check for and request the SENSOR permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BODY_SENSORS},
                        PERMISSION_SENSORS_REQUEST_CODE);
            }
        }

        // Check for and request the READ_EXTERNAL_STORAGE permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_STORAGE_REQUEST_CODE);
            }
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Initialize Accelerometer Sensor
        if (sensorManager != null) {
            accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Register the sensor listener
        if (accSensor != null) {
            sensorManager.registerListener(MainActivity.this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        initializeViews();

        videoCaptureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        videoUri = data.getData();
                        String videoPath = convertMediaUriToPath(videoUri);

                        // Now you can use SlowTask to process the videoPath and get the rate
                        new SlowTask().execute(videoPath);
                    }
                }
        );

        // For Symptoms update
        symptomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SymptomsLogging.class);
                intent.putExtra("rating",rating);
                startActivity(intent);
            }
        });

        // For HeartRate
        if(!hasCamera()){
            heartRateButton.setEnabled(false);
        }
        else
        {
            heartRateButton.setEnabled(true);
        }
        heartRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                heartRateTextView.setText("Calculating...");
                startRecordingHeartRate();
                //h_rate = calculate_h_rate();
            }
        });

        repirationRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                respiratoryRateTextView.setText("Calculating...");
                startRecordingRespiration();
            }
        });

        uploadSignsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call a method to insert both heart rate and respiratory rate
                insertHeartAndRespiratoryRates();
            }
        });

    }
    private void initializeViews()
    {
        symptomButton = findViewById(R.id.symptom);
        heartRateButton = findViewById(R.id.heartratebtn);
        heartRateTextView = findViewById(R.id.tfhr);
        respiratoryRateTextView = findViewById(R.id.tfrr);
        repirationRateButton = findViewById(R.id.respiration_rate);
        uploadSignsButton = findViewById(R.id.sign_btn);
        loadRating();
    }

    private void loadRating()
    {
        //load the rating
        if (rating == null) {
            rating = new float[10];
            rating[0] = 0.0f;
            rating[1] = 0.0f;
            rating[2] = 0.0f;
            rating[3] = 0.0f;
            rating[4] = 0.0f;
            rating[5] = 0.0f;
            rating[6] = 0.0f;
            rating[7] = 0.0f;
            rating[8] = 0.0f;
            rating[9] = 0.0f;
        }
    }

    //HeartRate related functions and data
    private boolean hasCamera() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)){
            return true;
        } else {
            return false;
        }
    }

    private void toggleTorch() {
        if (camManager == null) {
            camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                camId = camManager.getCameraIdList()[0]; // Use the first camera (rear camera)
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        try {
            camManager.setTorchMode(camId, !isTorchOn); // Toggle the torch
            isTorchOn = !isTorchOn;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startRecordingHeartRate() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
        videoCaptureLauncher.launch(intent);
        toggleTorch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (isTorchOn) {
            toggleTorch(); // Turn off the torch when recording is stopped
        }

        if (requestCode == VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoUri = data.getData();
            String videoPath = convertMediaUriToPath(videoUri);

            // Now you can use SlowTask to process the videoPath and get the rate
            new SlowTask().execute(videoPath);
        }
    }

    public String convertMediaUriToPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    public class SlowTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            ArrayList<Bitmap> frameList = new ArrayList<>();

            try {
                retriever.setDataSource(params[0]);
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
                int aduration = Integer.parseInt(duration);
                int i = 10;
                while (i < aduration) {
                    Bitmap bitmap = retriever.getFrameAtIndex(i);
                    frameList.add(bitmap);
                    i += 5;
                }
            } catch (Exception e) {
                // Handle the exception
            } finally {
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ArrayList<Long> a = new ArrayList<>();

                for (Bitmap i : frameList) {
                    long redBucket = 0;
                    for (int y = 550; y < 650; y++) {
                        for (int x = 550; x < 650; x++) {
                            int c = i.getPixel(x, y);
                            redBucket += Color.red(c) + Color.blue(c) + Color.green(c);
                        }
                    }
                    a.add(redBucket);
                }

                ArrayList<Long> b = new ArrayList<>();
                for (int i = 0; i < a.size() - 5; i++) {
                    long temp = (a.get(i) + a.get(i + 1) + a.get(i + 2) + a.get(i + 3) + a.get(i + 4)) / 4;
                    b.add(temp);
                }

                long x = b.get(0);
                int count = 0;
                for (int i = 1; i < b.size(); i++) {
                    long p = b.get(i);
                    if ((p - x) > 100) {
                        count = count + 1;
                    }
                    x = b.get(i);
                }

                int rate = (int) (((float) count / 45) * 60);
                return Integer.toString(rate / 2);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Update the TextView's text with the heart rate result
            heartRateTextView.setText("Heart Rate: " + result);
        }
    }

    private void startRecordingRespiration() {
        if (!isRecording) {
            // Start recording data
            accValX.clear();
            accValY.clear();
            accValZ.clear();
            isRecording = true;

            // Register the sensor listener
            if (sensorManager != null && accSensor != null) {
                sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            // Stop recording after 45 seconds
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecordingRespiration();
                }
            }, 45 * 1000); // 45 seconds in milliseconds
        }
    }

    private void stopRecordingRespiration() {
        if (isRecording) {
            // Stop recording
            isRecording = false;
            sensorManager.unregisterListener(this);
            // Calculate average respiratory rate
            int avgRespiratoryRate = RespiratoryCalculator();
            // Display the average respiratory rate
            respiratoryRateTextView.setText("Respiratory Rate: " + avgRespiratoryRate + " breaths per minute");
        }
    }

    private void insertHeartAndRespiratoryRates() {
        // Get heart rate value (you may need to adapt this part based on where your heart rate value is stored)
        int heartRateValue = 0;
        String heartRateResult = heartRateTextView.getText().toString();
        String[] parts = heartRateResult.split(":");
        if (parts.length == 2) {
            String numericPart = parts[1].trim(); // Get the numeric part
            heartRateValue = Integer.parseInt(numericPart);
            // Now you can use heartRateValue as an integer
        } else {
        }
        // Calculate average respiratory rate
        int respiratoryRate = RespiratoryCalculator();
        // Insert both values into the database
        dbHelper.insertHeartRate(heartRateValue);
        dbHelper.insertRespiratoryRate(respiratoryRate);
        Toast.makeText(this, "Signs saved successfully.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this example
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Record accelerometer data while recording is active
        if (isRecording) {
            float[] values = event.values;
            accValX.add(values[0]);
            accValY.add(values[1]);
            accValZ.add(values[2]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity is paused
        if (isRecording) {
            stopRecordingRespiration();
        }
    }

    private int RespiratoryCalculator() {
        int totalBreaths = 0;
        int durationInSeconds = 45;
        float previousValue = 0f;
        float currentValue = 0f;

        for (int i = 1; i < accValX.size(); i++) {
            currentValue = (float) Math.sqrt(
                    Math.pow(accValZ.get(i), 2.0) +
                            Math.pow(accValX.get(i), 2.0) +
                            Math.pow(accValY.get(i), 2.0)
            );

            if (Math.abs(previousValue - currentValue) > 0.52) {
                totalBreaths++;
            }

            previousValue = currentValue;
        }

        // Calculate the average respiratory rate in breaths per minute
        double avgRespiratoryRate = (double) totalBreaths / durationInSeconds * 60;

        return (int) avgRespiratoryRate;
    }

    // Handle the result of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_SENSORS_REQUEST_CODE) {
            // Handle the result of SENSOR permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // SENSOR permission granted, you can now access the accelerometer
            } else {
                // SENSOR permission denied, handle this case (e.g., show a message)
            }
        } else if (requestCode == PERMISSION_STORAGE_REQUEST_CODE) {
            // Handle the result of READ_EXTERNAL_STORAGE permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // READ_EXTERNAL_STORAGE permission granted, you can now access external storage
            } else {
                // READ_EXTERNAL_STORAGE permission denied, handle this case (e.g., show a message)
            }
        }
    }
}