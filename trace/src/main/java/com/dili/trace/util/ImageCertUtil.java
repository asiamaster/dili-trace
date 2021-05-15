package com.dili.trace.util;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.enums.ImageCertTypeEnum;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ImageCertUtil {

    public static Map<ImageCertTypeEnum, List<ImageCert>> groupedImageCertList(List<ImageCert>imageCertList) {

        Map<ImageCertTypeEnum, List<ImageCert>>map= StreamEx.ofNullable(imageCertList).flatCollection(Function.identity()).nonNull().filter(item->item.getCertType()!=null)
                .mapToEntry(item -> ImageCertTypeEnum.fromCode(item.getCertType()).orElse(null), Function.identity()).filterKeys(Objects::nonNull)
                .grouping();
        StreamEx.of(ImageCertTypeEnum.values()).filter(e->{
            return !map.containsKey(e);
        }).forEach(e->{
            map.put(e,new ArrayList<>());
        });
        return map;
    }
}
