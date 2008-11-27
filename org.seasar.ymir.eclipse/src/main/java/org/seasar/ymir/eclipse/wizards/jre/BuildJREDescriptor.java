/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.seasar.ymir.eclipse.wizards.jre;

import java.text.MessageFormat;

import org.eclipse.jdt.launching.JavaRuntime;

/**
 * JRE Descriptor used for the JRE container wizard page.
 */
public class BuildJREDescriptor extends JREDescriptor {
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.jres.JREDescriptor#getDescription()
     */
    public String getDescription() {
        return MessageFormat.format(Messages.getString("BuildJREDescriptor.0"), new Object[] { JavaRuntime //$NON-NLS-1$
                .getDefaultVMInstall().getName() });
    }
}
