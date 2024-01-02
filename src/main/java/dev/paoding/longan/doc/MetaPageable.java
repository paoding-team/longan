package dev.paoding.longan.doc;

import dev.paoding.longan.data.Pageable;

import java.util.HashMap;
import java.util.Map;

public class MetaPageable extends MetaParam {

    public Object getSample() {
        Map<String, Object> map = new HashMap<>();
        map.put("page", 1);
        map.put("size", 20);
        map.put("sort", "id");
        map.put("desc", true);
        return map;
    }

    public MetaPageable(String name) {
        this.setName(name);
        this.setAlias("分页对象");
        this.setType(Pageable.class);

        MetaAttribute pageMetaAttribute = new MetaAttribute();
        pageMetaAttribute.setName("page");
        pageMetaAttribute.setAlias("第几页");
        pageMetaAttribute.setType(Integer.class);
        pageMetaAttribute.setSample(1);
        pageMetaAttribute.setDescription("每页的条数");


        MetaAttribute sizeMetaAttribute = new MetaAttribute();
        sizeMetaAttribute.setName("size");
        sizeMetaAttribute.setAlias("条数");
        sizeMetaAttribute.setType(Integer.class);
        sizeMetaAttribute.setSample(20);
        sizeMetaAttribute.setDescription("每页的条数");

        MetaAttribute sortMetaAttribute = new MetaAttribute();
        sortMetaAttribute.setName("sort");
        sortMetaAttribute.setAlias("排序属性");
        sortMetaAttribute.setDescription("默认为 id");
        sortMetaAttribute.setType(String.class);
        sortMetaAttribute.setSample("id");


        MetaAttribute descMetaAttribute = new MetaAttribute();
        descMetaAttribute.setName("desc");
        descMetaAttribute.setAlias("是否倒序排列");
        descMetaAttribute.setDescription("默认为true");
        descMetaAttribute.setType(Boolean.class);
        descMetaAttribute.setSample(true);

        this.addChild(pageMetaAttribute);
        this.addChild(sizeMetaAttribute);
        this.addChild(sortMetaAttribute);
        this.addChild(descMetaAttribute);
    }
}
