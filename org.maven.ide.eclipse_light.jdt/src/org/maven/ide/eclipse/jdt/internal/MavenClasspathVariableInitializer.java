/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.jdt.internal;


import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.maven.ide.eclipse.jdt.MavenJdtPlugin;

/**
 * Maven classpath variable initializer is used to handle M2_REPO variable.
 *
 * @author Eugene Kuleshov
 */
public class MavenClasspathVariableInitializer extends ClasspathVariableInitializer {

  public MavenClasspathVariableInitializer() {
  }

  public void initialize(String variable) {
    MavenJdtPlugin.getDefault().getBuildpathManager().setupVariables();
  }

}
