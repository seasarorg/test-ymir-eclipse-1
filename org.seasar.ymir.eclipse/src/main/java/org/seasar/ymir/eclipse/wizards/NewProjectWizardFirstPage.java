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
import org.seasar.ymir.eclipse.ArtifactType;
import org.seasar.ymir.eclipse.FragmentEntry;
import org.seasar.ymir.eclipse.SkeletonEntry;
import org.seasar.ymir.eclipse.ViliBehavior;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.maven.ExtendedArtifact;
import org.seasar.ymir.eclipse.maven.ExtendedContext;
import org.seasar.ymir.eclipse.maven.util.ArtifactUtils;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferences;

import werkzeugkasten.mvnhack.repository.Artifact;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewProjectWizardFirstPage extends WizardPage {
    protected static final long WAIT_RESOLVE_SKELETON_ARTIFACT = 1000L;

    private boolean initialized;

    private volatile boolean visible;

    private SkeletonEntry[] entries;

    private CTabFolder tabFolder;

    private Label templateListLabel;

    private List skeletonTemplateListField;

    private Text skeletonTemplateDescriptionText;

    private Button chooseSkeletonFromTemplatesField;

    private Label customSkeletonGroupIdLabel;

    private Text customSkeletonGroupIdField;

    private Label customSkeletonArtifactIdLabel;

    private Text customSkeletonArtifactIdField;

    private Label customSkeletonVersionLabel;

    private Text customSkeletonVersionField;

    private Button useLatestSkeletonVersionField;

    private Text customSkeletonDescriptionText;

    private volatile ArtifactPair skeleton;

    private SkeletonArtifactResolver skeletonArtifactResolver;

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

    private Table archiveListTable;

    private java.util.List<ArtifactPair> customFragmentListModel;

    private volatile ArtifactPair[] fragmentTemplateArtifacts;

    /**
     * Constructor for SampleNewWizardPage.
     * @param preferences 
     * 
     * @param pageName
     */
    public NewProjectWizardFirstPage(ViliProjectPreferences preferences) {
        super("NewProjectWizardFirstPage"); //$NON-NLS-1$

        setTitle(Messages.getString("NewProjectWizardFirstPage.1")); //$NON-NLS-1$
        setDescription(Messages.getString("NewProjectWizardFirstPage.2")); //$NON-NLS-1$
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
        skeletonTabItem.setText(Messages.getString("NewProjectWizardFirstPage.11")); //$NON-NLS-1$

        Composite skeletonTabContent = new Composite(tabFolder, SWT.NULL);
        skeletonTabContent.setLayout(new GridLayout());
        skeletonTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        skeletonTabItem.setControl(skeletonTabContent);
        createSkeletonSelectionControl(skeletonTabContent);

        CTabItem fragmentTabItem = new CTabItem(tabFolder, SWT.NONE);
        fragmentTabItem.setText(Messages.getString("NewProjectWizardFirstPage.12")); //$NON-NLS-1$

        Composite fragmentTabContent = new Composite(tabFolder, SWT.NULL);
        fragmentTabContent.setLayout(new GridLayout());
        fragmentTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        fragmentTabItem.setControl(fragmentTabContent);
        createFragmentSelectionControl(fragmentTabContent);

        CTabItem advancedTabItem = new CTabItem(tabFolder, SWT.NONE);
        advancedTabItem.setText(Messages.getString("NewProjectWizardFirstPage.24")); //$NON-NLS-1$

        Composite advancedTabContent = new Composite(tabFolder, SWT.NULL);
        advancedTabContent.setLayout(new GridLayout());
        advancedTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        advancedTabItem.setControl(advancedTabContent);
        createAdvancedControl(advancedTabContent);
    }

    void createSkeletonSelectionControl(Composite parent) {
        chooseSkeletonFromTemplatesField = new Button(parent, SWT.CHECK | SWT.LEFT);
        chooseSkeletonFromTemplatesField.setLayoutData(new GridData());
        chooseSkeletonFromTemplatesField.setText(Messages.getString("NewProjectWizardFirstPage.6")); //$NON-NLS-1$
        chooseSkeletonFromTemplatesField.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = chooseSkeletonFromTemplatesField.getSelection();
                templateListLabel.setEnabled(enabled);
                skeletonTemplateListField.setEnabled(enabled);
                skeletonTemplateDescriptionText.setEnabled(enabled);
                customSkeletonGroupIdLabel.setEnabled(!enabled);
                customSkeletonGroupIdField.setEnabled(!enabled);
                customSkeletonArtifactIdLabel.setEnabled(!enabled);
                customSkeletonArtifactIdField.setEnabled(!enabled);
                useLatestSkeletonVersionField.setEnabled(!enabled);
                boolean versionEnabled = !enabled && !useLatestSkeletonVersionField.getSelection();
                customSkeletonVersionLabel.setEnabled(versionEnabled);
                customSkeletonVersionField.setEnabled(versionEnabled);

                resolveSkeletonArtifact();
            }
        });

        templateListLabel = new Label(parent, SWT.NONE);
        templateListLabel.setText(Messages.getString("NewProjectWizardFirstPage.5")); //$NON-NLS-1$

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(2, true));
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 250;
        data.heightHint = 150;
        composite.setLayoutData(data);

        skeletonTemplateListField = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        skeletonTemplateListField.setFont(parent.getFont());
        skeletonTemplateListField.setLayoutData(data);
        entries = Activator.getDefault().getTemplateEntry().getAllSkeletons();
        for (SkeletonEntry entry : entries) {
            skeletonTemplateListField.add(entry.getName());
        }
        skeletonTemplateListField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String description;
                int selectionIndex = skeletonTemplateListField.getSelectionIndex();
                if (selectionIndex != -1) {
                    description = entries[selectionIndex].getDescription();
                } else {
                    description = ""; //$NON-NLS-1$
                }
                skeletonTemplateDescriptionText.setText(description);

                resolveSkeletonArtifact();
            }
        });

        skeletonTemplateDescriptionText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
                | SWT.READ_ONLY);
        skeletonTemplateDescriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite skeletonComposite = new Composite(composite, SWT.NULL);
        skeletonComposite.setFont(parent.getFont());
        skeletonComposite.setLayout(new GridLayout(2, false));
        skeletonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        customSkeletonGroupIdLabel = new Label(skeletonComposite, SWT.NONE);
        customSkeletonGroupIdLabel.setText(Messages.getString("NewProjectWizardFirstPage.7")); //$NON-NLS-1$

        customSkeletonGroupIdField = new Text(skeletonComposite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        customSkeletonGroupIdField.setLayoutData(data);
        customSkeletonGroupIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact(WAIT_RESOLVE_SKELETON_ARTIFACT);
            }
        });

        customSkeletonArtifactIdLabel = new Label(skeletonComposite, SWT.NONE);
        customSkeletonArtifactIdLabel.setText(Messages.getString("NewProjectWizardFirstPage.8")); //$NON-NLS-1$

        customSkeletonArtifactIdField = new Text(skeletonComposite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        customSkeletonArtifactIdField.setLayoutData(data);
        customSkeletonArtifactIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact(WAIT_RESOLVE_SKELETON_ARTIFACT);
            }
        });

        customSkeletonVersionLabel = new Label(skeletonComposite, SWT.NONE);
        customSkeletonVersionLabel.setText(Messages.getString("NewProjectWizardFirstPage.9")); //$NON-NLS-1$

        customSkeletonVersionField = new Text(skeletonComposite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        customSkeletonVersionField.setLayoutData(data);
        customSkeletonVersionField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact(WAIT_RESOLVE_SKELETON_ARTIFACT);
            }
        });

        new Label(skeletonComposite, SWT.NONE);

        useLatestSkeletonVersionField = new Button(skeletonComposite, SWT.CHECK | SWT.LEFT);
        useLatestSkeletonVersionField.setText(Messages.getString("NewProjectWizardFirstPage.10")); //$NON-NLS-1$
        useLatestSkeletonVersionField.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                boolean enabled = useLatestSkeletonVersionField.getSelection();
                customSkeletonVersionLabel.setEnabled(!enabled);
                customSkeletonVersionField.setEnabled(!enabled);

                resolveSkeletonArtifact();
            }
        });

        customSkeletonDescriptionText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
                | SWT.READ_ONLY);
        customSkeletonDescriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    void createFragmentSelectionControl(Composite parent) {
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
    }

    void createAdvancedControl(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.getString("NewProjectWizardFirstPage.25")); //$NON-NLS-1$

        archiveListTable = new Table(parent, SWT.BORDER);
        archiveListTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        archiveListTable.setHeaderVisible(true);
        archiveListTable.setLinesVisible(true);
        TableColumn column = new TableColumn(archiveListTable, SWT.LEFT);
        column.setText(Messages.getString("NewProjectWizardFirstPage.7")); //$NON-NLS-1$
        column.setWidth(200);
        column = new TableColumn(archiveListTable, SWT.LEFT);
        column.setText(Messages.getString("NewProjectWizardFirstPage.8")); //$NON-NLS-1$
        column.setWidth(200);
        column = new TableColumn(archiveListTable, SWT.LEFT);
        column.setText(Messages.getString("NewProjectWizardFirstPage.9")); //$NON-NLS-1$
        column.setWidth(140);
    }

    private void updateArchiveListTable() {
        archiveListTable.removeAll();
        ArtifactPair[] pairs;
        ArtifactPair[] fragments = getFragments();
        if (skeleton != null) {
            pairs = new ArtifactPair[1 + fragments.length];
            pairs[0] = skeleton;
            System.arraycopy(fragments, 0, pairs, 1, fragments.length);
        } else {
            pairs = fragments;
        }
        for (ArtifactPair pair : pairs) {
            TableItem item = new TableItem(archiveListTable, SWT.NULL);
            Artifact artifact = pair.getArtifact();
            String version = artifact.getVersion();
            if (ArtifactUtils.isSnapshot(version)) {
                String actualVersion = null;
                if (artifact instanceof ExtendedArtifact) {
                    actualVersion = ((ExtendedArtifact) artifact).getActualVersion();
                }
                if (actualVersion == null) {
                    version = version + " (" + Messages.getString("NewProjectWizardFirstPage.27") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else {
                    version = version + " (" //$NON-NLS-1$
                            + actualVersion.substring(version.length() - ArtifactResolver.SNAPSHOT.length()) + ")"; //$NON-NLS-1$
                }
            }
            item.setText(new String[] { artifact.getGroupId(), artifact.getArtifactId(), version });
        }
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

        // IDialogSettings section = getDialogSettings().getSection(NewProjectWizard.DS_SECTION);

        chooseSkeletonFromTemplatesField.setSelection(true);
        skeletonTemplateListField.setSelection(0, 0);
        skeletonTemplateDescriptionText.setText(entries[0].getDescription());
        customSkeletonGroupIdLabel.setEnabled(false);
        customSkeletonGroupIdField.setEnabled(false);
        customSkeletonArtifactIdLabel.setEnabled(false);
        customSkeletonArtifactIdField.setEnabled(false);
        customSkeletonVersionLabel.setEnabled(false);
        customSkeletonVersionField.setEnabled(false);
        useLatestSkeletonVersionField.setEnabled(false);
        useLatestSkeletonVersionField.setSelection(true);

        resolveSkeletonArtifact();

        customFragmentVersionLabel.setEnabled(false);
        customFragmentVersionField.setEnabled(false);
        useLatestFragmentVersionField.setSelection(true);
        addCustomFragmentButton.setEnabled(false);
        removeCustomFragmentButton.setEnabled(false);
    }

    private boolean validateToResolveSkeletonArtifact() {
        if (getCustomSkeletonGroupId().length() == 0) {
            return false;
        }
        if (getCustomSkeletonArtifactId().length() == 0) {
            return false;
        }
        if (!chooseSkeletonFromTemplatesField.getSelection() && !useLatestSkeletonVersionField.getSelection()
                && customSkeletonVersionField.getText().length() == 0) {
            return false;
        }
        return true;
    }

    private void resolveSkeletonArtifact() {
        resolveSkeletonArtifact(0L);
    }

    private void resolveSkeletonArtifact(long wait) {
        clearSkeleton();
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
            setMessage(Messages.getString("NewProjectWizardFirstPage.0"), IMessageProvider.INFORMATION); //$NON-NLS-1$
            setErrorMessage(null);
        }

        SkeletonEntry entry = getSkeletonEntry();
        if (entry != null) {
            skeletonArtifactResolver = new SkeletonArtifactResolver(this, entry, wait);
            skeletonArtifactResolver.start();
        }
    }

    private void clearSkeleton() {
        skeleton = null;
        customSkeletonDescriptionText.setText(""); //$NON-NLS-1$

        ((NewProjectWizard) getWizard()).notifySkeletonCleared();
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

    private ExtendedContext getNonTransitiveContext() {
        return ((NewProjectWizard) getWizard()).getNonTransitiveContext();
    }

    private SkeletonEntry getSkeletonEntry() {
        if (isChosenSkeletonFromTemplate()) {
            int index = skeletonTemplateListField.getSelectionIndex();
            if (index == -1) {
                return null;
            } else {
                return entries[index];
            }
        } else {
            String version = getCustomSkeletonVersion();
            if (version.length() == 0) {
                version = null;
            }
            return new SkeletonEntry(getCustomSkeletonGroupId(), getCustomSkeletonArtifactId(), version, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    boolean isChosenSkeletonFromTemplate() {
        return chooseSkeletonFromTemplatesField.getSelection();
    }

    private String getCustomSkeletonGroupId() {
        if (isChosenSkeletonFromTemplate()) {
            int index = skeletonTemplateListField.getSelectionIndex();
            if (index == -1) {
                return ""; //$NON-NLS-1$
            } else {
                return entries[index].getGroupId();
            }
        } else {
            return customSkeletonGroupIdField.getText();
        }
    }

    private String getCustomSkeletonArtifactId() {
        if (isChosenSkeletonFromTemplate()) {
            int index = skeletonTemplateListField.getSelectionIndex();
            if (index == -1) {
                return ""; //$NON-NLS-1$
            } else {
                return entries[index].getArtifactId();
            }
        } else {
            return customSkeletonArtifactIdField.getText();
        }
    }

    private String getCustomSkeletonVersion() {
        if (isChosenSkeletonFromTemplate()) {
            return ""; //$NON-NLS-1$
        } else {
            if (useLatestSkeletonVersionField.getSelection()) {
                return ""; //$NON-NLS-1$
            } else {
                return customSkeletonVersionField.getText();
            }
        }
    }

    public ArtifactPair[] getFragments() {
        Map<String, ArtifactPair> map = new LinkedHashMap<String, ArtifactPair>();

        for (ArtifactPair fragment : fragmentTemplateArtifacts) {
            if (fragment != null) {
                map.put(ArtifactUtils.getUniqueId(fragment.getArtifact()), fragment);
            }
        }
        for (ArtifactPair fragment : customFragmentListModel) {
            map.put(ArtifactUtils.getUniqueId(fragment.getArtifact()), fragment);
        }
        return map.values().toArray(new ArtifactPair[0]);
    }

    public ArtifactPair getSkeleton() {
        return skeleton;
    }

    void setSkeletonAndFragments(ArtifactPair skeleton, ArtifactPair[] fragments) {
        this.skeleton = skeleton;
        if (!isChosenSkeletonFromTemplate()) {
            customSkeletonDescriptionText.setText(skeleton.getBehavior().getDescription());
        }

        // クリアしているのは、フラグメントつきスケルトンテンプレートを選択されている状態から別のスケルトンに変更された場合に
        // 前のスケルトンに付属しているフラグメントが残るのを避けるため。
        clearFragments();

        for (ArtifactPair fragment : fragments) {
            Artifact artifact = fragment.getArtifact();
            ViliBehavior behavior = fragment.getBehavior();

            boolean matched = false;
            for (int j = 0; j < fragmentTemplateEntries.length; j++) {
                if (artifact.getGroupId().equals(fragmentTemplateEntries[j].getGroupId())
                        && artifact.getArtifactId().equals(fragmentTemplateEntries[j].getArtifactId())) {
                    fragmentTemplateTable.getItem(j).setChecked(true);
                    fragmentTemplateArtifacts[j] = fragment;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                customFragmentListField.add(behavior.getLabel());
                customFragmentListModel.add(fragment);
            }
        }

        updateArchiveListTable();
        setPageComplete(validatePage());
    }

    boolean useSkeletonSnapshot() {
        return Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_USE_SNAPSHOT_SKELETON);
    }

    boolean useFragmentSnapshot() {
        return Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_USE_SNAPSHOT_FRAGMENT);
    }

    void putDialogSettings() {
        // IDialogSettings section = getDialogSettings().getSection(NewProjectWizard.DS_SECTION);
    }
}