package org.seasar.ymir.vili;

import java.util.Map;

import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.model.Actions;
import org.seasar.ymir.vili.model.maven.Project;

public interface ViliBehavior {
    String EXPANSION_INCLUDES = "expansion.includes"; //$NON-NLS-1$

    String EXPANSION_EXCLUDES = "expansion.excludes"; //$NON-NLS-1$

    String EXPANSION_MERGE_INCLUDES = "expansion.merge.includes"; //$NON-NLS-1$

    String EXPANSION_MERGE_EXCLUDES = "expansion.merge.excludes"; //$NON-NLS-1$

    String TEMPLATE_INCLUDES = "template.includes"; //$NON-NLS-1$

    String TEMPLATE_EXCLUDES = "template.excludes"; //$NON-NLS-1$

    String TEMPLATE_PARAMETERS = "template.parameters"; //$NON-NLS-1$

    String VIEWTEMPLATE_INCLUDES = "viewTemplate.includes";

    String VIEWTEMPLATE_EXCLUDES = "viewTemplate.excludes";

    String PREFIX_TEMPLATE_ENCODING = "template.encoding."; //$NON-NLS-1$

    String PREFIX_TEMPLATE_PARAMETER = "template.parameter."; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_TYPE = ".type"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_DEFAULT = ".default"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_CANDIDATES = ".candidates";

    String SUFFIX_TEMPLATE_PARAMETER_REQUIRED = ".required"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_LABEL = ".label"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_DESCRIPTION = ".description"; //$NON-NLS-1$

    String LABEL = "label"; //$NON-NLS-1$

    String DESCRIPTION = "description"; //$NON-NLS-1$

    String TYPE = "type"; //$NON-NLS-1$

    String VILIVERSION = "viliVersion"; //$NON-NLS-1$

    String PROJECTTYPE = "projectType"; //$NON-NLS-1$

    String CONFIGURATOR = "configurator"; //$NON-NLS-1$

    InclusionType shouldExpand(String path);

    InclusionType shouldMergeWhenExpanding(String path);

    InclusionType shouldEvaluateAsTemplate(String path);

    InclusionType shouldTreatAsViewTemplate(String path);

    String[] getTemplateParameters();

    ParameterType getTemplateParameterType(String name);

    String getTemplateParameterDefault(String name);

    String[] getTemplateParameterCandidates(String name);

    boolean isTemplateParameterRequired(String name);

    String getTemplateParameterLabel(String name);

    String getTemplateParameterDescription(String name);

    String getLabel();

    String getDescription();

    ArtifactType getArtifactType();

    ArtifactVersion getViliVersion();

    String getTemplateEncoding(String path);

    boolean isProjectOf(ProjectType type);

    Project getPom(boolean evaluate, Map<String, Object> parameters);

    IConfigurator getConfigurator();

    boolean isTieUpWithBundle(String bundleName);

    MapProperties getProperties();

    void notifyPropertiesChanged();

    ClassLoader getClassLoader();

    Actions getActions();
}
