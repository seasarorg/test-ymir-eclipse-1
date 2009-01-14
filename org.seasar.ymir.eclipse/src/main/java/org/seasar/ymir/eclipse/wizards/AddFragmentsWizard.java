package org.seasar.ymir.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ExtendedContext;

public class AddFragmentsWizard extends Wizard implements ISelectArtifactWizard {
    static final String DS_SECTION = "AddFragmentsWizard"; //$NON-NLS-1$

    private IProject project;

    private Mold[] fragmentMolds;

    private ViliProjectPreferences preferences;

    private ExtendedContext nonTransitiveContext;

    private SelectArtifactPage firstPage;

    private ConfigureParametersPage secondPage;

    public AddFragmentsWizard(IProject project, Mold... fragmentMolds) {
        this.project = project;
        this.fragmentMolds = fragmentMolds;

        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.getString("AddFragmentsWizard.1")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Globals.IMAGE_YMIR));

        preferences = Activator.getDefault().getViliProjectPreferences(project);
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
        if (fragmentMolds.length == 0) {
            firstPage = new SelectArtifactPage(project, preferences, nonTransitiveContext, false);
            firstPage.setTitle(Messages.getString("AddFragmentsWizard.1")); //$NON-NLS-1$
            firstPage.setDescription(Messages.getString("AddFragmentsWizard.2")); //$NON-NLS-1$
            addPage(firstPage);
        }
        secondPage = new ConfigureParametersPage(project, preferences);
        addPage(secondPage);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    public boolean performFinish() {
        secondPage.populateSkeletonParameters();
        try {
            final Mold[] fragments = getFragmentMolds();
            IRunnableWithProgress op = new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    monitor.beginTask(Messages.getString("AddFragmentsWizard.3"), 1); //$NON-NLS-1$
                    try {
                        Activator.getDefault().getProjectBuilder().addFragments(project, preferences, fragments,
                                new SubProgressMonitor(monitor, 1));
                    } catch (CoreException ex) {
                        throw new InvocationTargetException(ex);
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

    public void notifySkeletonCleared() {
        secondPage.notifySkeletonAndFragmentsCleared();
    }

    public Mold[] getFragmentMolds() {
        if (fragmentMolds.length > 0) {
            return fragmentMolds;
        } else {
            return firstPage.getFragmentTemplateMolds();
        }
    }

    public Mold getSkeletonMold() {
        return null;
    }

    public void notifyFragmentsChanged() {
        secondPage.notifyFragmentsChanged();
    }
}