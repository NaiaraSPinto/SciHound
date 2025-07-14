package com.jpl.nasa.scihound;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.GpsSatellite;
import android.os.Bundle;
import android.content.Context;
import android.location.LocationManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Spinner;
import android.content.Intent;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * The specific project interface that the user wants to collect the points of.
 * This is a neutral interface that can be interchanged with other types of projects
 * from the project_template.txt file.
 */
public class ProjectInterface extends AppCompatActivity implements SensorEventListener {
    private Context context = ProjectInterface.this;
    private Activity activity = ProjectInterface.this;

    // Map
    private Button Map;

    // GPS Location Display
    private TextView Latitude;
    private TextView Longitude;
    private TextView Accuracy;
    private TextView Satellites;
    private double lat, longi, accur;
    private int numOfSatellites;

    // GPS
    private LocationManager location;
    private GpsSatellite gpsSatellite;
    private Location oldBearingLocation = null;

    // Compass
    private float degrees;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity = null;
    private float[] geomagnetic = null;

    // Permission Constants
    private static final int myPermissionAccessCamera = 1;
    private static final int myPermissionAccessDataC = 2;
    private static final int myPermissionAccessDataS = 3;
    private static final int myPermissionAcessFineLocation = 4;
    private static final int myPermissionAccessCoarseLocation = 5;

    // Camera Intent Request Code and Camera AlertDialog to be canceled on picture capture
    private static final int cameraRequestCode = 6;
    private AlertDialog cameraDialog = null;

    // Email Intent Request Code
    private static final int emailRequestCode = 7;

    // Dropdown menu for classes
    private Spinner dropdown;

    // Buttons to add point and submit
    private Button submitPoints;
    private Button addPoint;

    // Aggregated points view
    private ListView allPoints;
    private PointsListAdapter listAdapter;
    private String GUID;
    private AggregatedPointInformation pointInformation;

    /**
     * The method that sets up the interface with all of the interactive widgets.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_interface);

        pointInformation = ProjectToPoints.typesToPoints.get(getIntent().getStringExtra("type"));

        Latitude = (TextView)findViewById(R.id.Latitude);
        Longitude = (TextView)findViewById(R.id.Longitude);
        Accuracy = (TextView)findViewById(R.id.Accuracy);
        Satellites = (TextView)findViewById(R.id.Satellites);
        dropdown = (Spinner)findViewById(R.id.Classes);
        addPoint = (Button)findViewById(R.id.Add_Point);
        submitPoints = (Button)findViewById(R.id.Submit_Points);
        allPoints = (ListView)findViewById(R.id.Aggregated_Points);

        Map = (Button)findViewById(R.id.Map);
        Map.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(CheckPermission.isNetworkAvailable(context)){
                    Intent goToIntent = new Intent(context,VisualizePoints.class);
                    goToIntent.putExtra("type", pointInformation.projectType);
                    startActivity(goToIntent);
                }else{
                    Toast internetToast = Toast.makeText(context, "Need internet connection to visualize points on map.", Toast.LENGTH_LONG);
                    TextView internetMessage = (TextView)internetToast.getView().findViewById(android.R.id.message);
                    internetMessage.setGravity(Gravity.CENTER);
                    internetToast.show();
                }
            }
        });

        // Setting up the ListView to add points
        listAdapter = new PointsListAdapter(context, activity, pointInformation);
        allPoints.setAdapter(listAdapter);

        // Populate the dropdown menu
        ArrayAdapter<String> dropAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, ProjectTypes.projectLandCoverClasses.get(pointInformation.projectType));
        // Specify the layout to use when the list of choices appears
        dropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        dropdown.setAdapter(dropAdapter);

        if (CheckPermission.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,context) && CheckPermission.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,context)) {
            // Setting up SensorManager
            sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            // Setting up Location
            location = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
            Toast moveToast = Toast.makeText(context, "Please wait a few seconds and move a little to allow the application to get geolocation.", Toast.LENGTH_LONG);
            TextView moveMessage = (TextView)moveToast.getView().findViewById(android.R.id.message);
            moveMessage.setGravity(Gravity.CENTER);
            moveToast.show();
            isLocationEnabled();
        } else {
            if (!CheckPermission.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, context)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        myPermissionAcessFineLocation);
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        myPermissionAccessCoarseLocation);
            }
        }

        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLocationEnabled();
                if(pointInformation.listItems.size() < AdminInformation.maxPoints){
                    if(lat != 0.0 && longi != 0.0 && numOfSatellites >= AdminInformation.minNumOfSatellites) {
                        GUID = UUID.randomUUID().toString();
                        AlertDialog.Builder cameraBuilder = new AlertDialog.Builder(context);
                        cameraBuilder.setTitle("Take optional photo of point")
                                .setMessage("Taking a photo of the point will help scientists see what you see. Thank you.")
                                .setCancelable(false)
                                .setPositiveButton("Open camera", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) { }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        pointInformation.photoExists.add(0,false);
                                        pointInformation.photoFilename.add(0,"");
                                        dialogInterface.cancel();
                                    }
                                });
                        cameraDialog = cameraBuilder.create();
                        cameraDialog.show();
                        cameraDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                if(CheckPermission.checkPermission(Manifest.permission.CAMERA,context)
                                        && CheckPermission.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)){
                                    CameraIntent.openAndUseCamera(context, cameraRequestCode, activity, GUID, pointInformation);
                                }else{
                                    if(!CheckPermission.checkPermission(Manifest.permission.CAMERA,context)){
                                        ActivityCompat.requestPermissions(activity,
                                                new String[]{Manifest.permission.CAMERA}, myPermissionAccessCamera);
                                    }else{
                                        ActivityCompat.requestPermissions(activity,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, myPermissionAccessDataC);
                                    }
                                }
                            }
                        });
                        allPoints.post(new Runnable() {
                            @Override
                            public void run() {
                                String className = dropdown.getSelectedItem().toString();
                                // The following timestamp is in Postgre SQL format.
                                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                pointInformation.emailClass.add(0,className);
                                pointInformation.emailDateTime.add(0,timeStamp);
                                pointInformation.listItems.add(0,(pointInformation.iid++) + ": " + className);
                                pointInformation.commentItems.add(0,"");
                                pointInformation.allLatitudes.add(0,lat);
                                pointInformation.allLongitudes.add(0,longi);
                                pointInformation.allAccuracy.add(0, accur);
                                pointInformation.allHeading.add(degrees);
                                pointInformation.GUID.add(0, GUID);
                                listAdapter.notifyDataSetChanged();
                                allPoints.smoothScrollToPosition(0);
                            }
                        });
                    }else{
                        Toast moveToast = null;
                        if(lat == 0.0 && longi == 0.0){
                            moveToast = Toast.makeText(context, "Please wait a few seconds and move a little to allow the application to get geolocation.", Toast.LENGTH_LONG);
                        }else{
                            moveToast = Toast.makeText(context, "Please walk around, so the receiver can see at least " + AdminInformation.minNumOfSatellites + " satellites.",
                                    Toast.LENGTH_LONG);
                        }
                        TextView moveMessage = (TextView)moveToast.getView().findViewById(android.R.id.message);
                        moveMessage.setGravity(Gravity.CENTER);
                        moveToast.show();
                    }
                }else{
                    Toast moveToast = Toast.makeText(context, "You are at the maximum allowed points collected in a session. Please submit and clear the points before collecting more points.", Toast.LENGTH_LONG);
                    TextView moveMessage = (TextView)moveToast.getView().findViewById(android.R.id.message);
                    moveMessage.setGravity(Gravity.CENTER);
                    moveToast.show();
                }

            }
        });

        submitPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckPermission.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,context)){
                    if(AdminInformation.sendType.equals("email")){
                        SendPointsInEmail.sendAllCollectedPoints(activity, context, emailRequestCode, pointInformation);
                    }else if(AdminInformation.sendType.equals("server")){
                        if(CheckPermission.isNetworkAvailable(context)){
                            // Send aggregated points to server
                            Toast.makeText(context,"Sending points to the server..",Toast.LENGTH_LONG).show();
                            SendPointsInEmail.sendPointsToDatabase(context, pointInformation, listAdapter);
                        }else{
                            android.app.AlertDialog.Builder serverDialogBuilder = new android.app.AlertDialog.Builder(context);
                            serverDialogBuilder.setTitle("No Internet Connection.")
                                    .setMessage("Please leave the app running in the background. Points will be sent to the server once an internet connection has become available.")
                                    .setCancelable(false)
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // TODO: not sure what this does quite yet
//                                            StrictMode.VmPolicy.Builder svb = new StrictMode.VmPolicy.Builder();
//                                            StrictMode.setVmPolicy(svb.build());
                                            SendPointsInEmail.sendPointsToDatabase(context, pointInformation, listAdapter);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                            android.app.AlertDialog serverDialog = serverDialogBuilder.create();
                            serverDialog.show();
                        }
                    }
                }else{
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, myPermissionAccessDataS);
                }
            }
        });
    }

    /**
     * When the user returns to the application from outside of the application.
     */
    protected void onResume() {
        super.onResume();
        if (CheckPermission.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,context) && CheckPermission.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,context)){
            isLocationEnabled();
        }
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    protected void onPause() {
        super.onPause();
        if(sensorManager != null){
            System.out.println("MEEP:HEREREREHREHRE");
            sensorManager.unregisterListener(this);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;
        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll
                degrees = (float)(Math.toDegrees(azimuth)+360)%360;
            }
        }
    }

    /**
     * LocationListener is a class that listens for location modification.
     * Once the user moves we want to change the interface to reflect this movement.
     */
    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            longi = location.getLongitude();
            accur = location.getAccuracy();
            numOfSatellites = location.getExtras().getInt("satellites");
            Latitude.setText("Latitude: " + lat);
            Longitude.setText("Longitude: " + longi);
            Accuracy.setText("Accurate within: " + BigDecimal.valueOf(accur)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue() + " m");
            Satellites.setText("Satellites received: " + numOfSatellites);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };

    /**
     * Function that checks if the user has their location enabled,
     * if not we will ask them to enable it.
     */
    private void isLocationEnabled() {
        if (!location.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Enable Location")
                    .setCancelable(false)
                    .setMessage("Your Locations Setting is not enabled. Please enable it in the Settings Menu.")
                    .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            Toast.makeText(context, "Please Enable Location Services to be able to use Application.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * The callback function once an intent finishes.
     * We check for who is calling the callback function, and then handle the result of the intent finishing.
     * @param requestCode The code that tells us which intent finished.
     * @param resultCode The result of the intent.
     * @param data The intent that just finished.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            cameraDialog.cancel();
        } else if (requestCode == emailRequestCode) {
            AlertDialog.Builder clearDialogBuilder = new AlertDialog.Builder(context);
            clearDialogBuilder.setTitle("Clear Points")
                    .setMessage("Do you want to clear all of collected points?")
                    .setCancelable(false)
                    .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pointInformation.clearAllPoints();
                            listAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog clearDialog = clearDialogBuilder.create();
            clearDialog.show();
        }
    }

    /**
     * This is the callback function when checking if the application has certain permissions.
     * If the user denies the permission for any of the required permissions, we will tell
     * them that the application needs permission to work properly.
     * @param requestCode The permission code of the request, we pass this in the permission function call.
     * @param permissions The specific permission that was requested.
     * @param grantResults The permission level, was the permission granted or not?
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Toast errorToast = null;
        TextView toastMessage = null;

        if (requestCode == myPermissionAccessCamera) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.CAMERA)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        errorToast = Toast.makeText(context,"Application needs access to camera to be able to take a picture.",Toast.LENGTH_LONG);
                        toastMessage = (TextView)errorToast.getView().findViewById(android.R.id.message);
                        toastMessage.setGravity(Gravity.CENTER);
                        errorToast.show();
                    }else if(!CheckPermission.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)){
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, myPermissionAccessDataC);
                    }
                }
            }
        } else if (requestCode == myPermissionAccessDataC) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        errorToast = Toast.makeText(context,"Application needs access to data to be able to take a picture.",Toast.LENGTH_LONG);
                        toastMessage = (TextView)errorToast.getView().findViewById(android.R.id.message);
                        toastMessage.setGravity(Gravity.CENTER);
                        errorToast.show();
                    }else{
                        CameraIntent.openAndUseCamera(context, cameraRequestCode, activity,GUID, pointInformation);
                    }
                }
            }
        } else if(requestCode == myPermissionAccessDataS){
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        errorToast = Toast.makeText(context,"Application needs access to data to be able to submit points.",Toast.LENGTH_LONG);
                        toastMessage = (TextView)errorToast.getView().findViewById(android.R.id.message);
                        toastMessage.setGravity(Gravity.CENTER);
                        errorToast.show();
                    }else{
                        submitPoints.callOnClick();
                    }
                }
            }
        } else if(requestCode == myPermissionAccessCoarseLocation ||
                requestCode == myPermissionAcessFineLocation){
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)){
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        errorToast = Toast.makeText(context, "Application needs access to location to be able to obtain geolocation.", Toast.LENGTH_LONG);
                        toastMessage = (TextView)errorToast.getView().findViewById(android.R.id.message);
                        toastMessage.setGravity(Gravity.CENTER);
                        errorToast.show();
                        finish();
                    }else{
                        location = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
                        Toast moveToast = Toast.makeText(context, "Please wait a few seconds and move a little to allow the application to get geolocation.", Toast.LENGTH_LONG);
                        TextView moveMessage = (TextView)moveToast.getView().findViewById(android.R.id.message);
                        moveMessage.setGravity(Gravity.CENTER);
                        moveToast.show();
                    }
                }
            }
        }
    }
}
