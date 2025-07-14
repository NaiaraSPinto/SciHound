package com.jpl.nasa.scihound;

import java.util.HashMap;

/**
 * Holds the project with the collected points for that project.
 * Each project will have their own instance of AggregatedPointInformation
 * Which holds all of the point information for that project.
 */
public class ProjectToPoints {
    /**
     * The HashMap that holds the relationship between the individual projects
     * and their aggregated points.
     */
    public static HashMap<String, AggregatedPointInformation> typesToPoints = new HashMap<String, AggregatedPointInformation>();

    /**
     * A method that adds a new project and a new instance of AggregatedPointInformation to the HashMap.
     * @param newType The new project type.
     * @param newPointInformation The new AggregatedPointInformation that holds the points.
     */
    public static void addNewTypeToPoints(String newType, AggregatedPointInformation newPointInformation){
        typesToPoints.put(newType, newPointInformation);
        typesToPoints.get(newType).projectType = newType;
    }
}
