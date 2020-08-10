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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.teaching.submission_check.ResultMessage;
import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.utils.FileUtilsTest;

public class EncodingCheckTest {

    private static final File TESTDATA = new File("src/test/resources/EncodingCheckTest");
    
    @Test
    public void noFiles() {
        File directory = new File(TESTDATA, "emptyDirectory");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        EncodingCheck check = new EncodingCheck();
        
        assertThat("Postcondition: should succeed on empty folder",
                check.run(directory), is(true));
        
        assertThat("Postcondition: should contain no messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void expectUtf8OnUtf8() {
        File directory = new File(TESTDATA, "UTF-8");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        EncodingCheck check = new EncodingCheck();
        
        assertThat("Postcondition: should succeed on correct charset",
                check.run(directory), is(true));
        
        assertThat("Postcondition: should contain no messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void expectUtf8OnWindows1258() {
        File directory = new File(TESTDATA, "windows-1258");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        EncodingCheck check = new EncodingCheck();
        
        assertThat("Postcondition: should not succeed on wrong charset",
                check.run(directory), is(false));
        
        assertThat("Postcondition: should contain a message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("encoding", MessageType.ERROR, "File has invalid encoding; expected UTF-8").setFile(new File("umlauts.txt"))
                )));
    }
    
    @Test
    public void expectUtf8OnUtf16() {
        File directory = new File(TESTDATA, "UTF-16");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        EncodingCheck check = new EncodingCheck();
        
        assertThat("Postcondition: should not succeed on wrong charset",
                check.run(directory), is(false));
        
        assertThat("Postcondition: should contain a message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("encoding", MessageType.ERROR, "File has invalid encoding; expected UTF-8").setFile(new File("umlauts.txt"))
                )));
    }
    
    @Test
    public void expectUtf8OnIso88591() {
        File directory = new File(TESTDATA, "ISO 8859-1");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        EncodingCheck check = new EncodingCheck();
        
        assertThat("Postcondition: should not succeed on wrong charset",
                check.run(directory), is(false));
        
        assertThat("Postcondition: should contain a message",
                check.getResultMessages(), is(Arrays.asList(
                        new ResultMessage("encoding", MessageType.ERROR, "File has invalid encoding; expected UTF-8").setFile(new File("umlauts.txt"))
                )));
    }
    
    @Test
    public void unreadableFile() throws IOException {
        File directory = new File(TESTDATA, "UTF-8");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        File file = new File(directory, "umlauts.txt");
        assertThat("Precondition: file should exist",
                file.isFile(), is(true));
        try {
            
            EncodingCheck check = new EncodingCheck();
            
            FileUtilsTest.setRigFileOperationsToFail(true);
            
            assertThat("Postcondition: should not succeed on unreadable file",
                    check.run(directory), is(false));
            
            assertThat("Postcondition: should create an error message",
                    check.getResultMessages(), is(Arrays.asList(
                            new ResultMessage("encoding", MessageType.ERROR, "An internal error occurred while checking file encoding")
                    )));
        } finally {
        	FileUtilsTest.setRigFileOperationsToFail(false);
        }
    }
    
    @Test
    public void ignoreBinaryFileKnownMimetype() {
        File directory = new File(TESTDATA, "binaryFile");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        EncodingCheck check = new EncodingCheck();
        
        assertThat("Postcondition: should succeed on correct charset",
                check.run(directory), is(true));
        
        assertThat("Postcondition: should contain no messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void ignoreBinaryFileNoMimetype() {
        File directory = new File(TESTDATA, "emptyFile");
        assertThat("Precondition: directory with test files should exist",
                directory.isDirectory(), is(true));
        
        EncodingCheck check = new EncodingCheck();
        
        assertThat("Postcondition: should succeed on correct charset",
                check.run(directory), is(true));
        
        assertThat("Postcondition: should contain no messages",
                check.getResultMessages(), is(Arrays.asList()));
    }
    
    @Test
    public void getter() {
        EncodingCheck check = new EncodingCheck();
        assertThat("should return default value",
                check.getWantedCharset(), is(StandardCharsets.UTF_8));
        
        check.setWantedCharset(StandardCharsets.ISO_8859_1);
        assertThat(check.getWantedCharset(), is(StandardCharsets.ISO_8859_1));
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
