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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ssehub.teaching.submission_check.checks.Check;

/**
 * Runs a given list of {@link Check}s and passes their output to a {@link ResultCollector}. Checks are run in sequence,
 * and later checks are only executed if the previous ones succeeded.
 * 
 * @author Adam
 */
public class CheckRunner {
    
    private static final Logger LOGGER = Logger.getLogger(CheckRunner.class.getName());

    private List<Check> checksToRun;
    
    private ResultCollector resultCollector;
    
    /**
     * Creates an empty {@link CheckRunner} with no {@link Check}s (yet).
     * 
     * @param resultCollector The {@link ResultCollector} to notify about all results.
     */
    public CheckRunner(ResultCollector resultCollector) {
        this.checksToRun = new LinkedList<>();
        this.resultCollector = resultCollector;
    }
    
    /**
     * Adds a {@link Check} to run. This check is only executed if all the previously added {@link Check}s succeeded.
     * 
     * @param check The {@link Check} to run.
     */
    public void addCheck(Check check) {
        this.checksToRun.add(check);
    }
    
    /**
     * Removes all {@link Check}s previously added via {@link #addCheck(Check)}.
     */
    public void clearChecks() {
        this.checksToRun.clear();
    }
    
    /**
     * Runs all checks on the given directory containing the submission files.
     * 
     * @param submissionDirectory The directory containing the submission.
     * 
     * @return Whether this run was successful, i.e. all {@link Check}s succeeded.
     */
    public boolean run(File submissionDirectory) {
        boolean success = true;
        for (Check check : this.checksToRun) {
            LOGGER.log(Level.FINE, "Running {0}...", check.getClass().getSimpleName());
            success = check.run(submissionDirectory);
            LOGGER.log(Level.INFO, "{0} {1}", new Object[] {
                    check.getClass().getSimpleName(), success ? "succeeded" : "failed"});
            
            resultCollector.addCheckResult(success);
            for (ResultMessage message : check.getResultMessages()) {
                LOGGER.log(Level.INFO, "{0}", message);
                resultCollector.addMessage(message);
            }
            
            if (!success) {
                break;
            }
        }
        
        return success;
    }
    
}
