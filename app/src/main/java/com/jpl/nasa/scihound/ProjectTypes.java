package com.jpl.nasa.scihound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that holds the different types of projects, project links, and land cover classes.
 * This class will be loaded by LoadProjectsFromTemplateFile.java during compile time.
 * It will hold all of the information from the project_template.txt file.
 */
public class ProjectTypes {
    public static ArrayList<String> projectTypes = new ArrayList<String>();
    public static ArrayList<String> projectInformationLinks = new ArrayList<String>();
    public static Map<String, ArrayList<String>> projectLandCoverClasses = new HashMap<String, ArrayList<String>>();

    /**
     * A method that quickly clears all of the project information.
     */
    public static void clearProjects(){
        projectTypes.clear();
        projectInformationLinks.clear();
        projectLandCoverClasses.clear();
    }
}
