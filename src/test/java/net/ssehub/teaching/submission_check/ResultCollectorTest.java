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
package net.ssehub.teaching.submission_check;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;

public class ResultCollectorTest {

    @Test
    public void exitCodeEmpty() {
        ResultCollector collector = new ResultCollector();
        
        assertThat("Postcondition: exit code should be success for empty collector",
                collector.getExitCode(Phase.PRE_COMMIT), is(0));
        assertThat("Postcondition: exit code should be success for empty collector",
                collector.getExitCode(Phase.POST_COMMIT), is(0));
    }
    
    @Test
    public void exitCodeSuccess() {
        ResultCollector collector = new ResultCollector();
        
        collector.addCheckResult(true);
        
        assertThat("Postcondition: exit code should be success for successful check results",
                collector.getExitCode(Phase.PRE_COMMIT), is(0));
        assertThat("Postcondition: exit code should be success for successful check results",
                collector.getExitCode(Phase.POST_COMMIT), is(0));
        
        collector.addCheckResult(true);
        collector.addCheckResult(true);
        collector.addCheckResult(true);
        
        assertThat("Postcondition: exit code should be success for successful check results",
                collector.getExitCode(Phase.PRE_COMMIT), is(0));
        assertThat("Postcondition: exit code should be success for successful check results",
                collector.getExitCode(Phase.POST_COMMIT), is(0));
    }
    
    @Test
    public void exitCodeUnsuccessful() {
        ResultCollector collector = new ResultCollector();
        
        collector.addCheckResult(false);
        
        assertThat("Postcondition: exit code should be failure for unsuccessful check results",
                collector.getExitCode(Phase.PRE_COMMIT), is(1));
        assertThat("Postcondition: exit code should be failure for unsuccessful check results",
                collector.getExitCode(Phase.POST_COMMIT), is(1));
        
        collector.addCheckResult(true);
        
        assertThat("Postcondition: exit code should still be failure for previous unsuccessful check result",
                collector.getExitCode(Phase.PRE_COMMIT), is(1));
        assertThat("Postcondition: exit code should still be failure for previous unsuccessful check result",
                collector.getExitCode(Phase.POST_COMMIT), is(1));
    }
    
    @Test
    public void messagesSetsPostCommitToFail() {
        ResultCollector collector = new ResultCollector();
        
        collector.addMessage(new ResultMessage("javac", MessageType.WARNING, "some message"));
        
        assertThat("Postcondition: pre-commit should succeed with messages pending",
                collector.getExitCode(Phase.PRE_COMMIT), is(0));
        assertThat("Postcondition: post-commit should not succeed with messages pending",
                collector.getExitCode(Phase.POST_COMMIT), is(1));
    }
    
    @Test
    public void storesAllMessages() {
        ResultCollector collector = new ResultCollector();
        
        assertThat("Precondition: empty collector should have no messages",
                collector.getAllMessages(), is(Arrays.asList()));
        
        collector.addMessage(new ResultMessage("javac", MessageType.ERROR, "abc"));
        
        assertThat("Postcondition: collector should store messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc")
                )));
        
        collector.addMessage(new ResultMessage("checkstyle", MessageType.WARNING, "def"));
        
        assertThat("Postcondition: collector should store messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc"),
                        new ResultMessage("checkstyle", MessageType.WARNING, "def")
                )));
    }
    
    @Test
    public void storesSucessConjunction() {
        ResultCollector collector = new ResultCollector();
        
        assertThat("Precondition: empty collector should have success stored",
                collector.getAllSuccessful(), is(true));
        
        collector.addCheckResult(true);
        assertThat("Postcondition: positive check results should let success true",
                collector.getAllSuccessful(), is(true));
        collector.addCheckResult(true);
        assertThat("Postcondition: positive check results should let success true",
                collector.getAllSuccessful(), is(true));
        
        collector.addCheckResult(false);
        assertThat("Postcondition: negative check results should turn success false",
                collector.getAllSuccessful(), is(false));
        
        collector.addCheckResult(true);
        assertThat("Postcondition: further results do not modify failure",
                collector.getAllSuccessful(), is(false));
        
        collector.addCheckResult(false);
        assertThat("Postcondition: further results do not modify failure",
                collector.getAllSuccessful(), is(false));
    }
    
    @Test
    public void storesMessagesForSubmission() {
        ResultCollector collector = new ResultCollector();
        
        Submission s1 = new Submission("Exercise01", "Group01");
        Submission s2 = new Submission("Exercise02", "Group01");
        
        assertThat("Precondition: empty collector should have no messages",
                collector.getMessageForSubmission(s1), is(Arrays.asList()));
        assertThat("Precondition: empty collector should have no messages",
                collector.getMessageForSubmission(s2), is(Arrays.asList()));
        
        collector.addMessage(new ResultMessage("javac", MessageType.ERROR, "abc"), s1);
        
        assertThat("Postcondition: collector should store for correct exercise",
                collector.getMessageForSubmission(s1), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc")
                )));
        assertThat("Postcondition: collector should store for correct exercise",
                collector.getMessageForSubmission(s2), is(Arrays.asList()));
        
        assertThat("Postcondition: should store all messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc")
                )));
        
        collector.addMessage(new ResultMessage("checkstyle", MessageType.WARNING, "def"), s2);
        
        assertThat("Postcondition: collector should store for correct exercise",
                collector.getMessageForSubmission(s1), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc")
                )));
        assertThat("Postcondition: collector should store for correct exercise",
                collector.getMessageForSubmission(s2), is(Arrays.asList(
                        new ResultMessage("checkstyle", MessageType.WARNING, "def")
                )));
        
        assertThat("Postcondition: should store all messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc"),
                        new ResultMessage("checkstyle", MessageType.WARNING, "def")
                )));
        
        collector.addMessage(new ResultMessage("encoding", MessageType.ERROR, "Invalid Encoding"), s1);
        
        assertThat("Postcondition: collector should store for correct exercise",
                collector.getMessageForSubmission(s1), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc"),
                        new ResultMessage("encoding", MessageType.ERROR, "Invalid Encoding")
                )));
        assertThat("Postcondition: collector should store for correct exercise",
                collector.getMessageForSubmission(s2), is(Arrays.asList(
                        new ResultMessage("checkstyle", MessageType.WARNING, "def")
                )));
        
        assertThat("Postcondition: should store all messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc"),
                        new ResultMessage("checkstyle", MessageType.WARNING, "def"),
                        new ResultMessage("encoding", MessageType.ERROR, "Invalid Encoding")
                )));
        
    }
    
}
