package org.seasar.ymir.eclipse.maven.impl;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;

import werkzeugkasten.mvnhack.Constants;
import werkzeugkasten.mvnhack.repository.impl.DefaultConfiguration;
import werkzeugkasten.mvnhack.repository.impl.LocalRepository;

public class ExtendedConfiguration extends DefaultConfiguration {
    public ExtendedConfiguration() {
    }

    public ExtendedConfiguration(Properties properties) {
        super(properties);
    }

    protected void load() {
        File cur = new File(".", Constants.DIR_REPOSITORY);
        if (cur.exists()) {
            addLocalRepository(cur);
        }
        StringBuilder stb = new StringBuilder();
        stb.append(".m2");
        stb.append('/');
        stb.append(Constants.DIR_REPOSITORY);
        File usr = new File(System.getProperty("user.home"), stb.toString());
        if (usr.exists()) {
            addLocalRepository(usr);
        }

        addRepository(new ExtendedRemoteRepository(Constants.CENTRAL_REPOSITORY, builder));
    }

    protected void addLocalRepository(File rep) {
        Constants.LOG.log(Level.INFO, "LocalRepository :{0}", rep.toString());
        LocalRepository lr = new ExtendedLocalRepository(rep, builder);
        addRepository(lr);
        addDestination(lr);
    }
}
