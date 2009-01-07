package org.seasar.ymir.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.vili.ArtifactPair;
import org.seasar.ymir.vili.ArtifactType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ArtifactResolver;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.maven.ExtendedArtifact;
import org.seasar.ymir.vili.maven.ExtendedContext;
import org.seasar.ymir.vili.maven.util.ArtifactUtils;
import org.seasar.ymir.vili.model.Fragment;
import org.seasar.ymir.vili.model.Skeleton;
import org.seasar.ymir.vili.util.ViliUtils;

import werkzeugkasten.mvnhack.repository.Artifact;

public class SelectArtifactPage extends WizardPage {
    protected static final long WAIT_RESOLVE_SKELETON_ARTIFACT = 1000L;

    private ClassLoader projectClassLoader;

    private ViliProjectPreferences preferences;

    private ExtendedContext context;

    private boolean showSkeletonTab;

    private boolean initialized;

    private volatile boolean visible;

    private Skeleton[] skeletons;

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

    private volatile ArtifactPair skeletonArtifactPair;

    private SkeletonArtifactResolver skeletonArtifactResolver;

    private Table fragmentTemplateTable;

    private Fragment[] fragments;

    private Text fragmentDescriptionText;

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

    private volatile ArtifactPair[] fragmentTemplateArtifactPairs;

    public SelectArtifactPage(ClassLoader projectClassLoader, ViliProjectPreferences preferences,
            ExtendedContext context, boolean showSkeletonTab) {
        super("SelectArtifactPage"); //$NON-NLS-1$

        this.projectClassLoader = projectClassLoader;
        this.preferences = preferences;
        this.context = context;
        this.showSkeletonTab = showSkeletonTab;
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

        if (showSkeletonTab) {
            CTabItem skeletonTabItem = new CTabItem(tabFolder, SWT.NONE);
            skeletonTabItem.setText(Messages.getString("SelectArtifactPage.11")); //$NON-NLS-1$

            Composite skeletonTabContent = new Composite(tabFolder, SWT.NULL);
            skeletonTabContent.setLayout(new GridLayout());
            skeletonTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
            skeletonTabItem.setControl(skeletonTabContent);
            createSkeletonSelectionControl(skeletonTabContent);
        }

        CTabItem fragmentTabItem = new CTabItem(tabFolder, SWT.NONE);
        fragmentTabItem.setText(Messages.getString("SelectArtifactPage.12")); //$NON-NLS-1$

        Composite fragmentTabContent = new Composite(tabFolder, SWT.NULL);
        fragmentTabContent.setLayout(new GridLayout());
        fragmentTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        fragmentTabItem.setControl(fragmentTabContent);
        createFragmentSelectionControl(fragmentTabContent);

        CTabItem advancedTabItem = new CTabItem(tabFolder, SWT.NONE);
        advancedTabItem.setText(Messages.getString("SelectArtifactPage.24")); //$NON-NLS-1$

        Composite advancedTabContent = new Composite(tabFolder, SWT.NULL);
        advancedTabContent.setLayout(new GridLayout());
        advancedTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        advancedTabItem.setControl(advancedTabContent);
        createAdvancedControl(advancedTabContent);
    }

    void createSkeletonSelectionControl(Composite parent) {
        chooseSkeletonFromTemplatesField = new Button(parent, SWT.CHECK | SWT.LEFT);
        chooseSkeletonFromTemplatesField.setLayoutData(new GridData());
        chooseSkeletonFromTemplatesField.setText(Messages.getString("SelectArtifactPage.6")); //$NON-NLS-1$
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
        templateListLabel.setText(Messages.getString("SelectArtifactPage.5")); //$NON-NLS-1$

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
        skeletons = Activator.getDefault().getTemplate().getAllSkeletons();
        for (Skeleton skeleton : skeletons) {
            skeletonTemplateListField.add(skeleton.getName());
        }
        skeletonTemplateListField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String description;
                int selectionIndex = skeletonTemplateListField.getSelectionIndex();
                if (selectionIndex != -1) {
                    description = skeletons[selectionIndex].getDescription();
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
        customSkeletonGroupIdLabel.setText(Messages.getString("SelectArtifactPage.7")); //$NON-NLS-1$

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
        customSkeletonArtifactIdLabel.setText(Messages.getString("SelectArtifactPage.8")); //$NON-NLS-1$

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
        customSkeletonVersionLabel.setText(Messages.getString("SelectArtifactPage.9")); //$NON-NLS-1$

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
        useLatestSkeletonVersionField.setText(Messages.getString("SelectArtifactPage.10")); //$NON-NLS-1$
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
                                Resolved resolved = resolveFragment(fragments[i]);
                                if (resolved.getPair() == null) {
                                    items[i].setChecked(false);
                                    setErrorMessage(resolved.getErrorMessage());
                                } else {
                                    fragmentTemplateArtifactPairs[i] = resolved.getPair();
                                }
                                fragmentTemplateTable.setSelection(i);
                                updateDescriptionText(i);
                            } else {
                                fragmentTemplateArtifactPairs[i] = null;
                            }
                            update();
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
                    if (fragmentTemplateArtifactPairs[index] == null) {
                        description = fragments[index].getDescription();
                    } else {
                        description = fragmentTemplateArtifactPairs[index].getBehavior().getDescription();
                    }
                } else {
                    description = ""; //$NON-NLS-1$
                }
                fragmentDescriptionText.setText(description);
            }
        });
        new TableColumn(fragmentTemplateTable, SWT.LEFT).setWidth(270);

        fragments = getFragmentTemplates();
        fragmentTemplateArtifactPairs = new ArtifactPair[fragments.length];
        for (Fragment fragment : fragments) {
            new TableItem(fragmentTemplateTable, SWT.NONE).setText(new String[] { fragment.getName() });
        }

        fragmentDescriptionText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
        fragmentDescriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label customFragmentListLabel = new Label(composite, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        customFragmentListLabel.setLayoutData(data);
        customFragmentListLabel.setText(Messages.getString("SelectArtifactPage.14")); //$NON-NLS-1$

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
        fragmentGroupIdLabel.setText(Messages.getString("SelectArtifactPage.7")); //$NON-NLS-1$

        customFragmentGroupIdField = new Text(leftComposite, SWT.BORDER);
        customFragmentGroupIdField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        customFragmentGroupIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setErrorMessage(null);

                addCustomFragmentButton.setEnabled(isCustomFragmentReadyForAdding());
            }
        });

        Label fragmentArtifactIdLabel = new Label(leftComposite, SWT.NONE);
        fragmentArtifactIdLabel.setText(Messages.getString("SelectArtifactPage.8")); //$NON-NLS-1$

        customFragmentArtifactIdField = new Text(leftComposite, SWT.BORDER);
        customFragmentArtifactIdField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        customFragmentArtifactIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setErrorMessage(null);

                addCustomFragmentButton.setEnabled(isCustomFragmentReadyForAdding());
            }
        });

        customFragmentVersionLabel = new Label(leftComposite, SWT.NONE);
        customFragmentVersionLabel.setText(Messages.getString("SelectArtifactPage.9")); //$NON-NLS-1$

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
        useLatestFragmentVersionField.setText(Messages.getString("SelectArtifactPage.10")); //$NON-NLS-1$
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
        addCustomFragmentButton.setText(Messages.getString("SelectArtifactPage.15")); //$NON-NLS-1$
        data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.RIGHT;
        addCustomFragmentButton.setLayoutData(data);
        addCustomFragmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);

                Resolved resolved = resolveFragment(customFragmentGroupIdField.getText(), customFragmentArtifactIdField
                        .getText(), useLatestFragmentVersionField.getSelection() ? null : customFragmentVersionField
                        .getText());
                if (resolved.getPair() != null) {
                    customFragmentListModel.add(resolved.getPair());
                    customFragmentListField.add(resolved.getPair().getBehavior().getLabel());
                    customFragmentDescriptionText.setText(resolved.getPair().getBehavior().getDescription());
                    customFragmentGroupIdField.setText(""); //$NON-NLS-1$
                    customFragmentArtifactIdField.setText(""); //$NON-NLS-1$
                    if (!useLatestFragmentVersionField.getSelection()) {
                        customFragmentVersionField.setText(""); //$NON-NLS-1$
                    }
                    update();
                } else {
                    setErrorMessage(resolved.getErrorMessage());
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
        removeCustomFragmentButton.setText(Messages.getString("SelectArtifactPage.20")); //$NON-NLS-1$
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

    private Fragment[] getFragmentTemplates() {
        java.util.List<Fragment> list = new ArrayList<Fragment>();
        for (Fragment fragment : Activator.getDefault().getTemplate().getAllFragments()) {
            if (!fragment.isAvailableOnlyIfProjectExists()) {
                list.add(fragment);
            }
        }
        return list.toArray(new Fragment[0]);
    }

    void createAdvancedControl(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.getString("SelectArtifactPage.25")); //$NON-NLS-1$

        archiveListTable = new Table(parent, SWT.BORDER);
        archiveListTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        archiveListTable.setHeaderVisible(true);
        archiveListTable.setLinesVisible(true);
        TableColumn column = new TableColumn(archiveListTable, SWT.LEFT);
        column.setText(Messages.getString("SelectArtifactPage.7")); //$NON-NLS-1$
        column.setWidth(200);
        column = new TableColumn(archiveListTable, SWT.LEFT);
        column.setText(Messages.getString("SelectArtifactPage.8")); //$NON-NLS-1$
        column.setWidth(200);
        column = new TableColumn(archiveListTable, SWT.LEFT);
        column.setText(Messages.getString("SelectArtifactPage.9")); //$NON-NLS-1$
        column.setWidth(140);
    }

    private void update() {
        updateArchiveListTable();
        setPageComplete(validatePage());

        ((ISelectArtifactWizard) getWizard()).notifyFragmentsChanged();
    }

    private void updateArchiveListTable() {
        archiveListTable.removeAll();
        ArtifactPair[] pairs;
        ArtifactPair[] fragments = getFragmentTemplateArtifactPairs();
        if (skeletonArtifactPair != null) {
            pairs = new ArtifactPair[1 + fragments.length];
            pairs[0] = skeletonArtifactPair;
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
                    version = version + " (" + Messages.getString("SelectArtifactPage.27") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
        if (showSkeletonTab) {
            if (skeletonArtifactPair == null) {
                return false;
            }
        } else {
            if (getFragmentTemplateArtifactPairs().length == 0) {
                return false;
            }
        }

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

        if (showSkeletonTab) {
            chooseSkeletonFromTemplatesField.setSelection(true);
            skeletonTemplateListField.setSelection(0, 0);
            skeletonTemplateDescriptionText.setText(skeletons[0].getDescription());
            customSkeletonGroupIdLabel.setEnabled(false);
            customSkeletonGroupIdField.setEnabled(false);
            customSkeletonArtifactIdLabel.setEnabled(false);
            customSkeletonArtifactIdField.setEnabled(false);
            customSkeletonVersionLabel.setEnabled(false);
            customSkeletonVersionField.setEnabled(false);
            useLatestSkeletonVersionField.setEnabled(false);
            useLatestSkeletonVersionField.setSelection(true);

            resolveSkeletonArtifact();
        }

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
            setMessage(Messages.getString("SelectArtifactPage.0"), IMessageProvider.INFORMATION); //$NON-NLS-1$
            setErrorMessage(null);
        }

        Skeleton skeleton = getSkeleton();
        if (skeleton != null) {
            skeletonArtifactResolver = new SkeletonArtifactResolver(this, preferences, context, skeleton, wait);
            skeletonArtifactResolver.start();
        }
    }

    private void clearSkeleton() {
        skeletonArtifactPair = null;
        customSkeletonDescriptionText.setText(""); //$NON-NLS-1$

        ((ISelectArtifactWizard) getWizard()).notifySkeletonCleared();
    }

    private void clearFragments() {
        for (int i = 0; i < fragments.length; i++) {
            TableItem item = fragmentTemplateTable.getItem(i);
            item.setChecked(false);
            fragmentTemplateArtifactPairs[i] = null;
        }
        customFragmentListField.removeAll();
        customFragmentDescriptionText.setText(""); //$NON-NLS-1$
        customFragmentListModel.clear();
    }

    private Resolved resolveFragment(Fragment fragment) {
        return resolveFragment(fragment.getGroupId(), fragment.getArtifactId(), fragment.getVersion());
    }

    private Resolved resolveFragment(final String groupId, final String artifactId, final String version) {
        final Resolved[] resolved = new Resolved[1];
        final boolean useFragmentSnapshot = useFragmentSnapshot();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                resolved[0] = doResolveFragment(groupId, artifactId, version, useFragmentSnapshot, monitor);
            }
        };

        try {
            getContainer().run(true, false, op);
            return resolved[0];
        } catch (InvocationTargetException ex) {
            Throwable realException = ex.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException.getMessage()); //$NON-NLS-1$
            return null;
        } catch (InterruptedException ex) {
            return null;
        }
    }

    private Resolved doResolveFragment(String groupId, String artifactId, String version, boolean useFragmentSnapshot,
            IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("SelectArtifactPage.21"), 2); //$NON-NLS-1$
        try {
            ArtifactResolver artifactResolver = Activator.getDefault().getArtifactResolver();
            ArtifactVersion viliVersion = preferences.getViliVersion();
            Resolved resolved = new Resolved();
            resolved.setErrorMessage(Messages.getString("SelectArtifactPage.19")); //$NON-NLS-1$
            if (version != null) {
                // バージョン指定ありの場合。
                ArtifactPair pair = ArtifactPair.newInstance(artifactResolver.resolve(context, groupId, artifactId,
                        version), projectClassLoader);
                if (pair != null) {
                    if (!ViliUtils.isCompatible(viliVersion, pair.getBehavior().getViliVersion())) {
                        resolved.setErrorMessage(MessageFormat.format(Messages.getString("SelectArtifactPage.28"), //$NON-NLS-1$
                                pair.getBehavior().getViliVersion().getWithoutQualifier(), viliVersion
                                        .getWithoutQualifier()));
                        pair = null;
                    } else if (pair.getBehavior().getArtifactType() != ArtifactType.FRAGMENT) {
                        // TODO isAvailableOnlyIfProjectExistsの判定を追加する。
                        resolved.setErrorMessage(Messages.getString("SelectArtifactPage.29")); //$NON-NLS-1$
                        pair = null;
                    }
                }
                resolved.setPair(pair);
                return resolved;
            } else {
                // バージョン指定なしの場合。
                version = artifactResolver.getLatestVersion(context, groupId, artifactId, useFragmentSnapshot);
                if (version == null) {
                    // バージョンが見つからなかったので終了。
                    return resolved;
                }
                ArtifactPair pair = ArtifactPair.newInstance(artifactResolver.resolve(context, groupId, artifactId,
                        version), projectClassLoader);
                if (pair == null) {
                    // アーティファクト自体見つからなかったら終了。
                    return resolved;
                } else if (ViliUtils.isCompatible(viliVersion, pair.getBehavior().getViliVersion())) {
                    // 見つかったもののViliバージョンが適合するなら終了。ただしタイプが違った場合は見つからなかったことにする。
                    if (pair.getBehavior().getArtifactType() != ArtifactType.FRAGMENT) {
                        // TODO isAvailableOnlyIfProjectExistsの判定を追加する。
                        resolved.setErrorMessage(Messages.getString("SelectArtifactPage.29")); //$NON-NLS-1$
                        pair = null;
                    }
                    resolved.setPair(pair);
                    return resolved;
                }

                // Viliバージョンが適合しなかった場合は全てのバージョンから検索する。
                String[] versions = artifactResolver.getVersions(context, groupId, artifactId, useFragmentSnapshot);
                SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
                subMonitor.beginTask(Messages.getString("SelectArtifactPage.31"), versions.length - 1); //$NON-NLS-1$
                try {
                    // 0番目はチェック済みなのでスキップする。
                    for (int i = 1; i < versions.length; i++, subMonitor.worked(1)) {
                        pair = ArtifactPair.newInstance(artifactResolver.resolve(context, groupId, artifactId,
                                versions[i]), projectClassLoader);
                        if (pair != null && ViliUtils.isCompatible(viliVersion, pair.getBehavior().getViliVersion())) {
                            // 見つかったもののViliバージョンが適合するなら終了。ただしタイプが違った場合は見つからなかったことにする。
                            if (pair.getBehavior().getArtifactType() != ArtifactType.FRAGMENT) {
                                resolved.setErrorMessage(Messages.getString("SelectArtifactPage.29")); //$NON-NLS-1$
                                pair = null;
                            }
                            resolved.setPair(pair);
                            return resolved;
                        }
                    }
                    return resolved;
                } finally {
                    subMonitor.done();
                }
            }
        } finally {
            monitor.done();
        }
    }

    private Skeleton getSkeleton() {
        if (isChosenSkeletonFromTemplate()) {
            int index = skeletonTemplateListField.getSelectionIndex();
            if (index == -1) {
                return null;
            } else {
                return skeletons[index];
            }
        } else {
            String version = getCustomSkeletonVersion();
            if (version.length() == 0) {
                version = null;
            }
            return new Skeleton(getCustomSkeletonGroupId(), getCustomSkeletonArtifactId(), version, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
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
                return skeletons[index].getGroupId();
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
                return skeletons[index].getArtifactId();
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

    public ArtifactPair[] getFragmentTemplateArtifactPairs() {
        Map<String, ArtifactPair> map = new LinkedHashMap<String, ArtifactPair>();

        for (ArtifactPair fragment : fragmentTemplateArtifactPairs) {
            if (fragment != null) {
                map.put(ArtifactUtils.getUniqueId(fragment.getArtifact()), fragment);
            }
        }
        for (ArtifactPair fragment : customFragmentListModel) {
            map.put(ArtifactUtils.getUniqueId(fragment.getArtifact()), fragment);
        }
        return map.values().toArray(new ArtifactPair[0]);
    }

    public ArtifactPair getSkeletonArtifactPair() {
        return skeletonArtifactPair;
    }

    void setSkeletonAndFragments(ArtifactPair skeletonArtifactPair, ArtifactPair[] fragmentArtifactPairs) {
        this.skeletonArtifactPair = skeletonArtifactPair;
        if (!isChosenSkeletonFromTemplate()) {
            customSkeletonDescriptionText.setText(skeletonArtifactPair.getBehavior().getDescription());
        }

        // クリアしているのは、フラグメントつきスケルトンテンプレートを選択されている状態から別のスケルトンに変更された場合に
        // 前のスケルトンに付属しているフラグメントが残るのを避けるため。
        clearFragments();

        for (ArtifactPair fragmentArtifactPair : fragmentArtifactPairs) {
            Artifact artifact = fragmentArtifactPair.getArtifact();
            ViliBehavior behavior = fragmentArtifactPair.getBehavior();

            boolean matched = false;
            for (int i = 0; i < fragments.length; i++) {
                if (artifact.getGroupId().equals(fragments[i].getGroupId())
                        && artifact.getArtifactId().equals(fragments[i].getArtifactId())) {
                    fragmentTemplateTable.getItem(i).setChecked(true);
                    fragmentTemplateArtifactPairs[i] = fragmentArtifactPair;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                customFragmentListField.add(behavior.getLabel());
                customFragmentListModel.add(fragmentArtifactPair);
            }
        }

        update();
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

    public ClassLoader getProjectClassLoader() {
        return projectClassLoader;
    }

    static class Resolved {
        private ArtifactPair pair;

        private String errorMessage;

        public ArtifactPair getPair() {
            return pair;
        }

        public void setPair(ArtifactPair pair) {
            this.pair = pair;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}