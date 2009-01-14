package org.seasar.ymir.vili.model;

import org.seasar.ymir.vili.IAction;

import net.skirnir.xom.annotation.Attribute;
import net.skirnir.xom.annotation.Child;
import net.skirnir.xom.annotation.Default;
import net.skirnir.xom.annotation.Required;

public class Action {
    private String groupId;

    private String artifactId;

    private String version;

    private String actionId;

    private String name;

    private String actionClass;

    private Class<? extends IAction> clazz;

    private String categoryId = "";

    public Action() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else if (obj == this) {
            return true;
        }

        Action o = (Action) obj;
        if (!equals(groupId, o.groupId)) {
            return false;
        }
        if (!equals(artifactId, o.artifactId)) {
            return false;
        }
        if (!equals(actionId, o.actionId)) {
            return false;
        }

        return true;
    }

    private boolean equals(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    @Override
    public int hashCode() {
        int code = 0;
        if (groupId != null) {
            code += groupId.hashCode();
        }
        if (artifactId != null) {
            code += artifactId.hashCode();
        }
        if (actionId != null) {
            code += actionId.hashCode();
        }
        return code;
    }

    public String getGroupId() {
        return groupId;
    }

    @Child(order = 1)
    @Required
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Child(order = 2)
    @Required
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Child(order = 3)
    @Required
    public void setVersion(String version) {
        this.version = version;
    }

    public String getActionId() {
        return actionId;
    }

    @Child(order = 4)
    @Required
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getName() {
        return name;
    }

    @Child(order = 5)
    @Required
    public void setName(String name) {
        this.name = name;
    }

    public String getActionClass() {
        return actionClass;
    }

    @Child(order = 6, value = "class")
    @Required
    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }

    public void setClass(Class<? extends IAction> clazz) {
        this.clazz = clazz;
    }

    public IAction newAction() {
        if (clazz != null) {
            try {
                return clazz.newInstance();
            } catch (Throwable ignore) {
            }
        }
        return null;
    }

    public String getCategoryId() {
        return categoryId;
    }

    @Attribute
    @Default("")
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
