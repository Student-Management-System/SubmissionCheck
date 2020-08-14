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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.checks.MockCheck;
import net.ssehub.teaching.submission_check.utils.LoggingSetup;

public class CheckRunnerTest {
    
    private CheckRunner runner;
    
    private ResultCollector collector;
    
    @Before
    public void initRunner() {
        this.collector = new ResultCollector();
        this.runner = new CheckRunner(collector);
    }
    
    @Test
    public void runWithNoChecks() {
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running no checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running no checks should succeed",
                collector.getAllSuccessful(), is(true));
        
        assertThat("Postcondition: running no checks should create no messages",
                collector.getAllMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void singleSuccessfulCheckNoMessages() {
        runner.addCheck(new MockCheck(true));
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running successful checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running successful checks should succeed",
                collector.getAllSuccessful(), is(true));
        
        assertThat("Postcondition: no check created any message",
                collector.getAllMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void singleSuccessfulCheckWithMessage() {
        runner.addCheck(new MockCheck(true, new ResultMessage("sometool", MessageType.WARNING, "some message")));
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running successful checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running successful checks should succeed",
                collector.getAllSuccessful(), is(true));
        
        assertThat("Postcondition: should return created message",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("sometool", MessageType.WARNING, "some message")
                )));
    }
    
    @Test
    public void singleSuccessfulCheckWithMultipleMessages() {
        runner.addCheck(new MockCheck(true,
                new ResultMessage("sometool", MessageType.ERROR, "some message"),
                new ResultMessage("sometool", MessageType.WARNING, "some other message")
        ));
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running successful checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running successful checks should succeed",
                collector.getAllSuccessful(), is(true));
        
        assertThat("Postcondition: should return created messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("sometool", MessageType.ERROR, "some message"),
                        new ResultMessage("sometool", MessageType.WARNING, "some other message")
                )));
    }
    
    @Test
    public void multipleSuccessfulWithMessage() {
        runner.addCheck(new MockCheck(true, new ResultMessage("sometool", MessageType.ERROR, "some message")));
        runner.addCheck(new MockCheck(true, new ResultMessage("othertool", MessageType.WARNING, "second tool")));
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running successful checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running successful checks should succeed",
                collector.getAllSuccessful(), is(true));
        
        assertThat("Postcondition: should return created messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("sometool", MessageType.ERROR, "some message"),
                        new ResultMessage("othertool", MessageType.WARNING, "second tool")
                )));
    }
    
    @Test
    public void clearRemovesChecks() {
        runner.addCheck(new MockCheck(false, new ResultMessage("sometool", MessageType.ERROR, "some message")));
        
        runner.clearChecks();
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running with no checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running with no checks should succeed",
                collector.getAllSuccessful(), is(true));
        
        assertThat("Postcondition: should not create any messages",
                collector.getAllMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void singleUnsuccessfulNoMessage() {
        runner.addCheck(new MockCheck(false));
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running unsuccessful checks should not succeed",
                success, is(false));
        
        assertThat("Postcondition: running unsuccessful checks should not succeed",
                collector.getAllSuccessful(), is(false));
        
        assertThat("Postcondition: no check created any message",
                collector.getAllMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void singleUnsuccessfulWithMessages() {
        runner.addCheck(new MockCheck(false,
                new ResultMessage("sometool", MessageType.ERROR, "some message"),
                new ResultMessage("sometool", MessageType.WARNING, "some other message")
        ));
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running unsuccessful checks should not succeed",
                success, is(false));
        
        assertThat("Postcondition: running unsuccessful checks should not succeed",
                collector.getAllSuccessful(), is(false));
        
        assertThat("Postcondition: should return created messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("sometool", MessageType.ERROR, "some message"),
                        new ResultMessage("sometool", MessageType.WARNING, "some other message")
                )));
    }
    
    @Test
    public void firstUnsuccessfulBlocksFurtherTests() {
        runner.addCheck(new MockCheck(true, new ResultMessage("firsttool", MessageType.WARNING, "first passes")));
        runner.addCheck(new MockCheck(false, new ResultMessage("secondtool", MessageType.ERROR, "second fails")));
        runner.addCheck(new MockCheck(true, new ResultMessage("thirdtool", MessageType.ERROR, "third doesn't run")));
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running unsuccessful checks should not succeed",
                success, is(false));
        
        assertThat("Postcondition: running unsuccessful checks should not succeed",
                collector.getAllSuccessful(), is(false));
        
        assertThat("Postcondition: should return created messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("firsttool", MessageType.WARNING, "first passes"),
                        new ResultMessage("secondtool", MessageType.ERROR, "second fails")
                )));
    }
    
    @Test
    public void multipleRunsAccumulate() {
        MockCheck check = new MockCheck(true, new ResultMessage("sometool", MessageType.ERROR, "first passes"));
        
        runner.addCheck(check);
        
        boolean success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running successful checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running successful checks should succeed",
                collector.getAllSuccessful(), is(true));
        
        assertThat("Postcondition: should return created messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("sometool", MessageType.ERROR, "first passes")
                )));
        
        check.setResultMessages(new ResultMessage("sometool", MessageType.WARNING, "second run passes too"));
        
        success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running successful checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running successful checks should succeed",
                collector.getAllSuccessful(), is(true));
        
        assertThat("Postcondition: should return created messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("sometool", MessageType.ERROR, "first passes"),
                        new ResultMessage("sometool", MessageType.WARNING, "second run passes too")
                )));
        
        check.setSuccess(false);
        check.setResultMessages(new ResultMessage("sometool", MessageType.ERROR, "third doesn't pass"));
        
        success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running unsuccessful checks should not succeed",
                success, is(false));
        
        assertThat("Postcondition: running unsuccessful checks should succeed",
                collector.getAllSuccessful(), is(false));
        
        assertThat("Postcondition: should return created messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("sometool", MessageType.ERROR, "first passes"),
                        new ResultMessage("sometool", MessageType.WARNING, "second run passes too"),
                        new ResultMessage("sometool", MessageType.ERROR, "third doesn't pass")
                )));
        
        check.setSuccess(true);
        check.setResultMessages(new ResultMessage("sometool", MessageType.ERROR, "fourth passes again"));
        
        success = runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: running successful checks should succeed",
                success, is(true));
        
        assertThat("Postcondition: running unsuccessful checks should succeed",
                collector.getAllSuccessful(), is(false));
        
        assertThat("Postcondition: should return created messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("sometool", MessageType.ERROR, "first passes"),
                        new ResultMessage("sometool", MessageType.WARNING, "second run passes too"),
                        new ResultMessage("sometool", MessageType.ERROR, "third doesn't pass"),
                        new ResultMessage("sometool", MessageType.ERROR, "fourth passes again")
                )));
    }
    
    @Test
    public void storesMessagesPerSubmission() {
        runner.addCheck(new MockCheck(true, new ResultMessage("javac", MessageType.ERROR, "doesn't compile")));
        
        runner.run(new Submission("Exercise01", "Group01"), new File(""));
        
        assertThat("Postcondition: should have all messages",
                collector.getAllMessages(), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "doesn't compile")
                )));
        assertThat("Postcondition: should have message by submission",
                collector.getMessageForSubmission(new Submission("Exercise01", "Group01")), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "doesn't compile")
                )));
    }
    
    
    @BeforeClass
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }

}
