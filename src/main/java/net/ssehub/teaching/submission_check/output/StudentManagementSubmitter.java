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
package net.ssehub.teaching.submission_check.output;

import java.util.List;

import net.ssehub.exercisesubmitter.protocol.backend.DataNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.DataNotFoundException.DataType;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.UnknownCredentialsException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assessment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmissionHookProtocol;
import net.ssehub.teaching.submission_check.ResultMessage;
import net.ssehub.teaching.submission_check.Submission;

/**
 * Submits {@link ResultMessage}s to the <b>Student Management Server</b>.
 * 
 * @author El-Sharkawy
 */
public class StudentManagementSubmitter {
    
    /**
     * Contains configuration for the communication with the Student Management System.
     * 
     * @author Adam
     */
    public static class StudentManagementConfig {

        private String url;
        
        private String courseName;
        
        private String courseSemester;
        
        private String authenticationUrl;
        
        private String authenticationUsername;
        
        private String authenticationPassword;

        /**
         * Returns the URL to the authentication system.
         * 
         * @return The authentication system URL.
         */
        public String getAuthenticationUrl() {
            return authenticationUrl;
        }

        /**
         * Sets the URL to the authentication system.
         * 
         * @param authenticationUrl The authentication system URL.
         */
        public void setAuthenticationUrl(String authenticationUrl) {
            this.authenticationUrl = authenticationUrl;
        }

        /**
         * Returns the URL to the Student Management System.
         * 
         * @return The Student Management System URL.
         */
        public String getUrl() {
            return url;
        }
        
        /**
         * Sets the URL to the Student Management System.
         * 
         * @param studentManagemenSystemtUrl The Student Management System URL.
         */
        public void setUrl(String studentManagemenSystemtUrl) {
            this.url = studentManagemenSystemtUrl;
        }

        /**
         * Returns the name of the course that the submission for this hook is for.
         * 
         * @return The course name.
         */
        public String getCourseName() {
            return courseName;
        }
        
        /**
         * Sets the name of the course that the submission for this hook is for.
         * 
         * @param courseName The course name.
         */
        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
        
        /**
         * Returns the semester of the course that the submission for this hook is for.
         * 
         * @return The course semester.
         */
        public String getCourseSemester() {
            return courseSemester;
        }
        
        /**
         * Sets the semester of the course that the submission for this hook is for.
         * 
         * @param semester The course semester.
         */
        public void setCourseSemester(String semester) {
            this.courseSemester = semester;
        }
        
        /**
         * Returns the user-name to authenticate with.
         * 
         * @return The user-name for authentication.
         */
        public String getAuthenticationUsername() {
            return authenticationUsername;
        }
        
        /**
         * Sets the user-name to authenticate with.
         * 
         * @param userName The user-name for authentication.
         */
        public void setAuthenticationUsername(String userName) {
            this.authenticationUsername = userName;
        }
        
        /**
         * Returns the password to authenticate with.
         * 
         * @return The password for authentication.
         */
        public String getAuthenticationPassword() {
            return authenticationPassword;
        }
        
        /**
         * Sets the password to authenticate with.
         * 
         * @param password The password for authentication.
         */
        public void setAuthenticationPassword(String password) {
            this.authenticationPassword = password;
        }
        
    }
    
    private SubmissionHookProtocol protocol;
    
    /**
     * This constructor is intended for testing and allows to mock the internally used network protocol.
     * 
     * @param protocol The protocol to use.
     */
    protected StudentManagementSubmitter(SubmissionHookProtocol protocol) {
        this.protocol = protocol;
    }
    
    /**
     * Creates a new {@link StudentManagementSubmitter} instance to submit {@link ResultMessage}s
     * to the <b>Student Management Server</b>.
     * 
     * @param configuration The configuration parameters for the communication with the student management system.
     * 
     * @throws UnknownCredentialsException If userName and password do not match to a valid (tutor)
     *     account on the server.
     * @throws ServerNotFoundException If one of the two servers is unreachable by the specified URLs.
     */
    public StudentManagementSubmitter(StudentManagementConfig configuration)
            throws UnknownCredentialsException, ServerNotFoundException {
        
        this(new SubmissionHookProtocol(
                configuration.getAuthenticationUrl(),
                configuration.getUrl(),
                configuration.getCourseName(),
                null // submission server is irrelevant
        ));
        
        protocol.setSemester(configuration.getCourseSemester());
        protocol.login(configuration.getAuthenticationUsername(), configuration.getAuthenticationPassword());
    }
    
    
    /**
     * Submits the results of the automatic tests of one submission to the <b>Student Management Server</b>.
     * 
     * @param submission The checked submission.
     * @param messages The results of the automatic tests to submit.
     * 
     * @return <code>true</code> if submission was successful, <code>false</code> otherwise.
     * 
     * @throws NetworkException When network problems occur (i.e., server not reachable, user not authorized).
     */
    public boolean submit(Submission submission, List<ResultMessage> messages) throws NetworkException {
        Assignment assignment = protocol.getAssignmentByName(submission.getExercise());
        if (null == assignment) {
            throw new DataNotFoundException("For the given submission was no configured assignment found on server",
                submission.getExercise(), DataType.ASSIGNMENTS_NOT_FOUND);
        }
        
        Assessment assessment = protocol.loadAssessmentByName(assignment, submission.getGroup());
        assessment.clearPartialAssessments();
        
        for (ResultMessage msg : messages) {
            String path = msg.getFile() != null ? msg.getFile().getPath() : null;
            assessment.addAutomaticReview(msg.getCheckName(), msg.getType().name(), msg.getMessage(), path,
                msg.getLine());
        }
        
        return protocol.submitAssessment(assignment, assessment);
    }

}
