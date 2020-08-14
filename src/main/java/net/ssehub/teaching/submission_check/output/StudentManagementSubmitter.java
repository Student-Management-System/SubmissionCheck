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
    
    private SubmissionHookProtocol protocol;
    
    /**
     * This constructor is mostly intended for testing and allows to mock the internally used network protocol.
     * @param protocol The protocol to use.
     */
    protected StudentManagementSubmitter(SubmissionHookProtocol protocol) {
        this.protocol = protocol;
    }
    
    /**
     * Creates a new {@link StudentManagementSubmitter} instance to submit {@link ResultMessage}s
     * to the <b>Student Management Server</b>.
     * @param authenticationURL The URL of the authentication server (aka Sparky service)
     * @param stdMgmtURL The URL of the student management service
     * @param courseName The course that is associated with the exercise submitter.
     * @param semester The semester for which the submission shall be reviewed (in form of 4 letters and 4 digits)
     * @param submissionServer The root (URL) where to submit assignments (exercises).
     * @param userName The user name to submit the results
     * @param password The password belonging to the user.
     * @throws UnknownCredentialsException If userName and password do not match to a valid (tutor)
     *     account on the server.
     * @throws ServerNotFoundException If one of the two servers is unreachable by the specified URLs.
     */
    //checkstyle: stop parameter number check
    public StudentManagementSubmitter(String authenticationURL, String stdMgmtURL, String courseName, String semester,
        String submissionServer, String userName, String password) throws UnknownCredentialsException,
        ServerNotFoundException {
    //checkstyle: resume parameter number check
        
        this(new SubmissionHookProtocol(authenticationURL, stdMgmtURL, courseName, submissionServer));
        protocol.setSemester(semester);
        protocol.login(userName, password);
    }
    
    
    /**
     * Submits the results of the automatic tests of one submission to the <b>Student Management Server</b>.
     * @param submission The checked submission.
     * @param messages The results of the automatic tests to submit.
     * 
     * @return <tt>true</tt> if submission was successful, <tt>false</tt> otherwise.
     * @throws NetworkException When network problems occur (i.e., server not reachable, user not authorized).
     */
    public boolean submit(Submission submission, List<ResultMessage> messages) throws NetworkException {
        Assignment assignment = protocol.getAssignmentByName(submission.getExercise());
        if (null == assignment) {
            throw new DataNotFoundException("For the given submission was no configured assignment found on server.",
                submission.getExercise(), DataType.ASSIGNMENTS_NOT_FOUND);
        }
        
        Assessment assessment = protocol.loadAssessmentByName(assignment, submission.getGroup());
        assessment.clearPartialAssessments();
        for (ResultMessage msg : messages) {
            String lineNo = msg.getLine() != null ? msg.getLine().toString() : null;
            String path = msg.getFile() != null ? msg.getFile().getPath() : null;
            assessment.addAutomaticReview(msg.getCheckName(), msg.getType().name(), msg.getMessage(), path, lineNo);
        }
        return protocol.submitAssessment(assignment, assessment);
    }

}
