/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.embedder;

/**
 * AbstractMavenEmbedderListener
 *
 * @author Eugene Kuleshov
 */
public abstract class AbstractMavenEmbedderListener {

  public void workspaceEmbedderCreated() {
  }

  public void workspaceEmbedderDestroyed() {
  }

  public void workspaceEmbedderInvalidated() {
  }
  
}
