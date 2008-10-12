/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.project;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Model;

/**
 * @author Eugene Kuleshov
 */
public class MavenProjectInfo {

  private final String label;

  private File pomFile;

  private Model model;

  private final MavenProjectInfo parent;

  /**
   * Map of MavenProjectInfo
   */
  private final Map<String, MavenProjectInfo> projects = new LinkedHashMap<String, MavenProjectInfo>();

  private final Set<String> profiles = new HashSet<String>();

  private boolean needsRename;


  public MavenProjectInfo(String label, File pomFile, Model model, MavenProjectInfo parent) {
    this.label = label;
    this.pomFile = pomFile;
    this.model = model;
    this.parent = parent;
  }

  public void setPomFile(File pomFile) {
    File oldDir = this.pomFile.getParentFile();
    File newDir = pomFile.getParentFile();
    
    for(MavenProjectInfo projectInfo : projects.values()) {
      File childPom = projectInfo.getPomFile();
      if(isSubDir(oldDir, childPom.getParentFile())) {
        String oldPath = oldDir.getAbsolutePath();
        String path = childPom.getAbsolutePath().substring(oldPath.length());
        projectInfo.setPomFile(new File(newDir, path));
      }
    }
    
    this.pomFile = pomFile;
  }

  public void setNeedsRename(boolean needsRename) {
    this.needsRename = needsRename;
  }
  
  public boolean isNeedsRename() {
    return this.needsRename;
  }
  
  private boolean isSubDir(File parentDir, File subDir) {
    if(parentDir.equals(subDir)) {
      return true;
    }
    
    if(subDir.getParentFile()!=null) {
      return isSubDir(parentDir, subDir.getParentFile());
    }
    
    return false;
  }

  public void add(MavenProjectInfo info) {
    String key = info.getLabel();
    MavenProjectInfo i = projects.get(key);
    if(i==null) {
      projects.put(key, info);
    } else {
      for(Iterator<String> it = info.getProfiles().iterator(); it.hasNext();) {
        i.addProfile(it.next());
      }
    }
  }
  
  public void addProfile(String profileId) {
    if(profileId!=null) {
      this.profiles.add(profileId);
    }
  }
  
  public void addProfiles(Collection<String> profiles) {
    this.profiles.addAll(profiles);
  }
  
  public String getLabel() {
    return this.label;
  }
  
  public File getPomFile() {
    return this.pomFile;
  }
  
  public Model getModel() {
    return this.model;
  }
  
  public void setModel(Model model) {
    this.model = model;
  }
  
  public Collection<MavenProjectInfo> getProjects() {
    return this.projects.values();
  }
 
  public MavenProjectInfo getParent() {
    return this.parent;
  }
  
  public Set<String> getProfiles() {
    return this.profiles;
  }
  
  public boolean equals(Object obj) {
    if(obj instanceof MavenProjectInfo) {
      MavenProjectInfo info = (MavenProjectInfo) obj;
      if(pomFile == null) {
        return info.getPomFile() == null;
      }
      return pomFile.equals(info.getPomFile());
    }
    return false;
  }
  
  public int hashCode() {
    return pomFile==null ? 0 : pomFile.hashCode();
  }

  public String toString() {
    return label + (pomFile == null ? "" : " " + pomFile.getAbsolutePath());
  }
  
}
