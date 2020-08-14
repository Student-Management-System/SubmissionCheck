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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.teaching.submission_check.svn.MockSvnInterface;
import net.ssehub.teaching.submission_check.svn.SvnException;
import net.ssehub.teaching.submission_check.svn.TransactionInfo;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.LoggingSetup;
import net.ssehub.teaching.submission_check.utils.LoggingSetupTest;

public class SubmissionHookTest {

    private static final File TESTDATA = new File("src/test/resources/SubmissionHookTest");
    
    @Test(expected = IllegalArgumentException.class)
    public void tooFewArguments() {
        new SubmissionHook(new String[] {"a", "b"}, new MockSvnInterface());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void tooManyArguments() {
        new SubmissionHook(new String[] {"a", "b", "c", "d"}, new MockSvnInterface());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidPhase() {
        new SubmissionHook(new String[] {"invalid", TESTDATA.getAbsolutePath(), "42"}, new MockSvnInterface());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nonExistingRepository() {
        new SubmissionHook(new String[] {"PRE", new File(TESTDATA, "doesnt_exist").getAbsolutePath(), "42"}, new MockSvnInterface());
    }
    
    @Test
    public void phasePre() {
        SubmissionHook hook = new SubmissionHook(new String[] {"PRE", TESTDATA.getAbsolutePath(), "42"}, new MockSvnInterface());
        
        assertThat(hook.getPhase(), is(Phase.PRE_COMMIT));
    }
    
    @Test
    public void phasePost() {
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", TESTDATA.getAbsolutePath(), "42"}, new MockSvnInterface());
        
        assertThat(hook.getPhase(), is(Phase.POST_COMMIT));
    }
    
    @Test
    public void constructorAttributesSet() {
        SubmissionHook hook = new SubmissionHook(new String[] {"PRE", TESTDATA.getAbsolutePath(), "42-c"}, new MockSvnInterface());
        
        assertThat(hook.getPhase(), is(Phase.PRE_COMMIT));
        assertThat(hook.getRepositoryPath(), is(TESTDATA.getAbsoluteFile()));
        assertThat(hook.getTransactionId(), is("42-c"));
    }
    
    @Test
    public void getMetadataFromSvnAttributesSet() throws SvnException {
        MockSvnInterface svnInterface = new MockSvnInterface();
        svnInterface.setExpectedPhase(Phase.POST_COMMIT);
        svnInterface.setExpectedRepositoryPath(TESTDATA.getAbsoluteFile());
        svnInterface.setExpectedTransactionId("42-c");
        
        svnInterface.setTransactionInfo(new TransactionInfo(TESTDATA.getAbsoluteFile(), "someuser", "42-c", Phase.POST_COMMIT));
        
        svnInterface.setModifiedSubmissions(new HashSet<>(Arrays.asList(
                new Submission("Homework01", "Group05"), new Submission("Homework17", "Group08"))));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", TESTDATA.getAbsolutePath(), "42-c"}, svnInterface);
        
        hook.queryMetadataFromSvn();
        
        assertThat(hook.getTransactionInfo(), is(new TransactionInfo(TESTDATA.getAbsoluteFile(), "someuser", "42-c", Phase.POST_COMMIT)));
        assertThat(hook.getModifiedSubmissions(), is(new HashSet<>(Arrays.asList(
                new Submission("Homework01", "Group05"),
                new Submission("Homework17", "Group08")
        ))));
    }
    
    @Test
    public void readConfiguration() throws IOException {
        File configFile = new File(TESTDATA, "config.properties");
        assertThat("Precondition: test config file exists",
                configFile.isFile(), is(true));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"PRE", TESTDATA.getAbsolutePath(), "42-c"}, new MockSvnInterface());
        
        assertThat("Precondition: configuration is not initialized",
                hook.getConfiguration(), is(nullValue()));
        
        hook.readConfiguration(configFile);
        
        assertThat("Postcondition: configuration is initialized",
                hook.getConfiguration(), not(nullValue()));
        
        assertThat("Postcondition: configuration has correct values read",
                hook.getConfiguration().getProperty("somesetting", new Submission("", "")), is("somevalue"));
    }
    
    @Test
    public void notifyStudentManagementSystemNotConfigured() throws SvnException, IOException {
        MockSvnInterface svnInterface = new MockSvnInterface();
        svnInterface.setExpectedPhase(Phase.POST_COMMIT);
        svnInterface.setExpectedRepositoryPath(TESTDATA.getAbsoluteFile());
        svnInterface.setExpectedTransactionId("42-c");
        
        svnInterface.setTransactionInfo(new TransactionInfo(TESTDATA.getAbsoluteFile(), "someuser", "42-c", Phase.POST_COMMIT));
        
        svnInterface.setModifiedSubmissions(new HashSet<>(Arrays.asList(
                new Submission("Homework01", "Group05"), new Submission("Homework17", "Group08"))));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", TESTDATA.getAbsolutePath(), "42-c"}, svnInterface);
        
        File configFile = new File(TESTDATA, "config.properties");
        assertThat("Precondition: test config file exists",
                configFile.isFile(), is(true));
        
        hook.readConfiguration(configFile);
        
        hook.queryMetadataFromSvn(); // initializes modified submissions
        
        ByteArrayOutputStream logoutput = new ByteArrayOutputStream();
        StreamHandler handler = new StreamHandler(logoutput, new SimpleFormatter());
        try {
            LoggingSetupTest.ROOT_LOGGER.addHandler(handler);
            
            hook.notifyStudentManagementSystem();
            
            handler.close();
            
            assertThat("Postcondition: should contain warning message about improper configuration",
                    new String(logoutput.toByteArray()), containsString("Student Management System connection not configured properly"));
            
        } finally {
            LoggingSetupTest.ROOT_LOGGER.removeHandler(handler);
        }
    }
    
    @Test
    public void notifyStudentManagementSystemIgnoredInPre() throws SvnException, IOException {
        MockSvnInterface svnInterface = new MockSvnInterface();
        svnInterface.setExpectedPhase(Phase.PRE_COMMIT);
        svnInterface.setExpectedRepositoryPath(TESTDATA.getAbsoluteFile());
        svnInterface.setExpectedTransactionId("42-c");
        
        svnInterface.setTransactionInfo(new TransactionInfo(TESTDATA.getAbsoluteFile(), "someuser", "42-c", Phase.PRE_COMMIT));
        
        svnInterface.setModifiedSubmissions(new HashSet<>(Arrays.asList(
                new Submission("Homework01", "Group05"), new Submission("Homework17", "Group08"))));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"PRE", TESTDATA.getAbsolutePath(), "42-c"}, svnInterface);
        
        File configFile = new File(TESTDATA, "config.properties");
        assertThat("Precondition: test config file exists",
                configFile.isFile(), is(true));
        
        hook.readConfiguration(configFile);
        
        hook.queryMetadataFromSvn(); // initializes modified submissions
        
        ByteArrayOutputStream logoutput = new ByteArrayOutputStream();
        StreamHandler handler = new StreamHandler(logoutput, new SimpleFormatter());
        try {
            LoggingSetupTest.ROOT_LOGGER.addHandler(handler);
            
            hook.notifyStudentManagementSystem();
            
            handler.close();
            
            assertThat("Postcondition: should do nothing in pre-commit, thus no log output",
                    new String(logoutput.toByteArray()), is(""));
            
        } finally {
            LoggingSetupTest.ROOT_LOGGER.removeHandler(handler);
        }
    }
    
    @Test
    public void notifyStudentManagementSystemNetworkError() throws SvnException, IOException {
        MockSvnInterface svnInterface = new MockSvnInterface();
        svnInterface.setExpectedPhase(Phase.POST_COMMIT);
        svnInterface.setExpectedRepositoryPath(TESTDATA.getAbsoluteFile());
        svnInterface.setExpectedTransactionId("42-c");
        
        svnInterface.setTransactionInfo(new TransactionInfo(TESTDATA.getAbsoluteFile(), "someuser", "42-c", Phase.POST_COMMIT));
        
        svnInterface.setModifiedSubmissions(new HashSet<>(Arrays.asList(
                new Submission("Homework01", "Group05"), new Submission("Homework17", "Group08"))));
        
        SubmissionHook hook = new SubmissionHook(new String[] {"POST", TESTDATA.getAbsolutePath(), "42-c"}, svnInterface);
        
        File configFile = new File(TESTDATA, "studentManagement.properties");
        assertThat("Precondition: test config file exists",
                configFile.isFile(), is(true));
        
        hook.readConfiguration(configFile);
        
        hook.queryMetadataFromSvn(); // initializes modified submissions
        
        ByteArrayOutputStream logoutput = new ByteArrayOutputStream();
        StreamHandler handler = new StreamHandler(logoutput, new SimpleFormatter());
        try {
            LoggingSetupTest.ROOT_LOGGER.addHandler(handler);
            
            hook.notifyStudentManagementSystem();
            
            handler.close();
            
            assertThat("Postcondition: should contain warning message about improper configuration",
                    new String(logoutput.toByteArray()), containsString("Failed to send result to Student Management System"));
            
        } finally {
            LoggingSetupTest.ROOT_LOGGER.removeHandler(handler);
        }
    }
    
    @BeforeClass
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }
    
}
