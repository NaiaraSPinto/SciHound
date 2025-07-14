package com.jpl.nasa.scihound;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Class that parses the project_template file in the ../res/raw/ directory.
 */
public class LoadProjectsFromTemplateFile {
    /**
     * Parses the project_template file and stores all the possible projects int ProjectType
     * @param context Allows the binding of objects and information.
     */
    public static void loadFile(Context context){
        ProjectTypes.clearProjects();
        InputStream iStream = context.getResources().openRawResource(R.raw.project_template);
        BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
        String currLine = null;
        try{
            currLine = reader.readLine();
            while(currLine != null){
                if(currLine.contains("pn:")){
                    if(currLine.equals("pn:")){
                        ProjectTypes.projectTypes.add(reader.readLine());
                    }else{
                        String[] project = currLine.split(":", 2);
                        if(project[1].charAt(0) == ' '){
                            ProjectTypes.projectTypes.add(project[1].substring(1));
                        }else{
                            ProjectTypes.projectTypes.add(project[1]);
                        }
                    }
                    currLine = reader.readLine();
                }else if(currLine.contains("pil:")){
                    if(currLine.equals("pil:")){
                        ProjectTypes.projectInformationLinks.add(reader.readLine());
                    }else{
                        String[] project = currLine.split(":",2);
                        if(project[1].charAt(0) == ' '){
                            ProjectTypes.projectInformationLinks.add(project[1].substring(1));
                        }else{
                            ProjectTypes.projectInformationLinks.add(project[1]);
                        }
                    }
                    currLine = reader.readLine();
                }else if(currLine.contains("plcc:")){
                    ArrayList<String> landCoverClasses = new ArrayList<String>();
                    if(!currLine.equals("plcc:")){
                        String[] project = currLine.split(":", 2);
                        if(project[1].charAt(0) == ' '){
                            landCoverClasses.add(project[1].substring(1));
                        }else{
                            landCoverClasses.add(project[1]);
                        }
                    }
                    while(!(currLine = reader.readLine()).equals("")){
                        landCoverClasses.add(currLine);
                    }
                    ProjectTypes.projectLandCoverClasses.put(ProjectTypes.projectTypes.get(
                            ProjectTypes.projectTypes.size() - 1
                    ), landCoverClasses);
                }else if(currLine.contains("ae:")){
                    if(currLine.equals("ae:")){
                        AdminInformation.adminEmail = reader.readLine();
                    }else{
                        String[] project = currLine.split(":",2);
                        if(project[1].charAt(0) == ' '){
                            AdminInformation.adminEmail = project[1].substring(1);
                        }else{
                            AdminInformation.adminEmail = project[1];
                        }
                    }
                    currLine = reader.readLine();
                } else if(currLine.equals("vp:")){
                    if(currLine.equals("vp:")){
                        AdminInformation.verificationPassword = reader.readLine();
                    }else{
                        String[] project = currLine.split(":",2);
                        if(project[1].charAt(0) == ' '){
                            AdminInformation.verificationPassword = project[1].substring(1);
                        }else{
                            AdminInformation.verificationPassword = project[1];
                        }
                    }
                    currLine = reader.readLine();
                } else if(currLine.equals("cq:")){
                    if(currLine.equals("cq:")){
                        AdminInformation.compressionQuality = Integer.parseInt(reader.readLine());
                    }else{
                        String[] project = currLine.split(":",2);
                        if(project[1].charAt(0) == ' '){
                            AdminInformation.compressionQuality = Integer.parseInt(project[1].substring(1));
                        }else{
                            AdminInformation.compressionQuality = Integer.parseInt(project[1]);
                        }
                    }
                    currLine = reader.readLine();
                } else if(currLine.equals("mnos:")){
                    if(currLine.equals("mnos:")){
                        AdminInformation.minNumOfSatellites = Integer.parseInt(reader.readLine());
                    }else{
                        String[] project = currLine.split(":",2);
                        if(project[1].charAt(0) == ' '){
                            AdminInformation.minNumOfSatellites = Integer.parseInt(project[1].substring(1));
                        }else{
                            AdminInformation.minNumOfSatellites = Integer.parseInt(project[1]);
                        }
                    }
                    currLine = reader.readLine();
                } else if(currLine.equals("st:")){
                    if(currLine.equals("st:")){
                        AdminInformation.sendType = reader.readLine();
                    }else{
                        String[] project = currLine.split(":",2);
                        if(project[1].charAt(0) == ' '){
                            AdminInformation.sendType = project[1].substring(1);
                        }else{
                            AdminInformation.sendType = project[1];
                        }
                    }
                    currLine = reader.readLine();
                }else{
                    currLine = reader.readLine();
                }
            }
        }catch(IOException e){
            e. printStackTrace();
        }
    }
}
