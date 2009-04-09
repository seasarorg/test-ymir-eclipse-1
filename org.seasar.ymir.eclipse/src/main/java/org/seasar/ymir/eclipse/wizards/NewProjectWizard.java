package org.seasar.ymir.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;

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
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.ProcessContext;
import org.seasar.ymir.vili.ProjectBuilder;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ExtendedContext;

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
        }
        return true;
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