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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.teaching.submission_check.checks.Check;
import net.ssehub.teaching.submission_check.checks.CheckstyleCheck;
import net.ssehub.teaching.submission_check.checks.CliJavacCheck;
import net.ssehub.teaching.submission_check.checks.EclipseConfigCheck;
import net.ssehub.teaching.submission_check.checks.EncodingCheck;
import net.ssehub.teaching.submission_check.checks.FileSizeCheck;
import net.ssehub.teaching.submission_check.checks.InternalJavacCheck;
import net.ssehub.teaching.submission_check.checks.JavacCheck;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.LoggingSetup;

public class ConfigurationTest {

    private static final File TESTDATA = new File("src/test/resources/ConfigurationTest");
    
    @Test(expected = FileNotFoundException.class)
    public void invalidConfigurationFile() throws IOException {
        File configFile = new File(TESTDATA, "doesnt_exist");
        assertThat("Precondition: test file should not exist",
                configFile.exists(), is(false));
        
        new Configuration(configFile);
    }
    
    @Test
    public void directlyConfiguredForExercise() throws IOException {
        File configFile = new File(TESTDATA, "singleExercise.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        assertThat("Postcondition: should get correct property value",
                config.getProperty("atestkey", new Submission("Exercise01", "")), is("some configured value"));
        
        assertThat("Postcondition: should return no value for other exercise",
                config.getProperty("atestkey", new Submission("Exercise02", "")), is(nullValue()));
    }
    
    @Test
    public void directlyConfiguredForAllExercises() throws IOException {
        File configFile = new File(TESTDATA, "forAll.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        assertThat("Postcondition: should get correct property value",
                config.getProperty("atestkey", new Submission("Exercise01", "")), is("some random configured value"));
        
        assertThat("Postcondition: should get correct property value",
                config.getProperty("atestkey", new Submission("Exercise02", "")), is("some random configured value"));
    }
    
    @Test
    public void directlyConfiguredOverriddenBySpecificExercise() throws IOException {
        File configFile = new File(TESTDATA, "specificOverrides.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        assertThat("Postcondition: should get global value",
                config.getProperty("atestkey", new Submission("Exercise01", "")), is("the global value"));
        
        assertThat("Postcondition: should get global value",
                config.getProperty("atestkey", new Submission("Exercise02", "")), is("the global value"));
        
        assertThat("Postcondition: should specific value",
                config.getProperty("atestkey", new Submission("Exercise03", "")), is("some specific value"));
        
        assertThat("Postcondition: should get global value",
                config.getProperty("atestkey", new Submission("Exercise04", "")), is("the global value"));
    }
    
    @Test
    public void emptySettings() throws IOException {
        File configFile = new File(TESTDATA, "empty.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        assertThat("Postcondition: should return null for all settings",
                config.getProperty("maxFileSize", new Submission("Exercise01", "")), is(nullValue()));
    }
    
    
    @Test
    public void fileSizeCheckDefault() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "empty.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        FileSizeCheck check = config.createFileSizeCheck(new Submission("Exercise01", ""));
        
        assertThat("Postcondition: should have default values set",
                check.getMaxFileSize(), is(10485760L));
        assertThat("Postcondition: should have default values set",
                check.getMaxSubmissionSize(), is(10485760L));
    }
    
    @Test
    public void fileSizeCheck() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "fileSizeCheck.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        FileSizeCheck check = config.createFileSizeCheck(new Submission("Exercise01", ""));
        
        assertThat("Postcondition: should have correct user-defined value",
                check.getMaxFileSize(), is(15L));
        assertThat("Postcondition: should have correct user-defined value",
                check.getMaxSubmissionSize(), is(17L));
        
        check = config.createFileSizeCheck(new Submission("Exercise02", ""));
        
        assertThat("Postcondition: should have correct global default value",
                check.getMaxFileSize(), is(10485760L));
        assertThat("Postcondition: should have correct global default value",
                check.getMaxSubmissionSize(), is(10485760L));
    }
    
    @Test(expected = ConfigurationException.class)
    public void fileSizeCheckInvalidFileSize() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "fileSizeCheckInvalidFile.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        config.createFileSizeCheck(new Submission("Exercise01", ""));
    }
    
    @Test(expected = ConfigurationException.class)
    public void fileSizeCheckInvalidSubmissionsSize() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "fileSizeCheckInvalidSubmission.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        config.createFileSizeCheck(new Submission("Exercise01", ""));
    }
    
    @Test
    public void encodingCheckDefault() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "empty.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        EncodingCheck check = config.createEncodingCheck(new Submission("Exercise01", ""));
        
        assertThat("Postcondition: should have default value set",
                check.getWantedCharset(), is(StandardCharsets.UTF_8));
    }
    
    @Test
    public void encodingCheck() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "encoding.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        EncodingCheck check = config.createEncodingCheck(new Submission("Exercise01", ""));
        
        assertThat(check.getWantedCharset(), is(StandardCharsets.US_ASCII));
    }
    
    @Test(expected = ConfigurationException.class)
    public void encodingCheckInvalid() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "encodingInvalid.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        config.createEncodingCheck(new Submission("Exercise01", ""));
    }
    
    @Test
    public void eclipseConfigCheckDefaults() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "empty.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        EclipseConfigCheck check = config.createEclipseConfigCheck(new Submission("Exercise01", ""));
        
        assertThat("Postcondition: missing setting should return default value",
                check.getRequireJavaProject(), is(false));
        assertThat("Postcondition: missing setting should return default value",
                check.getRequireCheckstyleProject(), is(false));
    }
    
    @Test
    public void eclipseConfigCheck() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "eclipseConfig.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        EclipseConfigCheck check = config.createEclipseConfigCheck(new Submission("Exercise01", ""));
        
        assertThat("Postcondition: should return configured value",
                check.getRequireJavaProject(), is(true));
        assertThat("Postcondition: should return configured value",
                check.getRequireCheckstyleProject(), is(false));
    }
    
    @Test
    public void javacCheckImplementation() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "empty.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        JavacCheck check = config.createJavacCheck(new Submission("Exercise01", ""), true);
        
        assertThat(check, instanceOf(InternalJavacCheck.class));
        
        check = config.createJavacCheck(new Submission("Exercise01", ""), false);
        assertThat(check, instanceOf(CliJavacCheck.class));
    }
    
    @Test
    public void javacCheckDefault() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "empty.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        JavacCheck check = config.createJavacCheck(new Submission("Exercise01", ""), false);
        
        assertThat("Postcondition: should have default value set",
                ((CliJavacCheck) check).getJavacCommand(), is("javac"));
        
        assertThat("Postcondition: should have default value set",
                check.getJavaVersion(), is(11));
        
        assertThat("Postcondition: should have default value set",
                check.getCharset(), is(StandardCharsets.UTF_8));
        
        assertThat("Postcondition: should have default value set",
                check.getEnableWarnings(), is(false));
        
        assertThat("Postcondition: should have default value set",
                check.getClasspath(), is(Arrays.asList()));
    }
    
    @Test
    public void javacCheck() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "javacCheck.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        JavacCheck check = config.createJavacCheck(new Submission("Exercise01", ""), false);
        
        assertThat(((CliJavacCheck) check).getJavacCommand(), is("/opt/some/javac"));
        assertThat(check.getJavaVersion(), is(8));
        assertThat(check.getCharset(), is(StandardCharsets.US_ASCII));
        assertThat(check.getEnableWarnings(), is(true));
        assertThat(check.getClasspath(), is(Arrays.asList(new File("libs/libA.jar"), new File("libs/libB.jar"))));
    }
    
    @Test(expected = ConfigurationException.class)
    public void javacCheckInvalidVersion() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "javacCheckInvalidVersion.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        config.createJavacCheck(new Submission("Exercise01", ""), false);
    }
    
    @Test(expected = ConfigurationException.class)
    public void javacCheckInvalidEncoding() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "encodingInvalid.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        config.createJavacCheck(new Submission("Exercise01", ""), false);
    }
    
    @Test
    public void checkstyelCheckDefaultEncoding() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "checkstyleCheckNoEncoding.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        CheckstyleCheck check = config.createCheckstyleCheck(new Submission("Exercise01", ""));
        
        assertThat("Postcondition: should have rules file set as configured",
                check.getCheckstyleRules(), is(new File("checkstyle/rulesA.xml")));
        
        assertThat("Postcondition: should have default value set",
                check.getCharset(), is(StandardCharsets.UTF_8));
    }
    
    @Test
    public void checkstyelCheckCustomEncoding() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "checkstyleCheckWithEncoding.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        CheckstyleCheck check = config.createCheckstyleCheck(new Submission("Exercise01", ""));
        
        assertThat("Postcondition: should have rules file set as configured",
                check.getCheckstyleRules(), is(new File("checkstyle/rulesA.xml")));
        
        assertThat("Postcondition: should have default value set",
                check.getCharset(), is(StandardCharsets.ISO_8859_1));
    }
    
    @Test(expected = ConfigurationException.class)
    public void checkstyelCheckMissingRulesFile() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "empty.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        config.createCheckstyleCheck(new Submission("Exercise01", ""));
    }
    
    @Test(expected = ConfigurationException.class)
    public void checkstyelCheckInvalidEncoding() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "checkstyleCheckInvalidEncoding.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        config.createCheckstyleCheck(new Submission("Exercise01", ""));
    }
    
    @Test
    public void createChecksForPreCommit() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "minimal.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        List<Check> checks = config.createChecks(new Submission("Exercise01", "A"), Phase.PRE_COMMIT);
        
        assertThat(checks.get(0), instanceOf(FileSizeCheck.class));
        assertThat(checks.get(1), instanceOf(EncodingCheck.class));
        assertThat(checks.get(2), instanceOf(EclipseConfigCheck.class));
        assertThat(checks.size(), is(3));
    }
    
    @Test
    public void createChecksForPostCommit() throws IOException, ConfigurationException {
        File configFile = new File(TESTDATA, "minimal.properties");
        assertThat("Precondition: test file should exist",
                configFile.isFile(), is(true));
        
        Configuration config = new Configuration(configFile);
        
        List<Check> checks = config.createChecks(new Submission("Exercise01", "A"), Phase.POST_COMMIT);
        
        assertThat(checks.get(0), instanceOf(EclipseConfigCheck.class));
        assertThat(checks.get(1), instanceOf(JavacCheck.class));
        assertThat(checks.get(2), instanceOf(CheckstyleCheck.class));
        assertThat(checks.size(), is(3));
    }
    
    @BeforeClass
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }
    
}
