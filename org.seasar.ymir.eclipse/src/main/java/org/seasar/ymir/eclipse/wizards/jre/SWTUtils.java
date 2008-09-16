/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.seasar.ymir.eclipse.wizards.jre;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Factory class to create SWT resources.
 * @since 3.3
 */
public class SWTUtils {
    private SWTUtils() {
    }

    /**
     * Returns a width hint for a button control.
     */
    public static int getButtonWidthHint(Button button) {
        button.setFont(JFaceResources.getDialogFont());
        GC gc = new GC(button);
        gc.setFont(button.getFont());
        FontMetrics fFontMetrics = gc.getFontMetrics();
        gc.dispose();
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fFontMetrics, IDialogConstants.BUTTON_WIDTH);
        return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    }

    /**
     * Sets width and height hint for the button control.
     * <b>Note:</b> This is a NOP if the button's layout data is not
     * an instance of <code>GridData</code>.
     * 
     * @param   the button for which to set the dimension hint
     */
    public static void setButtonDimensionHint(Button button) {
        Object gd = button.getLayoutData();
        if (gd instanceof GridData) {
            ((GridData) gd).widthHint = getButtonWidthHint(button);
            ((GridData) gd).horizontalAlignment = GridData.FILL;
        }
    }

    /**
     * Creates and returns a new radio button with the given
     * label.
     * 
     * @param parent parent control
     * @param label button label or <code>null</code>
     * 
     * @return a new radio button
     */
    public static Button createRadioButton(Composite parent, String label) {
        Button button = new Button(parent, SWT.RADIO);
        button.setFont(parent.getFont());
        if (label != null) {
            button.setText(label);
        }
        GridData gd = new GridData();
        button.setLayoutData(gd);
        SWTUtils.setButtonDimensionHint(button);
        return button;
    }

    /**
     * Creates and returns a new radio button with the given
     * label.
     * 
     * @param parent parent control
     * @param label button label or <code>null</code>
     * @param hspan the number of columns to span in the parent composite
     * 
     * @return a new radio button
     */
    public static Button createRadioButton(Composite parent, String label, int hspan) {
        Button button = new Button(parent, SWT.RADIO);
        button.setFont(parent.getFont());
        if (label != null) {
            button.setText(label);
        }
        GridData gd = new GridData(GridData.BEGINNING);
        gd.horizontalSpan = hspan;
        button.setLayoutData(gd);
        SWTUtils.setButtonDimensionHint(button);
        return button;
    }

    /**
     * Creates a Group widget
     * @param parent the parent composite to add this group to
     * @param text the text for the heading of the group
     * @param columns the number of columns within the group
     * @param hspan the horizontal span the group should take up on the parent
     * @param fill the style for how this composite should fill into its parent
     * Can be one of <code>GridData.FILL_HORIZONAL</code>, <code>GridData.FILL_BOTH</code> or <code>GridData.FILL_VERTICAL</code>
     * @return the new group
     */
    public static Group createGroup(Composite parent, String text, int columns, int hspan, int fill) {
        Group g = new Group(parent, SWT.NONE);
        g.setLayout(new GridLayout(columns, false));
        g.setText(text);
        g.setFont(parent.getFont());
        GridData gd = new GridData(fill);
        gd.horizontalSpan = hspan;
        g.setLayoutData(gd);
        return g;
    }

    /**
     * Creates a Composite widget
     * @param parent the parent composite to add this composite to
     * @param columns the number of columns within the composite
     * @param hspan the horizontal span the composite should take up on the parent
     * @param fill the style for how this composite should fill into its parent
     * Can be one of <code>GridData.FILL_HORIZONAL</code>, <code>GridData.FILL_BOTH</code> or <code>GridData.FILL_VERTICAL</code>
     * @return the new group
     */
    public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill) {
        Composite g = new Composite(parent, SWT.NONE);
        g.setLayout(new GridLayout(columns, false));
        g.setFont(font);
        GridData gd = new GridData(fill);
        gd.horizontalSpan = hspan;
        g.setLayoutData(gd);
        return g;
    }

    /**
     * Creates a Composite widget
     * @param parent the parent composite to add this composite to
     * @param columns the number of columns within the composite
     * @param hspan the horizontal span the composite should take up on the parent
     * @param fill the style for how this composite should fill into its parent
     * Can be one of <code>GridData.FILL_HORIZONAL</code>, <code>GridData.FILL_BOTH</code> or <code>GridData.FILL_VERTICAL</code>
     * @param marginwidth the width of the margin to place around the composite (default is 5, specified by GridLayout)
     * @param marginheight the height of the margin to place around the composite (default is 5, specified by GridLayout)
     * @return the new group
     */
    public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill,
            int marginwidth, int marginheight) {
        Composite g = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(columns, false);
        layout.marginWidth = marginwidth;
        layout.marginHeight = marginheight;
        g.setLayout(layout);
        g.setFont(font);
        GridData gd = new GridData(fill);
        gd.horizontalSpan = hspan;
        g.setLayoutData(gd);
        return g;
    }
}
