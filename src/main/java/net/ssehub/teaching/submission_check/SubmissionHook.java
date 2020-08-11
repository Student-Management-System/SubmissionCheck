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
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.checks.Check;
import net.ssehub.teaching.submission_check.svn.CliSvnInterface;
import net.ssehub.teaching.submission_check.svn.ISvnInterface;
import net.ssehub.teaching.submission_check.svn.SvnException;
import net.ssehub.teaching.submission_check.svn.TransactionInfo;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.FileUtils;
import net.ssehub.teaching.submission_check.utils.LoggingSetup;

/**
 * The main class of this hook. This is called by the SVN hook mechanism. Talks to the {@link ISvnInterface} for the
 * proper setup and then orchestrates the execution of the {@link Check}s.
 * 
 * @author Adam
 */
public class SubmissionHook {
    
    private static final Logger LOGGER = Logger.getLogger(SubmissionHook.class.getName());

    private Phase phase;
    
    private File repositoryPath;
    
    private String transactionId;
    
    private ISvnInterface svnInterface;
    
    private Configuration configuration;
    
    private TransactionInfo transactionInfo;
    
    private Set<Submission> modifiedSubmissions;
    
    private CheckRunner checkRunner;
    
    /**
     * Creates a new {@link SubmissionHook} instance.
     * 
     * @param args Command line arguments. Expected:
     *      <ul>
     *          <li>[0]: Either "PRE" or "POST" signalling the phase of this hook</li>
     *          <li>[1]: The path of the SVN repository</li>
     *          <li>[2]: The transaction identifier (revision number if phase is "POST"</li>
     *      </ul>
     * @param resultCollector The {@link ResultCollector} to add {@link Check} results to.
     * @param svnInterface The {@link ISvnInterface} that should be used for SVN operations.
     */
    public SubmissionHook(String[] args, ResultCollector resultCollector, ISvnInterface svnInterface)
            throws IllegalArgumentException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Expected exactly 3 arguments, got " + args.length);
        }
        
        switch (args[0]) {
        case "PRE":
            this.phase = Phase.PRE_COMMIT;
            break;
            
        case "POST":
            this.phase = Phase.POST_COMMIT;
            break;
            
        default:
            throw new IllegalArgumentException("Expected PRE or POST, got " + args[0]);
        }
        
        this.repositoryPath = new File(args[1]);
        if (!this.repositoryPath.isDirectory()) {
            throw new IllegalArgumentException(repositoryPath + " is not a directory");
        }
        
        this.transactionId = args[2];
        
        this.checkRunner = new CheckRunner(resultCollector);
        this.svnInterface = svnInterface;
    }

    /**
     * Returns the phase of this hook.
     * 
     * @return The phase of this hook.
     */
    public Phase getPhase() {
        return phase;
    }
    
    /**
     * Returns the repository path of this hook.
     * 
     * @return The repository path of this hook.
     */
    public File getRepositoryPath() {
        return repositoryPath;
    }
    
    /**
     * Returns the transaction ID of this hook.
     *  
     * @return The transaction ID.
     */
    public String getTransactionId() {
        return transactionId;
    }
    
    /**
     * Retrieves the metadata about the transaction and modified submissions from SVN.
     * 
     * @throws SvnException If an exception occurs during an SVN operation.
     */
    public void queryMetadataFromSvn() throws SvnException {
        this.transactionInfo = svnInterface.createTransactionInfo(phase, repositoryPath, transactionId);
        this.modifiedSubmissions = svnInterface.getModifiedSubmissions(transactionInfo);
    }
    
    /**
     * Reads a {@link Configuration} from the specified file path.
     * 
     * @param configurationFile The configuration file to read.
     * 
     * @throws IOException If reading the configuration file fails.
     */
    public void readConfiguration(File configurationFile) throws IOException {
        this.configuration = new Configuration(configurationFile);
    }
    
    /**
     * Returns the configuration that was read for this hook.
     * 
     * @return The {@link Configuration}.
     * 
     * @see #readConfiguration(File)
     */
    public Configuration getConfiguration() {
        return configuration;
    }
    
    /**
     * Information on the SVN transaction that this hook runs for. Populated in {@link #queryMetadataFromSvn()}.
     * 
     * @return The {@link TransactionInfo}.
     */
    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }
    
    /**
     * The list of modified submission directories. Populated in {@link #queryMetadataFromSvn()}.
     * 
     * @return The list of modified {@link Submission}s.
     */
    public Set<Submission> getModifiedSubmissions() {
        return modifiedSubmissions;
    }
    
    /**
     * Runs the {@link Check}s on all submissions that are modified by this transaction (as queried by
     * {@link #queryMetadataFromSvn()}).
     * 
     * @throws IOException If creating a temporary checkout fails. 
     * @throws SvnException If checking out the submission fails.
     * @throws ConfigurationException If the {@link Check}s for any submission are not correctly configured.
     * 
     * @see #getModifiedSubmissions()
     */
    public void runChecksOnAllModifiedSubmissions() throws IOException, SvnException, ConfigurationException {
        for (Submission submission : modifiedSubmissions) {
            runChecksOnSubmission(submission);
        }
    }
    
    /**
     * Runs the {@link Check}s on the given submission affected by this transaction.
     * 
     * @param submission The {@link Submission} folder.
     * 
     * @throws IOException If creating the temporary checkout fails.
     * @throws SvnException If checking out the submission fails.
     * @throws ConfigurationException If the {@link Check}s are not correctly configured.
     */
    public void runChecksOnSubmission(Submission submission) throws IOException, SvnException, ConfigurationException {
        File checkoutDirecotry = FileUtils.createTemporaryDirectory();
        svnInterface.checkoutSubmission(transactionInfo, submission, checkoutDirecotry);
        
        checkRunner.clearChecks();
        for (Check check : configuration.createChecks(submission, phase)) {
            checkRunner.addCheck(check);
        }
        boolean success = checkRunner.run(checkoutDirecotry);
        
        LOGGER.log(Level.INFO, "Check result for " + submission + ": " + (success ? "successful" : "unsuccessful"));
    }
    
    /**
     * The main method called by the hook mechanism of SVN.
     * 
     * @param args Command line arguments. Expected:
     *      <ul>
     *          <li>[0]: Either "PRE" or "POST" signalling the phase of this hook</li>
     *          <li>[1]: The path of the SVN repository</li>
     *          <li>[2]: The transaction identifier (revision number if phase is "POST"</li>
     *      </ul>
     */
    public static void main(String[] args) {
        LoggingSetup.setupFileLogging(new File("submission-check.log"));
        
        LOGGER.log(Level.INFO, "Hook called with arguments " + Arrays.toString(args));
        
        ResultCollector resultCollector = new ResultCollector();
        SubmissionHook hook = new SubmissionHook(args, resultCollector, new CliSvnInterface());
        
        try {
            hook.readConfiguration(new File("config.properties"));
            
            hook.queryMetadataFromSvn();
            
            TransactionInfo info = hook.getTransactionInfo();
            LOGGER.log(Level.INFO, "TransactionInfo:"
                    + " author: " + info.getAuthor()
                    + " affected submissions: " + hook.getModifiedSubmissions());
            
            if (!hook.configuration.getUnrestrictedUsers().contains(info.getAuthor())) {
                hook.runChecksOnAllModifiedSubmissions();
                
            } else {
                LOGGER.log(Level.INFO, info.getAuthor() + " is an unrestrited user, skipping all checks");
            }
            
        } catch (SvnException | IOException | ConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Exception in main", e);
            
            resultCollector.addCheckResult(false);
            resultCollector.addMessage(new ResultMessage("hook", MessageType.ERROR, "An internal error occurred"));
        }
        
        System.err.println(resultCollector.serializeMessages());
        System.exit(resultCollector.getExitCode(hook.phase));
    }
    
}
