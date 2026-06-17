package org.wildfly.maven.plugins.licenses;

import com.google.common.base.Strings;
import org.apache.maven.model.License;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wildfly.maven.plugins.licenses.model.ProjectLicenseInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

public class LicensesFileWriter {
  public void writeLicenseSummary(List<ProjectLicenseInfo> dependencies, File outputFile)
          throws ParserConfigurationException, TransformerException {
    DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = fact.newDocumentBuilder();
    Document doc = parser.newDocument();

    Node root = doc.createElement("licenseSummary");
    doc.appendChild(root);
    Node dependenciesNode = doc.createElement("dependencies");
    root.appendChild(dependenciesNode);

    for (ProjectLicenseInfo dep : dependencies) {
      dependenciesNode.appendChild(createDependencyNode(doc, dep));
    }

    // Prepare the output file File
    Result result = new StreamResult(outputFile.toURI().getPath());

    // Write the DOM document to the file
    Transformer xformer = TransformerFactory.newInstance().newTransformer();
    xformer.setOutputProperty(OutputKeys.INDENT, "yes");
    xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    xformer.transform(new DOMSource(doc), result);
  }

  private Node createDependencyNode(Document doc, ProjectLicenseInfo dep) {
    Node depNode = doc.createElement("dependency");
    appendTag(doc, depNode, "groupId", dep.getGroupId());
    appendTag(doc, depNode, "artifactId", dep.getArtifactId());
    appendTag(doc, depNode, "version", dep.getVersion());

    Node licensesNode = doc.createElement("licenses");
    if (dep.getLicenses().isEmpty()) {
      licensesNode.appendChild(doc.createComment("No license information available. "));
    } else {
      for (License lic : dep.getLicenses()) {
        licensesNode.appendChild(createLicenseNode(doc, lic));
      }
    }
    depNode.appendChild(licensesNode);
    return depNode;
  }

  private Node createLicenseNode(Document doc, License lic) {
    Node licenseNode = doc.createElement("license");
    appendTag(doc, licenseNode, "name", lic.getName());
    appendTag(doc, licenseNode, "url", lic.getUrl());
    appendTag(doc, licenseNode, "distribution", lic.getDistribution());
    appendTag(doc, licenseNode, "comments", lic.getComments());

    return licenseNode;
  }

  private void appendTag(Document doc, Node parentNode, String tagName, String content) {
    if (!Strings.isNullOrEmpty(content)) {
      Node element = doc.createElement(tagName);
      element.appendChild(doc.createTextNode(content));
      parentNode.appendChild(element);
    }
  }
}
