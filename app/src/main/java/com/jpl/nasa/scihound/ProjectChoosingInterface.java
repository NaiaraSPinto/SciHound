package com.jpl.nasa.scihound;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.content.Context;

import android.widget.ListView;

/**
 * The Project choosing interface class.
 * This is where the user will choose what type of project they want to collect points of.
 */
public class ProjectChoosingInterface extends AppCompatActivity {
    private Context context;
    private Activity activity;
    private ListView projectListView;

    /**
     * Creating the interface that the user can interact with to go to the different projects.
     * Communicates with ProjectListAdapter to set up the ListView of all of the projects that are available
     * from the project_template file.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_projects);
        context = ProjectChoosingInterface.this;
        activity = ProjectChoosingInterface.this;

        projectListView = (ListView) findViewById(R.id.Projects_ListView);

        ProjectListAdapter projListAdapter = new ProjectListAdapter(context, activity);

        projectListView.setAdapter(projListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            AlertDialog.Builder notifDialogBuilder = new AlertDialog.Builder(context);
            notifDialogBuilder.setTitle("Push notifications are not enabled.")
                    .setMessage("Please enable before continuing for best app experience.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

                            //for Android 5-7
                            intent.putExtra("app_package", getPackageName());
                            intent.putExtra("app_uid", getApplicationInfo().uid);

                            // for Android 8 and above
                            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

                            startActivity(intent);
                        }
                    });
            AlertDialog notifDialog = notifDialogBuilder.create();
            notifDialog.show();
        }

        // TODO: enable password expiration here
        // Ensure internet connection is available?
         if(CheckPermission.isNetworkAvailable(context)) {
            //PasswordExpiration.checkExpiration(context);
             PasswordExpiration.checkExpiration(context);
        }
    }
}