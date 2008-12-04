package org.seasar.ymir.eclipse;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public enum ProjectType {
    JAVA, WEB, DATABASE, YMIR;

    private static final Map<String, String> PROJECT_ALIAS_MAP;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("ymir", "java+web+database+ymir");
        PROJECT_ALIAS_MAP = Collections.unmodifiableMap(map);
    }

    public static EnumSet<ProjectType> createEnumSet(String projectType) {
        EnumSet<ProjectType> set = EnumSet.noneOf(ProjectType.class);
        if (projectType != null) {
            String expanded = PROJECT_ALIAS_MAP.get(projectType);
            if (expanded != null) {
                projectType = expanded;
            }
            for (StringTokenizer st = new StringTokenizer(projectType, "+"); st.hasMoreTokens();) {
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
