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

/**
 * Contains configuration for the communication with the Student Management System.
 * 
 * @author Adam
 */
public class StudentManagementConfig {

    private String authenticationUrl;
    
    private String studentManagemenSystemtUrl;
    
    private String courseName;
    
    private String semester;
    
    private String userName;
    
    private String password;

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
    public String getStudentManagemenSystemtUrl() {
        return studentManagemenSystemtUrl;
    }
    
    /**
     * Sets the URL to the Student Management System.
     * 
     * @param studentManagemenSystemtUrl The Student Management System URL.
     */
    public void setStudentManagemenSystemtUrl(String studentManagemenSystemtUrl) {
        this.studentManagemenSystemtUrl = studentManagemenSystemtUrl;
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
    public String getSemester() {
        return semester;
    }
    
    /**
     * Sets the semester of the course that the submission for this hook is for.
     * 
     * @param semester The course semester.
     */
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    /**
     * Returns the user-name to authenticate with.
     * 
     * @return The user-name for authentication.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Sets the user-name to authenticate with.
     * 
     * @param userName The user-name for authentication.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * Returns the password to authenticate with.
     * 
     * @return The password for authentication.
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password to authenticate with.
     * 
     * @param password The password for authentication.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
}
