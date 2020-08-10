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

/**
 * An exception signalling some failure in the interaction with SVN.
 * 
 * @author Adam
 */
public class SvnException extends Exception {

    private static final long serialVersionUID = 5380052371004358481L;
    
    /**
     * Creates an {@link SvnException}.
     * 
     * @param message A message explaining this exception.
     */
    public SvnException(String message) {
        super(message);
    }
    
    /**
     * Creates an {@link SvnException}.
     * 
     * @param message A message explaining this exception.
     * @param cause Another exception that caused this exception.
     */
    public SvnException(String message, Throwable cause) {
        super(message, cause);
    }

}
