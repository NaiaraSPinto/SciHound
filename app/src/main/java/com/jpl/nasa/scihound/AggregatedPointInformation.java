package com.jpl.nasa.scihound;

import java.util.ArrayList;

/**
 * Collected Points Information: Point's location data, photo file path, DateTime, etc.
 * There will be one instance of this class for each project.
 * Instances of this class are held in ProjectToPoints to allow users to have multiple projects at one time.
 */
public class AggregatedPointInformation {
    /**
     * The type of project that the points are associated with.
     */
    public String projectType;
    /**
     * The point's land cover class that is visualized through the ListView element.
     */
    public ArrayList<String> listItems = new ArrayList<String>();
    /**
     * The point's land cover class that is sent via the email intent.
     */
    public ArrayList<String> emailClass = new ArrayList<String>();
    /**
     * The point's date and time information that will be sent via the email intent.
     */
    public ArrayList<String> emailDateTime = new ArrayList<String>();
    /**
     * The point's comment that the user sets.
     */
    public ArrayList<String> commentItems = new ArrayList<String>();
    /**
     * The point's latitude information.
     */
    public ArrayList<Double> allLatitudes = new ArrayList<Double>();
    /**
     * The point's longitude information.
     */
    public ArrayList<Double> allLongitudes = new ArrayList<Double>();
    /**
     * The point's accuracy (in horizontal radial distance in meters) information.
     */
    public ArrayList<Double> allAccuracy = new ArrayList<Double>();
    /**
     * The point's bearing from true north in the interval (0.0, 360.0].
     */
    public ArrayList<Float> allHeading = new ArrayList<Float>();
    /**
     * The boolean information to see if the user took a picture of the point collected.
     */
    public ArrayList<Boolean> photoExists = new ArrayList<Boolean>();
    /**
     * The file path in the external storage of the android phone where will store the captured photo.
     */
    public ArrayList<String> photoFilename = new ArrayList<String>();
    /**
     * The Global Unique Identifier that will ensure that each point has a unique ID.
     */
    public ArrayList<String> GUID = new ArrayList<String>();
    /**
     * We iterate IID to show on the interface a unique name for each collected point. This won't affect the email sent.
     */
    public int iid = 1; // Item ID for ListView interface

    /**
     * Making the deletion consistent with the data stored.
     * We're removing a single point from the aggregated points.
     * @param position The list item's position that is being deleted.
     */
    public void deletePoint(int position) {
        listItems.remove(position);
        emailClass.remove(position);
        emailDateTime.remove(position);
        commentItems.remove(position);
        allLatitudes.remove(position);
        allLongitudes.remove(position);
        allAccuracy.remove(position);
        photoExists.remove(position);
        photoFilename.remove(position);
        GUID.remove(position);
    }

    /**
     * Clear the aggregated points after submitting through email intent.
     * Called when the user wants to remove all of the points, after sending the email.
     */
    public void clearAllPoints() {
        listItems.clear();
        emailClass.clear();
        emailDateTime.clear();
        commentItems.clear();
        allLatitudes.clear();
        allLongitudes.clear();
        allAccuracy.clear();
        photoExists.clear();
        photoFilename.clear();
        GUID.clear();
        iid = 1;
    }
}
