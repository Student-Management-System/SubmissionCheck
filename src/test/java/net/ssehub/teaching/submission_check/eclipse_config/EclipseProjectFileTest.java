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
package net.ssehub.teaching.submission_check.eclipse_config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class EclipseProjectFileTest {
    
    private static final File TESTDATA = new File("src/test/resources/EclipseProjectFileTest");

    @Test(expected = FileNotFoundException.class)
    public void notExistingFile() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "doesnt_exist");
        assertThat("Precondition: test file should not exist",
                file.exists(), is(false));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void invalidXml() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "invalidXml.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void invalidRoot() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "invalidRoot.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void missingName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "missingName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void missingBuildSpec() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "missingBuildSpec.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void missingNatures() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "missingNatures.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void doubleName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "doubleName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void doubleBuildSpec() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "doubleBuildSpec.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void doubleNatures() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "doubleNatures.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void invalidName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "invalidName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void emptyName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "emptyName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test
    public void onlyName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "onlyName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        EclipseProjectFile result = new EclipseProjectFile(file);
        
        assertThat("Postcondition: should have correct name",
                result.getName(), is("The Test Project Name"));
        
        assertThat("Postcondition: should have no builders",
                result.getBuilders(), is(Arrays.asList()));
        
        assertThat("Postcondition: should have no natures",
                result.getNatures(), is(Arrays.asList()));
    }
    
    @Test
    public void builders() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "builders.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        EclipseProjectFile result = new EclipseProjectFile(file);
        
        assertThat("Postcondition: should have correct builders",
                result.getBuilders(), is(Arrays.asList(
                        EclipseProjectFile.BUILDER_JAVA, EclipseProjectFile.BUILDER_CHECKSTYLE
                )));
        
        assertThat("Postcondition: should have no natures",
                result.getNatures(), is(Arrays.asList()));
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void invalidBuildSpec() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "invalidBuildSpec.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void buildCommandMissingName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "buildCommandMissingName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void buildCommandDoubleName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "buildCommandDoubleName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void buildCommandInvalidName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "buildCommandInvalidName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test
    public void natures() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "natures.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        EclipseProjectFile result = new EclipseProjectFile(file);
        
        assertThat("Postcondition: should have correct natures",
                result.getNatures(), is(Arrays.asList(
                        EclipseProjectFile.NATURE_JAVA, EclipseProjectFile.NATURE_CHECKSTYLE
                )));
        
        assertThat("Postcondition: should have no builders",
                result.getBuilders(), is(Arrays.asList()));
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void invalidNatures() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "invalidNatures.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test(expected = InvalidEclipseConfigException.class)
    public void naturesInvalidName() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "naturesInvalidName.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        new EclipseProjectFile(file);
    }
    
    @Test
    public void javaAndCheckstyle() throws InvalidEclipseConfigException, IOException {
        File file = new File(TESTDATA, "javaAndCheckstyle.xml");
        assertThat("Precondition: test file should exist",
                file.isFile(), is(true));
        
        EclipseProjectFile result = new EclipseProjectFile(file);
        
        assertThat("Postcondition: should have correct project name",
                result.getName(), is("SomeProjectName"));
        
        assertThat("Postcondition: should have correct natures",
                result.getNatures(), is(Arrays.asList(
                        EclipseProjectFile.NATURE_JAVA, EclipseProjectFile.NATURE_CHECKSTYLE
                )));
        
        assertThat("Postcondition: should have correct natures",
                result.getNatures(), is(Arrays.asList(
                        EclipseProjectFile.NATURE_JAVA, EclipseProjectFile.NATURE_CHECKSTYLE
                )));
    }
    
    
}
