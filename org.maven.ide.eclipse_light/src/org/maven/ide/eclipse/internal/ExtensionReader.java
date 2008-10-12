/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.core.MavenConsole;
import org.maven.ide.eclipse.core.MavenLogger;
import org.maven.ide.eclipse.embedder.MavenRuntimeManager;
import org.maven.ide.eclipse.index.IndexInfo;
import org.maven.ide.eclipse.internal.index.IndexInfoWriter;
import org.maven.ide.eclipse.project.MavenProjectManager;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;


/**
 * Extension reader
 * 
 * @author Eugene Kuleshov
 */
public class ExtensionReader {

  public static final String EXTENSION_PROJECT_CONFIGURATORS = IMavenConstants.PLUGIN_ID + ".projectConfigurators";

  private static final String ELEMENT_CONFIGURATOR = "configurator";

  /**
   * @param configFile previously saved indexes configuration
   * @return collection of {@link IndexInfo} loaded from given config
   */
  public static Collection<IndexInfo> readIndexInfoConfig(File configFile) {
    if(configFile != null && configFile.exists()) {
      FileInputStream is = null;
      try {
        is = new FileInputStream(configFile);
        IndexInfoWriter writer = new IndexInfoWriter();
        return writer.readIndexInfo(is);
      } catch(IOException ex) {
        MavenLogger.log("Unable to read index configuration", ex);
      } finally {
        if(is != null) {
          try {
            is.close();
          } catch(IOException ex) {
            MavenLogger.log("Unable to close index config stream", ex);
          }
        }
      }
    }

    return Collections.emptyList();
  }
  public static List<AbstractProjectConfigurator> readProjectConfiguratorExtensions(MavenProjectManager projectManager,
      MavenRuntimeManager runtimeManager, MavenConsole console) {
    ArrayList<AbstractProjectConfigurator> projectConfigurators = new ArrayList<AbstractProjectConfigurator>();
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint configuratorsExtensionPoint = registry.getExtensionPoint(EXTENSION_PROJECT_CONFIGURATORS);
    if(configuratorsExtensionPoint != null) {
      IExtension[] configuratorExtensions = configuratorsExtensionPoint.getExtensions();
      for(IExtension extension : configuratorExtensions) {
        IConfigurationElement[] elements = extension.getConfigurationElements();
        for(IConfigurationElement element : elements) {
          if(element.getName().equals(ELEMENT_CONFIGURATOR)) {
            try {
              Object o = element.createExecutableExtension(AbstractProjectConfigurator.ATTR_CLASS);

              AbstractProjectConfigurator projectConfigurator = (AbstractProjectConfigurator) o;
              projectConfigurator.setProjectManager(projectManager);
              projectConfigurator.setRuntimeManager(runtimeManager);
              projectConfigurator.setConsole(console);
              
              projectConfigurators.add(projectConfigurator);
            } catch(CoreException ex) {
              MavenLogger.log(ex);
            }
          }
        }
      }
    }
    
    return projectConfigurators;
  }
}
