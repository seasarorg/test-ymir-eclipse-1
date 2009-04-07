package org.seasar.ymir.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.seasar.ymir.eclipse.ui.MoldParametersControl;
import org.seasar.ymir.eclipse.ui.ViliProjectPreferencesControl;
import org.seasar.ymir.eclipse.ui.YmirConfigurationControl;
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;

public class ConfigureParametersPage extends WizardPage {
    private static final int SCROLL_UNIT = 16;

    private IProject project;

    private ViliProjectPreferences preferences;

    private Composite tabFolderParent;

    private CTabFolder tabFolder;

    private Label tabLabel;

    private ViliProjectPreferencesControl preferencesControl;

    private MoldParametersControl moldParametersControl;

    private Composite skeletonTabContent;

    private Composite ymirConfigurationTabContent;

    private boolean tabPrepared;

    private Mold skeleton;

    private ViliBehavior skeletonBehavior;

    private Mold[] fragments;

    private YmirConfigurationControl ymirConfigurationControl;

    public ConfigureParametersPage(IProject project, ViliProjectPreferences preferences) {
        super("ConfigureParametersPage"); //$NON-NLS-1$

        this.project = project;
        this.preferences = preferences;

        setTitle(Messages.getString("ConfigureParametersPage.1")); //$NON-NLS-1$
        setDescription(Messages.getString("ConfigureParametersPage.2")); //$NON-NLS-1$

        // このページが表示されるまでスケルトンパラメータの取得を遅延させているため、
        // このページに必ず遷移するようこうしている。
        setPageComplete(false);
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        tabFolderParent = new Composite(parent, SWT.NULL);
        tabFolderParent.setLayout(new FillLayout());
        setControl(tabFolderParent);

        setErrorMessage(null);
        setMessage(null);
    }

    void createTabFolder() {
        boolean moldParameterExists = skeletonParameterExists();

        if (skeleton != null || moldParameterExists) {
            tabFolder = new CTabFolder(tabFolderParent, SWT.NULL);
            tabFolder.setLayout(new FillLayout());
            tabFolder.setSimple(false);
            tabFolder.setTabHeight(tabFolder.getTabHeight() + 2);
        } else {
            tabLabel = new Label(tabFolderParent, SWT.NULL);
            tabLabel.setText(Messages.getString("ConfigureParametersPage.13")); //$NON-NLS-1$
        }

        if (skeleton != null) {
            CTabItem genericTabItem = new CTabItem(tabFolder, SWT.NONE);
            genericTabItem.setText(Messages.getString("ConfigureParametersPage.11")); //$NON-NLS-1$

            Composite genericTabContent = new Composite(tabFolder, SWT.NULL);
            genericTabContent.setLayout(new GridLayout());
            genericTabItem.setControl(genericTabContent);

            preferencesControl = new ViliProjectPreferencesControl(genericTabContent, preferences, false,
                    skeletonBehavior.isProjectOf(ProjectType.WEB), skeletonBehavior.isProjectOf(ProjectType.DATABASE)) {
                @Override
                public void setErrorMessage(String message) {
                    ConfigureParametersPage.this.setErrorMessage(message);
                }

                @Override
                public void setPageComplete(boolean isPageComplete) {
                    super.setPageComplete(isPageComplete);
                    notifyPageStatusChanged();
                }
            };
            preferencesControl.createControl();
        }

        if (moldParameterExists) {
            CTabItem skeletonTabItem = new CTabItem(tabFolder, SWT.NONE);
            skeletonTabItem.setText(Messages.getString("ConfigureParametersPage.12")); //$NON-NLS-1$

            final ScrolledComposite scroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL);
            scroll.setLayout(new FillLayout());
            scroll.setExpandHorizontal(true);
            scroll.setExpandVertical(true);
            scroll.getVerticalBar().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (e.detail == SWT.ARROW_UP) {
                        scroll.getVerticalBar().setIncrement(-SCROLL_UNIT);
                    } else if (e.detail == SWT.ARROW_DOWN) {
                        scroll.getVerticalBar().setIncrement(SCROLL_UNIT);
                    }
                }
            });
            skeletonTabItem.setControl(scroll);

            skeletonTabContent = new Composite(scroll, SWT.NONE);
            skeletonTabContent.setLayout(new GridLayout());
            scroll.setContent(skeletonTabContent);

            moldParametersControl = new MoldParametersControl(skeletonTabContent, project, preferences, getMolds(),
                    false) {
                @Override
                public void setErrorMessage(String message) {
                    ConfigureParametersPage.this.setErrorMessage(message);
                }

                @Override
                public void setPageComplete(boolean isPageComplete) {
                    super.setPageComplete(isPageComplete);
                    notifyPageStatusChanged();
                }
            };
            moldParametersControl.createControl();
            scroll.setMinHeight(skeletonTabContent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        }

        if (skeleton != null) {
            if (skeletonBehavior.isProjectOf(ProjectType.YMIR)) {
                CTabItem ymirConfigurationTabItem = new CTabItem(tabFolder, SWT.NONE);
                ymirConfigurationTabItem.setText(Messages.getString("ConfigureParametersPage.0")); //$NON-NLS-1$

                final ScrolledComposite scroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL);
                scroll.setLayout(new FillLayout());
                scroll.setExpandHorizontal(true); // ←君の意味を勘違いしていたせいで8時間を無駄にしたよ... orz 2008-09-20
                scroll.setExpandVertical(true);
                scroll.getVerticalBar().addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (e.detail == SWT.ARROW_UP) {
                            scroll.getVerticalBar().setIncrement(-SCROLL_UNIT);
                        } else if (e.detail == SWT.ARROW_DOWN) {
                            scroll.getVerticalBar().setIncrement(SCROLL_UNIT);
                        }
                    }
                });
                ymirConfigurationTabItem.setControl(scroll);

                ymirConfigurationTabContent = new Composite(scroll, SWT.NONE);
                ymirConfigurationTabContent.setLayout(new GridLayout());
                scroll.setContent(ymirConfigurationTabContent);

                ymirConfigurationControl = new YmirConfigurationControl(ymirConfigurationTabContent, preferences) {
                    @Override
                    public void setErrorMessage(String message) {
                        ConfigureParametersPage.this.setErrorMessage(message);
                    }

                    @Override
                    public void setPageComplete(boolean isPageComplete) {
                        super.setPageComplete(isPageComplete);
                        notifyPageStatusChanged();
                    }
                };
                ymirConfigurationControl.createControl();
                scroll.setMinHeight(ymirConfigurationTabContent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            }
        }

        tabFolderParent.layout();

        setDefaultValues();
    }

    private boolean skeletonParameterExists() {
        int count = 0;
        for (Mold mold : getMolds()) {
            count += mold.getBehavior().getTemplateParameters().length;
        }
        return count > 0;
    }

    private Mold[] getMolds() {
        List<Mold> moldList = new ArrayList<Mold>();
        if (skeleton != null) {
            moldList.add(skeleton);
        }
        if (fragments != null) {
            moldList.addAll(Arrays.asList(fragments));
        }
        return moldList.toArray(new Mold[0]);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (!tabPrepared) {
                ISelectArtifactWizard wizard = (ISelectArtifactWizard) getWizard();
                skeleton = wizard.getSkeletonMold();
                List<ViliBehavior> behaviorList = new ArrayList<ViliBehavior>();
                if (skeleton != null) {
                    skeletonBehavior = skeleton.getBehavior();
                    behaviorList.add(skeletonBehavior);
                }
                fragments = wizard.getFragmentMolds();
                for (Mold fragment : fragments) {
                    ViliBehavior fragmentBehavior = fragment.getBehavior();
                    behaviorList.add(fragmentBehavior);
                }

                startConfigurators(behaviorList);

                createTabFolder();
                tabPrepared = true;

                if (tabFolder != null) {
                    tabFolder.setSelection(0);
                }
                if (preferencesControl != null) {
                    preferencesControl.setVisible(true);
                }

                setPageComplete(validatePage());
            }
        }
    }

    private void startConfigurators(final List<ViliBehavior> behaviorList) {
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                monitor.beginTask(Messages.getString("ConfigureParametersPage.14"), behaviorList.size()); //$NON-NLS-1$
                try {
                    for (ViliBehavior behavior : behaviorList) {
                        behavior.getConfigurator().start(project, behavior, preferences);
                        behavior.update();
                        monitor.worked(1);
                    }
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, false, op);
        } catch (InvocationTargetException ex) {
            Throwable realException = ex.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException.getMessage()); //$NON-NLS-1$
        } catch (InterruptedException ex) {
        }
    }

    public void notifyPageStatusChanged() {
        setPageComplete((preferencesControl == null || preferencesControl.isPageComplete())
                && (moldParametersControl == null || moldParametersControl.isPageComplete())
                && (ymirConfigurationControl == null || ymirConfigurationControl.isPageComplete()));
    }

    public void notifySkeletonAndFragmentsCleared() {
        skeleton = null;
        skeletonBehavior = null;
        fragments = null;

        if (tabFolder != null) {
            tabFolder.dispose();
            tabFolder = null;
        }
        if (tabLabel != null) {
            tabLabel.dispose();
            tabLabel = null;
        }
        tabPrepared = false;

        skeletonTabContent = null;

        moldParametersControl = null;
        preferencesControl = null;

        ymirConfigurationTabContent = null;
        ymirConfigurationControl = null;

        setPageComplete(false);
    }

    public void notifyFragmentsChanged() {
        fragments = null;

        if (tabFolder != null) {
            tabFolder.dispose();
            tabFolder = null;
        }
        if (tabLabel != null) {
            tabLabel.dispose();
            tabLabel = null;
        }
        tabPrepared = false;

        skeletonTabContent = null;

        moldParametersControl = null;
        preferencesControl = null;

        ymirConfigurationTabContent = null;
        ymirConfigurationControl = null;

        setPageComplete(false);
    }

    boolean validatePage() {
        if (preferencesControl != null && !preferencesControl.validatePage()) {
            return false;
        }

        if (moldParametersControl != null && !moldParametersControl.validatePage()) {
            return false;
        }

        if (ymirConfigurationControl != null && !ymirConfigurationControl.validatePage()) {
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    void setDefaultValues() {
        if (preferencesControl != null) {
            preferencesControl.setDefaultValues();
        }
        if (moldParametersControl != null) {
            moldParametersControl.setDefaultValues();
        }
        if (ymirConfigurationControl != null) {
            ymirConfigurationControl.setDefaultValues();
        }
        if (tabFolder != null) {
            tabFolder.setSelection(0);
        }
    }

    public void populateMoldParameters() {
        if (moldParametersControl != null) {
            moldParametersControl.getMolds();
        }
    }

    public YmirConfigurationControl getYmirConfigurationControl() {
        return ymirConfigurationControl;
    }
}