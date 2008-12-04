package org.seasar.ymir.eclipse;

import org.seasar.ymir.eclipse.maven.Dependency;

public class DatabaseEntry implements Cloneable {
    private String name;

    private String type;

    private String driverClassName;

    private String databaseURL;

    private String user;

    private String password;

    private Dependency dependency;

    public DatabaseEntry(String name, String type, String driverClassName, String databaseURL, String user,
            String password, Dependency dependency) {
        this.name = name;
        this.type = type;
        this.driverClassName = driverClassName;
        this.databaseURL = databaseURL;
        this.user = user;
        this.password = password;
        this.dependency = dependency;
    }

    @Override
    public Object clone() {
        try {
            return (DatabaseEntry) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "name=" + name + ", type=" + type + ", driverClassName=" + driverClassName + ", URL=" + databaseURL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + ", user=" + user + ", password=" + password; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getURL() {
        return databaseURL;
    }

    public void setURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Dependency getDependency() {
        return dependency;
    }
}
