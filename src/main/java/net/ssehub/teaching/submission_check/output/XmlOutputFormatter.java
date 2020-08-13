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

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import net.ssehub.teaching.submission_check.ResultMessage;

/**
 * Provides a method for serializing {@link ResultMessage}s as XML, so that they can be sent to the client in a
 * parser-friendly way. 
 * 
 * @author Adam
 */
public class XmlOutputFormatter {

    private static final Logger LOGGER = Logger.getLogger(XmlOutputFormatter.class.getName());
    
    /**
     * Serializes the given list of {@link ResultMessage}s as an XML string.
     * 
     * @param messages The messages to serialize.
     * 
     * @return The messages serialized as an XML string.
     */
    public String format(List<ResultMessage> messages) {
        String result;
        
        try {
            Transformer xmlPrinter = TransformerFactory.newDefaultInstance().newTransformer();
            xmlPrinter.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            xmlPrinter.setOutputProperty(OutputKeys.INDENT, "yes");
            xmlPrinter.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            
            StringWriter output = new StringWriter();
            xmlPrinter.transform(new DOMSource(createXmlDocumentForMessages(messages)), new StreamResult(output));
            
            result = output.toString();
        } catch (DOMException | TransformerException | ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Failed to create XML for result message", e);
            result = "<submitResults>\n"
                    + "    <message tool=\"hook\" type=\"error\" "
                            + "message=\"Internal error: failed to create XML message\"/>\n"
                    + "</submitResults>\n";
        }
        
        return result;
    }
    
    /**
     * Converts all given {@link ResultMessage}s to an XML representation.
     * 
     * @param messages The list of {@link ResultMessage}s to convert to XML.
     * 
     * @return An XML representation of all messages.
     * 
     * @throws DOMException If creating the XML fails.
     * @throws ParserConfigurationException If creating the {@link Document} fails.
     */
    private Document createXmlDocumentForMessages(List<ResultMessage> messages)
            throws DOMException, ParserConfigurationException {
        Document document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        
        Node rootNode = document.createElement("submitResults");
        document.appendChild(rootNode);
        
        for (ResultMessage message : messages) {
            rootNode.appendChild(createXmlNodeForMessage(document, message));
        }
        
        return document;
    }
    
    /**
     * Converts the given {@link ResultMessage} into a single XML node.
     * 
     * @param xmlDocument The XML document of the created node.
     * @param message The message to convert.
     * 
     * @return An XML representation of the message.
     * 
     * @throws DOMException If creating the XML fails.
     */
    private Node createXmlNodeForMessage(Document xmlDocument, ResultMessage message) throws DOMException {
        Node node = xmlDocument.createElement("message");
        
        Node toolAttr = xmlDocument.createAttribute("tool");
        toolAttr.setTextContent(message.getCheckName());
        node.getAttributes().setNamedItem(toolAttr);
        
        Node typeAttr = xmlDocument.createAttribute("type");
        typeAttr.setTextContent(message.getType().toString());
        node.getAttributes().setNamedItem(typeAttr);
        
        Node messageAttr = xmlDocument.createAttribute("message");
        messageAttr.setTextContent(message.getMessage());
        node.getAttributes().setNamedItem(messageAttr);
        
        if (message.getFile() != null) {
            Node fileAttr = xmlDocument.createAttribute("file");
            fileAttr.setTextContent(message.getFile().getPath().replace(File.separatorChar, '/'));
            node.getAttributes().setNamedItem(fileAttr);
            
            if (message.getLine() != null) {
                Node lineAttr = xmlDocument.createAttribute("line");
                lineAttr.setTextContent(String.valueOf(message.getLine()));
                node.getAttributes().setNamedItem(lineAttr);
                
                if (message.getColumn() != null) {
                    Node exampleNode = xmlDocument.createElement("example");
                    node.appendChild(exampleNode);
                    
                    Node columnAttr = xmlDocument.createAttribute("position");
                    columnAttr.setTextContent(String.valueOf(message.getColumn()));
                    exampleNode.getAttributes().setNamedItem(columnAttr);
                }
            }
        }
        
        return node;
    }
    
}
