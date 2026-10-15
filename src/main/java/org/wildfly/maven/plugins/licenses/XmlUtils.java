/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.maven.plugins.licenses;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Shared XML utilities.
 */
final class XmlUtils {

    private XmlUtils() {
    }

    /**
     * Throws a {@link SAXException} if {@code value} is {@code null}, reporting
     * the missing element name and its parent context in the message.
     *
     * @param value          the parsed value to check
     * @param elemName       the XML element name that is missing
     * @param parentElemName the XML element name of the parent
     * @throws SAXException if {@code value} is {@code null}
     */
    static void requireElement(String value, String elemName, String parentElemName) throws SAXException {
        if (value == null) {
            throw new SAXException("Missing <" + elemName + "> in <" + parentElemName + ">");
        }
    }

    /**
     * Creates a {@link DocumentBuilder} with XXE and DTD processing disabled.
     *
     * @return a configured {@link DocumentBuilder}
     * @throws ParserConfigurationException if the parser cannot be configured
     */
    static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }
}
