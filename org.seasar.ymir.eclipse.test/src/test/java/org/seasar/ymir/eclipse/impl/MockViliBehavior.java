package org.seasar.ymir.eclipse.impl;

import java.util.Map;

import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.vili.IConfigurator;
import org.seasar.ymir.vili.InclusionType;
import org.seasar.ymir.vili.MoldType;
import org.seasar.ymir.vili.NullConfigurator;
import org.seasar.ymir.vili.ParameterType;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.model.Actions;
import org.seasar.ymir.vili.model.maven.Project;

public class MockViliBehavior implements ViliBehavior {
    private IConfigurator configurator = new NullConfigurator();

    public Actions getActions() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return null;
    }

    public IConfigurator getConfigurator() {
        return configurator;
    }

    public String getDescription() {
        return null;
    }

    public String getLabel() {
        return null;
    }

    public MoldType getMoldType() {
        return null;
    }

    public Project getPom(boolean evaluate, Map<String, Object> parameters) {
        return null;
    }

    public MapProperties getProperties() {
        return null;
    }

    public String getProperty(String name) {
        return null;
    }

    public String getProperty(String name, String defaultValue) {
        return null;
    }

    public String getTemplateEncoding(String path) {
        return null;
    }

    public String[] getTemplateParameterCandidates(String name) {
        return null;
    }

    public String[] getTemplateParameterMembers(String name) {
        return null;
    }

    public String getTemplateParameterDefault(String name) {
        return null;
    }

    public String[] getTemplateParameterDependents(String name) {
        return null;
    }

    public String getTemplateParameterDescription(String name) {
        return null;
    }

    public String getTemplateParameterLabel(String name) {
        return null;
    }

    public ParameterType getTemplateParameterType(String name) {
        return null;
    }

    public String[] getTemplateParameters() {
        return null;
    }

    public ArtifactVersion getViliVersion() {
        return null;
    }

    public boolean isAvailableOnlyIfProjectExists() {
        return false;
    }

    public boolean isProjectOf(ProjectType type) {
        return false;
    }

    public boolean isTemplateParameterRequired(String name) {
        return false;
    }

    public boolean isTieUpWithBundle(String bundleName) {
        return false;
    }

    public void notifyPropertiesChanged() {
    }

    public InclusionType shouldEvaluateAsTemplate(String path) {
        return null;
    }

    public InclusionType shouldExpand(String path) {
        return null;
    }

    public InclusionType shouldMergeWhenExpanding(String path) {
        return null;
    }

    public InclusionType shouldTreatAsViewTemplate(String path) {
        return null;
    }
}
