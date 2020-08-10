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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import net.ssehub.teaching.submission_check.Submission;
import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;

public class MockSvnInterface implements ISvnInterface {

    private Phase expectedPhase;
    
    private File expectedRepositoryPath;
    
    private String expectedTransactionId;
    
    private TransactionInfo transactionInfo;
    
    private Set<Submission> modifiedSubmissions;
    
    public void setExpectedPhase(Phase expectedPhase) {
        this.expectedPhase = expectedPhase;
    }
    
    public void setExpectedRepositoryPath(File expectedRepositoryPath) {
        this.expectedRepositoryPath = expectedRepositoryPath;
    }
    
    public void setExpectedTransactionId(String expectedTransactionId) {
        this.expectedTransactionId = expectedTransactionId;
    }
    
    public void setTransactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }
    
    public void setModifiedSubmissions(Set<Submission> modifiedSubmissions) {
        this.modifiedSubmissions = modifiedSubmissions;
    }
    
    @Override
    public TransactionInfo createTransactionInfo(Phase phase, File repositoryPath, String transactionId)
            throws SvnException {
        if (expectedPhase != null) {
            assertThat(phase, is(expectedPhase));
        }
        if (expectedRepositoryPath != null) {
            assertThat(repositoryPath, is(expectedRepositoryPath));
        }
        if (expectedTransactionId != null) {
            assertThat(transactionId, is(expectedTransactionId));
        }
        
        return transactionInfo;
    }

    @Override
    public Set<Submission> getModifiedSubmissions(TransactionInfo transaction) throws SvnException {
        if (expectedPhase != null) {
            assertThat(transaction.getPhase(), is(expectedPhase));
        }
        if (expectedRepositoryPath != null) {
            assertThat(transaction.getRepository(), is(expectedRepositoryPath));
        }
        if (expectedTransactionId != null) {
            assertThat(transaction.getTransactionId(), is(expectedTransactionId));
        }
        
        return modifiedSubmissions;
    }

    @Override
    public void checkoutSubmission(TransactionInfo transaction, Submission submission, File checkoutLocation)
            throws SvnException, IOException {
        if (expectedPhase != null) {
            assertThat(transaction.getPhase(), is(expectedPhase));
        }
        if (expectedRepositoryPath != null) {
            assertThat(transaction.getRepository(), is(expectedRepositoryPath));
        }
        if (expectedTransactionId != null) {
            assertThat(transaction.getTransactionId(), is(expectedTransactionId));
        }
        
        
    }

}
