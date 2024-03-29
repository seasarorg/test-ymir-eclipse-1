/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.embedder;

import java.io.Serializable;

import org.apache.maven.artifact.Artifact;

public class ArtifactKey implements Serializable {
  private static final long serialVersionUID = -8984509272834024387L;
  
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String classifier;

  /**
   * Note that this constructor uses Artifact.getBaseVersion
   */
  public ArtifactKey(Artifact a) {
    this(a.getGroupId(), a.getArtifactId(), a.getBaseVersion(), null);
  }

  public ArtifactKey(String groupId, String artifactId, String version, String classifier) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = classifier;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof ArtifactKey) {
      ArtifactKey other = (ArtifactKey) o;
      return equals(groupId, other.groupId)
          && equals(artifactId, other.artifactId)
          && equals(version, other.version)
          && equals(classifier, other.classifier);
    }
    return false;
  }

  public int hashCode() {
    int hash = 1;
    hash = hash * 31 + (groupId != null? groupId.hashCode(): 0);
    hash = hash * 31 + (artifactId != null? artifactId.hashCode(): 0);
    hash = hash * 31 + (version != null? version.hashCode(): 0);
    hash = hash * 31 + (classifier != null? classifier.hashCode(): 0);
    return hash;
  }

  private static boolean equals(Object o1, Object o2) {
    return o1 == null? o2 == null: o1.equals(o2);
  }

  // XXX this method does not belong here, it compares versions, while ArtifactKey baseVersions
  public static boolean equals(Artifact a1, Artifact a2) {
    if (a1 == null) {
      return a2 == null;
    }
    if (a2 == null) {
      return false;
    }
    return equals(a1.getGroupId(), a2.getGroupId())
        && equals(a1.getArtifactId(), a2.getArtifactId())
        && equals(a1.getVersion(), a2.getVersion())
        && equals(a1.getClassifier(), a2.getClassifier());
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(groupId).append(':').append(artifactId).append(':').append(version);
    return sb.toString();
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
