package org.seasar.ymir.vili;

import java.util.EnumSet;
import java.util.StringTokenizer;

public enum ProjectType {
    JAVA, WEB, DATABASE;

    public static EnumSet<ProjectType> createEnumSet(String projectType) {
        EnumSet<ProjectType> set = EnumSet.noneOf(ProjectType.class);
        if (projectType != null) {
            for (StringTokenizer st = new StringTokenizer(projectType, "+"); st.hasMoreTokens();) { //$NON-NLS-1$
                String tkn = st.nextToken().trim();
                ProjectType enm = enumOf(tkn);
                if (enm != null) {
                    set.add(enm);
                }
            }
        }
        return set;
    }

    public static ProjectType enumOf(String name) {
        for (ProjectType enm : values()) {
            if (enm.name().equalsIgnoreCase(name)) {
                return enm;
            }
        }
        return null;
    }
}
