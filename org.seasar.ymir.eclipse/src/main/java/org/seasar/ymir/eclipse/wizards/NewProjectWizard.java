package org.seasar.ymir.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.HotdeployType;
import org.seasar.ymir.eclipse.ui.YmirConfigurationControl;
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.ProcessContext;
import org.seasar.ymir.vili.ProjectBuilder;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ExtendedContext;
import org.seasar.ymir.vili.util.JdtUtils;

public class NewProjectWizard extends Wizard implements INewWizard, ISelectArtifactWizard {
    static final String REQUIRED_TEMPLATE = Messages.getString("NewProjectWizard.2"); //$NON-NLS-1$

    static final String DS_SECTION = "NewProjectWizard"; //$NON-NLS-1$

    private ViliProjectPreferences preferences;

    private ExtendedContext nonTransitiveContext;

    private SelectArtifactPage firstPage;

    private ConfigureProjectPage secondPage;

    private ConfigureParametersPage thirdPage;

    /**
     * Constructor for NewProjectWizard.
     */
    public NewProjectWizard() {
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.getString("NewProjectWizard.11")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Globals.IMAGE_YMIR));

        preferences = Activator.getDefault().newViliProjectPreferences();
        nonTransitiveContext = Activator.getDefault().getArtifactResolver().newContext(false);

        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        setDialogSettings(settings);
        IDialogSettings section = settings.getSection(DS_SECTION);
        if (section == null) {
            section = settings.addNewSection(DS_SECTION);
        }
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        firstPage = new SelectArtifactPage(null, preferences, ProcessContext.CREATE_PROJECT, nonTransitiveContext, true);
        firstPage.setTitle(Messages.getString("NewProjectWizard.26")); //$NON-NLS-1$
        firstPage.setDescription(Messages.getString("NewProjectWizard.27")); //$NON-NLS-1$
        addPage(firstPage);
        secondPage = new ConfigureProjectPage(preferences);
        addPage(secondPage);
        thirdPage = new ConfigureParametersPage(null, preferences);
        addPage(thirdPage);
    }

    @Override
    public void setContainer(IWizardContainer wizardContainer) {
        super.setContainer(wizardContainer);
        Shell shell = getShell();
        if (shell != null) {
            shell.setSize(600, shell.getSize().y);
        }
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    public boolean performFinish() {
        thirdPage.populateMoldParameters();
        try {
            final Mold skeleton = firstPage.getSkeletonMold();
            final Mold[] fragments = firstPage.getFragmentTemplateMolds();
            final IProject project = secondPage.getProjectHandle();
            final IPath locationPath = secondPage.getLocationPath();
            final IPath jreContainerPath = preferences.getJREContainerPath();
            preferences.setApplicationProperties(createApplicationProperties());
            IRunnableWithProgress op = new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    monitor.beginTask(Messages.getString("NewProjectWizard.1"), 2); //$NON-NLS-1$
                    try {
                        ProjectBuilder builder = Activator.getDefault().getProjectBuilder();
                        builder.createProject(project, locationPath, jreContainerPath, skeleton, preferences,
                                new SubProgressMonitor(monitor, 1));
                        builder.addFragments(project, preferences, fragments, new SubProgressMonitor(monitor, 1));
                    } catch (CoreException e) {
                        Activator.getDefault().getLog().log(e.getStatus());
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            };

            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException ex) {
            Throwable realException = ex.getTargetException();

            String message = realException.getMessage();
            if (message == null || message.length() == 0) {
                message = realException.getClass().getName();
            }

            ILog log = Activator.getDefault().getLog();
            IStatus status;
            if (realException instanceof CoreException) {
                status = ((CoreException) realException).getStatus();
            } else {
                status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, realException);
            }
            log.log(status);

            MessageDialog.openError(getShell(), "Error", message); //$NON-NLS-1$
            return false;
        } catch (CoreException ex) {
            ILog log = Activator.getDefault().getLog();
            log.log(ex.getStatus());

            String message = ex.getMessage();
            if (message == null || message.length() == 0) {
                message = ex.getClass().getName();
            }
            MessageDialog.openError(getShell(), "Error", message); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private MapProperties createApplicationProperties() throws CoreException {
        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        YmirConfigurationControl ymirConfig = thirdPage.getYmirConfigurationControl();
        if (ymirConfig != null) {
            prop.setProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME, preferences.getRootPackageName());
            String value = ymirConfig.getSuperclass();
            if (value.length() > 0) {
                prop.setProperty(ApplicationPropertiesKeys.SUPERCLASS, value);
            }
            prop.setProperty(ApplicationPropertiesKeys.SOURCECREATOR_ENABLE, String.valueOf(ymirConfig
                    .isAutoGenerationEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.FIELDPREFIX, JdtUtils.getFieldPrefix());
            prop.setProperty(ApplicationPropertiesKeys.FIELDSUFFIX, JdtUtils.getFieldSuffix());
            prop.setProperty(ApplicationPropertiesKeys.FIELDSPECIALPREFIX, JdtUtils.getFieldSpecialPrefix());
            prop.setProperty(ApplicationPropertiesKeys.ENABLEINPLACEEDITOR, String.valueOf(ymirConfig
                    .isInplaceEditorEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.ENABLECONTROLPANEL, String.valueOf(ymirConfig
                    .isControlPanelEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.USING_FREYJA_RENDER_CLASS, String.valueOf(ymirConfig
                    .isUsingFreyjaRenderClass()));
            prop.setProperty(ApplicationPropertiesKeys.BEANTABLE_ENABLED, String.valueOf(ymirConfig
                    .isBeantableEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.FORM_DTO_CREATION_FEATURE_ENABLED, String.valueOf(ymirConfig
                    .isFormDtoCreationFeatureEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.CONVERTER_CREATION_FEATURE_ENABLED, String.valueOf(ymirConfig
                    .isConverterCreationFeatureEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.DAO_CREATION_FEATURE_ENABLED, String.valueOf(ymirConfig
                    .isDaoCreationFeatureEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.DXO_CREATION_FEATURE_ENABLED, String.valueOf(ymirConfig
                    .isDxoCreationFeatureEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.TRYTOUPDATECLASSESWHENTEMPLATEMODIFIED, String
                    .valueOf(ymirConfig.isTryToUpdateClassesWhenTemplateModified()));

            boolean eclipseEnabled = ymirConfig.isEclipseEnabled();
            prop.setProperty(ApplicationPropertiesKeys.ECLIPSE_ENABLED, String.valueOf(eclipseEnabled));
            if (eclipseEnabled) {
                value = ymirConfig.getResourceSynchronizerURL();
                if (value.length() > 0) {
                    prop.setProperty(ApplicationPropertiesKeys.RESOURCE_SYNCHRONIZER_URL, value);
                }
                prop.setProperty(ApplicationPropertiesKeys.ECLIPSE_PROJECTNAME, secondPage.getProjectName());
            }

            prop.setProperty(ApplicationPropertiesKeys.S2CONTAINER_CLASSLOADING_DISABLEHOTDEPLOY, String
                    .valueOf(ymirConfig.getHotdeployType() != HotdeployType.S2));
            prop.setProperty(ApplicationPropertiesKeys.S2CONTAINER_COMPONENTREGISTRATION_DISABLEDYNAMIC, String
                    .valueOf(ymirConfig.getHotdeployType() == HotdeployType.VOID));
            prop.setProperty(ApplicationPropertiesKeys.HOTDEPLOY_TYPE, ymirConfig.getHotdeployType().getName());
        }

        return prop;
    }

    public ExtendedContext getNonTransitiveContext() {
        return nonTransitiveContext;
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    public void notifySkeletonCleared() {
        secondPage.notifySkeletonAndFragmentsCleared();
        thirdPage.notifySkeletonAndFragmentsCleared();
    }

    public Mold getSkeletonMold() {
        return firstPage.getSkeletonMold();
    }

    public Mold[] getFragmentMolds() {
        return firstPage.getFragmentTemplateMolds();
    }

    public void notifyFragmentsChanged() {
        thirdPage.notifyFragmentsChanged();
    }
}