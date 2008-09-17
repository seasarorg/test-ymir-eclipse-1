package org.seasar.ymir.eclipse;

import org.seasar.ymir.eclipse.maven.Dependency;

public class DatabaseEntry {
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
    public String toString() {
        return "name=" + name + ", type=" + type + ", driverClassName=" + driverClassName + ", URL=" + databaseURL
                + ", user=" + user + ", password=" + password;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getURL() {
        return databaseURL;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public Dependency getDependency() {
        return dependency;
    }
}
