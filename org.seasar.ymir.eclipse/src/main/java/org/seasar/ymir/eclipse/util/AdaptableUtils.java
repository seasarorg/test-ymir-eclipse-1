package org.seasar.ymir.eclipse.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @author taichi
 * 
 */
public class AdaptableUtils {
    public static IResource toResource(Object adaptable) {
        IResource result = null;
        if (adaptable instanceof IResource) {
            result = (IResource) adaptable;
        } else if (adaptable instanceof IAdaptable) {
            IAdaptable a = (IAdaptable) adaptable;
            result = (IResource) a.getAdapter(IResource.class);
        }
        return result;
    }

    public static IProject toProject(Object adaptable) {
        IProject result = null;
        if (adaptable instanceof IProject) {
            result = (IProject) adaptable;
        } else if (adaptable instanceof IAdaptable) {
            IAdaptable a = (IAdaptable) adaptable;
            result = (IProject) a.getAdapter(IProject.class);
        }
        if (result == null) {
            IResource r = toResource(adaptable);
            if (r != null) {
                result = r.getProject();
            }
        }
        return result;
    }
}
