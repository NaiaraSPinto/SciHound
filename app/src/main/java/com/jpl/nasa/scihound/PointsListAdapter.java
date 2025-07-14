package com.jpl.nasa.scihound;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

/**
 * List Adapter for the ListView that will hold all of the points on the interface for Project Interface.
 * This class handles what happens when an item in the ListView is interacted with.
 */
public class PointsListAdapter extends BaseAdapter implements ListAdapter {
    private AggregatedPointInformation pointInformation;
    private Context context;
    private Activity activity;

    public PointsListAdapter(Context context, Activity activity, AggregatedPointInformation pointInformation){
        this.pointInformation = pointInformation;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public int getCount(){
        return pointInformation.listItems.size();
    }

    @Override
    public Object getItem(int pos){
        return pointInformation.listItems.get(pos);
    }

    @Override
    public long getItemId(int pos){
        return 0;
    }

    /**
     * This method handles the widget interactions, by popping up an interactive menu, etc.
     * @param position The position of the item that was clicked.
     * @param convertView The ListView that holds the items.
     * @param viewParent The interface of the activity that the user is currently at.
     * @return The method returns a View that is of the item within the ListView.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup viewParent){
        View view = convertView;
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.points_list_item,null);
        }

        // Handle TextView and display string from list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(pointInformation.listItems.get(position));

        // Handle buttons and add onClickListeners
        Button deleteButton = (Button)view.findViewById(R.id.delete_button);
        ImageButton commentButton = (ImageButton)view.findViewById(R.id.comment_button);
        ImageButton cameraButton = (ImageButton)view.findViewById(R.id.camera_button);

        cameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder cameraBuilder = new AlertDialog.Builder(context);
                if(pointInformation.photoExists.get(position)){
                    // Build a bitmap of the image via the filepath and then display in the AlertDialog.
                    // For some reason the image is rotated 90 degrees when captured, so we rotate it
                    // Back to normal orientation.
                    Bitmap imgBitmap = BitmapFactory.decodeFile(pointInformation.photoFilename.get(position));
                    ImageView img = new ImageView(context);
                    img.setImageBitmap(imgBitmap);
                    img.setRotation(90);
                    cameraBuilder.setTitle("Image taken of " + pointInformation.listItems.get(position) + ":")
                            .setView(img)
                            .setPositiveButton("Retake Image", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Application already has permission to use the camera and storage at this point.
                                    CameraIntent.retakeImage(context, activity, 0, pointInformation.photoFilename.get(position));
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                }else{
                    cameraBuilder.setTitle("Photo was not taken of point")
                            .setMessage("If you want to add a photo for the specific point, " +
                                    "then please delete the point and recollect at the same location.")
                            .setCancelable(false)
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                }
                AlertDialog cameraDialog = cameraBuilder.create();
                cameraDialog.show();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);
                deleteBuilder.setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete: \n" + pointInformation.listItems.get(position) + "\nwith comment: \n" + pointInformation.commentItems.get(position))
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Items that affect the ListView and email
                                pointInformation.deletePoint(position);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog deleteDialog = deleteBuilder.create();
                deleteDialog.show();
            }
        });
        commentButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder commentBuilder = new AlertDialog.Builder(context);
                final EditText commentInput = new EditText(context);
                commentInput.setInputType(InputType.TYPE_CLASS_TEXT);
                commentInput.setSingleLine(false);
                commentInput.setText(pointInformation.commentItems.get(position));
                commentInput.setLines(1);
                commentInput.setMaxLines(10);
                commentInput.setHorizontalScrollBarEnabled(false);
                commentBuilder.setTitle("Comment about " + pointInformation.listItems.get(position) + ":")
                        .setCancelable(false)
                        .setView(commentInput)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pointInformation.commentItems.set(position, commentInput.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog commentDialog = commentBuilder.create();
                commentDialog.show();
            }
        });
        return view;
    }
}
