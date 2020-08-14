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
package net.ssehub.teaching.submission_check.output;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.ssehub.teaching.submission_check.ResultMessage;
import net.ssehub.teaching.submission_check.ResultMessage.MessageType;
import net.ssehub.teaching.submission_check.utils.LoggingSetup;

public class XmlOutputFormatterTest {
    
    @Test
    public void serializeSingleMessage() {
        List<ResultMessage> messages = new LinkedList<>();
        
        messages.add(new ResultMessage("toolname", MessageType.ERROR, "my message"));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                new XmlOutputFormatter().format(messages), is(
                        "<submitResults>" + linefeed
                        + "    <message message=\"my message\" tool=\"toolname\" type=\"error\"/>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializeSingleBasicMessageWithFile() {
        List<ResultMessage> messages = new LinkedList<>();
        
        messages.add(new ResultMessage("toolname", MessageType.ERROR, "my message").setFile(new File("dir/file.txt")));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                new XmlOutputFormatter().format(messages), is(
                        "<submitResults>" + linefeed
                        + "    <message file=\"dir/file.txt\" message=\"my message\" tool=\"toolname\" type=\"error\"/>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializeSingleBasicMessageWithFileAndLine() {
        List<ResultMessage> messages = new LinkedList<>();
        
        messages.add(new ResultMessage("toolname", MessageType.ERROR, "my message").setFile(new File("dir/file.txt")).setLine(595));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                new XmlOutputFormatter().format(messages), is(
                        "<submitResults>" + linefeed
                        + "    <message file=\"dir/file.txt\" line=\"595\" message=\"my message\" tool=\"toolname\" type=\"error\"/>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializeSingleBasicMessageWithFileAndLineAndColumn() {
        List<ResultMessage> messages = new LinkedList<>();
        
        messages.add(new ResultMessage("toolname", MessageType.ERROR, "my message").setFile(new File("dir/file.txt")).setLine(595).setColumn(67));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                new XmlOutputFormatter().format(messages), is(
                        "<submitResults>" + linefeed
                        + "    <message file=\"dir/file.txt\" line=\"595\" message=\"my message\" tool=\"toolname\" type=\"error\">" + linefeed
                        + "        <example position=\"67\"/>" + linefeed
                        + "    </message>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializeSpecialCharacters() {
        List<ResultMessage> messages = new LinkedList<>();
        
        messages.add(new ResultMessage("too<l>name", MessageType.ERROR, "my \"message\""));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: should have correct format",
                new XmlOutputFormatter().format(messages), is(
                        "<submitResults>" + linefeed
                        + "    <message message=\"my &quot;message&quot;\" tool=\"too&lt;l&gt;name\" type=\"error\"/>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }
    
    @Test
    public void serializedEmpty() {
        List<ResultMessage> messages = new LinkedList<>();
        
        assertThat("Postcondition: serialized messages should be empty for empty collector",
                new XmlOutputFormatter().format(messages), is("<submitResults/>" + System.lineSeparator()));
    }
    
    @Test
    public void serializeMultipleMessages() {
        List<ResultMessage> messages = new LinkedList<>();             
        
        messages.add(new ResultMessage("toolB", MessageType.ERROR, "message number 1"));
        messages.add(new ResultMessage("toolA", MessageType.ERROR, "abc is wrong").setFile(new File("abc.txt")));
        messages.add(new ResultMessage("toolA", MessageType.WARNING, "numbers are wrong too").setFile(new File("dir/numbers.txt")).setLine(5));
        messages.add(new ResultMessage("toolC", MessageType.ERROR, "you got many lines").setFile(new File("huge.csv")).setLine(2132132131).setColumn(10));
        
        String linefeed = System.lineSeparator();
        
        assertThat("Postcondition: format string should contain correct messages",
                new XmlOutputFormatter().format(messages), is(
                        "<submitResults>" + linefeed
                        + "    <message message=\"message number 1\" tool=\"toolB\" type=\"error\"/>" + linefeed
                        + "    <message file=\"abc.txt\" message=\"abc is wrong\" tool=\"toolA\" type=\"error\"/>" + linefeed
                        + "    <message file=\"dir/numbers.txt\" line=\"5\" message=\"numbers are wrong too\" tool=\"toolA\" type=\"warning\"/>" + linefeed
                        + "    <message file=\"huge.csv\" line=\"2132132131\" message=\"you got many lines\" tool=\"toolC\" type=\"error\">" + linefeed
                        + "        <example position=\"10\"/>" + linefeed
                        + "    </message>" + linefeed
                        + "</submitResults>" + linefeed
                ));
    }

    @BeforeAll
    public static void initLogger() {
        LoggingSetup.setupStdoutLogging();
    }
    
}
