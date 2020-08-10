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

import java.io.File;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.LoggingSetup;

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
    public void storesMessages() {
        ResultCollector collector = new ResultCollector();
        
        assertThat("Precondition: empty collector should have no messages",
                collector.getMessages(), is(Arrays.asList()));
        
        collector.addMessage(new ResultMessage("javac", MessageType.ERROR, "abc"));
        
        assertThat("Postcondition: collector should store messages",
                collector.getMessages(), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "abc")
                )));
        
        collector.addMessage(new ResultMessage("checkstyle", MessageType.WARNING, "def"));
        
        assertThat("Postcondition: collector should store messages",
                collector.getMessages(), is(Arrays.asList(
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
    public void serializeSingleMessage() {
        ResultCollector collector = new ResultCollector();
        
        collector.addMessage(new ResultMessage("toolname", MessageType.ERROR, "my message"));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                collector.serializeMessages(), is(
                        "<submitResults>" + linefeed
                        + "    <message message=\"my message\" tool=\"toolname\" type=\"error\"/>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializeSingleBasicMessageWithFile() {
        ResultCollector collector = new ResultCollector();
        
        collector.addMessage(new ResultMessage("toolname", MessageType.ERROR, "my message").setFile(new File("dir/file.txt")));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                collector.serializeMessages(), is(
                        "<submitResults>" + linefeed
                        + "    <message file=\"dir/file.txt\" message=\"my message\" tool=\"toolname\" type=\"error\"/>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializeSingleBasicMessageWithFileAndLine() {
        ResultCollector collector = new ResultCollector();
        
        collector.addMessage(new ResultMessage("toolname", MessageType.ERROR, "my message").setFile(new File("dir/file.txt")).setLine(595));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                collector.serializeMessages(), is(
                        "<submitResults>" + linefeed
                        + "    <message file=\"dir/file.txt\" line=\"595\" message=\"my message\" tool=\"toolname\" type=\"error\"/>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializeSingleBasicMessageWithFileAndLineAndColumn() {
        ResultCollector collector = new ResultCollector();
        
        collector.addMessage(new ResultMessage("toolname", MessageType.ERROR, "my message").setFile(new File("dir/file.txt")).setLine(595).setColumn(67));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                collector.serializeMessages(), is(
                        "<submitResults>" + linefeed
                        + "    <message file=\"dir/file.txt\" line=\"595\" message=\"my message\" tool=\"toolname\" type=\"error\">" + linefeed
                        + "        <example position=\"67\"/>" + linefeed
                        + "    </message>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializeSpecialCharacters() {
        ResultCollector collector = new ResultCollector();
        
        collector.addMessage(new ResultMessage("too<l>name", MessageType.ERROR, "my \"message\""));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                collector.serializeMessages(), is(
                        "<submitResults>" + linefeed
                        + "    <message message=\"my &quot;message&quot;\" tool=\"too&lt;l&gt;name\" type=\"error\"/>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializedEmpty() {
        ResultCollector collector = new ResultCollector();
        
        assertThat("Postcondition: serialized messages should be empty for empty collector",
                collector.serializeMessages(), is("<submitResults/>" + System.lineSeparator()));
    }
    
    @Test
    public void serializeMultipleMessages() {
        ResultCollector collector = new ResultCollector();                
        
        collector.addMessage(new ResultMessage("toolB", MessageType.ERROR, "message number 1"));
        collector.addMessage(new ResultMessage("toolA", MessageType.ERROR, "abc is wrong").setFile(new File("abc.txt")));
        collector.addMessage(new ResultMessage("toolA", MessageType.WARNING, "numbers are wrong too").setFile(new File("dir/numbers.txt")).setLine(5));
        collector.addMessage(new ResultMessage("toolC", MessageType.ERROR, "you got many lines").setFile(new File("huge.csv")).setLine(2132132131).setColumn(10));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: format string should contain correct messages",
                collector.serializeMessages(), is(
                        "<submitResults>" + linefeed
                        + "    <message message=\"message number 1\" tool=\"toolB\" type=\"error\"/>" + linefeed
                        + "    <message file=\"abc.txt\" message=\"abc is wrong\" tool=\"toolA\" type=\"error\"/>" + linefeed
                        + "    <message file=\"dir/numbers.txt\" line=\"5\" message=\"numbers are wrong too\" tool=\"toolA\" type=\"warning\"/>" + linefeed
                        + "    <message file=\"huge.csv\" line=\"2132132131\" message=\"you got many lines\" tool=\"toolC\" type=\"error\">" + linefeed
                        + "        <example position=\"10\"/>" + linefeed
                        + "    </message>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @BeforeClass
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }
    
}
