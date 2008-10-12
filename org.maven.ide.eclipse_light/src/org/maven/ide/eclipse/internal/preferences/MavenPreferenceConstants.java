/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.internal.preferences;


/**
 * Maven preferences constants 
 */
public interface MavenPreferenceConstants {
  
  static final String PREFIX = "eclipse.m2.";

  /** String */
  // public static final String P_LOCAL_REPOSITORY_DIR = PREFIX+"localRepositoryDirectory";
  
  /** true or false */
  // public static final String P_CHECK_LATEST_PLUGIN_VERSION = PREFIX+"checkLatestPluginVersion";
  
  /** String ??? */
  // public static final String P_GLOBAL_CHECKSUM_POLICY = PREFIX+"globalChecksumPolicy";

  /** boolean */
  public static final String P_OFFLINE = PREFIX + "offline";

  /** boolean */
  // public static final String P_UPDATE_SNAPSHOTS = PREFIX+"updateSnapshots";
  
  /** boolean */
  public static final String P_DEBUG_OUTPUT = PREFIX + "debugOutput";

  /** boolean */
  public static final String P_DOWNLOAD_SOURCES = PREFIX + "downloadSources";

  /** boolean */
  public static final String P_DOWNLOAD_JAVADOC = PREFIX + "downloadJavadoc";

  /** String */
  public static final String P_GLOBAL_SETTINGS_FILE = PREFIX + "globalSettingsFile";

  /** String */
  public static final String P_USER_SETTINGS_FILE = PREFIX + "userSettingsFile";

  /** String */
  public static final String P_OUTPUT_FOLDER = PREFIX + "outputFolder";

  /** boolean */
  public static final String P_DISABLE_JDK_WARNING = PREFIX + "disableJdkwarning";

  /** String */
  public static final String P_RUNTIMES = PREFIX + "runtimes";

  /** String */
  public static final String P_DEFAULT_RUNTIME = PREFIX + "defaultRuntime";

  /** boolean */
  public static final String P_UPDATE_INDEXES = PREFIX + "updateIndexes";

  /** boolean */
  public static final String P_UPDATE_PROJECTS = PREFIX + "updateProjects";
  
}
