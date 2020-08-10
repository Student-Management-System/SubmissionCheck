/*
 * Copyright 2020 Software Systems Engineering, University of Hildesheim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.teaching.submission_check.checks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.ssehub.teaching.submission_check.ResultMessage;
import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.eclipse_config.EclipseClasspathFile;
import net.ssehub.teaching.submission_check.eclipse_config.EclipseProjectFile;
import net.ssehub.teaching.submission_check.eclipse_config.InvalidEclipseConfigException;

/**
 * Checks that the submission has a valid eclipse project configuration. 
 * 
 * @author Adam
 */
public class EclipseConfigCheck extends Check {
    
    public static final String CHECK_NAME = "eclipse-configuration";

    private boolean requireJavaProject;
    
    private boolean requireCheckstyleProject;

    /**
     * Specifies whether Java projects are required. If this is set to <code>true</code>, the nature and buildCommands
     * are checked for a Java configuration. By default, this is <code>false</code>.
     * 
     * @param requireJavaProject Whether a Java project setup should be required.
     */
    public void setRequireJavaProject(boolean requireJavaProject) {
        this.requireJavaProject = requireJavaProject;
    }

    /**
     * Specifies whether Checkstyle projects are required. If this is set to <code>true</code>, the nature and
     * buildCommands are checked for a Checkstyle configuration. By default, this is <code>false</code>.
     * 
     * @param requireCheckstyleProject Whether a Checkstyle project setup should be required.
     */
    public void setRequireCheckstyleProject(boolean requireCheckstyleProject) {
        this.requireCheckstyleProject = requireCheckstyleProject;
    }
    
    /**
     * Returns the configured value whether Java projects are required.
     * 
     * @return The configured value.
     * 
     * @see #setRequireJavaProject(boolean)
     */
    public boolean getRequireJavaProject() {
        return requireJavaProject;
    }
    
    /**
     * Returns the configured value whether Checkstyle projects are required.
     * 
     * @return The configured value.
     * 
     * @see #setRequireCheckstyleProject(boolean)
     */
    public boolean getRequireCheckstyleProject() {
        return requireCheckstyleProject;
    }
    
    @Override
    public boolean run(File submissionDirectory) {
        boolean success;
        
        try {
            new EclipseClasspathFile(new File(submissionDirectory, ".classpath"));
            EclipseProjectFile project = new EclipseProjectFile(new File(submissionDirectory, ".project"));
            
            success = true;
            
            if (requireJavaProject) {
                if (!project.getBuilders().contains(EclipseProjectFile.BUILDER_JAVA)
                        && !project.getNatures().contains(EclipseProjectFile.NATURE_JAVA)) {
                    success = false;
                    
                    ResultMessage message = new ResultMessage(CHECK_NAME, MessageType.ERROR,
                            "Submission is not a Java project");
                    message.setFile(new File(".project"));
                    
                    addResultMessage(message);
                }
            }
            
            if (requireCheckstyleProject) {
                if (!project.getBuilders().contains(EclipseProjectFile.BUILDER_CHECKSTYLE)
                        && !project.getNatures().contains(EclipseProjectFile.NATURE_CHECKSTYLE)) {
                    
                    ResultMessage message = new ResultMessage(CHECK_NAME, MessageType.WARNING,
                            "Submission does not have Checkstyle enabled");
                    message.setFile(new File(".project"));
                    
                    addResultMessage(message);
                }
            }
            
        } catch (InvalidEclipseConfigException | FileNotFoundException e) {
            success = false;
            addResultMessage(new ResultMessage(CHECK_NAME, MessageType.ERROR,
                    "Does not contain a valid eclipse project"));
            
        } catch (IOException e) {
            success = false;
            addResultMessage(new ResultMessage(CHECK_NAME, MessageType.ERROR,
                    "An internal error occurred while checking eclipse project"));
        }
        
        return success;
    }

}
