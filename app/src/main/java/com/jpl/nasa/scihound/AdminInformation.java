package com.jpl.nasa.scihound;

/**
 * Class that holds the Admin-set information.
 * The admin's email, verification password, and compression quality information is held in this class.
 */
public class AdminInformation {
    /**
     * The admin that we're sending the collected points to.
     */
    public static String adminEmail;

    /**
     * The hard-coded verification password that an admin would set.
     */
    public static String verificationPassword;

    /**
     * Values must be in [0,100]. (High Quality : (90,100], Medium Quality : (80,90], Low Quality : (70,80]).
     * The lower the quality the more points with images can be collected.
     * 80 allows ~24 points with pictures to be collected.
     */
    public static int compressionQuality;

    /**
     * Number of points that we're limiting the user to collect in a single session.
     */
    public static final int maxPoints = 100;

    /**
     * Minimum number of satellites that the application should be seeing before collecting a point.
     * Empirically, we see that when the application sees 15 to 17 satellites it will have an accuracy reading < 5 meters.
     * The admin can set how strict the application is, by either increasing or decreasing this number.
     */
    public static int minNumOfSatellites;

    /**
     * The type of medium used for sending the points and the images. Right now we are expecting either
     * points sent to a server or to an email.
     */
    public static String sendType;
}
