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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.teaching.submission_check.Submission;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.FileUtils;
import net.ssehub.teaching.submission_check.utils.LoggingSetup;

public class CliSvnInterfaceIT {

    private static final File TESTDATA = new File("src/test/resources/CliSvnInterfaceIT");
    
    @BeforeClass
    public static void checkSvnlookInstalled() {
        ProcessBuilder pb = new ProcessBuilder("svnlook", "help");
        pb.redirectOutput(Redirect.DISCARD);
        pb.redirectError(Redirect.DISCARD);
        
        try {
            int result = pb.start().waitFor();
            assumeThat("Precondition: svnlook should be installed in PATH",
                    result, is(0));
        } catch (IOException | InterruptedException e) {
            assumeTrue("Precondition: svnlook should be installed in PATH", false);
        }
    }
    
    @Test
    public void postCommitAuthor() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repoWithTwoCommits.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        TransactionInfo info = svn.createTransactionInfo(Phase.POST_COMMIT, repo, "1");
        assertThat("Postcondition: should have read correct author name",
                info.getAuthor(), is("authorName1"));
        
        info = svn.createTransactionInfo(Phase.POST_COMMIT, repo, "2");
        assertThat("Postcondition: should have read correct author name",
                info.getAuthor(), is("secondAuthor"));
    }
    
    @Test(expected = SvnException.class)
    public void postInvalidRepo() throws IOException, SvnException {
        CliSvnInterface svn = new CliSvnInterface();
        
        svn.createTransactionInfo(Phase.POST_COMMIT, TESTDATA, "1");
    }
    
    @Test(expected = SvnException.class)
    public void postInvalidRevision() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repoWithTwoCommits.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        svn.createTransactionInfo(Phase.POST_COMMIT, repo, "15");
    }
    
    @Test
    public void preCommitAuthor() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repoInTransaction_2-2.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        TransactionInfo info = svn.createTransactionInfo(Phase.PRE_COMMIT, repo, "2-2");
        assertThat("Postcondition: should have read correct author name",
                info.getAuthor(), is("someThirdAuthor"));
    }
    
    @Test(expected = SvnException.class)
    public void preInvalidRepo() throws IOException, SvnException {
        CliSvnInterface svn = new CliSvnInterface();
        
        svn.createTransactionInfo(Phase.PRE_COMMIT, TESTDATA, "1");
    }
    
    @Test(expected = SvnException.class)
    public void preInvalidTransaction() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repoInTransaction_2-2.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        svn.createTransactionInfo(Phase.PRE_COMMIT, repo, "2-3");
    }
    
    @Test
    public void postModifiedSubmissions() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        assertThat("Postcondition: should contain no modified submissions",
                svn.getModifiedSubmissions(new TransactionInfo(repo, "www-data", "1", Phase.POST_COMMIT)),
                is(new HashSet<>()));
        
        assertThat("Postcondition: should contain the modified submissions",
                svn.getModifiedSubmissions(new TransactionInfo(repo, "student1", "2", Phase.POST_COMMIT)),
                is(new HashSet<>(Arrays.asList(
                        new Submission("Homework01Task01", "Group01")
                ))));
        
        assertThat("Postcondition: should contain the modified submissions",
                svn.getModifiedSubmissions(new TransactionInfo(repo, "student2", "3", Phase.POST_COMMIT)),
                is(new HashSet<>(Arrays.asList(
                        new Submission("Homework01Task01", "Group02"),
                        new Submission("Homework02Task02", "Group02")
                ))));
        assertThat("Postcondition: should contain the modified submissions",
                svn.getModifiedSubmissions(new TransactionInfo(repo, "student1", "4", Phase.POST_COMMIT)),
                is(new HashSet<>(Arrays.asList(
                        new Submission("Homework02Task02", "Group01")
                ))));
    }
    
    @Test
    public void preModifiedSubmissions() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repo2Exercises2GroupsInTransaction_4-4.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        assertThat("Postcondition: should contain the modified submissions",
                svn.getModifiedSubmissions(new TransactionInfo(repo, "student1", "4-4", Phase.PRE_COMMIT)),
                is(new HashSet<>(Arrays.asList(
                        new Submission("Homework02Task02", "Group01")
                ))));
    }
    
    @Test
    public void postCheckoutSubmission() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        TransactionInfo info = new TransactionInfo(repo, "student1", "2", Phase.POST_COMMIT);
        
        File target = FileUtils.createTemporaryDirectory();
        assertThat("Precondition: temporary target folder is created",
                target.isDirectory(), is(true));
        assertThat("Precondition: temporary target folder is empty",
                target.listFiles().length, is(0));
        
        svn.checkoutSubmission(info, new Submission("Homework01Task01", "Group01"), target);
        
        assertThat("Postcondition: target folder contains file",
                target.listFiles().length, is(1));
        
        File submissionFile = new File(target, "submitted-file.txt");
        assertThat("Postcondition: target folder contains file",
                submissionFile.isFile(), is(true));
        try (BufferedReader in = new BufferedReader(new FileReader(submissionFile))) {
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is("Some solution."));
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is(nullValue()));
        }
    }
    
    @Test
    public void postCheckoutOneSubmissionFromMultipleCommitted() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        TransactionInfo info = new TransactionInfo(repo, "student2", "3", Phase.POST_COMMIT);
        
        File target = FileUtils.createTemporaryDirectory();
        assertThat("Precondition: temporary target folder is created",
                target.isDirectory(), is(true));
        assertThat("Precondition: temporary target folder is empty",
                target.listFiles().length, is(0));
        
        svn.checkoutSubmission(info, new Submission("Homework01Task01", "Group02"), target);
        
        assertThat("Postcondition: target folder contains file",
                target.listFiles().length, is(1));
        
        File submissionFile = new File(target, "my solution.txt");
        assertThat("Postcondition: target folder contains file",
                submissionFile.isFile(), is(true));
        try (BufferedReader in = new BufferedReader(new FileReader(submissionFile))) {
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is("Wrong."));
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is(nullValue()));
        }
    }
    
    @Test
    public void postCheckoutSubmissionWithMultipleFilesAndFolders() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repo2Exercises2Groups.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        TransactionInfo info = new TransactionInfo(repo, "student1", "4", Phase.POST_COMMIT);
        
        File target = FileUtils.createTemporaryDirectory();
        assertThat("Precondition: temporary target folder is created",
                target.isDirectory(), is(true));
        assertThat("Precondition: temporary target folder is empty",
                target.listFiles().length, is(0));
        
        svn.checkoutSubmission(info, new Submission("Homework02Task02", "Group01"), target);
        
        assertThat("Postcondition: target folder contains 3 files",
                target.listFiles().length, is(3));
        
        File srcDir = new File(target, "src");
        assertThat("Postcondition: target folder contains src dir",
                srcDir.isDirectory(), is(true));
        
        File libsDir = new File(target, "libs");
        assertThat("Postcondition: target folder contains libs dir",
                libsDir.isDirectory(), is(true));
        
        File commentFile = new File(target, "comment.txt");
        assertThat("Postcondition: target folder contains comment file",
                commentFile.isFile(), is(true));
        try (BufferedReader in = new BufferedReader(new FileReader(commentFile))) {
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is("This submission has folders and multiple files."));
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is(nullValue()));
        }
        
        File sourceFile = new File(srcDir, "Main.java");
        assertThat("Postcondition: src folder contains source file",
                sourceFile.isFile(), is(true));
        try (BufferedReader in = new BufferedReader(new FileReader(sourceFile))) {
            assertThat("Postcondition: file content is correct", in.readLine(), is("import util.Util;"));
            assertThat("Postcondition: file content is correct", in.readLine(), is(""));
            assertThat("Postcondition: file content is correct", in.readLine(), is("public class Main {"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("    "));
            assertThat("Postcondition: file content is correct", in.readLine(), is("    public static void main(String[] args) {"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("        System.out.println(\"main()\");"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("        Util.method();"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("    }"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("    "));
            assertThat("Postcondition: file content is correct", in.readLine(), is("}"));
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is(nullValue()));
        }
        
        File libraryFile = new File(libsDir, "util-lib.jar");
        assertThat("Postcondition: libs folder contains library file",
                libraryFile.isFile(), is(true));
        assertThat("Postcondition: library file has correct size",
                FileUtils.getFileSize(libraryFile), is(946L));
    }
    
    @Test
    public void preCheckoutSubmissionWithMultipleFilesAndFolders() throws IOException, SvnException {
        File repo = prepareSvnRepo(new File(TESTDATA, "repo2Exercises2GroupsInTransaction_4-4.zip"));
        
        CliSvnInterface svn = new CliSvnInterface();
        
        TransactionInfo info = new TransactionInfo(repo, "student1", "4-4", Phase.PRE_COMMIT);
        
        File target = FileUtils.createTemporaryDirectory();
        assertThat("Precondition: temporary target folder is created",
                target.isDirectory(), is(true));
        assertThat("Precondition: temporary target folder is empty",
                target.listFiles().length, is(0));
        
        svn.checkoutSubmission(info, new Submission("Homework02Task02", "Group01"), target);
        
        assertThat("Postcondition: target folder contains 3 files",
                target.listFiles().length, is(3));
        
        File srcDir = new File(target, "src");
        assertThat("Postcondition: target folder contains src dir",
                srcDir.isDirectory(), is(true));
        
        File libsDir = new File(target, "libs");
        assertThat("Postcondition: target folder contains libs dir",
                libsDir.isDirectory(), is(true));
        
        File commentFile = new File(target, "comment.txt");
        assertThat("Postcondition: target folder contains comment file",
                commentFile.isFile(), is(true));
        try (BufferedReader in = new BufferedReader(new FileReader(commentFile))) {
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is("This submission has folders and multiple files."));
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is(nullValue()));
        }
        
        File sourceFile = new File(srcDir, "Main.java");
        assertThat("Postcondition: src folder contains source file",
                sourceFile.isFile(), is(true));
        try (BufferedReader in = new BufferedReader(new FileReader(sourceFile))) {
            assertThat("Postcondition: file content is correct", in.readLine(), is("import util.Util;"));
            assertThat("Postcondition: file content is correct", in.readLine(), is(""));
            assertThat("Postcondition: file content is correct", in.readLine(), is("public class Main {"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("    "));
            assertThat("Postcondition: file content is correct", in.readLine(), is("    public static void main(String[] args) {"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("        System.out.println(\"main(args)\");"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("        Util.method();"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("    }"));
            assertThat("Postcondition: file content is correct", in.readLine(), is("    "));
            assertThat("Postcondition: file content is correct", in.readLine(), is("}"));
            assertThat("Postcondition: file content is correct",
                    in.readLine(), is(nullValue()));
        }
        
        File libraryFile = new File(libsDir, "util-lib.jar");
        assertThat("Postcondition: libs folder contains library file",
                libraryFile.isFile(), is(true));
        assertThat("Postcondition: library file has correct size",
                FileUtils.getFileSize(libraryFile), is(946L));
    }
    
    private File prepareSvnRepo(File repoZip) throws IOException {
        assertThat("Precondition: test SVN repo zip file should exist",
                repoZip.isFile(), is(true));
        
        File directory = FileUtils.createTemporaryDirectory();
        
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(repoZip))) {
            
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                File target = new File(directory, entry.getName());
                if (entry.isDirectory()) {
                    target.mkdir();
                    
                } else {
                    FileOutputStream targetOut = new FileOutputStream(target);
                    
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = zipStream.read(buffer)) != -1) {
                        targetOut.write(buffer, 0, read);
                    }
                    
                    targetOut.close();
                    
                    zipStream.closeEntry();
                }
            }
            
        }
        
        assertThat("Precondition: extracted archive should look like a SVN repo",
                new File(directory, "conf").isDirectory(), is(true));
        
        assertThat("Precondition: extracted archive should look like a SVN repo",
                new File(directory, "db").isDirectory(), is(true));
        
        assertThat("Precondition: extracted archive should look like a SVN repo",
                new File(directory, "hooks").isDirectory(), is(true));
        
        assertThat("Precondition: extracted archive should look like a SVN repo",
                new File(directory, "locks").isDirectory(), is(true));
        
        assertThat("Precondition: extracted archive should look like a SVN repo",
                new File(directory, "format").isFile(), is(true));
        
        return directory;
    }
    
    @BeforeClass
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }
    
}
