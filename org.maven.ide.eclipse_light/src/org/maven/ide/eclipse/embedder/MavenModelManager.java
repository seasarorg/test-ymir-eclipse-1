/*******************************************************************************
 * Copyright (c) 2007, 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.embedder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.InputSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.apache.maven.embedder.MavenEmbedder;

import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.core.MavenConsole;


/**
 * Model manager used to read and and modify Maven models
 * 
 * @author Eugene Kuleshov XXX fix circular dependency
 */
public class MavenModelManager {

  private final MavenEmbedderManager embedderManager;

  private final MavenConsole console;

  public MavenModelManager(MavenEmbedderManager embedderManager, MavenConsole console) {
    this.embedderManager = embedderManager;
    this.console = console;
  }

  public org.apache.maven.model.Model readMavenModel(Reader reader) throws CoreException {
    try {
      MavenEmbedder embedder = embedderManager.getWorkspaceEmbedder();
      return embedder.readModel(reader);
    } catch(XmlPullParserException ex) {
      String msg = "Model parsing error; " + ex.toString();
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, ex));
    } catch(IOException ex) {
      String msg = "Can't read model; " + ex.toString();
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, ex));
    }
  }

  public org.apache.maven.model.Model readMavenModel(File pomFile) throws CoreException {
    try {
      MavenEmbedder embedder = embedderManager.getWorkspaceEmbedder();
      return embedder.readModel(pomFile);
    } catch(XmlPullParserException ex) {
      String msg = "Parsing error " + pomFile.getAbsolutePath() + "; " + ex.toString();
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, ex));
    } catch(IOException ex) {
      String msg = "Can't read model " + pomFile.getAbsolutePath() + "; " + ex.toString();
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, ex));
    }
  }

  public org.apache.maven.model.Model readMavenModel(IFile pomFile) throws CoreException {
    String name = pomFile.getProject().getName() + "/" + pomFile.getProjectRelativePath();
    try {
      MavenEmbedder embedder = embedderManager.getWorkspaceEmbedder();
      return embedder.readModel(pomFile.getLocation().toFile());
    } catch(XmlPullParserException ex) {
      String msg = "Parsing error " + name + "; " + ex.getMessage();
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, ex));
    } catch(IOException ex) {
      String msg = "Can't read model " + name + "; " + ex.toString();
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, ex));
    }
  }

  public void createMavenModel(IFile pomFile, org.apache.maven.model.Model model) throws CoreException {
    String pomFileName = pomFile.getLocation().toString();
    if(pomFile.exists()) {
      String msg = "POM " + pomFileName + " already exists";
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, null));
    }

    try {
      StringWriter sw = new StringWriter();

      MavenEmbedder embedder = embedderManager.getWorkspaceEmbedder();
      embedder.writeModel(sw, model, true);

      String pom = sw.toString();

      // XXX MNGECLIPSE-495
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(false);
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

      Document document = documentBuilder.parse(new InputSource(new StringReader(pom)));
      Element documentElement = document.getDocumentElement();

      NamedNodeMap attributes = document.getAttributes();

      if(attributes == null || attributes.getNamedItem("xmlns") == null) {
        Attr attr = document.createAttribute("xmlns");
        attr.setTextContent("http://maven.apache.org/POM/4.0.0");
        documentElement.setAttributeNode(attr);
      }

      if(attributes == null || attributes.getNamedItem("xmlns:xsi") == null) {
        Attr attr = document.createAttribute("xmlns:xsi");
        attr.setTextContent("http://www.w3.org/2001/XMLSchema-instance");
        documentElement.setAttributeNode(attr);
      }

      if(attributes == null || attributes.getNamedItem("xsi:schemaLocation") == null) {
        Attr attr = document.createAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation");
        attr.setTextContent("http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd");
        documentElement.setAttributeNode(attr);
      }

      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

      sw = new StringWriter();
      trans.transform(new DOMSource(document), new StreamResult(sw));
      pom = sw.toString();

      pomFile.create(new ByteArrayInputStream(pom.getBytes("UTF-8")), true, new NullProgressMonitor());

    } catch(RuntimeException ex) {
      String msg = "Can't create model " + pomFileName + "; " + ex.toString();
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, ex));
    } catch(Exception ex) {
      String msg = "Can't create model " + pomFileName + "; " + ex.toString();
      console.logError(msg);
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, ex));
    }
  }
}
