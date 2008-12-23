package org.seasar.ymir.vili.model.maven;

import net.skirnir.xom.annotation.Child;

public class PluginRepository {
    private String id;

    private String name;

    private String url;

    private Releases releases;

    public PluginRepository() {
    }

    public PluginRepository(String id, String name, String url) {
        this(id, name, url, false);
    }

    public PluginRepository(String id, String name, String url, boolean releases) {
        setId(id);
        setName(name);
        setUrl(url);
        if (releases) {
            setReleases(new Releases(true));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        PluginRepository o = (PluginRepository) obj;
        if (equals(o.url, url)) {
            return true;
        }

        return false;
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        if (url != null) {
            h += url.hashCode();
        }
        return h;
    }

    @Override
    public String toString() {
        return "id=" + id + ", url=" + url; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getId() {
        return id;
    }

    @Child(order = 1)
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @Child(order = 2)
    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    @Child(order = 3)
    public void setUrl(String url) {
        if (url != null && url.endsWith("/")) { //$NON-NLS-1$
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;
    }

    public Releases getReleases() {
        return releases;
    }

    @Child(order = 4)
    public void setReleases(Releases releases) {
        this.releases = releases;
    }
}
