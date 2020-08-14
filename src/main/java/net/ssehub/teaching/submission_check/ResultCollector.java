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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ssehub.teaching.submission_check.checks.Check;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;

/**
 * Collects all {@link ResultMessage}s and status of {@link Check}s during an execution.
 * 
 * @author Adam
 */
public class ResultCollector {
    
    private List<ResultMessage> allMessages;
    
    private Map<Submission, List<ResultMessage>> messagesBySubmission;
    
    private boolean overallSuccess;
    
    /**
     * Creates a new {@link ResultMessage} with no messages yet.
     */
    public ResultCollector() {
        this.allMessages = new LinkedList<>();
        this.messagesBySubmission = new HashMap<>();
        this.overallSuccess = true;
    }
    
    /**
     * Adds a {@link ResultMessage} that is not associated with any {@link Submission}. Usually this should only be
     * called for global messages (i.e. internal errors in the hook).
     * 
     * @param message A {@link ResultMessage}.
     */
    public void addMessage(ResultMessage message) {
        this.allMessages.add(message);
    }
    
    /**
     * Adds a {@link ResultMessage} that was produced by a {@link Check} for a {@link Submission}.
     * 
     * @param message A {@link ResultMessage}.
     * @param submission The {@link Submission} that the {@link ResultMessage} is created for.
     */
    public void addMessage(ResultMessage message, Submission submission) {
        this.allMessages.add(message);
        
        List<ResultMessage> submissionMessages = messagesBySubmission.get(submission);
        if (submissionMessages == null) {
            submissionMessages = new LinkedList<>();
            messagesBySubmission.put(submission, submissionMessages);
        }
        submissionMessages.add(message);
    }
    
    /**
     * Adds the result of a {@link Check} execution.
     * 
     * @param success Whether the check execution was successful.
     */
    public void addCheckResult(boolean success) {
        this.overallSuccess &= success;
    }
    
    /**
     * Returns all messages for a given {@link Submission} that were previously added.
     * 
     * @param submission The {@link Submission} to get all messages for.
     * 
     * @return An unmodifiable view on the messages for a given {@link Submission}.
     *      May be empty, but never <code>null</code>. 
     *      
     * @see #addMessage(ResultMessage, Submission)
     */
    public List<ResultMessage> getMessageForSubmission(Submission submission) {
        List<ResultMessage> submissionMessages = messagesBySubmission.get(submission);
        if (submissionMessages == null) {
            submissionMessages = Collections.emptyList();
        }
        return Collections.unmodifiableList(submissionMessages);
    }
    
    /**
     * Returns all previously added messages.
     * 
     * @return An unmodifiable view on the previously added messages.
     * 
     * @see #addMessage(ResultMessage)
     * @see #addMessage(ResultMessage, Submission)
     */
    public List<ResultMessage> getAllMessages() {
        return Collections.unmodifiableList(allMessages);
    }
    
    
    /**
     * Returns whether all previous check results are successful.
     * 
     * @return Whether all previous check results are successful.
     * 
     * @see #addCheckResult(boolean)
     */
    public boolean getAllSuccessful() {
        return overallSuccess;
    }
    
    /**
     * Returns the exit code that the hook should return. This depends on the phase:
     * <ul>
     *  <li>For the <b>PRE</b>-commit phase, a failure code is only generated if a test has failed. This is because
     *  a failure in this phase will cause the commit to be rejected.</li>
     *  <li>For the <b>POST</b>-commit phase, a failure code is generated if there is any message at all (even a
     *  warning). A failure here does not influence the commit, but is required so that the client receives the
     *  messages created by the hook.</li>
     * </ul>
     * 
     * @param phase The {@link Phase} of that the hook is running in.
     * 
     * @return The exit codes for the hook process.
     * 
     * @see System#exit(int)
     */
    public int getExitCode(Phase phase) {
        int exitCode;
        
        switch (phase) {
        case PRE_COMMIT:
            if (overallSuccess) {
                exitCode = 0;
            } else {
                exitCode = 1;
            }
            break;
            
        case POST_COMMIT:
            if (overallSuccess && allMessages.isEmpty()) {
                exitCode = 0;
            } else {
                exitCode = 1;
            }
            break;
            
        default:
            throw new IllegalArgumentException("Invalid phase " + phase);
        }
        
        return exitCode;
    }
    
}
