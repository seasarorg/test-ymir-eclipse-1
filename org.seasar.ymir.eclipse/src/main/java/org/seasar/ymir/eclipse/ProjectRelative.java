package org.seasar.ymir.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.vili.IAction;
import org.seasar.ymir.vili.model.Action;
import org.seasar.ymir.vili.model.Actions;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ProjectRelative implements IElementChangedListener, IResourceChangeListener, IPropertyChangeListener {
    private IProject project;

    private ClassLoader projectClassLoader;

    private Actions actions;

    public ProjectRelative(IProject project) {
        this.project = project;

        actions = createActions(Activator.getDefault().getPreferenceStore(project).getString(
                PreferenceConstants.P_ACTIONS));
    }

    @SuppressWarnings("unchecked")
    private Actions createActions(String text) {
        Actions actions = null;
        if (text != null && text.length() > 0) {
            actions = Activator.getDefault().getAsBean(text, Actions.class);
        }
        if (actions == null) {
            actions = new Actions();
        }

        for (Action action : actions.getActions()) {
            Artifact artifact = Activator.getDefault().getArtifactResolver().resolve(action.getGroupId(),
                    action.getArtifactId(), action.getVersion(), false);
            if (artifact != null) {
                try {
                    action.setClass((Class<? extends IAction>) ArtifactPair.newInstance(artifact, projectClassLoader)
                            .getBehavior().getClassLoader().loadClass(action.getActionClass()));
                } catch (ClassNotFoundException ex) {
                    Activator.getDefault().log(ex);
                }
            }
        }

        return actions;
    }

    public ClassLoader getProjectClassLoader() {
        if (projectClassLoader == null) {
            try {
                if (project.hasNature(Globals.NATURE_ID_JAVA)) {
                    projectClassLoader = new ProjectClassLoader(JavaCore.create(project));
                } else {
                    projectClassLoader = getClass().getClassLoader();
                }
            } catch (CoreException ignore) {
                // 次回リトライできるよう、projectClassLoaderはnullのままにしておく。
                return getClass().getClassLoader();
            }
        }
        return projectClassLoader;
    }

    public void elementChanged(ElementChangedEvent event) {
        IJavaElementDelta workspaceDelta = (IJavaElementDelta) event.getSource();
        for (IJavaElementDelta projectDelta : workspaceDelta.getAffectedChildren()) {
            IJavaProject javaProject = getJavaProject(projectDelta);
            if (javaProject == null || !javaProject.getProject().equals(project)) {
                continue;
            }

            if (isClasspathChanged(projectDelta)) {
                projectClassLoader = null;
            }
        }
    }

    private IJavaProject getJavaProject(IJavaElementDelta delta) {
        IJavaProject javaProject = delta.getElement().getJavaProject();
        if (javaProject != null) {
            return javaProject;
        }
        IJavaElementDelta[] children = delta.getAffectedChildren();
        for (int i = 0; i < children.length; i++) {
            javaProject = getJavaProject(children[i]);
            if (javaProject != null) {
                return javaProject;
            }
        }
        return null;
    }

    private boolean isClasspathChanged(IJavaElementDelta delta) {
        int flags = delta.getFlags();
        if ((flags & (IJavaElementDelta.F_CLASSPATH_CHANGED | IJavaElementDelta.F_ADDED_TO_CLASSPATH | IJavaElementDelta.F_REMOVED_FROM_CLASSPATH)) != 0) {
            return true;
        }
        IJavaElementDelta[] children = delta.getAffectedChildren();
        for (int i = 0; i < children.length; i++) {
            if (isClasspathChanged(children[i])) {
                return true;
            }
        }
        return false;
    }

    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getResource().getType() != IResource.PROJECT) {
            return;
        }
        if (!event.getResource().getProject().equals(project)) {
            return;
        }

        int type = event.getType();
        if (type == IResourceChangeEvent.PRE_CLOSE || type == IResourceChangeEvent.PRE_DELETE) {
            Activator.getDefault().removeProjectRelative(project);
        }
    }

    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (PreferenceConstants.P_ACTIONS.equals(property)) {
            actions = createActions((String) event.getNewValue());
        }
    }

    public Actions getActions() {
        return actions;
    }
}
