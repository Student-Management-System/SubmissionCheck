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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.ssehub.teaching.submission_check.Submission;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;

/**
 * Interface for implementations that communicate with the SVN server. The methods represent the operations on the
 * SVN server that are required for the submission hook to run. Implementations should be state-less between calls to
 * methods defined here.
 * 
 * @author Adam
 */
public interface ISvnInterface {

    /**
     * Creates a {@link TransactionInfo} based on the command-line arguments passed to this SVN hook.
     * 
     * @param phase The phase that the hook is currently running in, as passed to the command-line.
     * @param repositoryPath The repository path as passed to the command-line of the hook.
     * @param transactionId The transaction ID as passed to the command-line of the hook.
     * 
     * @return A {@link TransactionInfo} describing the transaction of this hook.
     * 
     * @throws SvnException If the interaction with the SVN repository fails.
     */
    public TransactionInfo createTransactionInfo(Phase phase, File repositoryPath, String transactionId)
            throws SvnException;
    
    /**
     * Creates a set of {@link Submission}s that are affected (i.e. modified) by the given transaction.
     * 
     * @param transaction The transaction to get the modified submissions for.
     * 
     * @return A set of {@link Submission}s.
     * 
     * @throws SvnException If the interaction with the SVN repository fails.
     */
    public Set<Submission> getModifiedSubmissions(TransactionInfo transaction) throws SvnException;
    
    /**
     * Checks out the content of a submission to a specified location.
     * 
     * @param transaction The transaction to check the submission out for.
     * @param submission The {@link Submission} directory to check out.
     * @param checkoutLocation The location where the checked-out content should be placed. This points to an empty
     *      directory.
     * 
     * @throws SvnException If the interaction with the SVN repository fails.
     * @throws IOException If I/O errors occur.
     */
    public void checkoutSubmission(TransactionInfo transaction, Submission submission, File checkoutLocation)
            throws SvnException, IOException;
    
}
