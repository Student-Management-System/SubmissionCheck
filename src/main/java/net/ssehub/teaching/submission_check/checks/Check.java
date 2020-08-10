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
package net.ssehub.teaching.submission_check.checks;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.teaching.submission_check.ResultMessage;

/**
 * A check that runs on a submission.
 * 
 * @author Adam
 */
public abstract class Check {
    
    private List<ResultMessage> messages;
    
    /**
     * Creates a re-usable {@link Check}.
     */
    public Check() {
        this.messages = new LinkedList<>();
    }

    /**
     * Runs this check on the given directory.
     * 
     * @param submissionDirectory The directory to run on, contains the submission to check.
     * 
     * @return Whether this check was successful.
     */
    public abstract boolean run(File submissionDirectory);
    
    /**
     * Adds a {@link ResultMessage} created during a {@link #run(File)} execution.
     * 
     * @param message The message to add.
     */
    protected void addResultMessage(ResultMessage message) {
        this.messages.add(message);
    }
    
    /**
     * Retrieves the result messages from the previous {@link #run(File)}. The result messages are cleared after this
     * method is invoked, i.e. further invocations will return empty sets until {@link #run(File)} is called again.
     * 
     * @return The {@link ResultMessage}s for the previous {@link #run(File)}.
     */
    public List<ResultMessage> getResultMessages() {
        List<ResultMessage> result = this.messages;
        this.messages = new LinkedList<>();
        return result;
    }
    
}
