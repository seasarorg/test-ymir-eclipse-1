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

    String VIEWTEMPLATE_INCLUDES = "viewTemplate.includes"; //$NON-NLS-1$

    String VIEWTEMPLATE_EXCLUDES = "viewTemplate.excludes"; //$NON-NLS-1$

    String PREFIX_TEMPLATE_ENCODING = "template.encoding."; //$NON-NLS-1$

    String PREFIX_TEMPLATE_PARAMETER = "template.parameter."; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_TYPE = ".type"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_DEFAULT = ".default"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_CANDIDATES = ".candidates"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_PARAMETERS = ".parameters"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_REQUIRED = ".required"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_DEPENDS = ".depends"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_MODIFIABLE = ".modifiable"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_LABEL = ".label"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_DESCRIPTION = ".description"; //$NON-NLS-1$

    String LABEL = "label"; //$NON-NLS-1$

    String DESCRIPTION = "description"; //$NON-NLS-1$

    String TYPE = "type"; //$NON-NLS-1$

    String VILIVERSION = "viliVersion"; //$NON-NLS-1$

    String PROJECTTYPE = "projectType"; //$NON-NLS-1$

    String CONFIGURATOR = "configurator"; //$NON-NLS-1$

    String AVAILABLEONLYIFPROJECTEXISTS = "availableOnlyIfProjectExists"; //$NON-NLS-1$

    InclusionType shouldExpand(String path);

    InclusionType shouldMergeWhenExpanding(String path);

    InclusionType shouldEvaluateAsTemplate(String path);

    InclusionType shouldTreatAsViewTemplate(String path);

    String[] getTemplateParameters();

    /**
     * @since 0.2.2
     */
    void setTemplateParameters(String[] names);

    /**
     * @since 0.2.2
     */
    String[] getTemplateParameters(String groupName);

    /**
     * @since 0.2.2
     */
    void setTemplateParameters(String groupName, String[] names);

    /**
     * @since 0.2.2
     */
    boolean isTemplateParameterModifiable(String name);

    /**
     * @since 0.2.2
     */
    void setTemplateParameterModifiable(String name, boolean modifiable);

    ParameterType getTemplateParameterType(String name);

    /**
     * @since 0.2.2
     */
    void setTemplateParameterType(String name, ParameterType type);

    String getTemplateParameterDefault(String name);

    /**
     * @since 0.2.2
     */
    void setTemplateParameterDefault(String name, String defaultValue);

    String[] getTemplateParameterCandidates(String name);

    /**
     * @since 0.2.2
     */
    void setTemplateParameterCandidates(String name, String[] candidates);

    boolean isTemplateParameterRequired(String name);

    /**
     * @since 0.2.2
     */
    void setTemplateParameterRequired(String name, boolean required);

    /**
     * @since 0.2.2
     */
    String[] getTemplateParameterDepends(String name);

    /**
     * @since 0.2.2
     */
    void setTemplateParameterDepends(String name, String[] depends);

    String[] getTemplateParameterDependents(String name);

    String getTemplateParameterLabel(String name);

    /**
     * @since 0.2.2
     */
    void setTemplateParameterLabel(String name, String label);

    String getTemplateParameterDescription(String name);

    /**
     * @since 0.2.2
     */
    void setTemplateParameterDescription(String name, String description);

    String getLabel();

    String getDescription();

    MoldType getMoldType();

    ArtifactVersion getViliVersion();

    boolean isAvailableOnlyIfProjectExists();

    String getTemplateEncoding(String path);

    boolean isProjectOf(ProjectType type);

    Project getPom(boolean evaluate, Map<String, Object> parameters);

    IConfigurator getConfigurator();

    boolean isTieUpWithBundle(String bundleName);

    String getProperty(String name);

    String getProperty(String name, String defaultValue);

    MapProperties getProperties();

    /**
     * @deprecated
     */
    void notifyPropertiesChanged();

    void update();

    ClassLoader getClassLoader();

    Actions getActions();

    ProcessContext getProcessContext();
}
