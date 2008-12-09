package org.seasar.ymir.eclipse.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ArtifactPair;
import org.seasar.ymir.eclipse.ArtifactType;
import org.seasar.ymir.eclipse.FragmentEntry;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.wizards.Messages;

import werkzeugkasten.mvnhack.repository.Artifact;

public class FragmentSelectionControl {
    private Composite parent;

    private boolean isPageComplete;

    private Table fragmentTemplateTable;

    private FragmentEntry[] fragmentTemplateEntries;

    private Text fragmentTemplateDescriptionText;

    private Button addCustomFragmentButton;

    private Button removeCustomFragmentButton;

    private Text customFragmentGroupIdField;

    private Text customFragmentArtifactIdField;

    private Label customFragmentVersionLabel;

    private Text customFragmentVersionField;

    private Button useLatestFragmentVersionField;

    private List customFragmentListField;

    private Text customFragmentDescriptionText;

    private java.util.List<ArtifactPair> customFragmentListModel;

    private volatile ArtifactPair[] fragmentTemplateArtifacts;

    public FragmentSelectionControl(Composite parent) {
        this.parent = parent;
    }

    public Control createControl() {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(2, true));
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 250;
        data.heightHint = 150;
        composite.setLayoutData(data);

        fragmentTemplateTable = new Table(composite, SWT.CHECK | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        fragmentTemplateTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        fragmentTemplateTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                if (e.detail == SWT.CHECK) {
                    TableItem[] items = fragmentTemplateTable.getItems();
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] == e.item) {
                            if (items[i].getChecked()) {
                                fragmentTemplateArtifacts[i] = ArtifactPair
                                        .newInstance(resolveFragmentArtifact(fragmentTemplateEntries[i]));
                                if (fragmentTemplateArtifacts[i] == null) {
                                    items[i].setChecked(false);
                                    setErrorMessage(Messages.getString("NewProjectWizardFirstPage.13")); //$NON-NLS-1$
                                }
                                fragmentTemplateTable.setSelection(i);
                                updateDescriptionText(i);
                            } else {
                                fragmentTemplateArtifacts[i] = null;
                            }
                            updateArchiveListTable();
                            break;
                        }
                    }
                } else {
                    updateDescriptionText(fragmentTemplateTable.getSelectionIndex());
                }
            }

            private void updateDescriptionText(int index) {
                String description;
                if (index != -1) {
                    if (fragmentTemplateArtifacts[index] == null) {
                        description = fragmentTemplateEntries[index].getDescription();
                    } else {
                        description = fragmentTemplateArtifacts[index].getBehavior().getDescription();
                    }
                } else {
                    description = ""; //$NON-NLS-1$
                }
                fragmentTemplateDescriptionText.setText(description);
            }
        });
        new TableColumn(fragmentTemplateTable, SWT.LEFT).setWidth(120);

        fragmentTemplateEntries = Activator.getDefault().getTemplateEntry().getAllFragments();
        fragmentTemplateArtifacts = new ArtifactPair[fragmentTemplateEntries.length];
        for (FragmentEntry fragment : fragmentTemplateEntries) {
            new TableItem(fragmentTemplateTable, SWT.NONE).setText(new String[] { fragment.getName() });
        }

        fragmentTemplateDescriptionText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
                | SWT.READ_ONLY);
        fragmentTemplateDescriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label customFragmentListLabel = new Label(composite, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        customFragmentListLabel.setLayoutData(data);
        customFragmentListLabel.setText(Messages.getString("NewProjectWizardFirstPage.14")); //$NON-NLS-1$

        Composite leftComposite = new Composite(composite, SWT.NULL);
        leftComposite.setFont(composite.getFont());
        leftComposite.setLayout(new GridLayout(2, false));
        leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightComposite = new Composite(composite, SWT.NULL);
        rightComposite.setFont(composite.getFont());
        rightComposite.setLayout(new GridLayout(2, false));
        data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 800;
        rightComposite.setLayoutData(data);

        Label fragmentGroupIdLabel = new Label(leftComposite, SWT.NONE);
        fragmentGroupIdLabel.setText(Messages.getString("NewProjectWizardFirstPage.7")); //$NON-NLS-1$

        customFragmentGroupIdField = new Text(leftComposite, SWT.BORDER);
        customFragmentGroupIdField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        customFragmentGroupIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setErrorMessage(null);

                addCustomFragmentButton.setEnabled(isCustomFragmentReadyForAdding());
            }
        });

        Label fragmentArtifactIdLabel = new Label(leftComposite, SWT.NONE);
        fragmentArtifactIdLabel.setText(Messages.getString("NewProjectWizardFirstPage.8")); //$NON-NLS-1$

        customFragmentArtifactIdField = new Text(leftComposite, SWT.BORDER);
        customFragmentArtifactIdField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        customFragmentArtifactIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setErrorMessage(null);

                addCustomFragmentButton.setEnabled(isCustomFragmentReadyForAdding());
            }
        });

        customFragmentVersionLabel = new Label(leftComposite, SWT.NONE);
        customFragmentVersionLabel.setText(Messages.getString("NewProjectWizardFirstPage.9")); //$NON-NLS-1$

        customFragmentVersionField = new Text(leftComposite, SWT.BORDER);
        customFragmentVersionField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        customFragmentVersionField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setErrorMessage(null);

                addCustomFragmentButton.setEnabled(isCustomFragmentReadyForAdding());
            }
        });

        new Label(leftComposite, SWT.NONE);

        useLatestFragmentVersionField = new Button(leftComposite, SWT.CHECK | SWT.LEFT);
        useLatestFragmentVersionField.setText(Messages.getString("NewProjectWizardFirstPage.10")); //$NON-NLS-1$
        useLatestFragmentVersionField.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                setErrorMessage(null);

                boolean enabled = useLatestFragmentVersionField.getSelection();
                customFragmentVersionLabel.setEnabled(!enabled);
                customFragmentVersionField.setEnabled(!enabled);

                addCustomFragmentButton.setEnabled(isCustomFragmentReadyForAdding());
            }
        });

        addCustomFragmentButton = new Button(leftComposite, SWT.PUSH);
        addCustomFragmentButton.setText(Messages.getString("NewProjectWizardFirstPage.15")); //$NON-NLS-1$
        data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.RIGHT;
        addCustomFragmentButton.setLayoutData(data);
        addCustomFragmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);

                Artifact artifact = resolveFragmentArtifact(customFragmentGroupIdField.getText(),
                        customFragmentArtifactIdField.getText(), useLatestFragmentVersionField.getSelection() ? null
                                : customFragmentVersionField.getText());
                if (artifact != null) {
                    ArtifactPair pair = ArtifactPair.newInstance(artifact);
                    if (pair.getBehavior().getArtifactType() == ArtifactType.FRAGMENT) {
                        customFragmentListModel.add(pair);
                        customFragmentListField.add(pair.getBehavior().getLabel());
                        customFragmentDescriptionText.setText(pair.getBehavior().getDescription());
                        customFragmentGroupIdField.setText(""); //$NON-NLS-1$
                        customFragmentArtifactIdField.setText(""); //$NON-NLS-1$
                        if (!useLatestFragmentVersionField.getSelection()) {
                            customFragmentVersionField.setText(""); //$NON-NLS-1$
                        }
                        updateArchiveListTable();
                    } else {
                        setErrorMessage(Messages.getString("NewProjectWizardFirstPage.22")); //$NON-NLS-1$
                    }
                } else {
                    setErrorMessage(Messages.getString("NewProjectWizardFirstPage.19")); //$NON-NLS-1$
                }
                addCustomFragmentButton.setEnabled(false);
            }
        });

        customFragmentListModel = new ArrayList<ArtifactPair>();
        customFragmentListField = new List(rightComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 80;
        customFragmentListField.setLayoutData(data);
        customFragmentListField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeCustomFragmentButton.setEnabled(customFragmentListField.getSelectionCount() > 0);

                int idx = customFragmentListField.getSelectionIndex();
                if (idx >= 0) {
                    customFragmentDescriptionText.setText(customFragmentListModel.get(idx).getBehavior()
                            .getDescription());
                }
            }
        });

        Composite rightButtonsComposite = new Composite(rightComposite, SWT.NULL);
        rightButtonsComposite.setFont(rightComposite.getFont());
        data = new GridData();
        data.verticalSpan = 2;
        rightButtonsComposite.setLayoutData(data);
        rightButtonsComposite.setLayout(new FillLayout());

        customFragmentDescriptionText = new Text(rightComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
                | SWT.READ_ONLY);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 80;
        customFragmentDescriptionText.setLayoutData(data);

        removeCustomFragmentButton = new Button(rightButtonsComposite, SWT.PUSH);
        removeCustomFragmentButton.setText(Messages.getString("NewProjectWizardFirstPage.20")); //$NON-NLS-1$
        removeCustomFragmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);

                java.util.List<ArtifactPair> list = new ArrayList<ArtifactPair>();
                int pre = 0;
                int[] indices = customFragmentListField.getSelectionIndices();
                for (int idx : indices) {
                    list.addAll(customFragmentListModel.subList(pre, idx));
                    pre = idx + 1;
                }
                list.addAll(customFragmentListModel.subList(pre, customFragmentListModel.size()));
                customFragmentListModel = list;
                customFragmentListField.remove(indices);

                removeCustomFragmentButton.setEnabled(false);
            }
        });

        return composite;
    }

    boolean isCustomFragmentReadyForAdding() {
        String groupId = customFragmentGroupIdField.getText();
        if (groupId.length() == 0) {
            return false;
        }
        String artifactId = customFragmentArtifactIdField.getText();
        if (artifactId.length() == 0) {
            return false;
        }
        String version = useLatestFragmentVersionField.getSelection() ? null : customFragmentVersionField.getText();
        if (version != null && version.length() == 0) {
            return false;
        }
        for (ArtifactPair pair : customFragmentListModel) {
            Artifact artifact = pair.getArtifact();
            if (artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId)) {
                return false;
            }
        }
        return true;
    }

    private void clearFragments() {
        for (int i = 0; i < fragmentTemplateEntries.length; i++) {
            TableItem item = fragmentTemplateTable.getItem(i);
            item.setChecked(false);
            fragmentTemplateArtifacts[i] = null;
        }
        customFragmentListField.removeAll();
        customFragmentDescriptionText.setText(""); //$NON-NLS-1$
        customFragmentListModel.clear();
    }

    private Artifact resolveFragmentArtifact(FragmentEntry fragment) {
        return resolveFragmentArtifact(fragment.getGroupId(), fragment.getArtifactId(), fragment.getVersion());
    }

    private Artifact resolveFragmentArtifact(final String groupId, final String artifactId, final String version) {
        final Artifact[] fragmentArtifact = new Artifact[1];
        final boolean useFragmentSnapshot = useFragmentSnapshot();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                fragmentArtifact[0] = doResolveFragmentArtifact(groupId, artifactId, version, useFragmentSnapshot,
                        monitor);
            }
        };

        try {
            getContainer().run(true, false, op);
            return fragmentArtifact[0];
        } catch (InvocationTargetException ex) {
            Throwable realException = ex.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException.getMessage()); //$NON-NLS-1$
            return null;
        } catch (InterruptedException ex) {
            return null;
        }
    }

    private Artifact doResolveFragmentArtifact(String groupId, String artifactId, String version,
            boolean useFragmentSnapshot, IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("NewProjectWizardFirstPage.21"), 2); //$NON-NLS-1$
        try {
            ArtifactResolver artifactResolver = Activator.getDefault().getArtifactResolver();
            if (version == null) {
                version = artifactResolver.getLatestVersion(getNonTransitiveContext(), groupId, artifactId,
                        useFragmentSnapshot);
                if (version == null) {
                    return null;
                }
            }
            monitor.worked(1);

            return artifactResolver.resolve(getNonTransitiveContext(), groupId, artifactId, version);
        } finally {
            monitor.done();
        }
    }

    public void setErrorMessage(String message) {
    }

    public boolean isPageComplete() {
        return isPageComplete;
    }

    public void setPageComplete(boolean isPageComplete) {
        this.isPageComplete = isPageComplete;
    }

    boolean useFragmentSnapshot() {
        return Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_USE_SNAPSHOT_FRAGMENT);
    }
}
