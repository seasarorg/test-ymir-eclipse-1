package org.seasar.ymir.eclipse;

import java.util.ArrayList;
import java.util.List;

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
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.ProcessContext;
import org.seasar.ymir.vili.model.Action;
import org.seasar.ymir.vili.model.Actions;
import org.seasar.ymir.vili.model.Fragment;
import org.seasar.ymir.vili.model.Fragments;
import org.seasar.ymir.vili.model.Skeleton;
import org.seasar.ymir.vili.util.ProjectClassLoader;
import org.seasar.ymir.vili.util.XOMUtils;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ProjectRelative implements IElementChangedListener, IResourceChangeListener, IPropertyChangeListener {
    private IProject project;

    private ClassLoader projectClassLoader;

    private Skeleton skeleton;

    private Actions actions;

    public ProjectRelative(IProject project) {
        this.project = project;

        skeleton = createSkeleton(Activator.getDefault().getPreferenceStore(project).getString(
                PreferenceConstants.P_SKELETON));
        actions = createActions(Activator.getDefault().getPreferenceStore(project).getString(
                PreferenceConstants.P_ACTIONS));
    }

    private Mold createMold(ProcessContext context, String groupId, String artifactId, String version) {
        if (groupId == null || artifactId == null || version == null) {
            return null;
        }
        Artifact artifact = Activator.getDefault().getArtifactResolver().resolve(groupId, artifactId, version, false);
        if (artifact != null) {
            return Mold.newInstance(artifact, getProjectClassLoader(), context);
        } else {
            return null;
        }
    }

    private Skeleton createSkeleton(String text) {
        Skeleton skeleton = null;
        if (text != null & text.length() > 0) {
            try {
                skeleton = XOMUtils.getAsBean(text, Skeleton.class);
            } catch (CoreException ex) {
                Activator.getDefault().log(ex);
            }
        }
        if (skeleton == null) {
            skeleton = new Skeleton();
            skeleton.setFragments(new Fragments());
        }

        return skeleton;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private Actions createActions(String text) {
        Actions actions = null;
        if (text != null && text.length() > 0) {
            try {
                actions = XOMUtils.getAsBean(text, Actions.class);
            } catch (CoreException ex) {
                Activator.getDefault().log(ex);
            }
        }
        if (actions == null) {
            actions = new Actions();
        }

        for (Action action : actions.getActions()) {
            Artifact artifact = Activator.getDefault().getArtifactResolver().resolve(action.getGroupId(),
                    action.getArtifactId(), action.getVersion(), false);
            if (artifact != null) {
                try {
                    action
                            .setClass((Class<? extends IAction>) Mold.newInstance(artifact, getProjectClassLoader(),
                                    ProcessContext.TEMPORARY).getBehavior().getClassLoader().loadClass(
                                    action.getActionClass()));
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
                    projectClassLoader = new ProjectClassLoader(JavaCore.create(project), getClass().getClassLoader());
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
        IResource resource = event.getResource();
        if (resource == null || resource.getType() != IResource.PROJECT) {
            return;
        }
        if (!resource.getProject().equals(project)) {
            return;
        }

        int type = event.getType();
        if (type == IResourceChangeEvent.PRE_CLOSE || type == IResourceChangeEvent.PRE_DELETE) {
            Activator.getDefault().removeProjectRelative(project);
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (PreferenceConstants.P_ACTIONS.equals(property)) {
            actions = createActions((String) event.getNewValue());
        }
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public Mold[] getMolds(ProcessContext context) {
        Mold skeletonMold = createMold(context, skeleton.getGroupId(), skeleton.getArtifactId(), skeleton.getVersion());

        List<Mold> moldList = new ArrayList<Mold>();
        if (skeletonMold != null) {
            moldList.add(skeletonMold);
        }

        for (Fragment fragment : skeleton.getFragments().getFragments()) {
            Artifact artifact = Activator.getDefault().getArtifactResolver().resolve(fragment.getGroupId(),
                    fragment.getArtifactId(), fragment.getVersion(), false);
            if (artifact != null) {
                Mold mold = Mold.newInstance(artifact, getProjectClassLoader(), context);
                moldList.add(mold);
            }
        }

        return moldList.toArray(new Mold[0]);
    }

    public Actions getActions() {
        return actions;
    }
}
