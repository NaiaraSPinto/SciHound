package com.jpl.nasa.scihound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.app.AlertDialog;

import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The Login activity that prompts the user for an email as well as the verification password that was assigned in AdminInformation.
 */
public class MainActivity extends AppCompatActivity {
    static Button submitButton, signOutButton;
    static Button infoButton;
    static EditText email, password;
    static Context context;
    static TextView mUser, mInstructions, mLink;

    private FirebaseAuth mAuth;

    /**
     * The method that creates the login interface.
     * Sets up all of the widgets on the screen for the user to interact with.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = MainActivity.this;

        submitButton = (Button)findViewById(R.id.Submit);
        signOutButton = (Button)findViewById(R.id.signOut);
        infoButton = (Button)findViewById(R.id.Info);
        mAuth = FirebaseAuth.getInstance();
        mUser = (TextView)findViewById(R.id.textViewUser);
        mInstructions = (TextView)findViewById(R.id.instructions);
        mLink = (TextView)findViewById(R.id.siteLink);
        // Set link to be clickable
        mLink.setMovementMethod(LinkMovementMethod.getInstance());

        signOutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                updateUI(currentUser);
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = (EditText)findViewById(R.id.Email);
                password = (EditText)findViewById(R.id.Password);

                signIn(email.getText().toString(), password.getText().toString());

                // This is where we would change this to communicate with a server to validate credentials
                // OLD CODE FROM PREVIOUS VERSION, NM 6/25/2020 ********************
//                if (password.getText().toString().equals(AdminInformation.verificationPassword)){
//                    Intent goToIntent = new Intent(context,ProjectChoosingInterface.class);
//                    UserInformation.setEmail(email.getText().toString());
//                    startActivity(goToIntent);
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("License")
                        .setMessage(loadLicense())
                        .setCancelable(false)
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                TextView infoText = (TextView) dialog.findViewById(android.R.id.message);
                infoText.setTextSize(10);
            }
        });

        LoadProjectsFromTemplateFile.loadFile(context);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    /**
     * Load the license from license_information.txt
     * @return the formated String that will be the message when clicking the info button.
     */
    private String loadLicense(){
        InputStream iStream = context.getResources().openRawResource(R.raw.license_information);
        BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
        String currLine = null;
        String licenseMessage = "";
        try {
            currLine = reader.readLine();
            while (currLine != null) {
                if(currLine.equals("START")){
                    while(!(currLine = reader.readLine()).equals("END")){
                        licenseMessage += currLine + "\n";
                    }
                    licenseMessage  += "\n-----------------------------------------------------------------------------------------------------\n\n";
                }
                currLine = reader.readLine();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return licenseMessage;
    }

    /**
     * Signs in a user once email and password fields are filled
     * Uses Firebase Authentication to verify user
     * Shows toast message if wrong email/password combo are entered
     * @author Natalie Magnus
     * @param email - user email
     * @param password - user password
     */
    private void signIn(final String email, String password) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            // user is already signed in
            Intent goToIntent = new Intent(context,ProjectChoosingInterface.class);
            UserInformation.setEmail(currentUser.getEmail());
            startActivity(goToIntent);
        } else if(email.length() > 0 && password.length() > 0) {

            // user is not signed in
            if(CheckPermission.isNetworkAvailable(context)) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Intent goToIntent = new Intent(context,ProjectChoosingInterface.class);
                                    UserInformation.setEmail(email);
                                    startActivity(goToIntent);
                                } else {
                                    // Hide Keyboard
                                    View v = MainActivity.this.getCurrentFocus();
                                    if(v != null){
                                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                                    }
                                    // Allow the main thread to close the keyboard before showing the toast message.
                                    try{
                                        Thread.sleep(100);
                                    }catch(InterruptedException e){
                                        e.printStackTrace();
                                    }
                                    Toast failedLogin = Toast.makeText(context,"Incorrect email/password combination.",Toast.LENGTH_LONG);
                                    TextView loginMessage = (TextView)failedLogin.getView().findViewById(android.R.id.message);
                                    loginMessage.setGravity(Gravity.CENTER);
                                    failedLogin.show();
                                }
                            }
                        });
            } else {
                // Internet is not available, and user is not logged in
                Toast internetToast = Toast.makeText(context,"Internet required for user login.",Toast.LENGTH_LONG);
                TextView message = (TextView)internetToast.getView().findViewById(android.R.id.message);
                message.setGravity(Gravity.CENTER);
                internetToast.show();
            }

        } else {
            Toast emptyString = Toast.makeText(context,"Please fill out both email and password fields.",Toast.LENGTH_LONG);
            TextView message = (TextView)emptyString.getView().findViewById(android.R.id.message);
            message.setGravity(Gravity.CENTER);
            emptyString.show();
        }
    }

    /**
     * Updates TextView on screen to reflect if user is logged in or not
     * @author Natalie Magnus
     * @param user - user that is currently signed in, can be null if none
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mUser.setText("Currently signed in as: " + user.getEmail());
            mInstructions.setText("Click 'Submit' to continue as current user.");
        } else {
            mUser.setText("Not currently signed in.");
            mInstructions.setText("Please enter credentials to continue.");
        }
    }
}
