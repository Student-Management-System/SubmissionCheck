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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.junit.Test;

import net.ssehub.teaching.submission_check.svn.TransactionInfo.Phase;

public class TransactionInfoTest {

    @Test
    public void testEquals() {
        TransactionInfo t1 = new TransactionInfo(new File("/repo"), "student1", "35", Phase.PRE_COMMIT);
        TransactionInfo t2 = new TransactionInfo(new File("/repo"), "student1", "35", Phase.PRE_COMMIT);
        TransactionInfo t3 = new TransactionInfo(new File("/repo2"), "student1", "35", Phase.PRE_COMMIT);
        TransactionInfo t4 = new TransactionInfo(new File("/repo"), "student2", "35", Phase.PRE_COMMIT);
        TransactionInfo t5 = new TransactionInfo(new File("/repo"), "student1", "42", Phase.PRE_COMMIT);
        TransactionInfo t6 = new TransactionInfo(new File("/repo"), "student1", "35", Phase.POST_COMMIT);
        
        assertThat(t1.equals(t1), is(true));
        assertThat(t1.equals(t2), is(true));
        
        assertThat(t1.equals(t3), is(false));
        assertThat(t1.equals(t4), is(false));
        assertThat(t1.equals(t5), is(false));
        assertThat(t1.equals(t6), is(false));
        
        assertThat(t1.equals(new Object()), is(false));
    }
    
    @Test
    public void testHashCode() {
        TransactionInfo t1 = new TransactionInfo(new File("/repo"), "student1", "35", Phase.PRE_COMMIT);
        TransactionInfo t2 = new TransactionInfo(new File("/repo"), "student1", "35", Phase.PRE_COMMIT);
        TransactionInfo t3 = new TransactionInfo(new File("/repo2"), "student1", "35", Phase.PRE_COMMIT);
        TransactionInfo t4 = new TransactionInfo(new File("/repo"), "student2", "35", Phase.PRE_COMMIT);
        TransactionInfo t5 = new TransactionInfo(new File("/repo"), "student1", "42", Phase.PRE_COMMIT);
        TransactionInfo t6 = new TransactionInfo(new File("/repo"), "student1", "35", Phase.POST_COMMIT);
        
        assertThat(t1.hashCode(), is(t1.hashCode()));
        assertThat(t2.hashCode(), is(t1.hashCode()));
        
        assertThat(t3.hashCode(), is(not(t1.hashCode())));
        assertThat(t4.hashCode(), is(not(t1.hashCode())));
        assertThat(t5.hashCode(), is(not(t1.hashCode())));
        assertThat(t6.hashCode(), is(not(t1.hashCode())));
    }
    
    @Test
    public void attributes() {
        TransactionInfo t1 = new TransactionInfo(new File("/repo"), "student1", "35", Phase.PRE_COMMIT);
        TransactionInfo t2 = new TransactionInfo(new File("/repo2"), "student2", "42", Phase.POST_COMMIT);
        
        assertThat(t1.getRepository(), is(new File("/repo")));
        assertThat(t1.getAuthor(), is("student1"));
        assertThat(t1.getTransactionId(), is("35"));
        assertThat(t1.getPhase(), is(Phase.PRE_COMMIT));
        
        assertThat(t2.getRepository(), is(new File("/repo2")));
        assertThat(t2.getAuthor(), is("student2"));
        assertThat(t2.getTransactionId(), is("42"));
        assertThat(t2.getPhase(), is(Phase.POST_COMMIT));
    }
    
}
