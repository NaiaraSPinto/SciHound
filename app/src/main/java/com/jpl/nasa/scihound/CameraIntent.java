package com.jpl.nasa.scihound;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.app.Activity;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

/**
 * Class that handles the access of the Camera.
 * Used for taking the initial picture of the point as well as retaking pictures of points.
 */
public class CameraIntent {
    /**
     * Create the camera intent and the file that the photo will be stored in
     * and then start the actual intent.
     * @param context The application context, that allows binding objects with information.
     * @param cameraRequestCode The camera request code that will be handled in the activity's onActivityResult
     * @param activity The activity that we will return to when the intent is finished.
     */
    public static void openAndUseCamera(Context context, int cameraRequestCode, Activity activity, String GUID, AggregatedPointInformation pointInformation){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if(cameraIntent.resolveActivity(context.getPackageManager()) != null){
            // Create the File where the photo should be stored
            File photoFile = null;
            try{
                photoFile = FileCreation.createImageFile(context, GUID);
                addCameraInformationToPointInformation(true, photoFile.getAbsolutePath(), pointInformation);
            }catch(IOException e){
                e.printStackTrace();
                addCameraInformationToPointInformation(false, "", pointInformation);
            }
            // Continue only if the File was successfully created
            if(photoFile != null){
                startCameraIntent(context, photoFile, cameraIntent, activity, cameraRequestCode);
            }
        }
    }

     /* Starts the intent that allows the user to retake a picture for the
     * specific point.
     * @param context The application context, that allows binding objects with information.
     * @param activity The activity that we will return to when the intent is finished.
     * @param cameraRequestCode The camera request code that will be handled in the activity's onActivityResult.
     * @param filePath The filepath that the picture taken will be stored.
     */
    public static void retakeImage(Context context, Activity activity, int cameraRequestCode, String filePath){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if(cameraIntent.resolveActivity(context.getPackageManager()) != null){
            File photoFile = new File(filePath);
            // Continue only if the File was successfully created
            if(photoFile != null){
                startCameraIntent(context, photoFile, cameraIntent, activity, cameraRequestCode);
            }
        }
    }

    /**
     * Method actually starts the camera activity.
     * Start the intent using the request code to handle the return for the activity.
     * @param context
     * @param photoFile
     * @param cameraIntent The intent that will be used to take a picture of the point.
     * @param activity
     * @param cameraRequestCode
     */
    private static void startCameraIntent(Context context, File photoFile, Intent cameraIntent, Activity activity, int cameraRequestCode){
        Uri photoUri = FileProvider.getUriForFile(context, "com.jpl.nasa.scihound.fileprovider", photoFile);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        activity.startActivityForResult(cameraIntent, cameraRequestCode);
    }

    /**
     * Method to add the newly created photo filepath to the aggregated point information.
     * If no photo is taken, then we set photoExists to false and the photoFilename would be simply "".
     * @param exists Adding if the a photo was taken of the point.
     * @param filePath The filepath to the picture if a picture was taken.
     * @param pointInformation The instance of AggregatedPointInformation that the point belongs to/
     */
    private static void addCameraInformationToPointInformation(boolean exists, String filePath, AggregatedPointInformation pointInformation){
        pointInformation.photoExists.add(0,exists);
        pointInformation.photoFilename.add(0,filePath);
    }
}
