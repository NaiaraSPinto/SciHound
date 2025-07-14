package com.jpl.nasa.scihound;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles forcing users to reset passwords after a certain expiration date.
 */
public class PasswordExpiration {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Checks last time password was updated and compares it to the current date.
     * If longer than a certain time, force user to reset password.
     * @param context
     */
    static void checkExpiration(final Context context) {
        DocumentReference docRef = db.collection("lastPassUpdate").document(UserInformation.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date prevDate = sdf.parse(document.get("date").toString());
                            Date currDate = sdf.parse(java.time.LocalDate.now().toString());

                            // Calculate time difference in milliseconds
                            long difference_In_Time = currDate.getTime() - prevDate.getTime();
                            // Convert time difference to number of days
                            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24));

                            if(difference_In_Days >= 174) { // 6 months, on average
                                forceReset(context);
                            } else if(difference_In_Days >= 164) { // 10 days before the 6 month deadline
                                nudgeReset(context);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    } else {
                        Map<String, String> date = new HashMap<>();
                        date.put("date", java.time.LocalDate.now().toString());

                        db.collection("lastPassUpdate").document(UserInformation.getEmail())
                                .set(date)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                } else {
                    // TODO: edit if document fails
                    // Toast.makeText(context,"Document get failed.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Enable popup and force user to reset password before continuing.
     * Signs out users in order to enable password reset.
     * Set to a new random secure password??
     * @param context
     */
    private static void forceReset(final Context context) {
        AlertDialog.Builder resetDialogBuilder = new AlertDialog.Builder(context);
        resetDialogBuilder.setTitle("It is time to reset your password.")
                .setMessage("Click 'Reset' to receive a password reset email. You will be signed out of your current session.")
                .setCancelable(false)
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetPassword(context);
                    }
                });
        AlertDialog resetDialog = resetDialogBuilder.create();
        resetDialog.show();
    }

    /**
     * Enable popup and remind user that they have x amount of days before being forced to reset password.
     * Allows them to do it immediately if they would like.
     * Will call forceReset() if they decide to complete at the time.
     * @param context
     */
    private static void nudgeReset(final Context context) {
        AlertDialog.Builder resetDialogBuilder = new AlertDialog.Builder(context);
        resetDialogBuilder.setTitle("You have 10 or less days until your password must be reset.")
                .setMessage("Each password must be reset after 6 months.")
                .setCancelable(false)
                .setPositiveButton("Reset Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        forceReset(context);
                    }
                })
                .setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog resetDialog = resetDialogBuilder.create();
        resetDialog.show();
    }

    /**
     * Generates a random, secure String for password
     * Designed to prevent user from accessing account until they reset password
     * @param length - length of random password
     * @return String password
     */
    private static String generateRandomPass(int length) {
        // ASCII range
        final String range = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();

        // for each iteration, choose a random char from 'range' String
        // Append chosen char to end of password generation
        for(int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(range.length());
            builder.append(range.charAt(randomIndex));
        }

        return builder.toString();
    }

    /**
     * Reset password of user
     * Set password to random, secure password and send user a password reset email through Firebase
     */
    private static void resetPassword(final Context context) {
        // Generate random password of length 20
        String pass = generateRandomPass(20);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Set current user's password to new random password, preventing them from logging in until they reset it
        user.updatePassword(pass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    // Send password reset email if updating user password was successful
                    auth.sendPasswordResetEmail(UserInformation.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(context,"Password reset link sent to email.",Toast.LENGTH_LONG).show();
                                // Update password reset date
                                updateDocData();
                                // Return to main login screen once email is sent
                                Intent goToIntent = new Intent(context,MainActivity.class);
                                context.startActivity(goToIntent);
                                // Sign out user and update UI
                                MainActivity.signOutButton.callOnClick();
                            }
                        }
                    });
                } else {
                    authenticate(context);
                }
            }
        });
    }

    /**
     * Updates last date of password reset in Firestore
     */
    private static void updateDocData() {
        // Retrieve
        DocumentReference docRef = db.collection("lastPassUpdate").document(UserInformation.getEmail());
        docRef.update("date", java.time.LocalDate.now().toString());
    }

    /**
     * Prompts user for password in order to re-authenticate in the event that update password fails
     */
    private static void authenticate(final Context context) {
        final String[] pass = {""};

        final AlertDialog.Builder resetDialogBuilder = new AlertDialog.Builder(context);
        resetDialogBuilder.setTitle("Please enter your old password.")
                .setCancelable(false);

        // Allows user to input old password and collect the info for re-authentication
        final EditText input = new EditText(context);
        input.setHint("Password");
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        resetDialogBuilder.setView(input);

        resetDialogBuilder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pass[0] = input.getText().toString();
                verifyUser(context, pass[0]);
            }
        });

        AlertDialog resetDialog = resetDialogBuilder.create();
        resetDialog.show();

    }

    /**
     * Method that uses password input to re-authenticate user
     * Handles if user enters a null or empty input
     * @param context - context
     * @param password - previous user password
     */
    static void verifyUser(final Context context, String password) {
        if(password == null || password.equals("")) {
            Toast.makeText(context,"Please do not leave the field blank.",Toast.LENGTH_LONG).show();
            authenticate(context);
            return;
        } else {
            try {
                AuthCredential creds = EmailAuthProvider.getCredential(UserInformation.getEmail(), password);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.reauthenticate(creds).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(context,"Re-authentication successful.",Toast.LENGTH_LONG).show();
                            resetPassword(context);
                        } else {
                            Toast.makeText(context,"Incorrect password.",Toast.LENGTH_LONG).show();
                            authenticate(context);
                        }
                    }
                });
            } catch(Exception e) {
                Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }

}
