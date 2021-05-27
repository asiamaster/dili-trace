package com.dili.trace.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.ImageCertTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.domain.ImageCert;
import com.google.common.collect.Lists;

import one.util.streamex.StreamEx;
import tk.mybatis.mapper.entity.Example;

/**
 * 图片存储
 */
@Service
public class ImageCertService extends BaseServiceImpl<ImageCert, Long> {
    /**
     * 图片转换
     *
     * @param urls
     * @param certTypeEnum
     * @return
     */
    public List<ImageCert> stringToImageCertList(String urls, ImageCertTypeEnum certTypeEnum, BillTypeEnum billTypeEnum) {

        return StreamEx.ofNullable(urls).map(String::trim).filter(item -> item.length() > 0)
                .flatArray(item -> {
                    return item.split(",");
                }).filter(StringUtils::isNotBlank).map(uid -> {
                    ImageCert cert = new ImageCert();
//                    cert.setUrl(url);
                    cert.setUid(uid);
                    cert.setCertType(certTypeEnum.getCode());
                    cert.setBillType(billTypeEnum.getCode());
                    return cert;
                }).toList();
    }



    /**
     * 保存图片
     *
     * @param imageCertList
     * @param billId
     * @param billType
     * @return
     */
    public List<ImageCert> insertImageCert(List<ImageCert> imageCertList, Long billId, BillTypeEnum billType) {
        if (billId != null) {
            // 先删除全部原有图片
            ImageCert deleteConditon = new ImageCert();
            deleteConditon.setBillId(billId);
            deleteConditon.setBillType(billType.getCode());
            this.deleteByExample(deleteConditon);

            // 增加新的图片
            return StreamEx.of(imageCertList).nonNull().map(cert -> {
                cert.setBillId(billId);
                cert.setId(null);
                cert.setBillType(billType.getCode());
                cert.setCreated(new Date());
                cert.setModified(new Date());
                this.insertSelective(cert);
                return cert;
            }).toList();
        }
        return Lists.newArrayList();
    }

    /**
     * 查找 图片
     *
     * @param billIdList
     * @return
     */
    public List<ImageCert> findImageCertListByBillIdList(List<Long> billIdList, BillTypeEnum billTypeEnum) {
        return this.findImageCertListByBillId(billIdList, billTypeEnum, null);
    }

    /**
     * 查找 图片
     *
     * @param billIdList
     * @return
     */
    public List<ImageCert> findImageCertListByBillIdList(List<Long> billIdList, ImageCertTypeEnum certType) {
        return this.findImageCertListByBillId(billIdList, null, certType);
    }


    /**
     * 查找 图片
     *
     * @param billId
     * @param billType
     * @return
     */
    public List<ImageCert> findImageCertListByBillId(Long billId, BillTypeEnum billType) {
        return this.findImageCertListByBillId(Arrays.asList(billId), billType, null);
    }

    /**
     * 查找 图片
     *
     * @param billIdList
     * @param billType
     * @return
     */
    private List<ImageCert> findImageCertListByBillId(List<Long> billIdList, BillTypeEnum billType, ImageCertTypeEnum certType) {
        if (billIdList == null || billIdList.isEmpty()) {
            return Lists.newArrayList();
        }
        if (billType == null && certType == null) {
            return Lists.newArrayList();
        }

        Example e = new Example(ImageCert.class);
        e.and().andIn("billId", billIdList);

        if (billType != null) {
            e.and().andEqualTo("billType", billType.getCode());
        }
        if (certType != null) {
            e.and().andEqualTo("certType", billType.getCode());
        }


        return this.getDao().selectByExample(e);
    }

}
