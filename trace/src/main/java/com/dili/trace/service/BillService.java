package com.dili.trace.service;

import cn.hutool.core.bean.BeanUtil;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.ImageCertTypeEnum;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:34.
 */
@Service
public class BillService extends TraceBaseService<RegisterBill, Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BillService.class);

    @Autowired
    QualityTraceTradeBillService qualityTraceTradeBillService;
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    UserPlateService userPlateService;
    @Autowired
    UsualAddressService usualAddressService;
    @Autowired
    DetectTaskService detectTaskService;
    @Autowired
    ImageCertService imageCertService;
    @Autowired
    DetectRequestService detectRequestService;


    /**
     * 根据id查询
     * @param billId
     * @return
     */
    public Optional<RegisterBill> getAvaiableBill(Long billId) {
        if (billId == null) {
            return Optional.empty();
        }
        RegisterBill item=super.get(billId);
        return Optional.ofNullable(item).filter(bill -> YesOrNoEnum.NO.getCode().equals(bill.getIsDeleted()));
    }

    /**
     * 返回mapper
     *
     * @return
     */
    public RegisterBillMapper getActualDao() {
        return (RegisterBillMapper) getDao();
    }


    @Override
    public EasyuiPageOutput listEasyuiPageByExample(RegisterBill domain, boolean useProvider) throws Exception {
        List<RegisterBill> list = listByExample(domain);
        Map<Long, DetectRequest> idAndDetectRquestMap = this.detectRequestService.findDetectRequestByIdList(StreamEx.of(list).map(RegisterBill::getDetectRequestId).toList());
        //检测值
        // Map<String, DetectRecord> recordMap = detectRecordService.findMapRegisterBillByIds(StreamEx.of(list).map(RegisterBill::getLatestDetectRecordId).toList());
        StreamEx.of(list).forEach(rb -> {
            rb.setDetectRequest(idAndDetectRquestMap.get(rb.getDetectRequestId()));
        });

        long total = list instanceof Page ? ((Page) list).getTotal() : list.size();
        List results = useProvider ? ValueProviderUtils.buildDataByProvider(domain, list) : list;
        return new EasyuiPageOutput(total, results);
    }


    /**
     * 通过code查询登记单
     *
     * @param code
     * @return
     */
    public RegisterBill findByCode(String code) {
        RegisterBill registerBill = new RegisterBill();
        registerBill.setCode(code);
        List<RegisterBill> list = list(registerBill);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 通过idlist查询登记单
     *
     * @param idList
     * @return
     */
    public List<RegisterBill> findByIdList(Collection<Long> idList) {
        RegisterBillDto dto = new RegisterBillDto();
        dto.setIdList(CollectionUtils.emptyIfNull(idList).stream().filter(Objects::nonNull).collect(Collectors.toList()));
        if (dto.getIdList().isEmpty()) {
            return Lists.newArrayList();
        }
        return this.listByExample(dto);
    }

    /**
     * 通过sampleCode查询登记单
     *
     * @param sampleCode
     * @return
     */
    public RegisterBill findBySampleCode(String sampleCode) {
        if (StringUtils.isBlank(sampleCode)) {
            return null;
        }
        RegisterBill registerBill = new RegisterBill();
        registerBill.setSampleCode(sampleCode.trim());
        List<RegisterBill> list = list(registerBill);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    /**
     * 查询登记单信息
     *
     * @param tradeNo
     * @return
     */
    public RegisterBillOutputDto findByTradeNo(String tradeNo) {
        QualityTraceTradeBill qualityTraceTradeBill = qualityTraceTradeBillService.findByTradeNo(tradeNo);
        if (qualityTraceTradeBill != null && StringUtils.isNotBlank(qualityTraceTradeBill.getRegisterBillCode())) {
            RegisterBill registerBill = new RegisterBill();
            registerBill.setCode(qualityTraceTradeBill.getRegisterBillCode());
            List<RegisterBill> list = this.listByExample(registerBill);
            if (list != null && list.size() > 0) {
                RegisterBillOutputDto target = new RegisterBillOutputDto();
                BeanUtil.copyProperties(list.get(0), target);
                return target;
            }
        }
        return null;
    }


    /**
     * 获得当前登录用户信息
     *
     * @return
     */

    private UserTicket getOptUser() {
        return SessionContext.getSessionContext().getUserTicket();
    }

    /**
     * 保存处理结果
     *
     * @param input
     * @return
     */
    public Long saveHandleResult(RegisterBill input) {
        if (input == null || input.getId() == null || input.getImageCertList() == null
                || StringUtils.isBlank(input.getHandleResult())) {
            throw new TraceBizException("参数错误");
        }
        List<ImageCert> imageCertList = StreamEx.ofNullable(input.getImageCertList()).flatCollection(Function.identity()).nonNull().toList();
        if (imageCertList.isEmpty()) {
            throw new TraceBizException("参数错误");
        }
        if (input.getHandleResult().trim().length() > 1000) {
            throw new TraceBizException("处理结果不能超过1000");
        }
        RegisterBill item = this.get(input.getId());
        if (item == null) {
            throw new TraceBizException("数据错误");
        }

        RegisterBill example = new RegisterBill();
        example.setId(item.getId());
        example.setHandleResult(input.getHandleResult());
        this.updateSelective(example);

        return example.getId();

    }

    /**
     * 保存图片并更新与图片有关的属性
     *
     * @param billId
     * @param imageList
     * @return
     */
    public Long updateHasImage(Long billId, List<ImageCert> imageList,BillTypeEnum billTypeEnum) {
        List<ImageCert> imageCertList = this.imageCertService.insertImageCert(imageList, billId,billTypeEnum);
        Map<Integer, List<ImageCert>> imageCertMap = StreamEx.of(imageCertList).groupingBy(ImageCert::getCertType);
        Integer hasDetectReport = Optional.ofNullable(imageCertMap.get(ImageCertTypeEnum.DETECT_REPORT.getCode())).map(List::size).orElse(0) > 0 ? 1 : 0;
        Integer hasOriginCertify = Optional.ofNullable(imageCertMap.get(ImageCertTypeEnum.ORIGIN_CERTIFIY.getCode())).map(List::size).orElse(0) > 0 ? 1 : 0;
        Integer hasHandleResult = Optional.ofNullable(imageCertMap.get(ImageCertTypeEnum.Handle_Result.getCode())).map(List::size).orElse(0) > 0 ? 1 : 0;
        if(BillTypeEnum.REGISTER_BILL==billTypeEnum){
            RegisterBill item = new RegisterBill();
            item.setId(billId);
            item.setHasDetectReport(hasDetectReport);
            item.setHasOriginCertifiy(hasOriginCertify);
            item.setHasHandleResult(hasHandleResult);
            this.updateSelective(item);
            return billId;
        }
        return billId;

    }


    /**
     * 上传检测报告
     *
     * @param input
     * @return
     */
    public Long doUploadDetectReport(RegisterBill input) {
        if (input == null || input.getId() == null) {
            throw new TraceBizException("参数错误");
        }
        List<ImageCert> imageCertList = StreamEx.ofNullable(input.getImageCertList()).flatCollection(Function.identity()).nonNull().toList();
        if (imageCertList.isEmpty()) {
            throw new TraceBizException("请上传报告");
        }
        RegisterBill item = this.get(input.getId());
        if (item == null) {
            throw new TraceBizException("数据错误");
        }
        if (!BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(item.getVerifyStatus())) {
            throw new TraceBizException("状态错误,不能上传检测报告");
        }

        List<ImageCert> imageCerts = StreamEx.ofNullable(this.imageCertService.findImageCertListByBillId(item.getBillId(),BillTypeEnum.fromCode(item.getBillType()).orElse(null)))
                .flatCollection(Function.identity()).filter(img -> {

                    return !ImageCertTypeEnum.DETECT_REPORT.equalsToCode(img.getCertType());
                }).append(imageCertList).toList();

        this.updateHasImage(item.getBillId(), imageCerts,BillTypeEnum.REGISTER_BILL);
        return item.getId();
    }

    /**
     * 上传产地证明
     *
     * @param input
     * @return
     */
    public Long doUploadOrigincertifiy(RegisterBill input) {
        if (input == null || input.getId() == null) {
            throw new TraceBizException("参数错误");
        }
        List<ImageCert> imageCertList = StreamEx.ofNullable(input.getImageCertList()).flatCollection(Function.identity()).nonNull().toList();

        if (imageCertList.isEmpty()) {
            throw new TraceBizException("请上传报告");
        }
        RegisterBill item = this.get(input.getId());
        if (item == null) {
            throw new TraceBizException("数据错误");
        }

        List<ImageCert> imageCerts = StreamEx.ofNullable(this.imageCertService.findImageCertListByBillId(item.getBillId(),BillTypeEnum.fromCode(item.getBillType()).orElse(null)))
                .flatCollection(Function.identity()).filter(img -> {

                    return !ImageCertTypeEnum.ORIGIN_CERTIFIY.equalsToCode(img.getCertType());
                }).append(imageCertList).toList();

        return item.getBillId();
    }

    /**
     * 查询登记单
     *
     * @param sampleCodeList
     * @return
     */
    public List<RegisterBill> findBySampleCodeList(List<String> sampleCodeList) {

        if (sampleCodeList == null || sampleCodeList.size() == 0) {
            return Collections.emptyList();
        }
        Example example = new Example(RegisterBill.class);
        example.createCriteria().andIn("sampleCode", sampleCodeList);
        return this.getDao().selectByExample(example);

    }

    /**
     * 查询并锁定登记单
     *
     * @param id
     * @return
     */

    public RegisterBill selectByIdForUpdate(Long id) {
        return this.getActualDao().selectByIdForUpdate(id).orElseThrow(() -> {
            return new TraceBizException("操作登记单失败");
        });
    }
}