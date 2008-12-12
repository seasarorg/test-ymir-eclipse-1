package org.seasar.ymir.vili;

import org.seasar.ymir.vili.maven.Project;
import org.seasar.ymir.vili.util.AntPathPatterns;

public interface ViliBehavior {
    String EXPANSION_EXCLUDES = "expansion.excludes"; //$NON-NLS-1$

    String EXPANSION_EXCLUDESIFEMPTY = "expansion.excludesIfEmpty"; //$NON-NLS-1$

    String EXPANSION_MERGES = "expansion.merges"; //$NON-NLS-1$

    String TEMPLATE_INCLUDES = "template.includes"; //$NON-NLS-1$

    String TEMPLATE_EXCLUDES = "template.excludes"; //$NON-NLS-1$

    String TEMPLATE_PARAMETERS = "template.parameters"; //$NON-NLS-1$

    String PREFIX_TEMPLATE_ENCODING = "template.encoding."; //$NON-NLS-1$

    String PREFIX_TEMPLATE_PARAMETER = "template.parameter."; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_TYPE = ".type"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_DEFAULT = ".default"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_REQUIRED = ".required"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_LABEL = ".label"; //$NON-NLS-1$

    String SUFFIX_TEMPLATE_PARAMETER_DESCRIPTION = ".description"; //$NON-NLS-1$

    String LABEL = "label"; //$NON-NLS-1$

    String DESCRIPTION = "description"; //$NON-NLS-1$

    String TYPE = "type"; //$NON-NLS-1$

    String PROJECTTYPE = "projectType"; //$NON-NLS-1$

    String CONFIGURATOR = "configurator"; //$NON-NLS-1$

    AntPathPatterns getExpansionExcludes();

    AntPathPatterns getExpansionExcludesIfEmpty();

    AntPathPatterns getExpansionMerges();

    AntPathPatterns getTemplateIncludes();

    AntPathPatterns getTemplateExcludes();

    String[] getTemplateParameters();

    ParameterType getTemplateParameterType(String name);

    String getTemplateParameterDefault(String name);

    boolean isTemplateParameterRequired(String name);

    String getTemplateParameterLabel(String name);

    String getTemplateParameterDescription(String name);

    String getLabel();

    String getDescription();

    ArtifactType getArtifactType();

    String getTemplateEncoding(String path);

    boolean isProjectOf(ProjectType type);

    Project getPom();

    Configurator newConfigurator(ClassLoader projectClassLoader);
}