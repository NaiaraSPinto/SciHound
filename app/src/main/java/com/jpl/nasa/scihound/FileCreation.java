package com.jpl.nasa.scihound;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.content.Context;

/**
 * Class to create CSV files or files for photos.
 * These files are returned, so that an email with the necessary information is sent to the admin.
 */
public class FileCreation {

    /**
     * Create the CSV file of all of the collected points for the email intent.
     * We store the CSV file in the android external storage.
     * The variables for the CSV files are as follows:
     * DateTime,Class,Latitude,Longitude,Accuracy, and Comment.
     * @return The CSV file that is created
     * @throws IOException We're creating and writing to a file, so the application could fail when doing so.
     */
    public static File createCSV(AggregatedPointInformation pointInformation) throws IOException {
        File root = Environment.getExternalStorageDirectory();
        File csvFile = null;
        if(root.canWrite()){
            File dir = new File(root.getAbsolutePath(), "/CSVFolder");
            dir.mkdir();
            csvFile = new File(dir, "/all_points.csv");
            CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFile.toString()));
            ArrayList<String[]> all_points = new ArrayList<String[]>();
            all_points.add(new String[]{"GUUID","Collector","ProjectType","DateTime","Class","Latitude","Longitude","Accuracy","Heading","Comment"});
            addAggregatedInformation(all_points, pointInformation);
            csvWriter.writeAll(all_points);
            csvWriter.close();
        }
        return csvFile;
    }

    /**
     * Create the image file where the photo will be stored.
     * We need to give it a unique name, so we use the DateTime that it was taken.
     * @param context Allows binding of objects and information.
     * @return  imageFile : The image file created.
     * @throws IOException
     */
    public static File createImageFile(Context context, String GUID) throws IOException{
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        imageFile = File.createTempFile(
                GUID,  // prefix
                ".jpg",   // suffix
                storageDir      // directory
        );

        return imageFile;
    }

    /**
     * Fill all_points with all of the information in AggregatedPointInformation.java
     * @param all_points holds all of the point information for when we create the CSV file.
     */
    private static void addAggregatedInformation(ArrayList<String[]> all_points, AggregatedPointInformation pointInformation){
        for(int i = 0; i < pointInformation.listItems.size(); ++i){
            all_points.add(new String[]{pointInformation.GUID.get(i)
                    , UserInformation.getEmail().equals("") ? "No Email Input" : UserInformation.getEmail()
                    , pointInformation.projectType
                    , pointInformation.emailDateTime.get(i)
                    , pointInformation.emailClass.get(i)
                    , pointInformation.allLatitudes.get(i).toString()
                    , pointInformation.allLongitudes.get(i).toString()
                    , pointInformation.allAccuracy.get(i).toString()
                    , pointInformation.allHeading.get(i).toString()
                    , pointInformation.commentItems.get(i)});
        }
    }
}
