package org.seasar.ymir.eclipse;

import org.seasar.ymir.eclipse.maven.Dependency;

public class DatabaseEntry {
    private String name;

    private String driverClassName;

    private String databaseURL;

    private String user;

    private String password;

    private Dependency dependency;

    public DatabaseEntry(String name, String driverClassName, String databaseURL, String user, String password,
            Dependency dependency) {
        this.name = name;
        this.driverClassName = driverClassName;
        this.databaseURL = databaseURL;
        this.user = user;
        this.password = password;
        this.dependency = dependency;
    }

    public String getName() {
        return name;
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
