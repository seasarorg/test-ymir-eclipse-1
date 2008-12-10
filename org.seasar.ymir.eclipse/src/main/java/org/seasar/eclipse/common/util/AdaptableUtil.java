/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.eclipse.common.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @author taichi
 * 
 */
public class AdaptableUtil {
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
