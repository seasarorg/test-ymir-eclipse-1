package org.seasar.ymir.vili.maven;

import java.util.LinkedHashSet;
import java.util.Set;

import net.skirnir.xom.annotation.Child;

public class PluginRepositories {
    private Set<PluginRepository> pluginRepositories = new LinkedHashSet<PluginRepository>();

    public PluginRepositories() {
    }

    public PluginRepositories(PluginRepository... pluginRepositories) {
        for (PluginRepository pluginRepository : pluginRepositories) {
            addPluginRepository(pluginRepository);
        }
    }

    public PluginRepository[] getPluginRepositories() {
        return pluginRepositories.toArray(new PluginRepository[0]);
    }

    @Child
    public void addPluginRepository(PluginRepository pluginRepository) {
        pluginRepositories.add(pluginRepository);
    }
}
