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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.svn.CliSvnInterface;
import net.ssehub.teaching.submission_check.svn.CliSvnInterfaceIT;
import net.ssehub.teaching.submission_check.svn.SvnException;
import net.ssehub.teaching.submission_check.svn.TransactionInfo;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.LoggingSetup;

public class SubmissionHookIT {

    private static final File TESTDATA = new File("src/test/resources/SubmissionHookIT");
    
    @Test
    public void postQueryMetadata() throws IOException, SvnException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "3"}, new CliSvnInterface());
        
        hook.queryMetadataFromSvn();
        
        assertThat("Postcondition: has correct transaction info",
                hook.getTransactionInfo(), is(new TransactionInfo(repo, "student2", "3", Phase.POST_COMMIT)));
    }
    
    @Test
    public void preQueryMetadata() throws IOException, SvnException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2GroupsInTransaction_4-4.zip"));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"PRE", repo.getPath(), "4-4"}, new CliSvnInterface());
        
        hook.queryMetadataFromSvn();
        
        assertThat("Postcondition: has correct transaction info",
                hook.getTransactionInfo(), is(new TransactionInfo(repo, "student1", "4-4", Phase.PRE_COMMIT)));
    }
    
    @Test
    public void postGetModifiedSubmission() throws IOException, SvnException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "3"},  new CliSvnInterface());
        
        hook.queryMetadataFromSvn();
        Set<Submission> modifiedSubmissions = hook.getModifiedSubmissions();
        
        assertThat("Postcondition: has correct set of modified submissions",
                modifiedSubmissions, is(new HashSet<>(Arrays.asList(
                        new Submission("Homework01Task01", "Group02"),
                        new Submission("Homework02Task02", "Group02")
                ))));
    }
    
    @Test
    public void preGetModifiedSubmission() throws IOException, SvnException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2GroupsInTransaction_4-4.zip"));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"PRE", repo.getPath(), "4-4"},  new CliSvnInterface());
        
        hook.queryMetadataFromSvn();
        Set<Submission> modifiedSubmissions = hook.getModifiedSubmissions();
        
        assertThat("Postcondition: has correct set of modified submissions",
                modifiedSubmissions, is(new HashSet<>(Arrays.asList(
                        new Submission("Homework02Task02", "Group01")
                ))));
    }
    
    @Test
    public void postRunChecksNoEclipseSubmission() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        File configurationFile = new File(TESTDATA, "basicConfig.properties");
        assertThat("Precondition: test configuration should exit",
                configurationFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "3"}, new CliSvnInterface());

        hook.readConfiguration(configurationFile);
        
        hook.queryMetadataFromSvn();
        hook.runChecksOnAllModifiedSubmissions();
        
        assertThat("Postcondition: execution should not be successful",
                hook.getResultCollector().getAllSuccessful(), is(false));
        assertThat("Postcondition: collector should have correct messages",
                hook.getResultCollector().getAllMessages(), is(Arrays.asList(
                        new ResultMessage("eclipse-configuration", MessageType.ERROR, "Does not contain a valid eclipse project"),
                        new ResultMessage("eclipse-configuration", MessageType.ERROR, "Does not contain a valid eclipse project")
                )));
    }
    
    @Test
    public void preRunChecksNoEclipseSubmission() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2GroupsInTransaction_4-4.zip"));
        
        File configurationFile = new File(TESTDATA, "basicConfig.properties");
        assertThat("Precondition: test configuration should exit",
                configurationFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"PRE", repo.getPath(), "4-4"}, new CliSvnInterface());

        hook.readConfiguration(configurationFile);
        
        hook.queryMetadataFromSvn();
        hook.runChecksOnAllModifiedSubmissions();
        
        assertThat("Postcondition: execution should not be successful",
                hook.getResultCollector().getAllSuccessful(), is(false));
        assertThat("Postcondition: collector should have not messages",
                hook.getResultCollector().getAllMessages(), is(Arrays.asList(
                        new ResultMessage("eclipse-configuration", MessageType.ERROR, "Does not contain a valid eclipse project")
                )));
    }
    
    @Test
    public void postRunChecksJavacError() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        File configurationFile = new File(TESTDATA, "basicConfig.properties");
        assertThat("Precondition: test configuration should exit",
                configurationFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "5"},  new CliSvnInterface());

        hook.readConfiguration(configurationFile);
        
        hook.queryMetadataFromSvn();
        hook.runChecksOnAllModifiedSubmissions();
        
        assertThat("Postcondition: execution should not be successful",
                hook.getResultCollector().getAllSuccessful(), is(false));
        assertThat("Postcondition: collector should have correct messages",
                hook.getResultCollector().getAllMessages(), is(Arrays.asList(
                        new ResultMessage("javac", MessageType.ERROR, "';' expected").setFile(new File("Main.java")).setLine(4).setColumn(51)
                )));
    }
    
    @Test
    public void postRunChecksCheckstyleError() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        File configurationFile = new File(TESTDATA, "basicConfig.properties");
        assertThat("Precondition: test configuration should exit",
                configurationFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "6"}, new CliSvnInterface());

        hook.readConfiguration(configurationFile);
        
        hook.queryMetadataFromSvn();
        hook.runChecksOnAllModifiedSubmissions();
        
        assertThat("Postcondition: execution should not be successful",
                hook.getResultCollector().getAllSuccessful(), is(false));
        assertThat("Postcondition: collector should have correct messages",
                hook.getResultCollector().getAllMessages(), is(Arrays.asList(
                        new ResultMessage("checkstyle", MessageType.ERROR, "File contains tab characters (this is the first instance)")
                                .setFile(new File("Main.java")).setLine(3).setColumn(1),
                        new ResultMessage("checkstyle", MessageType.ERROR, "'method def modifier' has incorrect indentation level 8, expected level should be 4")
                                .setFile(new File("Main.java")).setLine(3).setColumn(9),
                        new ResultMessage("checkstyle", MessageType.ERROR, "'method def' child has incorrect indentation level 16, expected level should be 8")
                                .setFile(new File("Main.java")).setLine(4).setColumn(17),
                        new ResultMessage("checkstyle", MessageType.ERROR, "'method def rcurly' has incorrect indentation level 8, expected level should be 4")
                                .setFile(new File("Main.java")).setLine(5).setColumn(9)
                )));
    }
    
    @Test
    public void postRunChecksCorrect() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        File configurationFile = new File(TESTDATA, "basicConfig.properties");
        assertThat("Precondition: test configuration should exit",
                configurationFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "7"},  new CliSvnInterface());

        hook.readConfiguration(configurationFile);
        
        hook.queryMetadataFromSvn();
        hook.runChecksOnAllModifiedSubmissions();
        
        assertThat("Postcondition: execution should be successful",
                hook.getResultCollector().getAllSuccessful(), is(true));
        assertThat("Postcondition: collector should have no messages",
                hook.getResultCollector().getAllMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void postExecuteCorrect() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        File configurationFile = new File(TESTDATA, "basicConfig.properties");
        assertThat("Precondition: test configuration should exit",
                configurationFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "7"},  new CliSvnInterface());
        
        PrintStream oldOut = System.err;
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(stderr));
            
            int exitCode = hook.execute(configurationFile);
            
            assertThat("Postcondition: successful exit code should be 0",
                    exitCode, is(0));
            assertThat("Postcondition: stderr should contain empty result set",
                    new String(stderr.toByteArray()).trim(), is("<submitResults/>"));
            
        } finally {
            System.setErr(oldOut);
        }
    }
    
    @Test
    public void postExecuteJavacErrors() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        File configurationFile = new File(TESTDATA, "basicConfig.properties");
        assertThat("Precondition: test configuration should exit",
                configurationFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "5"},  new CliSvnInterface());
        
        PrintStream oldOut = System.err;
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(stderr));
            
            int exitCode = hook.execute(configurationFile);
            
            assertThat("Postcondition: unsuccessful exit code should be 1",
                    exitCode, is(1));
            
            String linefeed = System.lineSeparator();
            assertThat("Postcondition: stderr should result messages",
                    new String(stderr.toByteArray()).trim(), is(
                            "<submitResults>" + linefeed
                            + "    <message file=\"Main.java\" line=\"4\" message=\"';' expected\" tool=\"javac\" type=\"error\">" + linefeed
                            + "        <example position=\"51\"/>" + linefeed
                            + "    </message>" + linefeed
                            + "</submitResults>"
                    ));
            
        } finally {
            System.setErr(oldOut);
        }
    }
    
    @Test
    public void postExecuteUnrestrictedUser() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        File configurationFile = new File(TESTDATA, "unrestrictedStudent2.properties");
        assertThat("Precondition: test configuration should exit",
                configurationFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", repo.getPath(), "5"},  new CliSvnInterface());
        
        PrintStream oldOut = System.err;
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(stderr));
            
            int exitCode = hook.execute(configurationFile);
            
            assertThat("Postcondition: successful exit code should be 0",
                    exitCode, is(0));
            assertThat("Postcondition: stderr should contain empty result set",
                    new String(stderr.toByteArray()).trim(), is("<submitResults/>"));
            
        } finally {
            System.setErr(oldOut);
        }
    }
    
    @Test
    public void executeNonExistingConfigFile() throws IOException, SvnException, ConfigurationException {
        File repo = CliSvnInterfaceIT.prepareTestdataSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        File configurationFile = new File(TESTDATA, "doesnt_exist.properties");
        assertThat("Precondition: test configuration should not exit",
                configurationFile.exists(), is(false));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"PRE", repo.getPath(), "7"},  new CliSvnInterface());
        
        int exitCode = hook.execute(configurationFile);
        
        assertThat("Postcondition: successful exit code should be 1",
                exitCode, is(1));
        assertThat("Postcondition: should have correct error message",
                hook.getResultCollector().getAllMessages(), is(Arrays.asList(
                        new ResultMessage("hook", MessageType.ERROR, "An internal error occurred")
                )));
    }
    
    @BeforeAll
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }
    
}
