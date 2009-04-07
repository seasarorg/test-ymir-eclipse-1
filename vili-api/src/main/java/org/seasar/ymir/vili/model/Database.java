package org.seasar.ymir.vili.model;

import org.seasar.ymir.vili.model.maven.Dependency;

import net.skirnir.xom.annotation.Child;

public class Database implements Cloneable {
    private String name;

    private String type;

    private String driverClassName;

    private String databaseURL;

    private String user;

    private String password;

    private Dependency dependency;

    public Database() {
    }

    public Database(String name, String type, String driverClassName,
            String databaseURL, String user, String password,
            Dependency dependency) {
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
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "name=" + name + ", type=" + type + ", driverClassName=" + driverClassName + ", URL=" + databaseURL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + ", user=" + user + ", password=" + password; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((databaseURL == null) ? 0 : databaseURL.hashCode());
        result = prime * result
                + ((driverClassName == null) ? 0 : driverClassName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Database other = (Database) obj;
        if (databaseURL == null) {
            if (other.databaseURL != null)
                return false;
        } else if (!databaseURL.equals(other.databaseURL))
            return false;
        if (driverClassName == null) {
            if (other.driverClassName != null)
                return false;
        } else if (!driverClassName.equals(other.driverClassName))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

    public String getName() {
        return name;
    }

    @Child(order = 1)
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    @Child(order = 2)
    public void setType(String type) {
        this.type = type;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    @Child(order = 3)
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getURL() {
        return databaseURL;
    }

    @Child(order = 4)
    public void setURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    public String getUser() {
        return user;
    }

    @Child(order = 5)
    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    @Child(order = 6)
    public void setPassword(String password) {
        this.password = password;
    }

    public Dependency getDependency() {
        return dependency;
    }

    @Child(order = 7)
    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }
}
