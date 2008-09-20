package org.seasar.ymir.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
import org.seasar.ymir.eclipse.FragmentEntry;
import org.seasar.ymir.eclipse.SkeletonEntry;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.util.ArtifactUtils;

import werkzeugkasten.mvnhack.repository.Artifact;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewProjectWizardSecondPage extends WizardPage {
    private boolean initialized;

    private volatile boolean visible;

    private SkeletonEntry[] entries;

    private CTabFolder tabFolder;

    private Label templateListLabel;

    private List templateListField;

    private Text templateDescriptionText;

    private Button chooseFromTemplatesField;

    private Label skeletonGroupIdLabel;

    private Text skeletonGroupIdField;

    private Label skeletonArtifactIdLabel;

    private Text skeletonArtifactIdField;

    private Label skeletonVersionLabel;

    private Text skeletonVersionField;

    private Button useLatestSkeletonVersionField;

    private volatile ArtifactPair skeleton;

    private SkeletonArtifactResolver skeletonArtifactResolver;

    private Table optionTemplateTable;

    private FragmentEntry[] optionTemplateEntries;

    private Text optionTemplateDescriptionText;

    private Button addCustomOptionButton;

    private Button removeCustomOptionButton;

    private Text fragmentGroupIdField;

    private Text fragmentArtifactIdField;

    private Label fragmentVersionLabel;

    private Text fragmentVersionField;

    private Button useLatestFragmentVersionField;

    private List customOptionListField;

    private java.util.List<ArtifactPair> customOptionListModel;

    private volatile ArtifactPair[] optionTemplateArtifacts;

    /**
     * Constructor for SampleNewWizardPage.
     * 
     * @param pageName
     */
    public NewProjectWizardSecondPage() {
        super("NewProjectWizardSecondPage"); //$NON-NLS-1$

        setTitle(Messages.getString("NewProjectWizardSecondPage.1")); //$NON-NLS-1$
        setDescription(Messages.getString("NewProjectWizardSecondPage.2")); //$NON-NLS-1$
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new FillLayout());
        setControl(composite);

        createTabFolder(composite);

        setPageComplete(false);
        setErrorMessage(null);
        setMessage(null);
    }

    void createTabFolder(Composite parent) {
        tabFolder = new CTabFolder(parent, SWT.NULL);
        tabFolder.setSimple(false);
        tabFolder.setTabHeight(tabFolder.getTabHeight() + 2);

        CTabItem skeletonTabItem = new CTabItem(tabFolder, SWT.NONE);
        skeletonTabItem.setText(Messages.getString("NewProjectWizardSecondPage.11")); //$NON-NLS-1$

        Composite skeletonTabContent = new Composite(tabFolder, SWT.NULL);
        skeletonTabContent.setLayout(new GridLayout());
        skeletonTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        skeletonTabItem.setControl(skeletonTabContent);
        createSkeletonSelectionControl(skeletonTabContent);

        CTabItem fragmentTabItem = new CTabItem(tabFolder, SWT.NONE);
        fragmentTabItem.setText(Messages.getString("NewProjectWizardSecondPage.12")); //$NON-NLS-1$

        Composite fragmentTabContent = new Composite(tabFolder, SWT.NULL);
        fragmentTabContent.setLayout(new GridLayout());
        fragmentTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        fragmentTabItem.setControl(fragmentTabContent);
        createFragmentSelectionControl(fragmentTabContent);
    }

    void createSkeletonSelectionControl(Composite parent) {
        chooseFromTemplatesField = new Button(parent, SWT.CHECK | SWT.LEFT);
        chooseFromTemplatesField.setLayoutData(new GridData());
        chooseFromTemplatesField.setText(Messages.getString("NewProjectWizardSecondPage.6")); //$NON-NLS-1$
        chooseFromTemplatesField.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = chooseFromTemplatesField.getSelection();
                templateListLabel.setEnabled(enabled);
                templateListField.setEnabled(enabled);
                templateDescriptionText.setEnabled(enabled);
                skeletonGroupIdLabel.setEnabled(!enabled);
                skeletonGroupIdField.setEnabled(!enabled);
                skeletonArtifactIdLabel.setEnabled(!enabled);
                skeletonArtifactIdField.setEnabled(!enabled);
                useLatestSkeletonVersionField.setEnabled(!enabled);
                boolean versionEnabled = !enabled && !useLatestSkeletonVersionField.getSelection();
                skeletonVersionLabel.setEnabled(versionEnabled);
                skeletonVersionField.setEnabled(versionEnabled);

                resolveSkeletonArtifact();
            }
        });

        templateListLabel = new Label(parent, SWT.NONE);
        templateListLabel.setText(Messages.getString("NewProjectWizardSecondPage.5")); //$NON-NLS-1$

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(2, true));
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 250;
        data.heightHint = 150;
        composite.setLayoutData(data);

        templateListField = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        templateListField.setFont(parent.getFont());
        templateListField.setLayoutData(data);
        entries = Activator.getDefault().getSkeletonEntries();
        for (SkeletonEntry entry : entries) {
            templateListField.add(entry.getName());
        }
        templateListField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String description;
                int selectionIndex = templateListField.getSelectionIndex();
                if (selectionIndex != -1) {
                    description = entries[selectionIndex].getDescription();
                } else {
                    description = ""; //$NON-NLS-1$
                }
                templateDescriptionText.setText(description);

                resolveSkeletonArtifact();
            }
        });

        templateDescriptionText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
        templateDescriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));

        composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        skeletonGroupIdLabel = new Label(composite, SWT.NONE);
        skeletonGroupIdLabel.setText(Messages.getString("NewProjectWizardSecondPage.7")); //$NON-NLS-1$

        skeletonGroupIdField = new Text(composite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        skeletonGroupIdField.setLayoutData(data);
        skeletonGroupIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact();
            }
        });

        skeletonArtifactIdLabel = new Label(composite, SWT.NONE);
        skeletonArtifactIdLabel.setText(Messages.getString("NewProjectWizardSecondPage.8")); //$NON-NLS-1$

        skeletonArtifactIdField = new Text(composite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        skeletonArtifactIdField.setLayoutData(data);
        skeletonArtifactIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact();
            }
        });

        skeletonVersionLabel = new Label(composite, SWT.NONE);
        skeletonVersionLabel.setText(Messages.getString("NewProjectWizardSecondPage.9")); //$NON-NLS-1$

        skeletonVersionField = new Text(composite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        skeletonVersionField.setLayoutData(data);
        skeletonVersionField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact();
            }
        });

        new Label(composite, SWT.NONE);

        useLatestSkeletonVersionField = new Button(composite, SWT.CHECK | SWT.LEFT);
        useLatestSkeletonVersionField.setText(Messages.getString("NewProjectWizardSecondPage.10")); //$NON-NLS-1$
        useLatestSkeletonVersionField.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                boolean enabled = useLatestSkeletonVersionField.getSelection();
                skeletonVersionLabel.setEnabled(!enabled);
                skeletonVersionField.setEnabled(!enabled);

                resolveSkeletonArtifact();
            }
        });
    }

    void createFragmentSelectionControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(2, true));
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 250;
        data.heightHint = 150;
        composite.setLayoutData(data);

        optionTemplateTable = new Table(composite, SWT.CHECK | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        optionTemplateTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        optionTemplateTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                if (e.detail == SWT.CHECK) {
                    TableItem[] items = optionTemplateTable.getItems();
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] == e.item) {
                            if (items[i].getChecked()) {
                                optionTemplateArtifacts[i] = ArtifactPair
                                        .newInstance(resolveFragmentArtifact(optionTemplateEntries[i]));
                                if (optionTemplateArtifacts[i] == null) {
                                    items[i].setChecked(false);
                                    setErrorMessage(Messages.getString("NewProjectWizardSecondPage.13")); //$NON-NLS-1$
                                }
                            } else {
                                optionTemplateArtifacts[i] = null;
                            }
                            break;
                        }
                    }
                } else {
                    String description;
                    int selectionIndex = optionTemplateTable.getSelectionIndex();
                    if (selectionIndex != -1) {
                        description = optionTemplateEntries[selectionIndex].getDescription();
                    } else {
                        description = ""; //$NON-NLS-1$
                    }
                    optionTemplateDescriptionText.setText(description);
                }
            }
        });
        new TableColumn(optionTemplateTable, SWT.LEFT).setWidth(120);

        optionTemplateEntries = Activator.getDefault().getFragmentEntries();
        optionTemplateArtifacts = new ArtifactPair[optionTemplateEntries.length];
        for (FragmentEntry fragment : optionTemplateEntries) {
            new TableItem(optionTemplateTable, SWT.NONE).setText(new String[] { fragment.getName() });
        }

        optionTemplateDescriptionText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
                | SWT.READ_ONLY);
        optionTemplateDescriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label customOptionListLabel = new Label(composite, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        customOptionListLabel.setLayoutData(data);
        customOptionListLabel.setText(Messages.getString("NewProjectWizardSecondPage.14")); //$NON-NLS-1$

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
        fragmentGroupIdLabel.setText(Messages.getString("NewProjectWizardSecondPage.7")); //$NON-NLS-1$

        fragmentGroupIdField = new Text(leftComposite, SWT.BORDER);
        fragmentGroupIdField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fragmentGroupIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setErrorMessage(null);

                addCustomOptionButton.setEnabled(isCustomOptionReadyForAdding());
            }
        });

        Label fragmentArtifactIdLabel = new Label(leftComposite, SWT.NONE);
        fragmentArtifactIdLabel.setText(Messages.getString("NewProjectWizardSecondPage.8")); //$NON-NLS-1$

        fragmentArtifactIdField = new Text(leftComposite, SWT.BORDER);
        fragmentArtifactIdField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fragmentArtifactIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setErrorMessage(null);

                addCustomOptionButton.setEnabled(isCustomOptionReadyForAdding());
            }
        });

        fragmentVersionLabel = new Label(leftComposite, SWT.NONE);
        fragmentVersionLabel.setText(Messages.getString("NewProjectWizardSecondPage.9")); //$NON-NLS-1$

        fragmentVersionField = new Text(leftComposite, SWT.BORDER);
        fragmentVersionField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fragmentVersionField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setErrorMessage(null);

                addCustomOptionButton.setEnabled(isCustomOptionReadyForAdding());
            }
        });

        new Label(leftComposite, SWT.NONE);

        useLatestFragmentVersionField = new Button(leftComposite, SWT.CHECK | SWT.LEFT);
        useLatestFragmentVersionField.setText(Messages.getString("NewProjectWizardSecondPage.10")); //$NON-NLS-1$
        useLatestFragmentVersionField.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                setErrorMessage(null);

                boolean enabled = useLatestFragmentVersionField.getSelection();
                fragmentVersionLabel.setEnabled(!enabled);
                fragmentVersionField.setEnabled(!enabled);

                addCustomOptionButton.setEnabled(isCustomOptionReadyForAdding());
            }
        });

        addCustomOptionButton = new Button(leftComposite, SWT.PUSH);
        addCustomOptionButton.setText(Messages.getString("NewProjectWizardSecondPage.15")); //$NON-NLS-1$
        data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.RIGHT;
        addCustomOptionButton.setLayoutData(data);
        addCustomOptionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);

                Artifact artifact = resolveFragmentArtifact(fragmentGroupIdField.getText(), fragmentArtifactIdField
                        .getText(), useLatestFragmentVersionField.getSelection() ? null : fragmentVersionField
                        .getText());
                if (artifact != null) {
                    ArtifactPair pair = ArtifactPair.newInstance(artifact);
                    customOptionListModel.add(pair);
                    customOptionListField.add(pair.getBehavior().getLabel());
                    fragmentGroupIdField.setText(""); //$NON-NLS-1$
                    fragmentArtifactIdField.setText(""); //$NON-NLS-1$
                    if (!useLatestFragmentVersionField.getSelection()) {
                        fragmentVersionField.setText(""); //$NON-NLS-1$
                    }
                } else {
                    setErrorMessage(Messages.getString("NewProjectWizardSecondPage.19")); //$NON-NLS-1$
                }
                addCustomOptionButton.setEnabled(false);
            }
        });

        customOptionListModel = new ArrayList<ArtifactPair>();
        customOptionListField = new List(rightComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        customOptionListField.setLayoutData(new GridData(GridData.FILL_BOTH));
        customOptionListField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeCustomOptionButton.setEnabled(customOptionListField.getSelectionCount() > 0);
            }
        });

        Composite leftButtonsComposite = new Composite(rightComposite, SWT.NULL);
        leftButtonsComposite.setFont(rightComposite.getFont());
        leftButtonsComposite.setLayout(new FillLayout());

        removeCustomOptionButton = new Button(leftButtonsComposite, SWT.PUSH);
        removeCustomOptionButton.setText(Messages.getString("NewProjectWizardSecondPage.20")); //$NON-NLS-1$
        removeCustomOptionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);

                java.util.List<ArtifactPair> list = new ArrayList<ArtifactPair>();
                int pre = 0;
                int[] indices = customOptionListField.getSelectionIndices();
                for (int idx : indices) {
                    list.addAll(customOptionListModel.subList(pre, idx));
                    pre = idx + 1;
                }
                list.addAll(customOptionListModel.subList(pre, customOptionListModel.size()));
                customOptionListModel = list;
                customOptionListField.remove(indices);

                removeCustomOptionButton.setEnabled(false);
            }
        });

    }

    boolean isCustomOptionReadyForAdding() {
        String groupId = fragmentGroupIdField.getText();
        if (groupId.length() == 0) {
            return false;
        }
        String artifactId = fragmentArtifactIdField.getText();
        if (artifactId.length() == 0) {
            return false;
        }
        String version = useLatestFragmentVersionField.getSelection() ? null : fragmentVersionField.getText();
        if (version != null && version.length() == 0) {
            return false;
        }
        for (ArtifactPair pair : customOptionListModel) {
            Artifact artifact = pair.getArtifact();
            if (artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId)) {
                return false;
            }
        }
        return true;
    }

    boolean validatePage() {
        if (skeleton == null) {
            return false;
        }
        setErrorMessage(null);
        return true;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.visible = visible;
        if (!initialized) {
            setDefaultValues();
            initialized = true;
        }
    }

    boolean isVisible() {
        return visible;
    }

    void setDefaultValues() {
        tabFolder.setSelection(0);

        chooseFromTemplatesField.setSelection(true);
        templateListField.setSelection(0, 0);
        templateDescriptionText.setText(entries[0].getDescription());
        skeletonGroupIdLabel.setEnabled(false);
        skeletonGroupIdField.setEnabled(false);
        skeletonArtifactIdLabel.setEnabled(false);
        skeletonArtifactIdField.setEnabled(false);
        skeletonVersionLabel.setEnabled(false);
        skeletonVersionField.setEnabled(false);
        useLatestSkeletonVersionField.setEnabled(false);
        useLatestSkeletonVersionField.setSelection(true);

        resolveSkeletonArtifact();

        fragmentVersionLabel.setEnabled(false);
        fragmentVersionField.setEnabled(false);
        useLatestFragmentVersionField.setSelection(true);
        addCustomOptionButton.setEnabled(false);
        removeCustomOptionButton.setEnabled(false);
    }

    private boolean validateToResolveSkeletonArtifact() {
        if (getSkeletonGroupId().length() == 0) {
            return false;
        }
        if (getSkeletonArtifactId().length() == 0) {
            return false;
        }
        if (!chooseFromTemplatesField.getSelection() && !useLatestSkeletonVersionField.getSelection()
                && skeletonVersionField.getText().length() == 0) {
            return false;
        }
        return true;
    }

    private void resolveSkeletonArtifact() {
        clearSkeletonAndFragments();
        setPageComplete(false);

        if (skeletonArtifactResolver != null) {
            skeletonArtifactResolver.cancel();
        }

        if (!validateToResolveSkeletonArtifact()) {
            setMessage(null);
            setErrorMessage(null);
            return;
        }

        if (isVisible()) {
            setMessage(Messages.getString("NewProjectWizardSecondPage.0"), IMessageProvider.INFORMATION); //$NON-NLS-1$
            setErrorMessage(null);
        }

        SkeletonEntry entry = getSkeletonEntry();
        if (entry != null) {
            skeletonArtifactResolver = new SkeletonArtifactResolver(this, entry);
            skeletonArtifactResolver.start();
        }
    }

    private void clearSkeletonAndFragments() {
        skeleton = null;
        for (int i = 0; i < optionTemplateEntries.length; i++) {
            TableItem item = optionTemplateTable.getItem(i);
            item.setChecked(false);
            optionTemplateArtifacts[i] = null;
        }
        customOptionListField.removeAll();
        customOptionListModel.clear();

        ((NewProjectWizard) getWizard()).notifySkeletonAndFragmentsCleared();
    }

    private Artifact resolveFragmentArtifact(FragmentEntry fragment) {
        return resolveFragmentArtifact(fragment.getGroupId(), fragment.getArtifactId(), fragment.getVersion());
    }

    private Artifact resolveFragmentArtifact(final String groupId, final String artifactId, final String version) {
        final Artifact[] fragmentArtifact = new Artifact[1];
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                fragmentArtifact[0] = doResolveFragmentArtifact(groupId, artifactId, version, monitor);
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
            IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("NewProjectWizardSecondPage.21"), 2); //$NON-NLS-1$
        try {
            ArtifactResolver artifactResolver = Activator.getDefault().getArtifactResolver();
            if (version == null) {
                version = artifactResolver.getLatestVersion(groupId, artifactId);
                if (version == null) {
                    return null;
                }
            }
            monitor.worked(1);

            return artifactResolver.resolve(groupId, artifactId, version, false);
        } finally {
            monitor.done();
        }
    }

    private SkeletonEntry getSkeletonEntry() {
        if (!chooseFromTemplatesField.getSelection()) {
            String version = getSkeletonVersion();
            if (version.length() == 0) {
                version = null;
            }
            return new SkeletonEntry(getSkeletonGroupId(), getSkeletonArtifactId(), version);
        } else {
            int index = templateListField.getSelectionIndex();
            if (index == -1) {
                return null;
            } else {
                return entries[index];
            }
        }
    }

    private String getSkeletonGroupId() {
        if (!chooseFromTemplatesField.getSelection()) {
            return skeletonGroupIdField.getText();
        } else {
            int index = templateListField.getSelectionIndex();
            if (index == -1) {
                return ""; //$NON-NLS-1$
            } else {
                return entries[index].getGroupId();
            }
        }
    }

    private String getSkeletonArtifactId() {
        if (!chooseFromTemplatesField.getSelection()) {
            return skeletonArtifactIdField.getText();
        } else {
            int index = templateListField.getSelectionIndex();
            if (index == -1) {
                return ""; //$NON-NLS-1$
            } else {
                return entries[index].getArtifactId();
            }
        }
    }

    private String getSkeletonVersion() {
        if (!chooseFromTemplatesField.getSelection()) {
            if (useLatestSkeletonVersionField.getSelection()) {
                return ""; //$NON-NLS-1$
            } else {
                return skeletonVersionField.getText();
            }
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    public ArtifactPair[] getSkeletonAndFragments() {
        Map<String, ArtifactPair> map = new LinkedHashMap<String, ArtifactPair>();

        map.put(ArtifactUtils.getUniqueId(skeleton.getArtifact()), skeleton);

        for (ArtifactPair fragment : optionTemplateArtifacts) {
            if (fragment != null) {
                map.put(ArtifactUtils.getUniqueId(fragment.getArtifact()), fragment);
            }
        }
        for (ArtifactPair fragment : customOptionListModel) {
            map.put(ArtifactUtils.getUniqueId(fragment.getArtifact()), fragment);
        }
        return map.values().toArray(new ArtifactPair[0]);
    }

    void setSkeleton(Artifact skeletonArtifact) {
        this.skeleton = ArtifactPair.newInstance(skeletonArtifact);
    }

    void setFragments(Artifact[] fragments) {
        for (Artifact fragment : fragments) {
            boolean matched = false;
            for (int j = 0; j < optionTemplateEntries.length; j++) {
                if (fragment.getGroupId().equals(optionTemplateEntries[j].getGroupId())
                        && fragment.getArtifactId().equals(optionTemplateEntries[j].getArtifactId())) {
                    optionTemplateTable.getItem(j).setChecked(true);
                    optionTemplateArtifacts[j] = ArtifactPair.newInstance(fragment);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                customOptionListField.add(ArtifactUtils.getUniqueId(fragment));
                customOptionListModel.add(ArtifactPair.newInstance(fragment));
            }
        }
    }
}