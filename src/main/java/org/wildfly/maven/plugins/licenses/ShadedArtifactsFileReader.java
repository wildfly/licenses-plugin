/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.maven.plugins.licenses;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses the shaded-artifacts XML file.
 */
public class ShadedArtifactsFileReader {

    private static final String ELEM_SHADED_ARTIFACTS = "shaded-artifacts";
    private static final String ELEM_ARTIFACT = "artifact";
    private static final String ELEM_GROUP_ID = "groupId";
    private static final String ELEM_ARTIFACT_ID = "artifactId";
    private static final String ELEM_VERSION = "version";
    private static final String ELEM_CLASSIFIER = "classifier";
    private static final String ELEM_SHADED_DEPS = "shaded-dependencies";
    private static final String ELEM_DEPENDENCY = "dependency";

    private ShadedArtifactsFileReader() {
    }

    /**
     * Parses {@code path} and returns the shaded dependency mapping.
     *
     * @param path path to the {@code shaded-artifacts.xml} file
     * @return a list of fat jars.
     * @throws IOException if the file cannot be read
     * @throws ParserConfigurationException if the XML parser cannot be configured
     * @throws SAXException if the XML is malformed
     */
    public static List<FatJar> read(Path path) throws IOException, ParserConfigurationException, SAXException {
        List<FatJar> result = new ArrayList<>();
        DocumentBuilder builder = XmlUtils.createDocumentBuilder();
        try (InputStream is = Files.newInputStream(path)) {
            Document doc = builder.parse(is);
            Element root = doc.getDocumentElement();
            root.normalize();
            if (!ELEM_SHADED_ARTIFACTS.equals(root.getTagName())) {
                throw new SAXException("Unexpected root element: " + root.getTagName());
            }
            NodeList artifactNodes = root.getElementsByTagName(ELEM_ARTIFACT);
            for (int i = 0; i < artifactNodes.getLength(); i++) {
                Node node = artifactNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    result.add(parseArtifact((Element) node));
                }
            }
        }
        return result;
    }

    private static FatJar parseArtifact(Element artifactElem) throws SAXException {
        String groupId = null;
        String artifactId = null;
        String version = null;
        String classifier = null;
        List<ShadedDependency> deps = new ArrayList<>();
        NodeList children = artifactElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            switch (child.getNodeName()) {
                case ELEM_GROUP_ID:
                    groupId = child.getTextContent();
                    break;
                case ELEM_ARTIFACT_ID:
                    artifactId = child.getTextContent();
                    break;
                case ELEM_VERSION:
                    version = child.getTextContent();
                    break;
                case ELEM_CLASSIFIER:
                    classifier = child.getTextContent();
                    break;
                case ELEM_SHADED_DEPS:
                    parseShadedDependencies((Element) child, deps);
                    break;
                default:
                    break;
            }
        }
        XmlUtils.requireElement(groupId, ELEM_GROUP_ID, ELEM_ARTIFACT);
        XmlUtils.requireElement(artifactId, ELEM_ARTIFACT_ID, ELEM_ARTIFACT);
        XmlUtils.requireElement(version, ELEM_VERSION, ELEM_ARTIFACT);
        return new FatJar(groupId, artifactId, version, classifier, deps);
    }

    private static void parseShadedDependencies(Element shadedDepsElem, List<ShadedDependency> deps) throws SAXException {
        NodeList children = shadedDepsElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && ELEM_DEPENDENCY.equals(child.getNodeName())) {
                deps.add(parseDependency((Element) child));
            }
        }
    }

    private static ShadedDependency parseDependency(Element depElem) throws SAXException {
        String groupId = null;
        String artifactId = null;
        String version = null;
        String classifier = null;
        NodeList children = depElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            switch (child.getNodeName()) {
                case ELEM_GROUP_ID:
                    groupId = child.getTextContent();
                    break;
                case ELEM_ARTIFACT_ID:
                    artifactId = child.getTextContent();
                    break;
                case ELEM_VERSION:
                    version = child.getTextContent();
                    break;
                case ELEM_CLASSIFIER:
                    classifier = child.getTextContent();
                    break;
                default:
                    break;
            }
        }
        XmlUtils.requireElement(groupId, ELEM_GROUP_ID, ELEM_DEPENDENCY);
        XmlUtils.requireElement(artifactId, ELEM_ARTIFACT_ID, ELEM_DEPENDENCY);
        XmlUtils.requireElement(version, ELEM_VERSION, ELEM_DEPENDENCY);
        return new ShadedDependency(groupId, artifactId, version, classifier);
    }

    /**
     * A fat jar (shaded artifact) entry: the artifact coordinates and its list
     * of bundled dependencies as read from the XML.
     */
    public static final class FatJar {

        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String classifier;
        private final List<ShadedDependency> dependencies;

        FatJar(String groupId, String artifactId, String version, String classifier, List<ShadedDependency> dependencies) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.classifier = classifier;
            this.dependencies = dependencies;
        }

        public List<ShadedDependency> getDependencies() {
            return dependencies;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public String getClassifier() {
            return classifier;
        }
    }

    /**
     * A shaded (bundled) dependency entry: GAV coordinates as read from the
     * XML.
     */
    public static final class ShadedDependency {

        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String classifier;

        ShadedDependency(String groupId, String artifactId, String version, String classifier) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.classifier = classifier;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public String getClassifier() {
            return classifier;
        }
    }
}
