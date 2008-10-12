/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.index;

import java.util.Date;
import java.util.List;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;

public class IndexedArtifactFile {
  
  public final String repository;

  public final String group;

  public final String artifact;

  public final String fname;

  public final String version;

  private ArtifactVersion artifactVersion;

  public final String type;

  public final String classifier;
  
  public final long size;

  public final Date date;

  public final int sourcesExists;

  public final int javadocExists;

  public final String prefix;

  public final List<String> goals;

  public IndexedArtifactFile(String repository, String group, String artifact, String version, String type,
      String classifier, String fname, long size, Date date, int sourcesExists, int javadocExists, String prefix,
      List<String> goals) {
    this.repository = repository;
    this.group = group;
    this.artifact = artifact;
    this.version = version;
    this.type = type;
    this.classifier = classifier;
    this.fname = fname;
    this.size = size;
    this.date = date == null ? null : new Date(date.getTime());
    this.sourcesExists = sourcesExists;
    this.javadocExists = javadocExists;
    this.prefix = prefix;
    this.goals = goals;
  }

  public Dependency getDependency() {
    Dependency dependency = new Dependency();
    dependency.setArtifactId(artifact);
    dependency.setGroupId(group);
    dependency.setVersion(version);
    dependency.setClassifier(classifier);
    dependency.setType(type); // TODO: investigate difference between packaging and type
    //http://docs.codehaus.org/display/MAVEN/Packaging+vs+Type+-+Derived+and+Attached+Artifacts
    return dependency;
  }

  public ArtifactVersion getArtifactVersion() {
    if (artifactVersion == null) {
      artifactVersion = new DefaultArtifactVersion(version);
    }
    return artifactVersion;
  }
}