package com.jpl.nasa.scihound;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolLongClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

/**
 * The class allows the user to visualize the points that they have collect.
 * Uses the MapBox API to create a nice map and markers of the collected points.
 */
public class VisualizePoints extends AppCompatActivity implements
        OnMapReadyCallback {
    private Context context;
    private Activity activity;

    private MapView mapView;
    private SymbolManager symbolManager;
    private Symbol symbol;

    private int myPermissionAcessFineLocation = 0;
    private int myPermissionAccessCoarseLocation = 1;

    private AggregatedPointInformation pointInformation;

    /**
     * Sets the map interface for the user and the collected points.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = VisualizePoints.this;
        activity = VisualizePoints.this;

        pointInformation = ProjectToPoints.typesToPoints.get(getIntent().getStringExtra("type"));

        // TODO: edit MapBox API key??
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);
        // setContentView(R.layout.activity_annotation_plugin_symbol_listener);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded(){
            @Override
            public void onStyleLoaded(@NonNull Style style){
                        for(int i = 0 ; i < pointInformation.listItems.size(); ++i){
                            mapboxMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(pointInformation.allLatitudes.get(i), pointInformation.allLongitudes.get(i)))
                                    .title(pointInformation.listItems.get(i)))
                                    .setSnippet(pointInformation.commentItems.get(i));
                        }
                startLocationTracking(mapboxMap, style);


//                style.addImage("MARKER_IMAGE", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.mapbox_marker_icon_default)));
//                // create symbol manager object
//                symbolManager = new SymbolManager(mapView, mapboxMap, style);
//
//                symbolManager.setIconAllowOverlap(true);
//                symbolManager.setIconIgnorePlacement(true);
//
//                for(int i = 0 ; i < pointInformation.listItems.size(); ++i) {
//                    // Add symbol at specified lat/lon
//                    symbol = symbolManager.create(new SymbolOptions()
//                            .withLatLng(new LatLng(pointInformation.allLatitudes.get(i), pointInformation.allLongitudes.get(i)))
//                            .withIconImage("MARKER_IMAGE")
//                            .withIconSize(1.3f)
//                            .withDraggable(true));
//                }
//
//                // add click listeners if desired
//                symbolManager.addClickListener(new OnSymbolClickListener() {
//                    @Override
//                    public boolean onAnnotationClick(Symbol symbol) {
//                        Toast.makeText(context,"symbol clicked", Toast.LENGTH_SHORT).show();
//                        return true;
//                    }
//                });
//
//                symbolManager.addLongClickListener((new OnSymbolLongClickListener() {
//                    @Override
//                    public boolean onAnnotationLongClick(Symbol symbol) {
//                        return true;
//                    }
//                }));
//
//                symbolManager.addDragListener(new OnSymbolDragListener() {
//                    @Override
//                    // Left empty on purpose
//                    public void onAnnotationDragStarted(Symbol annotation) {
//                    }
//
//                    @Override
//                    // Left empty on purpose
//                    public void onAnnotationDrag(Symbol symbol) {
//                    }
//
//                    @Override
//                    // Left empty on purpose
//                    public void onAnnotationDragFinished(Symbol annotation) {
//                    }
//                });
//
//                // set non-data-driven properties, such as:
//                symbolManager.setIconAllowOverlap(true);
//                symbolManager.setIconTranslate(new Float[]{-4f,5f});

            }
        });
    }

    /**
     * Starts the map interface to center the screen to where the user is currently.
     * @param mapboxMap The map interface that holds all of the information
     * @param style The specific map style.
     */
    private void startLocationTracking(MapboxMap mapboxMap, Style style){
        if(CheckPermission.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,context) && CheckPermission.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,context)){
            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(context, style).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

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
    }

    @Override
    public void onStart(){
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
