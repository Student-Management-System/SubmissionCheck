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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;

public class SubmissionTest {

    @Test
    public void attributes() {
        Submission s1 = new Submission("Exercise03", "Group05");
        Submission s2 = new Submission("Homework10", "Friends");
        
        assertThat(s1.getGroup(), is("Group05"));
        assertThat(s2.getGroup(), is("Friends"));
        
        assertThat(s1.getExercise(), is("Exercise03"));
        assertThat(s2.getExercise(), is("Homework10"));
    }
    
    @Test
    public void pathInRepo() {
        Submission s1 = new Submission("Exercise03", "Group05");
        Submission s2 = new Submission("Homework10", "Friends");
        
        assertThat(s1.getPathInRepo(), is(new File("Exercise03/Group05")));
        assertThat(s2.getPathInRepo(), is(new File("Homework10/Friends")));
    }
    
    @Test
    public void testToString() {
        Submission s1 = new Submission("Exercise03", "Group05");
        Submission s2 = new Submission("Homework10", "Friends");
        
        assertThat(s1.toString(), is("Exercise03/Group05"));
        assertThat(s2.toString(), is("Homework10/Friends"));
    }
    
    @Test
    public void equalsAndHash() {
        Submission s1 = new Submission("Exercise03", "Group05");
        Submission s2 = new Submission("Exercise03", "Group05");
        Submission s3 = new Submission("Homework10", "Group05");
        Submission s4 = new Submission("Exercise03", "Friends");
        
        assertThat(s1.equals(s1), is(true));
        assertThat(s1.hashCode() == s1.hashCode(), is(true));
        
        assertThat(s1.equals(s2), is(true));
        assertThat(s2.equals(s1), is(true));
        assertThat(s1.hashCode() == s2.hashCode(), is(true));
        
        assertThat(s1.equals(s3), is(false));
        assertThat(s3.equals(s1), is(false));
        assertThat(s1.hashCode() == s3.hashCode(), is(false));
        
        assertThat(s1.equals(s4), is(false));
        assertThat(s4.equals(s1), is(false));
        assertThat(s1.hashCode() == s4.hashCode(), is(false));
        
        assertThat(s1.equals(new Object()), is(false));
    }
    
}
