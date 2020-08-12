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
import java.util.Objects;

/**
 * Represents a submission directory in the directory. This is a directory where a group submits their solution for a
 * given exercise. 
 * 
 * @author Adam
 */
public class Submission {

    private String exercise;
    
    private String group;

    /**
     * Creates a submission.
     * 
     * @param exercise The name of the exercise that this submission is for.
     * @param group The name of the group that this submission is from.
     */
    public Submission(String exercise, String group) {
        this.exercise = exercise;
        this.group = group;
    }

    /**
     * Returns the name of the exercise that this submission is for.
     * 
     * @return The name of the exercise.
     */
    public String getExercise() {
        return exercise;
    }
    
    /**
     * Returns the name of the group that this submission is from.
     * 
     * @return The name of the group.
     */
    public String getGroup() {
        return group;
    }
    
    /**
     * Returns the path inside the repository where this particular submission is stored.
     * 
     * @return The path relative to the repository root.
     */
    public File getPathInRepo() {
        return new File(exercise + '/' + group);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(exercise, group);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Submission)) {
            return false;
        }
        Submission other = (Submission) obj;
        return Objects.equals(exercise, other.exercise) && Objects.equals(group, other.group);
    }

    @Override
    public String toString() {
        return  exercise + '/' + group;
    }
    
}
