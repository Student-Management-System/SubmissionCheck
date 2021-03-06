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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import net.ssehub.teaching.submission_check.checks.Check;
import net.ssehub.teaching.submission_check.checks.CheckstyleCheck;
import net.ssehub.teaching.submission_check.checks.CliJavacCheck;
import net.ssehub.teaching.submission_check.checks.EclipseConfigCheck;
import net.ssehub.teaching.submission_check.checks.EncodingCheck;
import net.ssehub.teaching.submission_check.checks.FileSizeCheck;
import net.ssehub.teaching.submission_check.checks.InternalJavacCheck;
import net.ssehub.teaching.submission_check.checks.JavacCheck;
import net.ssehub.teaching.submission_check.output.StudentManagementSubmitter;
import net.ssehub.teaching.submission_check.output.StudentManagementSubmitter.StudentManagementConfig;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.FileUtils;

/**
 * Represents the preferences set by the user in a configuration file. This class also contains logic in which phase
 * tests should run; this is not configurable by the user (see {@link #createChecks(Submission, Phase)}).
 * 
 * @author Adam
 */
public class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    
    private Properties properties;
    
    private Set<String> unrestrictedUsers;
    
    private String logLevel;
    
    /**
     * Creates a {@link Configuration} based on the given properties file written by the user.
     * 
     * @param configurationFile A properties file written by the user.
     * 
     * @throws IOException If reading the file fails.
     */
    public Configuration(File configurationFile) throws IOException {
        properties = new Properties();
        properties.load(FileUtils.newReader(configurationFile));
        
        initalizeUnrestrictedUsers();
        this.logLevel = properties.getProperty("logLevel");
        if (this.logLevel == null) {
            this.logLevel = "INFO";
        }
    }
    
    /**
     * Initializes the {@link #unrestrictedUsers} set based on {@link #properties}.
     */
    private void initalizeUnrestrictedUsers() {
        this.unrestrictedUsers = new HashSet<>();
        
        String setting = properties.getProperty("unrestrictedUsers");
        if (setting != null && !setting.isBlank()) {
            for (String user : setting.split(",")) {
                this.unrestrictedUsers.add(user.trim());
            }
        }
    }
    
    /**
     * Gets the property for the given key that is configured for the given submission.
     * 
     * Package visibility for test cases.
     * 
     * @param key The property key.
     * @param submission The submission to get the value for.
     * 
     * @return The value of that property, or <code>null</code> if not specified.
     */
    String getProperty(String key, Submission submission) {
        String result = properties.getProperty(submission.getExercise() + '.' + key);
        if (result == null) {
            result = properties.getProperty("all." + key);
        }
        return result;
    }
    
    /**
     * Interface for a lambda-expression passed to
     * {@link Configuration#ifPropertyNotNull(String, Submission, Consumer)).
     */
    private interface PropertyConsumer {

        /**
         * This function is executed when the property value is not <code>null</code>.
         * 
         * @param value The property value.
         * 
         * @throws ConfigurationException If the configuration is invalid.
         */
        public void consume(String value) throws ConfigurationException;
        
    }
    
    /**
     * Executes a given function if {@link #getProperty(String, Submission)} returns non-<code>null</code> for that
     * setting.
     * 
     * @param key The property key.
     * @param submission The submission to get the value for.
     * @param then The function to execute when the value is non-<code>null</code>. Gets the property value as argument.
     * 
     * @throws ConfigurationException If the consumer throws a {@link ConfigurationException}.
     */
    private void ifPropertyNotNull(String key, Submission submission, PropertyConsumer then)
            throws ConfigurationException {
        String value = getProperty(key, submission);
        if (value != null) {
            then.consume(value);
        }
    }
    
    /**
     * Gets the property for the given key that is configured for the given submission. If the property is not set,
     * a {@link ConfigurationException} is thrown (i.e. the property is required).
     * 
     * @param key The property key.
     * @param submission The submission to get the value for.
     * 
     * @return The value of that property, never <code>null</code>.
     * 
     * @throws ConfigurationException If the given property key is not specified.
     */
    private String getRequiredProperty(String key, Submission submission) throws ConfigurationException {
        String value = getProperty(key, submission);
        if (value == null) {
            throw new ConfigurationException("Required value " + key + " is missing for submission "
                    + submission.getExercise());
        }
        return value;
    }
    
    /**
     * Returns the set of user-names that have unrestricted access, i.e. no {@link Check}s should be performed for their
     * submissions. If not explicitly configured, this is an empty set.
     * 
     * @return The {@link Set} of unrestricted users.
     */
    public Set<String> getUnrestrictedUsers() {
        return Collections.unmodifiableSet(this.unrestrictedUsers);
    }
    
    /**
     * Returns the log level that is configured by the user. If not explicitly configured, this is "INFO".
     * 
     * @return The configured log level.
     */
    public String getLogLevel() {
        return logLevel;
    }
    
    /**
     * Creates a {@link FileSizeCheck} with the parameters as configured by the user.
     * 
     * @param submission The submission that this check is for.
     * 
     * @return The configured {@link FileSizeCheck}.
     * 
     * @throws ConfigurationException If the configured values are invalid.
     */
    public FileSizeCheck createFileSizeCheck(Submission submission) throws ConfigurationException {
        FileSizeCheck check = new FileSizeCheck();
        
        ifPropertyNotNull("maxFileSize", submission, (value) -> {
            try {
                check.setMaxFileSize(Long.parseLong(value));
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Invalid maxFileSize setting: " + value, e);
            }
        });
        
        ifPropertyNotNull("maxSize", submission, (value) -> {
            try {
                check.setMaxSubmissionSize(Long.parseLong(value));
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Invalid maxSize setting: " + value, e);
            }
        });
        
        return check;
    }
    
    /**
     * Creates an {@link EncodingCheck} with the parameters as configured by the user.
     * 
     * @param submission The submission that this check is for.
     * 
     * @return The configured {@link EncodingCheck}.
     * 
     * @throws ConfigurationException If the configured values are invalid.
     */
    public EncodingCheck createEncodingCheck(Submission submission) throws ConfigurationException {
        EncodingCheck check = new EncodingCheck();
        
        ifPropertyNotNull("encoding", submission, (value) -> {
            try {
                check.setWantedCharset(Charset.forName(value));
            } catch (IllegalCharsetNameException e) {
                throw new ConfigurationException("Invalid encoding: " + value, e);
            }
        });
        
        return check;
            
    }
    
    /**
     * Creates an {@link EclipseConfigCheck} with the parameters as configured by the user.
     * 
     * @param submission The submission that this check is for.
     * @param onlyBasic Whether only a basic check for the existence of an eclipse project configuration should be done.
     *      If <code>false</code>, further checks (e.g. if the project is a Java project) are done, too.
     * 
     * @return The configured {@link EclipseConfigCheck}.
     */
    public EclipseConfigCheck createEclipseConfigCheck(Submission submission, boolean onlyBasic) {
        EclipseConfigCheck check = new EclipseConfigCheck();
        
        if (!onlyBasic) {
            check.setRequireJavaProject(
                    Boolean.valueOf(getProperty("eclipseConfig.requireJava", submission)));
            check.setRequireCheckstyleProject(
                    Boolean.valueOf(getProperty("eclipseConfig.requireCheckstyle", submission)));
        }
        
        return check;
    }
    
    /**
     * Creates an {@link JavacCheck} with the parameters as configured by the user.
     * 
     * @param submission The submission that this check is for.
     * @param useInternal Whether to use the {@link InternalJavacCheck} or the {@link CliJavacCheck}.
     *      Note: check {@link InternalJavacCheck#isSupported()}.
     * 
     * @return The configured {@link JavacCheck}.
     * 
     * @throws ConfigurationException If the configured values are invalid.
     */
    public JavacCheck createJavacCheck(Submission submission, boolean useInternal) throws ConfigurationException {
        JavacCheck check;
        
        if (useInternal) {
            LOGGER.info("Using InternalJavacCheck");
            check = new InternalJavacCheck();
            
        } else {
            LOGGER.info("Using CliJavacCheck");
            CliJavacCheck cliCheck = new CliJavacCheck();
            
            ifPropertyNotNull("javac.command", submission, (value) -> {
                cliCheck.setJavacCommand(value);
            });
            
            check = cliCheck;
        }
        
        ifPropertyNotNull("javac.version", submission, (value) -> {
            try {
                check.setJavaVersion(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Invalid Java version: " + value, e);
            }
        });
        
        ifPropertyNotNull("encoding", submission, (value) -> {
            try {
                check.setCharset(Charset.forName(value));
            } catch (IllegalCharsetNameException e) {
                throw new ConfigurationException("Invalid encoding: " + value, e);
            }
        });
        
        ifPropertyNotNull("javac.warnings", submission, (value) -> {
            check.setEnableWarnings(Boolean.valueOf(value));
        });
        
        ifPropertyNotNull("javac.classpath", submission, (value) -> {
            for (String element : value.split(",")) {
                check.addToClasspath(new File(element.trim()));
            }
        });
        
        return check;
    }
    
    /**
     * Creates an {@link CheckstyleCheck} with the parameters as configured by the user.
     * 
     * @param submission The submission that this check is for.
     * 
     * @return The configured {@link CheckstyleCheck}.
     * 
     * @throws ConfigurationException If the configured values are invalid.
     */
    public CheckstyleCheck createCheckstyleCheck(Submission submission) throws ConfigurationException {
        String rulesProperty = getProperty("checkstyle.rules", submission);
        if (rulesProperty == null) {
            throw new ConfigurationException("Required checkstyle.rules not configured for submission "
                    + submission.getExercise());
        }
        
        CheckstyleCheck check = new CheckstyleCheck(new File(rulesProperty));
        
        ifPropertyNotNull("encoding", submission, (value) -> {
            try {
                check.setCharset(Charset.forName(value));
            } catch (IllegalCharsetNameException e) {
                throw new ConfigurationException("Invalid encoding: " + value, e);
            }
        });
        
        return check;
    }
    
    /**
     * Creates all checks that are configured for the given submission directory and hook phase.
     * 
     * @param submission The {@link Submission} that the {@link Check}s will run on.
     * @param hookPhase The phase that the checks will run at.
     * 
     * @return The list of {@link Check}s that are configured by the user.
     * 
     * @throws ConfigurationException If the configured values for any {@link Check} are invalid.
     */
    public List<Check> createChecks(Submission submission, Phase hookPhase) throws ConfigurationException {
        List<Check> checks = new LinkedList<>();
        
        switch (hookPhase) {
        case PRE_COMMIT:
            checks.add(createFileSizeCheck(submission));
            checks.add(createEncodingCheck(submission));
            checks.add(createEclipseConfigCheck(submission, true));
            break;
            
        case POST_COMMIT:
            checks.add(createEclipseConfigCheck(submission, false));
            checks.add(createJavacCheck(submission, InternalJavacCheck.isSupported()));
            checks.add(createCheckstyleCheck(submission));
            break;
            
        default:
            throw new IllegalArgumentException("Invalid hook phase: " + hookPhase);
        }
        
        return checks; 
    }
    
    /**
     * Creates the configuration for the {@link StudentManagementSubmitter} for the given submission.
     * 
     * @param submission The submission to create the configuration for.
     * 
     * @return The configuration parameters for the {@link StudentManagementSubmitter}.
     * 
     * @throws ConfigurationException If the configuration is incomplete.
     */
    public StudentManagementConfig getStudentManagementSystemConfiguration(Submission submission)
            throws ConfigurationException {
        
        StudentManagementConfig config = new StudentManagementConfig();
        
        config.setUrl(getRequiredProperty("managementSystem.url", submission));
        config.setCourseName(getRequiredProperty("managementSystem.course.name", submission));
        config.setCourseSemester(getRequiredProperty("managementSystem.course.semester", submission));
        
        config.setAuthenticationUrl(getRequiredProperty("managementSystem.auth.url", submission));
        config.setAuthenticationUsername(getRequiredProperty("managementSystem.auth.username", submission));
        config.setAuthenticationPassword(getRequiredProperty("managementSystem.auth.password", submission));
        
        return config;
    }

}
