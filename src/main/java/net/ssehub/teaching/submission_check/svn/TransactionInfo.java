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
import java.util.Objects;

/**
 * Information about the transaction that the submission hook is running for.
 * 
 * @author Adam
 */
public class TransactionInfo {

    /**
     * Phases of a transaction.
     */
    public enum Phase {
        PRE_COMMIT,
        POST_COMMIT,
    }
    
    private File repository;
    
    private String author;

    private String transactionId;
    
    private Phase phase;

    /**
     * Creates a {@link TransactionInfo}.
     * 
     * @param repository The path of the SVN repository that the transaction belongs to.
     * @param author The name of the author of the transaction.
     * @param transactionId The ID of the transaction; depends on the {@link Phase}.
     * @param phase The {@link Phase} of this transaction.
     */
    public TransactionInfo(File repository, String author, String transactionId, Phase phase) {
        this.repository = repository;
        this.author = author;
        this.transactionId = transactionId;
        this.phase = phase;
    }

    /**
     * Returns the path of the SVN repository that the transaction belongs to.
     * 
     * @return The path of the SVN repository.
     */
    public File getRepository() {
        return repository;
    }

    
    /**
     * Returns the name of the author of this transaction.
     * 
     * @return The author name.
     */
    public String getAuthor() {
        return author;
    }

    
    /**
     * Returns the ID of the transaction. Content depends on the {@link Phase}:
     * <ul>
     *  <li>for {@link Phase#PRE_COMMIT} this is the SVN transaction ID</li>
     *  <li>for {@link Phase#POST_COMMIT} this is the SVN revision number</li>
     * </ul>
     * 
     * @return The ID of the transaction.
     */
    public String getTransactionId() {
        return transactionId;
    }

    
    /**
     * The phase of this transaction.
     * 
     * @return The phase of this transaction.
     */
    public Phase getPhase() {
        return phase;
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, phase, repository, transactionId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TransactionInfo)) {
            return false;
        }
        TransactionInfo other = (TransactionInfo) obj;
        return Objects.equals(author, other.author) && phase == other.phase
                && Objects.equals(repository, other.repository) && Objects.equals(transactionId, other.transactionId);
    }
    
}
