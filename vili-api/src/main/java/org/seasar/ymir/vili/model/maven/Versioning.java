package org.seasar.ymir.vili.model.maven;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.skirnir.xom.annotation.Child;

public class Versioning {
    private static final String FORMAT = "yyyyMMddHHmmss";

    private String release;

    private Snapshot snapshot;

    private Versions versions;

    private String lastUpdated;

    public Versions getVersions() {
        return versions;
    }

    public String getRelease() {
        return release;
    }

    @Child(order = 1)
    public void setRelease(String release) {
        this.release = release;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    @Child(order = 2)
    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Child(order = 3)
    public void setVersions(Versions versions) {
        this.versions = versions;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public Date getLastUpdatedDate() {
        if (lastUpdated == null) {
            return null;
        } else {
            try {
                return getDateFormat().parse(lastUpdated);
            } catch (ParseException ex) {
                return null;
            }
        }
    }

    @Child(order = 4)
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        if (lastUpdatedDate == null) {
            lastUpdated = null;
        } else {
            lastUpdated = getDateFormat().format(lastUpdatedDate);
        }
    }

    DateFormat getDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf;
    }
}
