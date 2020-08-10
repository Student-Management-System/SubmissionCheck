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
package net.ssehub.teaching.submission_check.checks;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.junit.Test;

import net.ssehub.teaching.submission_check.ResultMessage;
import net.ssehub.teaching.submission_check.ResultMessage.MessageType;

public class EclipseConfigCheckTest {

    private static final File TESTDATA = new File("src/test/resources/EclipseConfigCheckTest");
    
    @Test
    public void missingClasspath() {
        File direcory = new File(TESTDATA, "missingClasspath");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        
        assertThat("Postcondition: missing classpath should not succeed",
                check.run(direcory), is(false));
        
        assertThat("Postcondition: should create an error message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("eclipse-configuration", MessageType.ERROR, "Does not contain a valid eclipse project")
                )));
    }
    
    @Test
    public void missingProject() {
        File direcory = new File(TESTDATA, "missingProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        
        assertThat("Postcondition: missing project should not succeed",
                check.run(direcory), is(false));
        
        assertThat("Postcondition: should create an error message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("eclipse-configuration", MessageType.ERROR, "Does not contain a valid eclipse project")
                )));
    }
    
    @Test
    public void valid() {
        File direcory = new File(TESTDATA, "javaProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        
        assertThat("Postcondition: correct setup should succeed",
                check.run(direcory), is(true));
        
        assertThat("Postcondition: should not create any messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void nonJavaProjectAllowed() {
        File direcory = new File(TESTDATA, "nonJavaProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        
        assertThat("Postcondition: a non-java project should succeed when allowed",
                check.run(direcory), is(true));
        
        assertThat("Postcondition: should not create any messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void nonJavaProjectNotAllowed() {
        File direcory = new File(TESTDATA, "nonJavaProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        check.setRequireJavaProject(true);
        
        assertThat("Postcondition: a non-java project should not succeed when not allowed",
                check.run(direcory), is(false));
        
        assertThat("Postcondition: should create an error message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("eclipse-configuration", MessageType.ERROR, "Submission is not a Java project")
                            .setFile(new File(".project"))
                )));
    }
    
    @Test
    public void validRequiredJavaProject() {
        File direcory = new File(TESTDATA, "javaProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        check.setRequireJavaProject(true);
        
        assertThat("Postcondition: a java project should succeed when required",
                check.run(direcory), is(true));
        
        assertThat("Postcondition: should not create any messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void nonCheckstyleProjectAllowed() {
        File direcory = new File(TESTDATA, "javaProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        
        assertThat("Postcondition: a non-checkstyle project should succeed when allowed",
                check.run(direcory), is(true));
        
        assertThat("Postcondition: should not create any messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void nonCheckstyleProjectNotAllowed() {
        File direcory = new File(TESTDATA, "javaProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        check.setRequireCheckstyleProject(true);
        
        assertThat("Postcondition: a non-checkstyle project should succeed even when not allowed",
                check.run(direcory), is(true));
        
        assertThat("Postcondition: should create an error message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("eclipse-configuration", MessageType.WARNING, "Submission does not have Checkstyle enabled")
                            .setFile(new File(".project"))
                )));
    }
    
    @Test
    public void validRequiredCheckstyleProject() {
        File direcory = new File(TESTDATA, "checkstyleProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        EclipseConfigCheck check = new EclipseConfigCheck();
        check.setRequireJavaProject(true);
        
        assertThat("Postcondition: a checkstyle project should succeed when required",
                check.run(direcory), is(true));
        
        assertThat("Postcondition: should not create any messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void unreadableProjectFile() throws IOException {
        File direcory = new File(TESTDATA, "javaProject");
        assertThat("Precondition: directory with test files should exist",
                direcory.isDirectory(), is(true));
        
        File projectFile = new File(direcory, ".project");
        assertThat("Precondition: project file should exist",
                projectFile.isFile(), is(true));
        try (RandomAccessFile lock = new RandomAccessFile(projectFile, "rw")) {
            lock.getChannel().lock();
            
            EclipseConfigCheck check = new EclipseConfigCheck();
            
            assertThat("Postcondition: should not succeed on unreadable file",
                    check.run(direcory), is(false));
            
            assertThat("Postcondition: should create an error message",
                    check.getResultMessages(), is(Arrays.asList(
                            new ResultMessage("eclipse-configuration", MessageType.ERROR, "An internal error occurred while checking eclipse project")
                    )));
        }
    }
    
    @Test
    public void getters() {
        EclipseConfigCheck check = new EclipseConfigCheck();
        
        assertThat("should return correct default value",
                check.getRequireJavaProject(), is(false));
        assertThat("should return correct default value",
                check.getRequireCheckstyleProject(), is(false));
        
        check.setRequireJavaProject(true);
        assertThat(check.getRequireJavaProject(), is(true));
        assertThat(check.getRequireCheckstyleProject(), is(false));
        
        check.setRequireJavaProject(false);
        check.setRequireCheckstyleProject(true);
        assertThat(check.getRequireJavaProject(), is(false));
        assertThat(check.getRequireCheckstyleProject(), is(true));
    }
    
}
