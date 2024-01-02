package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Validate;
import dev.paoding.longan.data.Entity;

public class MetaAttributeService {
    public static MetaAttribute load(String modelName, Validate validate) throws NotFoundException {
        MetaAttribute metaAttribute = new MetaAttribute();
        metaAttribute.setId(String.valueOf(DocumentService.nextId()));
        metaAttribute.setName(validate.name());
        if (validate.notNull()) {
            metaAttribute.setNotNull(true);
        }
        if (validate.notBlank()) {
            metaAttribute.setNotBlank(true);
        }
        if (validate.notEmpty()) {
            metaAttribute.setNotEmpty(true);
        }
        if (validate.size().length == 2) {
            metaAttribute.setSize(validate.size());
        }
        if (!validate.regexp().isEmpty()) {
            metaAttribute.setRegexp(validate.regexp());
        }

        MetaField metaField = MetaModelService.getMetaField(modelName + "." + validate.name());
        if (metaField == null) {
            throw new NotFoundException("Field '" + validate.name() + "' was not found in class " + modelName).append(metaField);
        }

//        if (originalMetaField.isTypeIsModel()) {
//            metaAttribute.setActualJavaType(originalMetaField.getActualJavaType());
////            MetaField idMetaField = MetaModelService.getMetaField(originalMetaField.getJavaType() + ".id");
////            if(idMetaField != null) {
////                MetaAttribute idMetaAttribute = new MetaAttribute();
////                idMetaAttribute.setName(idMetaField.getName());
////                idMetaAttribute.setType(idMetaField.getType());
////                idMetaAttribute.setSample(idMetaField.getSample());
////                idMetaAttribute.setDescription(idMetaField.getDescription());
////
//////                metaAttribute.addChild(idMetaAttribute);
////            }
////
//        }
//        if(originalMetaField.getActualTypeIsModel()){
//            MetaField idMetaField = MetaModelService.getMetaField(originalMetaField.getActualJavaType() + ".id");
//            if(idMetaField != null) {
//                MetaAttribute idMetaAttribute = new MetaAttribute();
//                idMetaAttribute.setName(idMetaField.getName());
//                idMetaAttribute.setType(idMetaField.getType());
//                idMetaAttribute.setSample(idMetaField.getSample());
//                idMetaAttribute.setDescription(idMetaField.getDescription());
////            idMetaAttribute.setNotNull(true);
////                metaAttribute.addChild(idMetaAttribute);
////
//                metaAttribute.setActualType(originalMetaField.getActualType());
//            }
//        }

        metaAttribute.setType(metaField.getType());
        if(metaField.getType().isAnnotationPresent(Entity.class)){
            metaAttribute.setValidator(validate.validator());
        }
        metaAttribute.setAlias(metaField.getAlias());
        metaAttribute.setSample(metaField.getSample());
        metaAttribute.setDescription(metaField.getDescription());
        metaAttribute.setJavaType(metaField.getJavaType());
        metaAttribute.setDartType(metaField.getDartType());
        metaAttribute.setJsType(metaField.getJsType());
        metaAttribute.setTypeIsModel(metaField.isTypeModel());
        metaAttribute.setActualTypeIsModel(metaField.isActualTypeModel());
        metaAttribute.setActualJavaType(metaField.getActualJavaType());
        if(metaField.getActualType() != null) {
            if(metaField.getActualType().isAnnotationPresent(Entity.class)){
                metaAttribute.setValidator(validate.validator());
            }
            metaAttribute.setActualType(metaField.getActualType());
        }
        return metaAttribute;
    }
}
