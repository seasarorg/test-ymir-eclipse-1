package org.seasar.ymir.eclipse.preferences.impl;

import org.seasar.ymir.vili.DatabaseEntry;
import org.seasar.ymir.vili.ViliProjectPreferencesProvider;
import org.seasar.ymir.vili.maven.Dependency;

abstract public class ViliProjectPreferencesProviderBase implements ViliProjectPreferencesProvider {
    private static final String FIELDSPECIALPREFIX = "this."; //$NON-NLS-1$

    protected final DatabaseEntry[] databaseEntries = new DatabaseEntry[] {
            new DatabaseEntry("H2 Database Engine", "h2", "org.h2.Driver", "jdbc:h2:file:%WEBAPP%/WEB-INF/h2/h2", "sa", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    "", new Dependency("com.h2database", "h2", "1.0.78", "runtime")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            new DatabaseEntry("MySQL Community Server", "mysql", "com.mysql.jdbc.Driver", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    "jdbc:mysql://localhost:3306/[DBNAME]", "", "", new Dependency("mysql", "mysql-connector-java", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                            "5.1.6", "runtime")), //$NON-NLS-1$ //$NON-NLS-2$
            new DatabaseEntry("PostgreSQL 8.3 database (JDBC-3.0)", "postgresql", "org.postgresql.Driver", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    "jdbc:postgresql://localhost:5432/[DBNAME]", "", "", new Dependency("postgresql", "postgresql", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                            "8.3-603.jdbc3", "runtime")), //$NON-NLS-1$ //$NON-NLS-2$
            new DatabaseEntry("PostgreSQL 8.3 database (JDBC-4.0)", "postgresql", "org.postgresql.Driver", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    "jdbc:postgresql://localhost:5432/[DBNAME]", "", "", new Dependency("postgresql", "postgresql", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                            "8.3-603.jdbc4", "runtime")), //$NON-NLS-1$ //$NON-NLS-2$
            new DatabaseEntry(Messages.getString("ViliProjectPreferencesProviderBase.0"), "", "", "", "", "", null), }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    public DatabaseEntry[] getDatabaseEntries() {
        return databaseEntries;
    }

    public String getFieldSpecialPrefix() {
        if (getFieldPrefix().equals("") && getFieldSuffix().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
            return FIELDSPECIALPREFIX;
        } else {
            return ""; //$NON-NLS-1$
        }
    }
}
