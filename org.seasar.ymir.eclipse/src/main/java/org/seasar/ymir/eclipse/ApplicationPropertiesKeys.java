package org.seasar.ymir.eclipse;

public interface ApplicationPropertiesKeys {
    String ROOT_PACKAGE_NAME = "rootPackageName"; //$NON-NLS-1$

    String BEANTABLE_ENABLED = "beantable.enable"; //$NON-NLS-1$

    String SOURCECREATOR_ENABLE = "extension.sourceCreator.enable"; //$NON-NLS-1$

    String SUPERCLASS = "extension.sourceCreator.superclass"; //$NON-NLS-1$

    String USING_FREYJA_RENDER_CLASS = "extension.sourceCreator.useFreyjaRenderClasses"; //$NON-NLS-1$

    String FIELDSPECIALPREFIX = "extension.sourceCreator.fieldSpecialPrefix"; //$NON-NLS-1$

    String FIELDPREFIX = "extension.sourceCreator.fieldPrefix"; //$NON-NLS-1$

    String FIELDSUFFIX = "extension.sourceCreator.fieldSuffix"; //$NON-NLS-1$

    String ENABLEINPLACEEDITOR = "extension.sourceCreator.enableInplaceEditor"; //$NON-NLS-1$

    String ENABLECONTROLPANEL = "extension.sourceCreator.enableControlPanel"; //$NON-NLS-1$

    String FORM_DTO_CREATION_FEATURE_ENABLED = "extension.sourceCreator.feature.createFormDto.enable"; //$NON-NLS-1$

    String CONVERTER_CREATION_FEATURE_ENABLED = "extension.sourceCreator.feature.createConverter.enable"; //$NON-NLS-1$

    String DAO_CREATION_FEATURE_ENABLED = "extension.sourceCreator.feature.createDao.enable"; //$NON-NLS-1$

    String DXO_CREATION_FEATURE_ENABLED = "extension.sourceCreator.feature.createDxo.enable"; //$NON-NLS-1$

    String ECLIPSE_ENABLED = "extension.sourceCreator.eclipse.enable"; //$NON-NLS-1$

    String RESOURCE_SYNCHRONIZER_URL = "extension.sourceCreator.eclipse.resourceSynchronizerURL"; //$NON-NLS-1$

    String ECLIPSE_PROJECTNAME = "extension.sourceCreator.eclipse.projectName"; //$NON-NLS-1$

    String TRYTOUPDATECLASSESWHENTEMPLATEMODIFIED = "extension.sourceCreator.tryToUpdateClassesWhenTemplateModified"; //$NON-NLS-1$

    String S2CONTAINER_CLASSLOADING_DISABLEHOTDEPLOY = "s2container.classLoading.disableHotdeploy"; //$NON-NLS-1$

    String S2CONTAINER_COMPONENTREGISTRATION_DISABLEDYNAMIC = "s2container.componentRegistration.disableDynamic"; //$NON-NLS-1$

    String HOTDEPLOY_TYPE = "hotdeployType"; //$NON-NLS-1$
}
