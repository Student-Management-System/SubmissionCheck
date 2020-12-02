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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.ssehub.teaching.submission_check.Submission;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;
import net.ssehub.teaching.submission_check.utils.FileUtils;

/**
 * An {@link ISvnInterface} that uses the <code>svnlook</code> command-line tool to interact with the SVN repository.
 * 
 * @author Adam
 */
public class CliSvnInterface implements ISvnInterface {
    
    private static final Logger LOGGER = Logger.getLogger(CliSvnInterface.class.getName());
    
    protected File repositoryPath;
    
    protected Phase phase;
    
    protected String transactionId;
    
    /**
     * Runs the <code>svnlook</code> command with the given sub-command. Repository path and transaction identifier
     * are set automatically based on {@link #repositoryPath}, {@link #phase}, and {@link #transactionId}.
     * 
     * @param subcommand The sub-command to run.
     * @param additionalArguments Additional command-line arguments to be passed to the process.
     * 
     * @return The standard output of the <code>svnlook</code> process as a list of lines.
     * 
     * @throws SvnException If the <code>svnlook</code> exists abnormally.
     */
    private List<String> runSvnLookCommand(String subcommand, String... additionalArguments) throws SvnException {
        return runSvnLookCommand(subcommand, null, additionalArguments);
    }
    
    /**
     * Runs the <code>svnlook</code> command with the given sub-command. Repository path and transaction identifier
     * are set automatically based on {@link #repositoryPath}, {@link #phase}, and {@link #transactionId}.
     * 
     * @param subcommand The sub-command to run.
     * @param outputRedirect A file to write the command output to. If <code>null</code>, then the output will be
     *      returned as a list of lines instead.
     * @param additionalArguments Additional command-line arguments to be passed to the process.
     * 
     * @return The standard output of the <code>svnlook</code> process as a list of lines. <code>null</code> if
     *      outputRedirect is non-<code>null</code>.
     * 
     * @throws SvnException If the <code>svnlook</code> exists abnormally.
     */
    protected List<String> runSvnLookCommand(String subcommand, File outputRedirect, String... additionalArguments)
            throws SvnException {
        List<String> command = new LinkedList<>();
        command.add("svnlook");
        command.add(subcommand);
        command.add(this.repositoryPath.getAbsolutePath());
        
        switch (this.phase) {
        case PRE_COMMIT:
            command.add("--transaction");
            command.add(this.transactionId);
            break;
            
        case POST_COMMIT:
            command.add("--revision");
            command.add(this.transactionId);
            break;
        
        default:
            throw new SvnException("Invalid phase: " + this.phase);
        }
        
        for (String additionalArgument : additionalArguments) {
            command.add(additionalArgument);
        }
        
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(this.repositoryPath);
        builder.redirectError(Redirect.PIPE);
        if (outputRedirect != null) {
            builder.redirectOutput(outputRedirect);
        } else {
            builder.redirectOutput(Redirect.PIPE);
        }
        
        List<String> stdout = null;
        try {
            Process process = builder.start();
            captureAndLogStderr(process);
            
            if (outputRedirect == null) {
                stdout = new LinkedList<>();
                BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = stdoutReader.readLine()) != null) {
                    stdout.add(line);
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new SvnException("svnlook exited with exit code " + exitCode);
            }
            
        } catch (IOException e) {
            throw new SvnException("Exception while running svnlook command", e);
        } catch (InterruptedException e) {
            throw new SvnException("Exception while waiting for svnlook command", e);
        }
        
        return stdout;
    }
    
    /**
     * Reads the error output stream of the given process and logs it to the {@link #LOGGER} if it's not empty.
     * 
     * @param process The process to capture the error output from.
     */
    private void captureAndLogStderr(Process process) {
        new Thread(() -> {
            try (BufferedReader stderrReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                
                StringBuilder stderr = new StringBuilder();
                String line;
                while ((line = stderrReader.readLine()) != null) {
                    stderr.append(line).append('\n');
                }
                
                if (stderr.length() > 0) {
                    LOGGER.log(Level.WARNING, "Got error output from svnlook:\n{0}", stderr);
                }
                
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to read stderr from svnlook", e);
            }
        }).start();
    }

    @Override
    public TransactionInfo createTransactionInfo(Phase phase, File repositoryPath, String transactionId)
            throws SvnException {
        
        this.repositoryPath = repositoryPath;
        this.phase = phase;
        this.transactionId = transactionId;
        
        List<String> output = runSvnLookCommand("author");
        if (output.size() != 1) {
            throw new SvnException("svnlook author created " + output.size() + " lines of output, expected 1");
        }
        
        String author = output.get(0);
        
        return new TransactionInfo(this.repositoryPath, author, this.transactionId, this.phase);
    }
    
    @Override
    public Set<Submission> getModifiedSubmissions(TransactionInfo transaction) throws SvnException {
        this.repositoryPath = transaction.getRepository();
        this.phase = transaction.getPhase();
        this.transactionId = transaction.getTransactionId();
        
        Set<Submission> changedSubmissions = new HashSet<>();
        
        List<String> output = runSvnLookCommand("changed");
        for (String changeLine : output) {
            Submission changedSubmission = getChangedSubmissionFolder(changeLine);
            if (changedSubmission != null) {
                changedSubmissions.add(changedSubmission);
            }
        }
        
        return changedSubmissions;
    }
    
    /**
     * Returns the number of parents that the given file path has.
     * 
     * @param file The file path.
     * 
     * @return The number of parents that this file path has.
     */
    private static int getNumberOfParents(File file) {
        int result = 0;
        if (file.getParentFile() != null) {
            result = 1 + getNumberOfParents(file.getParentFile());
        }
        return result;
    }
    
    /**
     * Parses the given change line from <code>svnlook changed</code> and returns the submission folder that was
     * affected.
     * 
     * @param changeLine The change line as created by <code>svnlook changed</code>.
     * 
     * @return The affect submission folder, or <code>null</code> if no submission folder is affected.
     * 
     * @throws SvnException If the line has an invalid format.
     */
    private Submission getChangedSubmissionFolder(String changeLine) throws SvnException {
        if (changeLine.length() < 3) {
            throw new SvnException("Got empty line from svnlook changed");
        }
        
        String change = changeLine.substring(0, 2);
        if (!change.matches("^((A|D|U) )|_U|UU$")) {
            throw new SvnException("Got invalid change '" + change + "' in line " + changeLine);
        }
        
        File path = new File(changeLine.substring(2).trim());
        if (getNumberOfParents(path) > 1) {
            while (getNumberOfParents(path) > 1) {
                path = path.getParentFile();
            }
            
        } else {
            // something not deep enough to be submission content is modified -> we don't care about that
            path = null;
        }
        
        Submission submission = null;
        if (path != null) {
            submission = new Submission(path.getParentFile().getName(), path.getName());
        }
        
        return submission;
    }

    @Override
    public void checkoutSubmission(TransactionInfo transaction, Submission submission, File checkoutLocation)
            throws SvnException, IOException {
        
        LOGGER.log(Level.FINER, "Checking out submission {0} to {1}", new Object[] {submission, checkoutLocation});
        
        this.repositoryPath = transaction.getRepository();
        this.phase = transaction.getPhase();
        this.transactionId = transaction.getTransactionId();
        
        List<File> filesInSubmission = runSvnLookCommand("tree", "--full-paths", submission.getPathInRepo().getPath())
                .stream()
                .filter((path) -> !path.endsWith("/")) // directories always have a trailing slash (only directories)
                .map((path) -> new File(path))
                .collect(Collectors.toList());
        
        for (File file : filesInSubmission) {
            checkoutFile(file, submission, checkoutLocation);
        }
    }
    
    /**
     * Checks out the given file in the repository into the given target submission directory.
     * 
     * @param fileInRepo The file path relative to the repository root.
     * @param submission The {@link Submission} in the repository that this file is a part of.
     * @param checkoutLocation The base-directory to write the output file to. Sub-folders will be created where
     *      necessary.
     *      
     * @throws SvnException If checking out the file with <code>svnlook</code> fails.
     * @throws IOException If creating the parent directory fails.
     */
    private void checkoutFile(File fileInRepo, Submission submission, File checkoutLocation)
            throws SvnException, IOException {
        
        File targetFile = new File(checkoutLocation,
                FileUtils.getRelativeFile(submission.getPathInRepo(), fileInRepo).getPath());
        
        File parentDir = targetFile.getParentFile();
        if (!parentDir.isDirectory() && !parentDir.mkdirs()) {
            throw new IOException("Could not create directory " + parentDir);
        }
        
        runSvnLookCommand("cat", targetFile, fileInRepo.getPath());
    }

}
