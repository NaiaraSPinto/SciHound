package com.jpl.nasa.scihound;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.InputType;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.content.Context;
import android.view.View;
import android.app.AlertDialog;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * List Adapter for the project choosing interface ListView.
 * This List Adapter handles the user interaction with the items within the
 * ListView within the project choosing interface that holds all of the possible interfaces.
 */
public class ProjectListAdapter extends BaseAdapter implements ListAdapter {

    private Context context;
    private Activity activity;

    private Button projectButton;
    private ImageButton projectInformation;

    private Map<Integer, String> positionToProject = new HashMap<Integer, String>();

    public ProjectListAdapter(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    @Override
    public int getCount(){
        return ProjectTypes.projectTypes.size();
    }

    @Override
    public Object getItem(int pos){
        return ProjectTypes.projectTypes.get(pos);
    }

    @Override
    public long getItemId(int pos){
        return 0;
    }

    /**
     * This method handles the widget interactions.
     * @param position The item position within the ListView
     * @param convertView The ListView view.
     * @param viewParent The Activity view that holds the ListView.
     * @return An item view for each of the projects
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup viewParent){
        View view = convertView;
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.project_list_item,null);
        }

        projectButton = (Button)view.findViewById(R.id.project_button);
        projectInformation = (ImageButton)view.findViewById(R.id.project_information_button);

        projectButton.setText(ProjectTypes.projectTypes.get(position));

        if(position % 2 == 0)
            projectButton.setTextColor(Color.parseColor("#0b3d91"));
        else
            projectButton.setTextColor(Color.parseColor("#ff0000"));
        for(int i = 0; i < ProjectTypes.projectTypes.size(); ++i){
            positionToProject.put(i, ProjectTypes.projectTypes.get(i));
        }

        projectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String projectType = positionToProject.get(position);
                if(!ProjectToPoints.typesToPoints.containsKey(projectType)){
                    ProjectToPoints.addNewTypeToPoints(projectType, new AggregatedPointInformation());
                }
                Intent goToIntent = new Intent(context, ProjectInterface.class);
                goToIntent.putExtra("type", projectType);
                activity.startActivity(goToIntent);
            }
        });

        projectInformation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent floodInformationIntent = new Intent();
                floodInformationIntent.setAction(Intent.ACTION_VIEW);
                floodInformationIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                floodInformationIntent.setData(Uri.parse(ProjectTypes.projectInformationLinks.get(position)));
                activity.startActivity(floodInformationIntent);
            }
        });

        return view;
    }
}
