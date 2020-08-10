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
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.teaching.submission_check.ResultMessage;
import net.ssehub.teaching.submission_check.ResultMessage.MessageType;

public class FileSizeCheckTest {
    
    private static final File TESTDATA = new File("src/test/resources/FileSizeCheckTest");
    
    @Test
    public void emptyDirectory() {
        File directory = new File(TESTDATA, "emptyDirectory");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        FileSizeCheck check = new FileSizeCheck();
        
        assertThat("Postcondition: run on an empty directory should succeed",
                check.run(directory), is(true));
        
        assertThat("Postcondition: should not create any messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void sinlgeFileLimitHeld() {
        File directory = new File(TESTDATA, "singleFile100Bytes");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        FileSizeCheck check = new FileSizeCheck();
        check.setMaxFileSize(100);
        
        assertThat("Postcondition: run with no violations should succeed",
                check.run(directory), is(true));
        
        assertThat("Postcondition: should not create any messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void sinlgeFileLimitViolated() {
        File directory = new File(TESTDATA, "singleFile100Bytes");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        FileSizeCheck check = new FileSizeCheck();
        check.setMaxFileSize(99);
        
        assertThat("Postcondition: run with violations should not succeed",
                check.run(directory), is(false));
        
        assertThat("Postcondition: should create error message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("file-size", MessageType.ERROR, "File is too large").setFile(new File("100bytes.txt"))
                )));
    }
    
    @Test
    public void submissionSizeLimitViolatedBySingleFile() {
        File directory = new File(TESTDATA, "singleFile100Bytes");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        FileSizeCheck check = new FileSizeCheck();
        check.setMaxSubmissionSize(99);
        
        assertThat("Postcondition: run with violations should not succeed",
                check.run(directory), is(false));
        
        assertThat("Postcondition: should create error message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("file-size", MessageType.ERROR, "Submission size is too large")
                )));
    }
    
    @Test
    public void multipleFileLimitsHeld() {
        File directory = new File(TESTDATA, "multipleFiles");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        FileSizeCheck check = new FileSizeCheck();
        check.setMaxFileSize(200);
        
        assertThat("Postcondition: run with no violations should succeed",
                check.run(directory), is(true));
        
        assertThat("Postcondition: should not create any messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void multipleFileLimitsViolated() {
        File directory = new File(TESTDATA, "multipleFiles");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        FileSizeCheck check = new FileSizeCheck();
        check.setMaxFileSize(99);
        
        assertThat("Postcondition: run with violations should not succeed",
                check.run(directory), is(false));
        
        assertThat("Postcondition: should create error messages for each violation",
                check.getResultMessages(), containsInAnyOrder(
                        new ResultMessage("file-size", MessageType.ERROR, "File is too large").setFile(new File("100bytes.txt")),
                        new ResultMessage("file-size", MessageType.ERROR, "File is too large").setFile(new File("200bytes.txt"))
                ));
    }
    
    @Test
    public void multipleFileLimitsSomeViolated() {
        File directory = new File(TESTDATA, "multipleFiles");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        FileSizeCheck check = new FileSizeCheck();
        check.setMaxFileSize(150);
        
        assertThat("Postcondition: run with violations should not succeed",
                check.run(directory), is(false));
        
        assertThat("Postcondition: should create error message for violation",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("file-size", MessageType.ERROR, "File is too large").setFile(new File("200bytes.txt"))
                )));
    }
    
    @Test
    public void multipleFilesSubmissionTooLarge() {
        File directory = new File(TESTDATA, "multipleFiles");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        FileSizeCheck check = new FileSizeCheck();
        check.setMaxFileSize(200);
        check.setMaxSubmissionSize(250);
        
        assertThat("Postcondition: run with violations should not succeed",
                check.run(directory), is(false));
        
        assertThat("Postcondition: should create error message for violation",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("file-size", MessageType.ERROR, "Submission size is too large")
                )));
    }
    
    @Test
    public void getters() {
        FileSizeCheck check = new FileSizeCheck();
        
        assertThat("should return default value",
                check.getMaxFileSize(), is(10485760L));
        assertThat("should return default value",
                check.getMaxSubmissionSize(), is(10485760L));
        
        check.setMaxFileSize(123);
        check.setMaxSubmissionSize(456);
        
        assertThat(check.getMaxFileSize(), is(123L));
        assertThat(check.getMaxSubmissionSize(), is(456L));
        
    }
    
    @BeforeClass
    public static void createEmptyDirectory() {
        File directory = new File(TESTDATA, "emptyDirectory");
        if (!directory.isDirectory()) {
            boolean created = directory.mkdir();
            if (!created) {
                Assert.fail("Setup: Could not create empty test directory " + directory.getPath());
            }
        }
    }
    
}
