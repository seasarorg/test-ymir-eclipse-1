/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.jdt.internal.ui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import org.maven.ide.eclipse.actions.AbstractMavenMenuCreator;
import org.maven.ide.eclipse.actions.SelectionUtil;
import org.maven.ide.eclipse.jdt.internal.actions.DownloadSourcesAction;


/**
 * Maven menu creator for JDT
 * 
 * @author Eugene Kuleshov
 */
public class MavenJdtMenuCreator extends AbstractMavenMenuCreator {

  private static final String ID_SOURCES = "org.maven.ide.eclipse_light.downloadSourcesAction";

  private static final String ID_JAVADOC = "org.maven.ide.eclipse_light.downloadJavaDocAction";

  /* (non-Javadoc)
   * @see org.maven.ide.eclipse.internal.actions.AbstractMavenMenuCreator#createMenu(org.eclipse.jface.action.MenuManager)
   */
  public void createMenu(IMenuManager mgr) {
    int selectionType = SelectionUtil.getSelectionType(selection);
    if(selectionType == SelectionUtil.UNSUPPORTED) {
      return;
    }

    if(selectionType == SelectionUtil.PROJECT_WITH_NATURE) {
      mgr.appendToGroup(UPDATE, getAction(new DownloadSourcesAction(ID_JAVADOC), //
          DownloadSourcesAction.ID_JAVADOC, "Download JavaDoc"));
      mgr.appendToGroup(UPDATE, getAction(new DownloadSourcesAction(ID_SOURCES), // 
          DownloadSourcesAction.ID_SOURCES, "Download Sources"));
    }

    if(selectionType == SelectionUtil.JAR_FILE) {
      mgr.appendToGroup(UPDATE, getAction(new DownloadSourcesAction(ID_SOURCES), //
          DownloadSourcesAction.ID_SOURCES, "Download Sources"));
      mgr.appendToGroup(UPDATE, getAction(new DownloadSourcesAction(ID_JAVADOC), //
          DownloadSourcesAction.ID_JAVADOC, "Download JavaDoc"));

      mgr.prependToGroup(OPEN, new Separator());
    }

    if(selectionType == SelectionUtil.WORKING_SET) {
      mgr.appendToGroup(UPDATE, getAction(new DownloadSourcesAction(ID_SOURCES), //
          DownloadSourcesAction.ID_SOURCES, "Download Sources"));
      mgr.appendToGroup(UPDATE, getAction(new DownloadSourcesAction(ID_JAVADOC), //
          DownloadSourcesAction.ID_JAVADOC, "Download JavaDoc"));
    }
  }

}
