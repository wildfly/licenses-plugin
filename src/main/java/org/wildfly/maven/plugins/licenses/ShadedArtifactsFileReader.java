/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.maven.plugins.licenses;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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

    private static XMLInputFactory createFactory() {
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        return xmlFactory;
    }

    /**
     * Parses {@code path} and returns the shaded dependency mapping.
     *
     * @param path path to the {@code shaded-artifacts.xml} file
     * @return a list of fat jars.
     * @throws IOException if the file cannot be read
     * @throws XMLStreamException if the XML is malformed
     */
    public static List<FatJar> read(Path path) throws IOException, XMLStreamException {
        List<FatJar> result = new ArrayList<>();
        XMLInputFactory xmlFactory = createFactory();
        try (java.io.Reader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            XMLStreamReader xml = xmlFactory.createXMLStreamReader(reader);
            try {
                parseDocument(xml, result);
            } finally {
                xml.close();
            }
        }
        return result;
    }

    private static void parseDocument(XMLStreamReader xml, List<FatJar> result)
            throws XMLStreamException {
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT && ELEM_SHADED_ARTIFACTS.equals(xml.getLocalName())) {
                parseRoot(xml, result);
            }
        }
    }

    private static void parseRoot(XMLStreamReader xml, List<FatJar> result)
            throws XMLStreamException {
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT && ELEM_ARTIFACT.equals(xml.getLocalName())) {
                parseArtifact(xml, result);
            } else if (event == XMLStreamConstants.END_ELEMENT && ELEM_SHADED_ARTIFACTS.equals(xml.getLocalName())) {
                break;
            }
        }
    }

    private static void parseArtifact(XMLStreamReader xml, List<FatJar> result)
            throws XMLStreamException {
        String groupId = null;
        String artifactId = null;
        String classifier = null;
        String version = null;
        List<ShadedDependency> deps = new ArrayList<>();
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (xml.getLocalName()) {
                    case ELEM_GROUP_ID:
                        groupId = xml.getElementText();
                        break;
                    case ELEM_ARTIFACT_ID:
                        artifactId = xml.getElementText();
                        break;
                    case ELEM_VERSION:
                        version = xml.getElementText();
                        break;
                    case ELEM_CLASSIFIER:
                        classifier = xml.getElementText();
                        break;
                    case ELEM_SHADED_DEPS:
                        parseShadedDependencies(xml, deps);
                        break;
                    default:
                        break;
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && ELEM_ARTIFACT.equals(xml.getLocalName())) {
                break;
            }
        }
        if (groupId != null && artifactId != null) {
            result.add( new FatJar(groupId, artifactId, version, classifier, deps));
        }
    }

    private static void parseShadedDependencies(XMLStreamReader xml, List<ShadedDependency> deps)
            throws XMLStreamException {
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT && ELEM_DEPENDENCY.equals(xml.getLocalName())) {
                ShadedDependency dep = parseDependency(xml);
                if (dep != null) {
                    deps.add(dep);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && ELEM_SHADED_DEPS.equals(xml.getLocalName())) {
                break;
            }
        }
    }

    private static ShadedDependency parseDependency(XMLStreamReader xml) throws XMLStreamException {
        String groupId = null;
        String artifactId = null;
        String version = null;
        String classifier = null;
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (xml.getLocalName()) {
                    case ELEM_GROUP_ID:
                        groupId = xml.getElementText();
                        break;
                    case ELEM_ARTIFACT_ID:
                        artifactId = xml.getElementText();
                        break;
                    case ELEM_VERSION:
                        version = xml.getElementText();
                        break;
                    case ELEM_CLASSIFIER:
                        classifier = xml.getElementText();
                        break;
                    default:
                        break;
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && ELEM_DEPENDENCY.equals(xml.getLocalName())) {
                break;
            }
        }
        if (groupId == null || artifactId == null) {
            return null;
        }
        return new ShadedDependency(groupId, artifactId, version, classifier);
    }

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
