package org.seasar.ymir.eclipse;

public interface ApplicationPropertiesKeys {
    String ROOT_PACKAGE_NAME = "rootPackageName";

    String SUPERCLASS = "extension.sourceCreator.superclass";

    String USING_FREYJA_RENDER_CLASS = "extension.sourceCreator.useFreyjaRenderClasses";

    String BEANTABLE_ENABLED = "beantable.enable";

    String FORM_DTO_CREATION_FEATURE_ENABLED = "extension.sourceCreator.feature.createFormDto.enable";

    String CONVERTER_CREATION_FEATURE_ENABLED = "extension.sourceCreator.feature.createConverter.enable";

    String DAO_CREATION_FEATURE_ENABLED = "extension.sourceCreator.feature.createDao.enable";

    String DXO_CREATION_FEATURE_ENABLED = "extension.sourceCreator.feature.createDxo.enable";

    String ECLIPSE_ENABLED = "extension.sourceCreator.eclipse.enable";

    String RESOURCE_SYNCHRONIZER_URL = "extension.sourceCreator.eclipse.resourceSynchronizerURL";
}
