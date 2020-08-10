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
package net.ssehub.teaching.submission_check.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class FileUtilsTest {
    
    private static final File TESTDATA = new File("src/test/resources/FileUtilsTest");

    @Test
    public void findSuffixFilesEmptyDirectory() {
        File directory = new File(TESTDATA, "emptyDirectory");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should not find files in empty directory",
                FileUtils.findFilesBySuffix(directory, ".txt"), is(new HashSet<>(Arrays.asList())));
        
        assertThat("Postcondition: should not find files in empty directory",
                FileUtils.findFilesBySuffix(directory, ""), is(new HashSet<>(Arrays.asList())));
    }
    
    @Test
    public void findSuffixFilesNoMatches() {
        File directory = new File(TESTDATA, "noTxt");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should not find any matching files",
                FileUtils.findFilesBySuffix(directory, ".txt"), is(new HashSet<>(Arrays.asList())));
    }
    
    @Test
    public void findSuffixFilesOneMatch() {
        File directory = new File(TESTDATA, "oneTxt");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should find one match",
                FileUtils.findFilesBySuffix(directory, ".txt"), is(new HashSet<>(Arrays.asList(
                        new File(directory, "file.txt")))));
    }
    
    @Test
    public void findSuffixFilesMultipleMatch() {
        File directory = new File(TESTDATA, "multipleTxt");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should find two matches",
                FileUtils.findFilesBySuffix(directory, ".txt"), is(new HashSet<>(Arrays.asList(
                        new File(directory, "file.txt"),  new File(directory, "another.txt")))));
    }
    
    @Test
    public void findSuffixFilesNoDotSuffix() {
        File directory = new File(TESTDATA, "noTxt");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should find one matching file",
                FileUtils.findFilesBySuffix(directory, "ffix"), is(new HashSet<>(Arrays.asList(
                        new File(directory, "noSuffix")))));
    }
    
    @Test
    public void findSuffixFilesSubdirectories() {
        File directory = new File(TESTDATA, "subDirectories");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should find multiple matching files in sub-directories",
                FileUtils.findFilesBySuffix(directory, ".txt"), is(new HashSet<>(Arrays.asList(
                        new File(directory, "subdir1/file.txt"),
                        new File(directory, "subdir2/another.txt"),
                        new File(directory, "subdir2/subsubdir/file.txt")))));
    }
    
    @Test
    public void findAllFilesEmptyDirectory() {
        File directory = new File(TESTDATA, "emptyDirectory");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should not find files in empty directory",
                FileUtils.findAllFiles(directory), is(new HashSet<>(Arrays.asList())));
    }
    
    @Test
    public void findAllFilesFlat() {
        File directory = new File(TESTDATA, "oneTxt");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should find all files in flat directory",
                FileUtils.findAllFiles(directory), is(new HashSet<>(Arrays.asList(
                        new File(directory, "Compiled.class"),
                        new File(directory, "file.txt"),
                        new File(directory, "noSuffix"),
                        new File(directory, "Source.java")
                ))));
    }
    
    @Test
    public void findAllFilesNested() {
        File directory = new File(TESTDATA, "subDirectories");
        assertThat("Precondition: test directory (" + directory.getPath() + ") should exist",
                directory.isDirectory());
        
        assertThat("Postcondition: should find all files in flat directory",
                FileUtils.findAllFiles(directory), is(new HashSet<>(Arrays.asList(
                        new File(directory, "subdir1/Compiled.class"),
                        new File(directory, "subdir1/file.txt"),
                        new File(directory, "subdir1/noSuffix"),
                        new File(directory, "subdir1/Source.java"),
                        
                        new File(directory, "subdir2/Compiled.class"),
                        new File(directory, "subdir2/another.txt"),
                        new File(directory, "subdir2/noSuffix"),
                        new File(directory, "subdir2/Source.java"),
                        
                        new File(directory, "subdir2/subsubdir/Compiled.class"),
                        new File(directory, "subdir2/subsubdir/file.txt"),
                        new File(directory, "subdir2/subsubdir/noSuffix"),
                        new File(directory, "subdir2/subsubdir/Source.java")
                ))));
    }
    
    @Test
    public void relativizeDirectlyNested() {
        assertThat("Postcondition: should relativize file directly nested in base direcotry",
                FileUtils.getRelativeFile(new File("some/dir/"), new File("some/dir/file.txt")),
                is(new File("file.txt")));
    }
    
    @Test
    public void relativizeNestedInSubDirectories() {
        assertThat("Postcondition: should relativize file nested in sub-directories",
                FileUtils.getRelativeFile(new File("some/dir/"), new File("some/dir/a/b/file.txt")),
                is(new File("a/b/file.txt")));
    }
    
    @Test
    public void relativizeNextToEachOther() {
        assertThat("Postcondition: should relativize file next to directory",
                FileUtils.getRelativeFile(new File("some/dir/"), new File("some/file.txt")),
                is(new File("../file.txt")));
    }
    
    @Test
    public void relativizeFurtherOutside() {
        assertThat("Postcondition: should relativize file that is not inside directory",
                FileUtils.getRelativeFile(new File("some/dir/"), new File("another/file.txt")),
                is(new File("../../another/file.txt")));
    }
    
    @Test
    public void relativizeAbsolutePaths() {
        assertThat("Postcondition: should relativize files given as absolute paths",
                FileUtils.getRelativeFile(new File("/some/dir/"), new File("/some/dir/nested/file.txt")),
                is(new File("nested/file.txt")));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void relativizeThrowsIfDirAbsoluteAndFileRelative() {
            FileUtils.getRelativeFile(new File("/some/dir/"), new File("some/dir/nested/file.txt"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void relativizeThrowsIfDirRelativeAndFileAbsolute() {
        FileUtils.getRelativeFile(new File("some/dir/"), new File("/some/dir/nested/file.txt"));
    }
    
    @Test(expected = FileNotFoundException.class)
    public void fileSizeOnDirectory() throws IOException {
        assertThat("Precondition: test directory should exist",
                TESTDATA.isDirectory(), is(true));
        
        FileUtils.getFileSize(TESTDATA);
    }
    
    @Test(expected = FileNotFoundException.class)
    public void fileSizeOnNotExistingFile() throws IOException {
        File file = new File(TESTDATA, "doesnt_exist");
        assertThat("Precondition: test file should not exist",
                file.exists(), is(false));
        
        FileUtils.getFileSize(file);
    }
    
    @Test
    public void fileSize100Bytes() throws IOException {
        File file = new File(TESTDATA, "100bytes.txt");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        assertThat("Postcondition: file size should be correct",
                FileUtils.getFileSize(file), is(100L));
    }
    
    @Test
    public void fileSize0Bytes() throws IOException {
        File file = new File(TESTDATA, "0bytes.txt");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        assertThat("Postcondition: file size should be correct",
                FileUtils.getFileSize(file), is(0L));
    }
    
    @Test
    public void fileSize2006Bytes() throws IOException {
        File file = new File(TESTDATA, "2006bytes.txt");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        assertThat("Postcondition: file size should be correct",
                FileUtils.getFileSize(file), is(2006L));
    }
    
    @Test(expected = FileNotFoundException.class)
    public void parseXmlNonExistingFile() throws IOException, SAXException {
        File file = new File(TESTDATA, "doesnt_exist.xml");
        assertThat("Precondition: test file should not exist",
                file.exists(), is(false));
        
        FileUtils.parseXml(file);
    }
    
    @Test(expected = SAXException.class)
    public void parseInvalidXml() throws IOException, SAXException {
        File file = new File(TESTDATA, "invalid.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        FileUtils.parseXml(file);
    }
    
    @Test
    public void parseValidXml() throws IOException, SAXException {
        File file = new File(TESTDATA, "valid.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        Document document = FileUtils.parseXml(file);
        
        Node parentNode = document.getDocumentElement();
        assertThat(parentNode.getNodeName(), is("parent"));
        assertThat(parentNode.getNodeType(), is(Node.ELEMENT_NODE));
        assertThat(parentNode.getChildNodes().getLength(), is(1));
        
        Node childNode = parentNode.getChildNodes().item(0);
        assertThat(childNode.getNodeName(), is("child"));
        assertThat(childNode.getNodeType(), is(Node.ELEMENT_NODE));
        assertThat(childNode.getChildNodes().getLength(), is(1));
        
        Node textNode = childNode.getChildNodes().item(0); 
        assertThat(textNode.getNodeType(), is(Node.TEXT_NODE));
        assertThat(textNode.getTextContent(), is("text"));
    }
    
    @Test(expected = IOException.class)
    public void parseXmlBlockedFile() throws IOException, SAXException {
        File file = new File(TESTDATA, "valid.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        try (FileBlocker blocker = new FileBlocker(file)) {
            FileUtils.parseXml(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("Should throw IOException instead of FileNotFoundException");
        }
    }
    
    @Test(expected = IOException.class)
    public void deleteNonExistingDirecotry() throws IOException {
        File file = new File(TESTDATA, "doesnt_exist");
        assertThat("Precondition: test file should not exist",
                file.exists(), is(false));
        
        FileUtils.deleteDirectory(file);
    }
    
    @Test(expected = IOException.class)
    public void deleteDirectoryOnFile() throws IOException {
        File file = new File(TESTDATA, "100bytes.txt");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        FileUtils.deleteDirectory(file);
    }
    
    @Test
    public void deleteDirectoryEmpty() throws IOException {
        File directory = new File(TESTDATA, "temp_directory");
        directory.mkdir();
        assertThat("Precondition: directory should exist",
                directory.isDirectory(), is(true));
        assertThat("Precondition: directory should be empty",
                directory.listFiles().length, is(0));
        
        FileUtils.deleteDirectory(directory);
        
        assertThat("Postcondition: directory should not exist",
                directory.exists(), is(false));
    }
    
    @Test
    public void deleteDirectorySingleFileInside() throws IOException {
        File directory = new File(TESTDATA, "temp_directory");
        directory.mkdir();
        assertThat("Precondition: directory should exist",
                directory.isDirectory(), is(true));
        assertThat("Precondition: directory should be empty",
                directory.listFiles().length, is(0));
        
        File file = new File(directory, "some_file.txt");
        file.createNewFile();
        
        assertThat("Precondition: directory should not be empty",
                directory.listFiles().length, is(1));
        
        FileUtils.deleteDirectory(directory);
        
        assertThat("Postcondition: directory should not exist",
                directory.exists(), is(false));
        assertThat("Postcondition: file should not exist",
                file.exists(), is(false));
    }
    
    @Test
    public void deleteDirectoryMultipleFilesInside() throws IOException {
        File directory = new File(TESTDATA, "temp_directory");
        directory.mkdir();
        assertThat("Precondition: directory should exist",
                directory.isDirectory(), is(true));
        assertThat("Precondition: directory should be empty",
                directory.listFiles().length, is(0));
        
        for (int i = 0; i < 10; i++) {
            File file = new File(directory, "some_file" + i + ".txt");
            file.createNewFile();
        }
        
        assertThat("Precondition: directory should not be empty",
                directory.listFiles().length, is(10));
        
        FileUtils.deleteDirectory(directory);
        
        assertThat("Postcondition: directory should not exist",
                directory.exists(), is(false));
    }
    
    @Test(expected = IOException.class)
    public void deleteDirectoryCantDeleteFile() throws IOException {
        File directory = new File(TESTDATA, "singleFile");
        assertThat("Precondition: directory should exist",
                directory.isDirectory(), is(true));
        assertThat("Precondition: directory should not be empty",
                directory.listFiles().length, is(1));
        
        File file = new File(directory, "some_file.txt");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        try (FileBlocker blocker = new FileBlocker(file)) {
            
            FileUtils.deleteDirectory(directory);
        }
    }
    
    @Test
    public void deleteDirectoryNestedDirectories() throws IOException {
        File directory = new File(TESTDATA, "temp_directory");
        directory.mkdir();
        assertThat("Precondition: directory should exist",
                directory.isDirectory(), is(true));
        assertThat("Precondition: directory should be empty",
                directory.listFiles().length, is(0));
        
        File nested = new File(directory, "nested");
        nested.mkdir();
        assertThat("Precondition: nested directory should exist",
                directory.isDirectory(), is(true));
        
        for (int i = 0; i < 10; i++) {
            File file = new File(nested, "some_file" + i + ".txt");
            file.createNewFile();
        }
        
        for (int i = 0; i < 10; i++) {
            File file = new File(directory, "some_file" + i + ".txt");
            file.createNewFile();
        }
        
        assertThat("Precondition: directory should not be empty",
                directory.listFiles().length, is(11));
        
        assertThat("Precondition: nested directory should not be empty",
                nested.listFiles().length, is(10));
        
        FileUtils.deleteDirectory(directory);
        
        assertThat("Postcondition: directory should not exist",
                directory.exists(), is(false));
        assertThat("Postcondition: nested directory should not exist",
                nested.exists(), is(false));
    }
    
    @Test
    public void temporaryDirectoryCleaned() throws IOException {
        File tempdir = FileUtils.createTemporaryDirectory();
        
        assertThat("created temporary directory should exist",
                tempdir.isDirectory(), is(true));
        assertThat("created temporary directory should be empty",
                tempdir.listFiles().length, is(0));
        
        FileUtils.cleanTemporaryFolders();
        
        assertThat("created temporary directory should not exist after cleaning",
                tempdir.exists(), is(false));
    }
    
    @Test
    public void temporaryDirectoryCleanFails() throws IOException {
        File tempdir = FileUtils.createTemporaryDirectory();
        
        assertThat("created temporary directory should exist",
                tempdir.isDirectory(), is(true));
        assertThat("created temporary directory should be empty",
                tempdir.listFiles().length, is(0));
        
        File file = new File(tempdir, "some-file.txt");
        file.createNewFile();
        try (FileBlocker blocker = new FileBlocker(file)) {
            
            FileUtils.cleanTemporaryFolders();
            
            assertThat("created temporary directory should still exist after failed cleaning",
                    tempdir.isDirectory(), is(true));
        }
        
        FileUtils.deleteDirectory(tempdir);
        assertThat("cleanup: created temporary directory should not exist after cleanup",
                tempdir.exists(), is(false));
    }
    
    @Test
    public void temporaryDirectoryCreateFile() throws IOException {
        File tempdir = FileUtils.createTemporaryDirectory();
        
        assertThat("precondition: created temporary directory should exist",
                tempdir.isDirectory(), is(true));
        assertThat("precondition: created temporary directory should be empty",
                tempdir.listFiles().length, is(0));
        
        File nested = new File(tempdir, "nested_file");
        
        assertThat("precondition: nested file should not exist",
                nested.exists(), is(false));
        
        assertThat("postcondition: creation of nested file should succeed",
                nested.createNewFile(), is(true));
        assertThat("postcondition: nested file should exist",
                nested.isFile(), is(true));
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
    
    @BeforeClass
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }
    
}
