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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.ssehub.exercisesubmitter.protocol.backend.DataNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assessment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment.State;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmissionHookProtocol;
import net.ssehub.studentmgmt.backend_api.model.AssessmentDto;
import net.ssehub.studentmgmt.backend_api.model.PartialAssessmentDto;
import net.ssehub.studentmgmt.backend_api.model.UserDto;
import net.ssehub.teaching.submission_check.ResultMessage;
import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.Submission;

/**
 * Tests the {@link StudentManagementSubmitter}.
 * 
 * @author El-Sharkawy
 */
public class StudentManagementSubmitterTest {
    
    private static class MockSubmissionHookProtocol extends SubmissionHookProtocol {

        private Assignment assignment;
        private Assessment assessment;
        
        public MockSubmissionHookProtocol() {
            super(null, null, null, null);
        }
        
        @Override
        public Assignment getAssignmentByName(String name) {
            assignment = new Assignment(name, null, State.SUBMISSION, false);
            return assignment;
        }
        
        @Override
        public Assessment loadAssessmentByName(Assignment assignment, String name) {
            AssessmentDto dto = new AssessmentDto();
            UserDto user = new UserDto();
            user.setDisplayName("A user");
            user.setUsername("auser");
            user.setEmail("email@example.com");
            dto.setUser(user);
            assessment = new Assessment(dto, assignment);
            return assessment;
        }
        
        @Override
        public boolean submitAssessment(Assignment assignment, Assessment assessment) {
            return true;
        }
        
    }

    @Test
    public void oneBasicMessage() throws NetworkException {
        // Simulate submission
        MockSubmissionHookProtocol protocol = new MockSubmissionHookProtocol();
        StudentManagementSubmitter submitter = new StudentManagementSubmitter(protocol);
        Submission submission = new Submission("exercise", "auser");
        ResultMessage msg = new ResultMessage("Javac", MessageType.ERROR, "A compilation failure.");
        
        // Check that submission was successful
        assertTrue(submitter.submit(submission, Arrays.asList(msg)));
        
        // Check that expected data was submitted
        assertEquals(1, protocol.assessment.partialAsssesmentSize());
        PartialAssessmentDto partial = protocol.assessment.getPartialAssessment(0);
        assertEquals(msg.getCheckName(), partial.getType());
        assertEquals(msg.getType().name(), partial.getSeverity().name());
        assertEquals(msg.getMessage(), partial.getComment());
    }
    
    @Test
    public void oneFullMessage() throws NetworkException {
        // Simulate submission
        MockSubmissionHookProtocol protocol = new MockSubmissionHookProtocol();
        StudentManagementSubmitter submitter = new StudentManagementSubmitter(protocol);
        Submission submission = new Submission("exercise", "auser");
        ResultMessage msg = new ResultMessage("Javac", MessageType.ERROR, "A compilation failure.");
        msg.setFile(new File("src/pkg/Class.java"));
        msg.setLine(1);
        
        // Check that submission was successful
        assertTrue(submitter.submit(submission, Arrays.asList(msg)));
        
        // Check that expected data was submitted
        assertEquals(1, protocol.assessment.partialAsssesmentSize());
        PartialAssessmentDto partial = protocol.assessment.getPartialAssessment(0);
        assertEquals(msg.getCheckName(), partial.getType());
        assertEquals(msg.getType().name(), partial.getSeverity().name());
        assertEquals(msg.getMessage(), partial.getComment());
        // TODO SE: Currently not fully supported by the student management server
    }
    
    @Test
    public void invalidAssignment() throws NetworkException {
        MockSubmissionHookProtocol protocol = new MockSubmissionHookProtocol() {
            @Override
            public Assignment getAssignmentByName(String name) {
                return null;
            }
        };
        
        StudentManagementSubmitter submitter = new StudentManagementSubmitter(protocol);
        Submission submission = new Submission("exercise", "auser");
        ResultMessage msg = new ResultMessage("Javac", MessageType.ERROR, "A compilation failure.");
        
        assertThrows(DataNotFoundException.class, () -> {
            submitter.submit(submission, Arrays.asList(msg));
        });
    }
    
}
