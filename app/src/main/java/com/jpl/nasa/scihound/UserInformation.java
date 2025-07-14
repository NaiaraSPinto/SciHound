package com.jpl.nasa.scihound;

/**
 * Class that holds the user's email information, this is collected on the login page.
 */
public class UserInformation {
    // User that is collecting the points
    private static String userEmail;

    /**
     * Setting the user email
     * @param newEmail The new user email that we set.
     */
    public static void setEmail(String newEmail){
        userEmail = newEmail;
    }

    /**
     * Getting the user email
     * @return The user email as a String.
     */
    public static String getEmail(){
        return userEmail;
    }
}
