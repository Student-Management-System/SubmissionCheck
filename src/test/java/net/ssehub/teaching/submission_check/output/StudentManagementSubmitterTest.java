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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.ssehub.exercisesubmitter.protocol.backend.DataNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.DataNotFoundException.DataType;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assessment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment.State;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmissionHookProtocol;
import net.ssehub.studentmgmt.backend_api.model.AssessmentDto;
import net.ssehub.studentmgmt.backend_api.model.PartialAssessmentDto;
import net.ssehub.studentmgmt.backend_api.model.PartialAssessmentDto.SeverityEnum;
import net.ssehub.studentmgmt.backend_api.model.ParticipantDto;
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

        private boolean createInitialPartialAssessments = false;
        
        private boolean submitReturnValue = true;
        
        private Assignment assignment;
        private Assessment assessment;
        
        public MockSubmissionHookProtocol() {
            super(null, null, null, null);
        }
        
        @Override
        public Assignment getAssignmentByName(String name) {
            assignment = new Assignment(name, null, State.SUBMISSION, false, 0);
            return assignment;
        }
        
        @Override
        public Assessment loadAssessmentByName(Assignment assignment, String name) {
            AssessmentDto dto = new AssessmentDto();
            ParticipantDto user = new ParticipantDto();
            user.setDisplayName("A user");
            user.setUsername("auser");
            user.setEmail("email@example.com");
            dto.setParticipant(user);
            
            if (createInitialPartialAssessments) {
                PartialAssessmentDto partialAssessment = new PartialAssessmentDto();
                partialAssessment.setAssessmentId("123");
                partialAssessment.setComment("some old comment");
                partialAssessment.setId(new BigDecimal(321));
                partialAssessment.setPath("Submission/Test.java");
                partialAssessment.setSeverity(SeverityEnum.ERROR);
                partialAssessment.setTitle("some old title");
                partialAssessment.setType("some old type");
                
                dto.addPartialAssessmentsItem(partialAssessment);
            }
            
            assessment = new Assessment(dto, assignment);
            return assessment;
        }
        
        @Override
        public boolean submitAssessment(Assignment assignment, Assessment assessment) {
            return submitReturnValue;
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
    public void noMessage() throws NetworkException {
        // set up submission
        MockSubmissionHookProtocol protocol = new MockSubmissionHookProtocol();
        StudentManagementSubmitter submitter = new StudentManagementSubmitter(protocol);
        Submission submission = new Submission("exercise", "auser");
        
        // run submission
        assertTrue(submitter.submit(submission, Arrays.asList()));
        
        // check that no partial assessment is created
        assertEquals(0, protocol.assessment.partialAsssesmentSize());
    }
    
    @Test
    public void noMessageClearsOldPartialAssessments() throws NetworkException {
        // set up submission
        MockSubmissionHookProtocol protocol = new MockSubmissionHookProtocol();
        protocol.createInitialPartialAssessments = true;
        StudentManagementSubmitter submitter = new StudentManagementSubmitter(protocol);
        Submission submission = new Submission("exercise", "auser");
        
        // run submission
        assertTrue(submitter.submit(submission, Arrays.asList()));
        
        // check that old partial assessment is cleared
        assertEquals(0, protocol.assessment.partialAsssesmentSize());
    }
    
    @Test
    public void oneMessageOverridesOldPartialAssessments() throws NetworkException {
        // set up submission
        MockSubmissionHookProtocol protocol = new MockSubmissionHookProtocol();
        protocol.createInitialPartialAssessments = true;
        StudentManagementSubmitter submitter = new StudentManagementSubmitter(protocol);
        Submission submission = new Submission("exercise", "auser");
        ResultMessage msg = new ResultMessage("Javac", MessageType.ERROR, "A compilation failure.");
        
        // run submission
        assertTrue(submitter.submit(submission, Arrays.asList(msg)));
        
        // check that old partial assessment is cleared and correct one was inserted
        assertEquals(1, protocol.assessment.partialAsssesmentSize());
        PartialAssessmentDto partial = protocol.assessment.getPartialAssessment(0);
        assertEquals(msg.getCheckName(), partial.getType());
        assertEquals(msg.getType().name(), partial.getSeverity().name());
        assertEquals(msg.getMessage(), partial.getComment());
    }
    
    @Test
    public void returnValueFalse() throws NetworkException {
        // set up submission
        MockSubmissionHookProtocol protocol = new MockSubmissionHookProtocol();
        protocol.submitReturnValue = false;
        StudentManagementSubmitter submitter = new StudentManagementSubmitter(protocol);
        Submission submission = new Submission("exercise", "auser");
        
        // run submission
        assertFalse(submitter.submit(submission, Arrays.asList()));
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
        
        DataNotFoundException exc = assertThrows(DataNotFoundException.class, () -> {
            submitter.submit(submission, Arrays.asList(msg));
        });
        
        assertAll(
            () -> assertEquals("For the given submission was no configured assignment found on server", exc.getMessage()),
            () -> assertEquals("exercise", exc.getMissingItem()),
            () -> assertEquals(DataType.ASSIGNMENTS_NOT_FOUND, exc.getType())
        );
    }
    
}
