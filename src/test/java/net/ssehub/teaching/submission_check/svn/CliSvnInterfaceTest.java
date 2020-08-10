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
package net.ssehub.teaching.submission_check.svn;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import net.ssehub.teaching.submission_check.Submission;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.FileUtils;

/**
 * Tests the {@link CliSvnInterface} by mocking the output of the <code>svnlook</code> command.
 * 
 * @author Adam
 */
public class CliSvnInterfaceTest extends CliSvnInterface {

    private static final File TESTDATA = new File("src/test/resources/CliSvnInterfaceTest");
    
    private String author;
    
    private boolean createWrongAuthor;
    
    private String[] modifiedFiles;
    
    private Map<String, List<String>> fileLists; 
    
    private TransactionInfo expectedTransactionInfo;
    
    @Test
    public void author() throws SvnException {
        author = "someauthor";
        
        expectedTransactionInfo = new TransactionInfo(TESTDATA, null, "42-g", Phase.PRE_COMMIT);
        
        TransactionInfo info = this.createTransactionInfo(Phase.PRE_COMMIT, TESTDATA, "42-g");
        
        
        assertThat("Postcondition: transaction info should have correct fields set",
                info, is(new TransactionInfo(TESTDATA, "someauthor", "42-g", Phase.PRE_COMMIT)));
    }
    
    @Test(expected = SvnException.class)    
    public void authorInvalid() throws SvnException {
        author = "otherauthor";
        createWrongAuthor = true;

        expectedTransactionInfo = new TransactionInfo(TESTDATA, null, "42-g", Phase.PRE_COMMIT);
        
        this.createTransactionInfo(Phase.PRE_COMMIT, TESTDATA, "42-g");
    }
    
    @Test
    public void modifiedSubmissionsSingle() throws SvnException {
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        modifiedFiles = new String[] { "A Exercise01/Group05/Homework.java" };
        
        assertThat("Postcondition: should have correct submission path",
                getModifiedSubmissions(info), is(new HashSet<>(Arrays.asList(
                        new Submission("Exercise01", "Group05")
                ))));
    }
    
    @Test
    public void modifiedSubmissionsMultipleInSameSubmission() throws SvnException {
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        modifiedFiles = new String[] {
                "A Exercise01/Group05/Homework.java",
                "U Exercise01/Group05/Util.java",
                "D Exercise01/Group05/Old.java",
        };
        
        assertThat("Postcondition: should have correct submission path",
                getModifiedSubmissions(info), is(new HashSet<>(Arrays.asList(
                        new Submission("Exercise01", "Group05")
                ))));
    }
    
    @Test
    public void modifiedSubmissionsMultipleInDifferentSubmissions() throws SvnException {
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        modifiedFiles = new String[] {
                "A   Exercise01/Group05/Homework.java",
                "U\tExercise01/Group05/Util.java",
                "D   Exercise05/Group04/Old.java",
        };
        
        assertThat("Postcondition: should have correct submission paths",
                getModifiedSubmissions(info), is(new HashSet<>(Arrays.asList(
                        new Submission("Exercise01", "Group05"),
                        new Submission("Exercise05", "Group04")
                ))));
    }
    
    @Test
    public void modifiedSubmissionsSubfolders() throws SvnException {
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        modifiedFiles = new String[] {
                "A Exercise01/Group05/src/some/folder/Homework.java",
        };
        
        assertThat("Postcondition: should have correct submission path",
                getModifiedSubmissions(info), is(new HashSet<>(Arrays.asList(
                        new Submission("Exercise01", "Group05")
                ))));
    }
    
    @Test
    public void modifiedSubmissionsAllOutsideOfSubmissions() throws SvnException {
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        modifiedFiles = new String[] {
                "U permissions",
        };
        
        assertThat("Postcondition: should detect no submission paths",
                getModifiedSubmissions(info), is(new HashSet<>()));
    }
    
    @Test
    public void modifiedSubmissionsSubmissionFolder() throws SvnException {
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        modifiedFiles = new String[] {
                "D Exercise01/something",
        };
        
        assertThat("Postcondition: should detect no submission paths",
                getModifiedSubmissions(info), is(new HashSet<>()));
    }
    
    @Test(expected = SvnException.class)
    public void modifiedSubmissionsEmptyLine() throws SvnException {
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        modifiedFiles = new String[] {
                "D Exercise01/something",
                "",
        };
        
        getModifiedSubmissions(info);
    }
    
    @Test(expected = SvnException.class)
    public void modifiedSubmissionsInvalidChangeChar() throws SvnException {
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        modifiedFiles = new String[] {
                "X Exercise01/something"
        };
        
        getModifiedSubmissions(info);
    }
    
    @Test
    public void checkoutSubmissionSingleFile() throws SvnException, IOException {
        File targetDirecoty = new File(TESTDATA, "checkout");
        targetDirecoty.mkdir();
        assertThat("Precondition: test output directory should be empty",
                targetDirecoty.listFiles().length, is(0));
        
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        fileLists = new HashMap<>();
        fileLists.put(new File("Exercise01/Group06").getPath(), Arrays.asList(
                "Exercise01/Group06/",
                "Exercise01/Group06/Main.java"
        ));
        
        checkoutSubmission(info, new Submission("Exercise01", "Group06"), targetDirecoty);
        
        assertThat("Postcondition: should have only 1 file created",
                targetDirecoty.listFiles().length, is(1));
        File main = new File(targetDirecoty, "Main.java");
        try (BufferedReader in = new BufferedReader(new FileReader(main))) {
            assertThat("Postcondition: should have correct file content",
                    in.readLine(), is("this file was created by the test, filename: " + new File("Exercise01/Group06/Main.java").toString()));
        }
    }
    
    @Test
    public void checkoutSubmissionSubDirectory() throws SvnException, IOException {
        File targetDirecoty = new File(TESTDATA, "checkout");
        targetDirecoty.mkdir();
        assertThat("Precondition: test output directory should be empty",
                targetDirecoty.listFiles().length, is(0));
        
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        fileLists = new HashMap<>();
        fileLists.put(new File("Exercise01/Group06").getPath(), Arrays.asList(
                "Exercise01/Group06/",
                "Exercise01/Group06/src/",
                "Exercise01/Group06/src/pkg/",
                "Exercise01/Group06/src/pkg/Main.java"
        ));
        
        checkoutSubmission(info, new Submission("Exercise01", "Group06"), targetDirecoty);
        
        assertThat("Postcondition: should have only 1 file created",
                targetDirecoty.listFiles().length, is(1));
        File main = new File(targetDirecoty, "src/pkg/Main.java");
        try (BufferedReader in = new BufferedReader(new FileReader(main))) {
            assertThat("Postcondition: should have correct file content",
                    in.readLine(), is("this file was created by the test, filename: "
                            + new File("Exercise01/Group06/src/pkg/Main.java").toString()));
        }
    }
    
    @Test
    public void checkoutSubmissionMultipleFiles() throws SvnException, IOException {
        File targetDirecoty = new File(TESTDATA, "checkout");
        targetDirecoty.mkdir();
        assertThat("Precondition: test output directory should be empty",
                targetDirecoty.listFiles().length, is(0));
        
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        fileLists = new HashMap<>();
        fileLists.put(new File("Exercise01/Group06").getPath(), Arrays.asList(
                "Exercise01/Group06/",
                "Exercise01/Group06/.checkstyle",
                "Exercise01/Group06/.project",
                "Exercise01/Group06/Main.java",
                "Exercise01/Group06/Util.java"
        ));
        
        checkoutSubmission(info, new Submission("Exercise01", "Group06"), targetDirecoty);
        
        assertThat("Postcondition: should have only 4 files created",
                targetDirecoty.listFiles().length, is(4));
        
        for (String name : new String[] {"Main.java", "Util.java", ".project", ".checkstyle"}) {
            File file = new File(targetDirecoty, name);
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                assertThat("Postcondition: should have correct file content",
                        in.readLine(), is("this file was created by the test, filename: " + new File("Exercise01/Group06/" + name).toString()));
            }
        }
    }
    
    @Test
    public void checkoutSubmissionMultipleFilesWithDirectories() throws SvnException, IOException {
        File targetDirecoty = new File(TESTDATA, "checkout");
        targetDirecoty.mkdir();
        assertThat("Precondition: test output directory should be empty",
                targetDirecoty.listFiles().length, is(0));
        
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        fileLists = new HashMap<>();
        fileLists.put(new File("Exercise01/Group06").getPath(), Arrays.asList(
                "Exercise01/Group06/",
                "Exercise01/Group06/.checkstyle",
                "Exercise01/Group06/.project",
                "Exercise01/Group06/src/",
                "Exercise01/Group06/src/Main.java",
                "Exercise01/Group06/src/Util.java"
        ));
        
        checkoutSubmission(info, new Submission("Exercise01", "Group06"), targetDirecoty);
        
        assertThat("Postcondition: should have 3 files (2 files + 1 directory) created",
                targetDirecoty.listFiles().length, is(3));
        
        for (String name : new String[] {"src/Main.java", "src/Util.java", ".project", ".checkstyle"}) {
            File file = new File(targetDirecoty, name);
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                assertThat("Postcondition: should have correct file content",
                        in.readLine(), is("this file was created by the test, filename: " + new File("Exercise01/Group06/" + name).toString()));
            }
        }
    }
    
    @Test(expected = IOException.class)
    public void checkoutSubmissionInvalidDirectory() throws SvnException, IOException {
        File targetDirecoty = new File(TESTDATA, "checkout");
        targetDirecoty.mkdir();
        assertThat("Precondition: test output directory should be empty",
                targetDirecoty.listFiles().length, is(0));
        
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        fileLists = new HashMap<>();
        fileLists.put(new File("Exercise01/Group06").getPath(), Arrays.asList(
                "Exercise01/Group06/",
                "Exercise01/Group06/dir", // note: no trailing slash -> not a directory
                "Exercise01/Group06/dir/file.txt"
        ));
        
        checkoutSubmission(info, new Submission("Exercise01", "Group06"), targetDirecoty);
    }
    
    @Test
    public void checkoutSubmissionMultipleSubmissions() throws SvnException, IOException {
        File targetDirecoty = new File(TESTDATA, "checkout");
        targetDirecoty.mkdir();
        assertThat("Precondition: test output directory should be empty",
                targetDirecoty.listFiles().length, is(0));
        
        TransactionInfo info = new TransactionInfo(TESTDATA, "other", "42-g", Phase.POST_COMMIT);
        expectedTransactionInfo = info;
        
        fileLists = new HashMap<>();
        fileLists.put(new File("Exercise01/Group06").getPath(), Arrays.asList(
                "Exercise01/Group06/",
                "Exercise01/Group06/Main.java"
        ));
        fileLists.put(new File("Exercise05/Group04").getPath(), Arrays.asList(
                "Exercise05/Group04/",
                "Exercise05/Group04/Arrays.java"
        ));
        
        checkoutSubmission(info, new Submission("Exercise01", "Group06"), targetDirecoty);
        
        assertThat("Postcondition: should have only 1 file created",
                targetDirecoty.listFiles().length, is(1));
        File main = new File(targetDirecoty, "Main.java");
        try (BufferedReader in = new BufferedReader(new FileReader(main))) {
            assertThat("Postcondition: should have correct file content",
                    in.readLine(), is("this file was created by the test, filename: " + new File("Exercise01/Group06/Main.java").toString()));
        }

        FileUtils.deleteDirectory(targetDirecoty);
        
        checkoutSubmission(info, new Submission("Exercise05", "Group04"), targetDirecoty);
        
        assertThat("Postcondition: should have only 1 file created",
                targetDirecoty.listFiles().length, is(1));
        main = new File(targetDirecoty, "Arrays.java");
        try (BufferedReader in = new BufferedReader(new FileReader(main))) {
            assertThat("Postcondition: should have correct file content",
                    in.readLine(), is("this file was created by the test, filename: " + new File("Exercise05/Group04/Arrays.java").toString()));
        }
    }
    
    @Override
    protected List<String> runSvnLookCommand(String subcommand, File outputRedirect, String... additionalArguments)
            throws SvnException {
        
        List<String> output = new LinkedList<>();
        
        assertThat("should have correct repository path set",
                this.repositoryPath, is(expectedTransactionInfo.getRepository()));
        assertThat("should have correct phase set",
                this.phase, is(expectedTransactionInfo.getPhase()));
        assertThat("should have correct transaction ID set",
                this.transactionId, is(expectedTransactionInfo.getTransactionId()));
        
        switch (subcommand) {
        case "author":
            assertThat("should have no additional arguments supplied",
                    additionalArguments.length, is(0));
            output.add(author);
            if (createWrongAuthor) {
                output.add("another line");
            }
            break;
        
        case "changed":
            assertThat("should have no additional arguments supplied",
                    additionalArguments.length, is(0));
            output.addAll(Arrays.asList(modifiedFiles));
            break;
        
        case "tree":
            assertThat("should have two additional arguments supplied",
                    additionalArguments.length, is(2));
            assertThat("first additional argument should be --full-paths",
                    additionalArguments[0], is("--full-paths"));
            output.addAll(fileLists.get(additionalArguments[1]));
            break;
            
        case "cat":
            assertThat("should have one additional argument supplied",
                    additionalArguments.length, is(1));
            output.add("this file was created by the test, filename: " + additionalArguments[0]);
            break;
            
        default:
            fail("invalid command " + subcommand);
        }
        
        if (outputRedirect != null) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(outputRedirect))) {
                for (String line : output) {
                    out.write(line + '\n');
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            output = null;
        }
        
        return output;
    }
    
    @After
    public void cleanCheckoutDirectory() throws IOException {
        File checkout = new File(TESTDATA, "checkout");
        if (checkout.isDirectory()) {
            FileUtils.deleteDirectory(checkout);
        }
    }
    
}
