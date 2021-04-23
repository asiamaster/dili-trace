package com.dili.trace;

import com.alibaba.fastjson.JSON;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.enums.ImageCertTypeEnum;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class JsonPathTest {
    @Test
    public void parseJson() {
        RegisterBill rb = new RegisterBill();
        rb.setImageCertList(new ArrayList<>());

        ImageCert imageCert = new ImageCert();
        imageCert.setCertType(ImageCertTypeEnum.DETECT_REPORT.getCode());
        rb.getImageCertList().add(imageCert);


        ImageCert imageCert2 = new ImageCert();
        imageCert2.setCertType(ImageCertTypeEnum.ORIGIN_CERTIFIY.getCode());
        rb.getImageCertList().add(imageCert2);
        String json = JSON.toJSONString(rb);
//        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);

        Configuration conf = Configuration.defaultConfiguration();
//        Configuration conf2 = conf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

        Configuration conf2 = conf.addOptions(Option.ALWAYS_RETURN_LIST,Option.DEFAULT_PATH_LEAF_TO_NULL);
        Object id = JsonPath.using(conf2).parse(json).read("$.id");
        System.out.println(id);
        List<Object> certTypeList = JsonPath.using(conf2).parse(json).read("$.imageCertList[*].certType");
        System.out.println(certTypeList);
    }


}
