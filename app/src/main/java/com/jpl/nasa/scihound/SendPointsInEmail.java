package com.jpl.nasa.scihound;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.widget.Toast;
import android.app.Activity;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.jpl.nasa.scihound.Notifications.CHANNEL_ID;
import static com.jpl.nasa.scihound.Notifications.CHANNEL2_ID;

/**
 * The class that handles the email intent by collecting all of the point information and then starting the intent.
 */
public class SendPointsInEmail {
    private static NotificationManagerCompat notificationManager;

    /**
     * Create the CSV file of all of the point information from FileCreation.createCSV().
     * Get all of photos taken of the points and put as attachment.
     * Prepare the email with the correct fields and then start the email intent
     * For the user to send to the admin.
     * @param activity The activity that we initiate the email intent from, so we know what to return to when returning from the email intent.
     * @param context The application context that allows binding of objects to information.
     */
     static void sendAllCollectedPoints(final Activity activity, final Context context, final int emailRequestCode, final AggregatedPointInformation pointInformation){
            // Make sure we have at least one collected point to send
        if(pointInformation.listItems.size() != 0){
            AlertDialog.Builder emailDialogBuilder = new AlertDialog.Builder(context);
            emailDialogBuilder.setTitle("Select Email Application")
                    .setMessage("Sending points will only work with email applications.")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            StrictMode.VmPolicy.Builder svb = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(svb.build());
                            File csvFile = null;
                            try {
                                csvFile = FileCreation.createCSV(pointInformation);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            if (csvFile != null) {
                                createEmailIntentAndStart(activity,getAllAttachments(csvFile, context, pointInformation), emailRequestCode, pointInformation);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog emailDialog = emailDialogBuilder.create();
            emailDialog.show();
        }else{
            Toast.makeText(context,"No points to send...",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method to send images taken of points to Firebase Cloud Storage,
     * Url or other identification will be stored in Firestore database linking the information,
     * Meant to be called in sendPointsToDatabase
     * @param pointInformation We use this to get to project type and GUID for each point
     */
    private static void sendImagesToStorage(final AggregatedPointInformation pointInformation, final Context context){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference ref;
        for(int i = 0; i < pointInformation.photoFilename.size(); i++){
            if(pointInformation.photoExists.get(i) == false) {
                continue;
            }
            ref = storageReference.child("Images/" + pointInformation.projectType + "/" + pointInformation.GUID.get(i) + ".jpg");

            ref.putFile(Uri.fromFile(new File(pointInformation.photoFilename.get(i))))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Push notification to let user know points are submitted
                            notificationManager = NotificationManagerCompat.from(context);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL2_ID)
                                    .setSmallIcon(R.drawable.ic_notif_alert)
                                    .setContentTitle("Photos submitted!")
                                    .setContentText("If points have been sent, SciHound can be closed.")
                                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                    .setPriority(NotificationCompat.PRIORITY_MAX);

                            notificationManager.notify(Notifications.NOTIFICATION2_ID, builder.build());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Should rarely happen, if ever
                            notificationManager = NotificationManagerCompat.from(context);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL2_ID)
                                    .setSmallIcon(R.drawable.ic_notif_alert)
                                    .setContentTitle("Failed to submit photos.")
                                    .setContentText("Try again and contact admin if issues continue.")
                                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                    .setPriority(NotificationCompat.PRIORITY_MAX);

                            notificationManager.notify(Notifications.NOTIFICATION2_ID, builder.build());
                        }
                    });
        }
    }

    /**
     * Sends collected point data to Firebase Firestore Database,
     * Need separate method (sendImagesToStorage) for sending images to server
     * @author Natalie Magnus
     * @param pointInformation We use this to get the point information to store
     */
    static void sendPointsToDatabase(final Context context, final AggregatedPointInformation pointInformation, final PointsListAdapter listAdapter) {

        // Make sure we have points to send to database
        if(pointInformation.listItems.size() != 0){
            sendImagesToStorage(pointInformation, context);

            for(int i = 0; i < pointInformation.listItems.size(); i++) {
                // Hashmap of data to store in database
                Map<String, Object> dataToSave = new HashMap<String, Object>();
                dataToSave.put("GUID", pointInformation.GUID.get(i));
                dataToSave.put("collector", UserInformation.getEmail());
                dataToSave.put("project", pointInformation.projectType);
                dataToSave.put("timestamp", pointInformation.emailDateTime.get(i));
                dataToSave.put("class", pointInformation.emailClass.get(i));
                dataToSave.put("latitude", pointInformation.allLatitudes.get(i).toString());
                dataToSave.put("longitude", pointInformation.allLongitudes.get(i).toString());
                dataToSave.put("accuracy", pointInformation.allAccuracy.get(i).toString());
                dataToSave.put("heading", pointInformation.allHeading.get(i));
                dataToSave.put("comment", pointInformation.commentItems.get(i));
                if(pointInformation.photoExists.get(i)) {
                    dataToSave.put("photo", pointInformation.GUID.get(i) + ".jpg");
                } else {
                    dataToSave.put("photo", pointInformation.photoFilename.get(i));
                }

                //******************************************************
                // Function below doesn't work with new projects created, no Firebase security rules
                // Can implement later if needed
                // No need to view points on website at the moment
                // NM, 8/14/20
                //******************************************************

                // Saves point data under Project collection, created for querying on SciHound website
//                String pointPath = pointInformation.projectType + "/" + pointInformation.GUID.get(i);
//                DocumentReference mPointRef = FirebaseFirestore.getInstance().document(pointPath);
//
//                mPointRef.set(dataToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful()) {
//                            // Push notification to let user know points are submitted
//                            notificationManager = NotificationManagerCompat.from(context);
//
//                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL2_ID)
//                                    .setSmallIcon(R.drawable.jpl_logo)
//                                    .setContentTitle("Points now available on website!")
//                                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
//                                    .setPriority(NotificationCompat.PRIORITY_MAX);
//
//                            notificationManager.notify(Notifications.NOTIFICATION2_ID, builder.build());
//                        } else {
//                            // Should rarely happen, if ever
//                            notificationManager = NotificationManagerCompat.from(context);
//
//                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL2_ID)
//                                    .setSmallIcon(R.drawable.jpl_logo)
//                                    .setContentTitle("Failed to submit points to website.")
//                                    .setContentText("Try again and contact admin if issues continue.")
//                                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
//                                    .setPriority(NotificationCompat.PRIORITY_MAX);
//
//                            notificationManager.notify(Notifications.NOTIFICATION2_ID, builder.build());
//                        }
//                    }
//                });

                // Saves point data under individual users, created for assigning point data with users - still needed?
                String docPath = "users/" + UserInformation.getEmail() + "/" + pointInformation.projectType + "/" + pointInformation.GUID.get(i);
                DocumentReference mDocRef = FirebaseFirestore.getInstance().document(docPath);

                mDocRef.set(dataToSave).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                                // Push notification to let user know their data is submitted
                                notificationManager = NotificationManagerCompat.from(context);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_notif_alert)
                                        .setContentTitle("Points submitted to server!")
                                        .setContentText("If photos have been sent, SciHound can be closed.")
                                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                        .setPriority(NotificationCompat.PRIORITY_MAX);

                                notificationManager.notify(Notifications.NOTIFICATION_ID, builder.build());
                        } else {
                                // Should rarely happen, if ever
                                notificationManager = NotificationManagerCompat.from(context);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_notif_alert)
                                        .setContentTitle("Failed to submit points to server.")
                                        .setContentText("Try again and contact admin if issues continue.")
                                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                                        .setPriority(NotificationCompat.PRIORITY_MAX);

                                notificationManager.notify(Notifications.NOTIFICATION_ID, builder.build());
                        }
                    }
                });
            }
            // Allow the previous dialog to close before prompting to clear points
            try{
                Thread.sleep(100);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            // Ask user if they want to clear all points
            AlertDialog.Builder clearDialogBuilder = new AlertDialog.Builder(context);
            clearDialogBuilder.setTitle("Clear all collected points?")
                    .setMessage("Points are saved on device if internet connection is unavailable. You should receive push notifications once points are sent.")
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

        } else{
            Toast.makeText(context,"No points to send...",Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Method creates the Array List of Uri's that make up the attachments for the email.
     * We first add the csv file that contains all the information about the points.
     * Then we add the images that were captured of points.
     * @param csvFile The CSV file that contains all of the point information.
     * @param context The context of the activity that is calling this function
     * @param pointInformation How we get the photo file paths to add as an attachment
     * @return an ArrayList<Uri> which represents the all of the attached files, which would be the CSV file and the collect point photos.
     */
    private static ArrayList<Uri> getAllAttachments(File csvFile, Context context, final AggregatedPointInformation pointInformation){
        ArrayList<Uri> allAttachments = new ArrayList<Uri>();
        allAttachments.add(Uri.fromFile(csvFile));
        for (int i = 0; i < pointInformation.photoFilename.size(); ++i) {
            String filePath = pointInformation.photoFilename.get(i);
            compressImages(filePath, context);
            Uri currPicture = Uri.fromFile(new File(filePath+"_.jpg"));
            allAttachments.add(currPicture);
        }
        return allAttachments;
    }

    /**
     * Method created the email intent with the email title, subject, message, recipients, and attachments.
     * After creating the email intent with all the necessary fields, we then start the activity of opening
     * an email application to send the email.
     * @param activity The activity that we're starting the email intent from, so we can return the result to it.
     * @param allAttachments The Uri of all the attachments that will be in the email.
     * @param emailRequestCode The request code that will be checked in the activity that calls this function.
     * @param pointInformation We use this to get the project type
     */
    private static void createEmailIntentAndStart(Activity activity, ArrayList<Uri> allAttachments, int emailRequestCode
            , final AggregatedPointInformation pointInformation){
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SciHound: Collected Points, Project Type: " + pointInformation.projectType)
                .putExtra(Intent.EXTRA_TEXT,
                        (UserInformation.getEmail().equals("") ? "Collector did not input email." : "Points collected from: " + UserInformation.getEmail()))
                .putExtra(Intent.EXTRA_EMAIL, new String[]{AdminInformation.adminEmail, UserInformation.getEmail()})
                .setData(Uri.parse("mailto:"))
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, allAttachments)
                .setType("message/rfc822")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(emailIntent, emailRequestCode);
    }

    /**
     * Compress the image associated with filePath to a new file with the file path "<filePath>_.jpg".
     * @param filePath The file that we're compressing
     * @param context to use context.getContentResolver()
     */
    private static void compressImages(String filePath, Context context){
        OutputStream compressOut;
        File compressFile = new File(filePath + "_.jpg");
        try {
            compressOut = new FileOutputStream(compressFile);
            Bitmap imgBitmap = BitmapFactory.decodeFile(filePath);
            imgBitmap.compress(Bitmap.CompressFormat.JPEG, AdminInformation.compressionQuality, compressOut);
            compressOut.flush();
            compressOut.close();
            MediaStore.Images.Media.insertImage(context.getContentResolver(), compressFile.getAbsolutePath(), compressFile.getName(), compressFile.getName());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
