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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.seasar.ymir.eclipse.Activator;

/**
 * A composite that displays installed JRE's in a combo box, with a 'manage...'
 * button to modify installed JREs.
 * <p>
 * This block implements ISelectionProvider - it sends selection change events
 * when the checked JRE in the table changes, or when the "use default" button
 * check state changes.
 * </p>
 */
public class JREsComboBlock {
    public static final String PROPERTY_JRE = "PROPERTY_JRE"; //$NON-NLS-1$

    /**
     * This block's control
     */
    private Composite fControl;

    /**
     * VMs being displayed
     */
    private List<IVMInstall> fVMs = new ArrayList<IVMInstall>();

    /**
     * The main control
     */
    private Combo fCombo;

    /**
     * JRE change listeners
     */
    private ListenerList fListeners = new ListenerList();

    /**
     * Default JRE descriptor or <code>null</code> if none.
     */
    private JREDescriptor fDefaultDescriptor = null;

    /**
     * Specific JRE descriptor or <code>null</code> if none.
     */
    private JREDescriptor fSpecificDescriptor = null;

    /**
     * Default JRE radio button or <code>null</code> if none
     */
    private Button fDefaultButton = null;

    /**
     * Selected JRE radio button
     */
    private Button fSpecificButton = null;

    /**
     * The title used for the JRE block
     */
    private String fTitle = null;

    /**
     * Selected JRE profile radio button
     */
    private Button fEnvironmentsButton = null;

    /**
     * Combo box of JRE profiles
     */
    private Combo fEnvironmentsCombo = null;

    // a path to an unavailable JRE
    private IPath fErrorPath;

    /**
     * List of execution environments
     */
    private List<IExecutionEnvironment> fEnvironments = new ArrayList<IExecutionEnvironment>();

    private IStatus fStatus = OK_STATUS;

    private static IStatus OK_STATUS = new Status(IStatus.OK, Activator.getId(), 0, "", null); //$NON-NLS-1$

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        fListeners.add(listener);
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        fListeners.remove(listener);
    }

    private void firePropertyChange() {
        PropertyChangeEvent event = new PropertyChangeEvent(this, PROPERTY_JRE, null, getPath());
        Object[] listeners = fListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
            listener.propertyChange(event);
        }
    }

    /**
     * Creates this block's control in the given control.
     * 
     * @param anscestor containing control
     */
    public void createControl(Composite parent) {
        Font font = parent.getFont();
        fControl = SWTUtils.createComposite(parent, font, 1, 1, GridData.FILL_BOTH);
        if (fTitle == null) {
            fTitle = Messages.getString("JREsComboBlock.3");
        }
        Group group = SWTUtils.createGroup(fControl, fTitle, 1, 1, GridData.FILL_HORIZONTAL);
        Composite comp = SWTUtils.createComposite(group, font, 2, 1, GridData.FILL_BOTH, 0, 0);
        // display a 'use default JRE' check box
        if (fDefaultDescriptor != null) {
            fDefaultButton = SWTUtils.createRadioButton(comp, fDefaultDescriptor.getDescription(), 3);
            fDefaultButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (fDefaultButton.getSelection()) {
                        setUseDefaultJRE();
                        setStatus(OK_STATUS);
                        firePropertyChange();
                    }
                }
            });
        }

        // specific JRE type
        String text = Messages.getString("JREsComboBlock.1");
        if (fSpecificDescriptor != null) {
            text = fSpecificDescriptor.getDescription();
        }
        fSpecificButton = SWTUtils.createRadioButton(comp, text, 1);
        fSpecificButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (fSpecificButton.getSelection()) {
                    fCombo.setEnabled(true);
                    if (fCombo.getText().length() == 0 && !fVMs.isEmpty()) {
                        fCombo.select(0);
                    }
                    if (fVMs.isEmpty()) {
                        setError(Messages.getString("JREsComboBlock.0"));
                    } else {
                        setStatus(OK_STATUS);
                    }
                    fEnvironmentsCombo.setEnabled(false);
                    firePropertyChange();
                }
            }
        });
        fCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
        fCombo.setFont(font);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        fCombo.setLayoutData(data);
        ControlAccessibleListener.addListener(fCombo, fSpecificButton.getText());

        fCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setStatus(OK_STATUS);
                firePropertyChange();
            }
        });

        fillWithWorkspaceJREs();

        // execution environments
        fEnvironmentsButton = SWTUtils.createRadioButton(comp, Messages.getString("JREsComboBlock.4"));
        fEnvironmentsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (fEnvironmentsButton.getSelection()) {
                    fCombo.setEnabled(false);
                    if (fEnvironmentsCombo.getText().length() == 0 && !fEnvironments.isEmpty()) {
                        fEnvironmentsCombo.select(0);
                    }
                    fEnvironmentsCombo.setEnabled(true);
                    if (fEnvironments.isEmpty()) {
                        setError(Messages.getString("JREsComboBlock.5"));
                    } else {
                        setStatus(OK_STATUS);
                    }
                    firePropertyChange();
                }
            }
        });

        fEnvironmentsCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
        fEnvironmentsCombo.setFont(font);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        fEnvironmentsCombo.setLayoutData(data);

        fEnvironmentsCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setPath(JavaRuntime.newJREContainerPath(getEnvironment()));
                firePropertyChange();
            }
        });

        fillWithWorkspaceProfiles();
    }

    /**
     * Returns this block's control
     * 
     * @return control
     */
    public Control getControl() {
        return fControl;
    }

    /**
     * Sets the JREs to be displayed in this block
     * 
     * @param vms JREs to be displayed
     */
    protected void setJREs(List<VMStandin> jres) {
        fVMs.clear();
        fVMs.addAll(jres);
        // sort by name
        Collections.sort(fVMs, new Comparator<IVMInstall>() {
            public int compare(IVMInstall left, IVMInstall right) {
                return left.getName().compareToIgnoreCase(right.getName());
            }

            public boolean equals(Object obj) {
                return obj == this;
            }
        });
        // now make an array of names
        String[] names = new String[fVMs.size()];
        Iterator<IVMInstall> iter = fVMs.iterator();
        int i = 0;
        while (iter.hasNext()) {
            IVMInstall vm = (IVMInstall) iter.next();
            names[i] = vm.getName();
            i++;
        }
        fCombo.setItems(names);
        fCombo.setVisibleItemCount(Math.min(names.length, 20));
    }

    protected Shell getShell() {
        return getControl().getShell();
    }

    /**
     * Selects a specific JRE based on type/name.
     * 
     * @param vm JRE or <code>null</code>
     */
    private void selectJRE(IVMInstall vm) {
        fSpecificButton.setSelection(true);
        fDefaultButton.setSelection(false);
        fEnvironmentsButton.setSelection(false);
        fCombo.setEnabled(true);
        fEnvironmentsCombo.setEnabled(false);
        if (vm != null) {
            int index = fVMs.indexOf(vm);
            if (index >= 0) {
                fCombo.select(index);
            }
        }
        firePropertyChange();
    }

    /**
     * Selects a JRE based environment.
     * 
     * @param env environment or <code>null</code>
     */
    private void selectEnvironment(IExecutionEnvironment env) {
        fSpecificButton.setSelection(false);
        fDefaultButton.setSelection(false);
        fCombo.setEnabled(false);
        fEnvironmentsButton.setSelection(true);
        fEnvironmentsCombo.setEnabled(true);
        if (env != null) {
            int index = fEnvironments.indexOf(env);
            if (index >= 0) {
                fEnvironmentsCombo.select(index);
            }
        }
        firePropertyChange();
    }

    /**
     * Returns the selected JRE or <code>null</code> if none.
     * 
     * @return the selected JRE or <code>null</code> if none
     */
    public IVMInstall getJRE() {
        int index = fCombo.getSelectionIndex();
        if (index >= 0) {
            return fVMs.get(index);
        }
        return null;
    }

    /**
     * Returns the selected Environment or <code>null</code> if none.
     * 
     * @return the selected Environment or <code>null</code> if none
     */
    private IExecutionEnvironment getEnvironment() {
        int index = fEnvironmentsCombo.getSelectionIndex();
        if (index >= 0) {
            return (IExecutionEnvironment) fEnvironments.get(index);
        }
        return null;
    }

    /**
     * Populates the JRE table with existing JREs defined in the workspace.
     */
    protected void fillWithWorkspaceJREs() {
        // fill with JREs
        List<VMStandin> standins = new ArrayList<VMStandin>();
        IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
        for (int i = 0; i < types.length; i++) {
            IVMInstallType type = types[i];
            IVMInstall[] installs = type.getVMInstalls();
            for (int j = 0; j < installs.length; j++) {
                IVMInstall install = installs[j];
                standins.add(new VMStandin(install));
            }
        }
        setJREs(standins);
    }

    /**
     * Populates the JRE profile combo with profiles defined in the workspace.
     */
    protected void fillWithWorkspaceProfiles() {
        fEnvironments.clear();
        IExecutionEnvironment[] environments = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
        for (int i = 0; i < environments.length; i++) {
            fEnvironments.add(environments[i]);
        }
        // sort by name
        Collections.sort(fEnvironments, new Comparator<IExecutionEnvironment>() {
            public int compare(IExecutionEnvironment left, IExecutionEnvironment right) {
                return left.getId().compareToIgnoreCase(right.getId());
            }

            public boolean equals(Object obj) {
                return obj == this;
            }
        });
        // now make an array of names
        String[] names = new String[fEnvironments.size()];
        Iterator<IExecutionEnvironment> iter = fEnvironments.iterator();
        int i = 0;
        while (iter.hasNext()) {
            IExecutionEnvironment env = (IExecutionEnvironment) iter.next();
            IPath path = JavaRuntime.newJREContainerPath(env);
            IVMInstall install = JavaRuntime.getVMInstall(path);
            if (install != null) {
                names[i] = MessageFormat.format(Messages.getString("JREsComboBlock.15"), new Object[] { env.getId(),
                        install.getName() });
            } else {
                names[i] = MessageFormat.format(Messages.getString("JREsComboBlock.16"), new Object[] { env.getId() });
            }
            i++;
        }
        fEnvironmentsCombo.setItems(names);
        fEnvironmentsCombo.setVisibleItemCount(Math.min(names.length, 20));
    }

    /**
     * Sets the Default JRE Descriptor for this block.
     * 
     * @param descriptor default JRE descriptor
     */
    public void setDefaultJREDescriptor(JREDescriptor descriptor) {
        fDefaultDescriptor = descriptor;
        setButtonTextFromDescriptor(fDefaultButton, descriptor);
    }

    private void setButtonTextFromDescriptor(Button button, JREDescriptor descriptor) {
        if (button != null) {
            // update the description & JRE in case it has changed
            String currentText = button.getText();
            String newText = descriptor.getDescription();
            if (!newText.equals(currentText)) {
                button.setText(newText);
                fControl.layout();
            }
        }
    }

    /**
     * Sets the specific JRE Descriptor for this block.
     * 
     * @param descriptor specific JRE descriptor
     */
    public void setSpecificJREDescriptor(JREDescriptor descriptor) {
        fSpecificDescriptor = descriptor;
        setButtonTextFromDescriptor(fSpecificButton, descriptor);
    }

    /**
     * Returns whether the 'use default JRE' button is checked.
     * 
     * @return whether the 'use default JRE' button is checked
     */
    public boolean isDefaultJRE() {
        if (fDefaultButton != null) {
            return fDefaultButton.getSelection();
        }
        return false;
    }

    /**
     * Sets this control to use the 'default' JRE.
     */
    private void setUseDefaultJRE() {
        if (fDefaultDescriptor != null) {
            fDefaultButton.setSelection(true);
            fSpecificButton.setSelection(false);
            fEnvironmentsButton.setSelection(false);
            fCombo.setEnabled(false);
            fEnvironmentsCombo.setEnabled(false);
            firePropertyChange();
        }
    }

    /**
     * Sets the title used for this JRE block
     * 
     * @param title title for this JRE block 
     */
    public void setTitle(String title) {
        fTitle = title;
    }

    /**
     * Refresh the default JRE description.
     */
    public void refresh() {
        setDefaultJREDescriptor(fDefaultDescriptor);
    }

    /**
     * Returns a classpath container path identifying the selected JRE.
     * 
     * @return classpath container path or <code>null</code>
     * @since 3.2
     */
    public IPath getPath() {
        if (!getStatus().isOK() && fErrorPath != null) {
            return fErrorPath;
        }
        if (fEnvironmentsButton.getSelection()) {
            int index = fEnvironmentsCombo.getSelectionIndex();
            if (index >= 0) {
                IExecutionEnvironment env = fEnvironments.get(index);
                return JavaRuntime.newJREContainerPath(env);
            }
            return null;
        }
        if (fSpecificButton.getSelection()) {
            int index = fCombo.getSelectionIndex();
            if (index >= 0) {
                IVMInstall vm = (IVMInstall) fVMs.get(index);
                return JavaRuntime.newJREContainerPath(vm);
            }
            return null;
        }
        return JavaRuntime.newDefaultJREContainerPath();
    }

    /**
     * Sets the selection based on the given container path and returns
     * a status indicating if the selection was successful.
     * 
     * @param containerPath
     * @return status 
     */
    public void setPath(IPath containerPath) {
        fErrorPath = null;
        setStatus(OK_STATUS);
        if (JavaRuntime.newDefaultJREContainerPath().equals(containerPath)) {
            setUseDefaultJRE();
        } else {
            String envId = JavaRuntime.getExecutionEnvironmentId(containerPath);
            if (envId != null) {
                IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
                IExecutionEnvironment environment = manager.getEnvironment(envId);
                if (environment == null) {
                    fErrorPath = containerPath;
                    selectEnvironment(environment);
                    setError(MessageFormat.format(Messages.getString("JREsComboBlock.6"), new Object[] { envId }));
                } else {
                    selectEnvironment(environment);
                    IVMInstall[] installs = environment.getCompatibleVMs();
                    if (installs.length == 0) {
                        setError(MessageFormat.format(Messages.getString("JREsComboBlock.7"),
                                new Object[] { environment.getId() }));
                    }
                }
            } else {
                IVMInstall install = JavaRuntime.getVMInstall(containerPath);
                if (install == null) {
                    selectJRE(install);
                    fErrorPath = containerPath;
                    String installTypeId = JavaRuntime.getVMInstallTypeId(containerPath);
                    if (installTypeId == null) {
                        setError(Messages.getString("JREsComboBlock.8"));
                    } else {
                        IVMInstallType installType = JavaRuntime.getVMInstallType(installTypeId);
                        if (installType == null) {
                            setError(MessageFormat.format(Messages.getString("JREsComboBlock.9"),
                                    new Object[] { installTypeId }));
                        } else {
                            String installName = JavaRuntime.getVMInstallName(containerPath);
                            if (installName == null) {
                                setError(MessageFormat.format(Messages.getString("JREsComboBlock.10"),
                                        new Object[] { installType.getName() }));
                            } else {
                                setError(MessageFormat.format(Messages.getString("JREsComboBlock.11"), new Object[] {
                                        installName, installType.getName() }));
                            }
                        }
                    }
                } else {
                    selectJRE(install);
                    File location = install.getInstallLocation();
                    if (location == null) {
                        setError(Messages.getString("JREsComboBlock.12"));
                    } else if (!location.exists()) {
                        setError(Messages.getString("JREsComboBlock.13"));
                    }
                }
            }
        }
    }

    private void setError(String message) {
        setStatus(new Status(IStatus.ERROR, Activator.getId(), 150, message, null));
    }

    /**
     * Returns the status of the JRE selection.
     * 
     * @return status
     */
    public IStatus getStatus() {
        return fStatus;
    }

    private void setStatus(IStatus status) {
        fStatus = status;
    }

    public void setEnabled(boolean enabled) {
        fDefaultButton.setEnabled(enabled);
        fSpecificButton.setEnabled(enabled);
        fEnvironmentsButton.setEnabled(enabled);
        fCombo.setEnabled(enabled && fSpecificButton.getSelection());
        fEnvironmentsCombo.setEnabled(enabled && fEnvironmentsButton.getSelection());
    }
}
