package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BasePage;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.IDTO;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.SeparateSalesRecord;
import com.dili.trace.dto.ECommerceBillPrintOutput;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.glossary.RegisterBilCreationSourceEnum;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.glossary.SampleSourceEnum;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import com.dili.trace.util.MarketUtil;
import one.util.streamex.StreamEx;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * 电商报备
 */
@Service
public class ECommerceBillService {
    private static final Logger logger = LoggerFactory.getLogger(ECommerceBillService.class);

    @Autowired
    CodeGenerateService codeGenerateService;
    @Autowired
    BillService billService;
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    QrCodeService qrCodeService;
    @Value("${current.baseWebPath}")
    private String baseWebPath;
    @Autowired
    DetectRequestService detectRequestService;
    @Resource
    RegisterBillMapper billMapper;
    @Autowired
    UidRestfulRpcService uidRestfulRpcService;
    @Autowired
    SyncUserInfoService syncUserInfoService;
    @Autowired
    SyncCategoryService syncCategoryService;


    /**
     * 查询
     *
     * @param query
     * @return
     * @throws Exception
     */
    public String listPage(RegisterBillDto query) throws Exception {
        RegisterBillDto dto = this.preBuildDTO(query);
        dto.setMarketId(MarketUtil.returnMarket());
        dto.setBillType(this.supportedBillType().getCode());
        dto.setIsDeleted(YesOrNoEnum.NO.getCode());
        BasePage<RegisterBillDto> page = this.billService.buildQuery(dto).listPageByFun(q -> this.billMapper.queryListByExample(q));

        Map<Long, DetectRequest> idAndDetectRquestMap = this.detectRequestService.findDetectRequestByIdList(StreamEx.of(page.getDatas()).map(RegisterBill::getDetectRequestId).toList());
        //检测值
        // Map<String, DetectRecord> recordMap = detectRecordService.findMapRegisterBillByIds(StreamEx.of(list).map(RegisterBill::getLatestDetectRecordId).toList());
        StreamEx.of(page.getDatas()).forEach(rb -> {
            rb.setDetectRequest(idAndDetectRquestMap.get(rb.getDetectRequestId()));
        });
        List results = ValueProviderUtils.buildDataByProvider(query, page.getDatas());
        EasyuiPageOutput out = new EasyuiPageOutput(page.getTotalItem(), results);
        return out.toString();
    }

    /**
     * 构造查询
     *
     * @param dto
     * @return
     */
    private RegisterBillDto preBuildDTO(RegisterBillDto dto) {
        String attr = StringUtils.trimToEmpty(dto.getAttr());
        String attrValue = dto.getAttrValue();
        if (attrValue != null && (StringUtils.isNotBlank(attrValue))) {
            switch (attr) {
                case "code":
                    dto.setCode(attrValue);
                    break;
                case "latestDetectOperator":
                    dto.setLatestDetectOperator(attrValue);
                    break;
                case "name":
                    dto.setName(attrValue);
                    break;
                case "likeSampleCode":
                    dto.setLikeSampleCode(attrValue);
                    break;
            }
        }

        StringBuilder sql = new StringBuilder();
        Boolean hasCheckSheet = dto.getHasCheckSheet();
        if (hasCheckSheet != null) {
            if (sql.length() > 0) {
                sql.append(" AND ");
            }
            if (hasCheckSheet) {
                sql.append("  (check_sheet_id is not null) ");
            } else {
                sql.append("  (check_sheet_id is null) ");
            }
        }
        if (sql.length() > 0) {
            dto.setMetadata(IDTO.AND_CONDITION_EXPR, sql.toString());
        }

        return dto;
    }

    /**
     * 支持的类型
     *
     * @return
     */
    public BillTypeEnum supportedBillType() {
        return BillTypeEnum.E_COMMERCE_BILL;
    }

    /**
     * 查找 最新操作数据
     *
     * @param input
     * @param operatorUser
     * @return
     * @throws Exception
     */
    public RegisterBill findHighLightEcommerceBill(RegisterBillDto input, OperatorUser operatorUser) throws Exception {
        RegisterBillDto dto = new RegisterBillDto();
        dto.setOperatorId(operatorUser.getId());
//		dto.setState(RegisterBillStateEnum.ALREADY_CHECK.getCode());
        dto.setDetectStatus(DetectStatusEnum.FINISH_DETECT.getCode());
        dto.setBillType(this.supportedBillType().getCode());
        dto.setRows(1);
        dto.setSort("code");
        dto.setOrder("desc");
        return this.billService.listByExample(dto).stream().findFirst().orElse(new RegisterBill());
    }

    /**
     * 审核
     *
     * @param inputBill
     * @param operatorUser
     * @return
     */
    @Transactional
    public Long doAuditEcommerceBill(RegisterBill inputBill, OperatorUser operatorUser) {

        RegisterBill item = this.billService.get(inputBill.getId());
        if (item == null) {
            throw new TraceBizException("没有找到数据，可能已经被删除");
        }

        if (!this.supportedBillType().equalsToCode(item.getBillType())) {
            throw new TraceBizException("数据错误,登记单类型错误");
        }
        if (!BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(item.getVerifyStatus())) {
            throw new TraceBizException("登记单状态错误");
        }

        RegisterBill updatable = new RegisterBill();
        updatable.setModified(new Date());
        updatable.setId(item.getId());
        updatable.setOperatorId(operatorUser.getId());
        updatable.setOperatorName(operatorUser.getName());


        if (BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(item.getVerifyStatus())) {

            //审核时没有上传图片
			    /*List<ImageCert>imageCertList=StreamEx.ofNullable(inputBill.getImageCerts()).flatCollection(Function.identity()).nonNull().toList();

				if (imageCertList.isEmpty()) {
					throw new TraceBizException("参数错误");
				}
				this.billService.updateHasImage(item.getId(),imageCertList);
				this.billService.updateHasImage(item.getId(),imageCertList);*/
            updatable.setDetectStatus(DetectStatusEnum.WAIT_DETECT.getCode());
            updatable.setLatestDetectOperator(operatorUser.getName());
            updatable.setLatestDetectTime(new Date());
            updatable.setLatestPdResult("100%");

            DetectRequest detectRequest = this.detectRequestService.createDefault(item.getId(), Optional.ofNullable(operatorUser));
            // 维护检测编号
            detectRequest.setDetectCode(uidRestfulRpcService.detectRequestBizNumber(operatorUser.getMarketName()));
            this.detectRequestService.updateSelective(detectRequest);


        } else if (DetectStatusEnum.NONE.equalsToCode(inputBill.getDetectStatus())) {
            updatable.setDetectStatus(DetectStatusEnum.WAIT_DETECT.getCode());
            DetectRequest detectRequest = this.detectRequestService.createDefault(item.getId(), Optional.ofNullable(operatorUser));
            detectRequest.setDetectSource(SampleSourceEnum.AUTO_CHECK.getCode());
            detectRequest.setDetectType(DetectTypeEnum.INITIAL_CHECK.getCode());
            detectRequest.setDetectResult(DetectResultEnum.NONE.getCode());
            // 维护检测编号
            detectRequest.setDetectCode(uidRestfulRpcService.detectRequestBizNumber(operatorUser.getMarketName()));
            this.detectRequestService.updateSelective(detectRequest);
        } else {
            throw new TraceBizException("参数错误");
        }
        updatable.setSampleCode(this.codeGenerateService.nextECommerceBillSampleCode());

        this.billService.updateSelective(updatable);

        return updatable.getId();
    }

    /**
     * 删除电商登记单
     *
     * @param inputBill
     * @param operatorUser
     * @return
     */
    @Transactional
    public Long doDeleteEcommerceBill(RegisterBill inputBill, OperatorUser operatorUser) {
        if (inputBill == null || inputBill.getId() == null) {

            throw new TraceBizException("参数错误");
        }
        RegisterBill item = this.billService.get(inputBill.getId());
        if (item == null) {
            throw new TraceBizException("没有找到数据，可能已经被删除");
        }
        if (!this.supportedBillType().equalsToCode(item.getBillType())) {
            throw new TraceBizException("数据错误,登记单类型错误");
        }

        if (!BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(item.getVerifyStatus())) {
            throw new TraceBizException("登记单状态错误");
        }

        this.billService.delete(item.getId());

        return item.getId();
    }

    /**
     * 查询详情
     *
     * @param billId
     * @return
     */
    public RegisterBillOutputDto detailEcommerceBill(Long billId) {

        RegisterBill query = new RegisterBill();
        query.setId(billId);
        query.setBillType(this.supportedBillType().getCode());
        RegisterBillOutputDto data = this.billService.listByExample(query).stream().findFirst().map(bill -> {
            RegisterBillOutputDto output = new RegisterBillOutputDto();
            try {
                BeanUtils.copyProperties(output, bill);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
                throw new TraceBizException("服务器出错，请重试");
            }

            return output;

        }).orElseThrow(() -> {
            return new TraceBizException("数据不存在");
        });
        List<SeparateSalesRecord> separateSalesRecordList = this.separateSalesRecordService
                .findByRegisterBillCode(data.getCode());
        data.setSeparateSalesRecords(separateSalesRecordList);
        return data;
    }

    /**
     * 创建登记单
     *
     * @param registerBill
     * @return
     */
    @Transactional
    public Long createECommerceBill(RegisterBill registerBill, List<SeparateSalesRecord> separateInputList,
                                    OperatorUser operatorUser) {

        if (registerBill == null) {
            throw new TraceBizException("参数错误");
        }
        Long billId = this.createBill(registerBill, operatorUser);

        // 创建检测请求
        DetectRequest item = this.detectRequestService.createDefault(billId, Optional.ofNullable(operatorUser));
        DetectRequest detectRequest = new DetectRequest();
        detectRequest.setId(item.getId());
        detectRequest.setDetectType(DetectTypeEnum.INITIAL_CHECK.getCode());
        detectRequest.setDetectSource(SampleSourceEnum.AUTO_CHECK.getCode());
        detectRequest.setDetectResult(DetectResultEnum.NONE.getCode());
        this.detectRequestService.updateSelective(detectRequest);

        //patch bill单上的requestId
        updateBillDetectRequestId(registerBill.getId(), item.getId());

        if (CollectionUtils.isNotEmpty(separateInputList)) {
            BigDecimal totalSalesWeight = StreamEx.of(CollectionUtils.emptyIfNull(separateInputList)).nonNull().filter(r -> {
                return !StringUtils.isAllBlank(r.getTallyAreaNo(), r.getSalesUserName(), r.getSalesPlate());
            }).map(r -> {
                if (r.getSalesWeight() == null || r.getSalesWeight().compareTo(BigDecimal.ZERO) < 0) {
                    throw new TraceBizException("分销重量不能小于零");
                }
                return r;
            }).map(SeparateSalesRecord::getSalesWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalSalesWeight.compareTo(registerBill.getWeight()) > 0) {
                throw new TraceBizException("分销重量不能超过总重量");
            }
            this.createSeparateSalesRecord(registerBill.getCode(), separateInputList);
        }
        return billId;

    }

    /**
     * 创建检测请求单后与委托单进行关联
     *
     * @param billId
     * @param detectRequestId
     */
    private void updateBillDetectRequestId(Long billId, Long detectRequestId) {
        RegisterBill upBill = new RegisterBill();
        upBill.setId(billId);
        upBill.setDetectRequestId(detectRequestId);
        this.billService.updateSelective(upBill);
    }

    /**
     * 查询并返回登记单可打印不干胶信息
     *
     * @param billId
     * @return
     */
    public ECommerceBillPrintOutput prePrint(Long billId) {

        if (billId == null) {
            throw new TraceBizException("参数错误");
        }
        RegisterBill bill = this.billService.get(billId);
        if (bill == null) {
            throw new TraceBizException("数据不存在");
        }
        if (!this.supportedBillType().getCode().equals(bill.getBillType())) {
            throw new TraceBizException("登记单类型错误");
        }
        String content = this.baseWebPath + "/ecommerceBill/billQRDetail.html?id=" + billId;

        try {
            InputStream logoFileInputStream = new ClassPathResource("static/resources/qrlogo/logo.png")
                    .getInputStream();
            String base64Qrcode = this.qrCodeService.getBase64QrCode(content, logoFileInputStream, 174, 174);
            ECommerceBillPrintOutput out = ECommerceBillPrintOutput.build(bill, base64Qrcode);

            return out;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new TraceBizException("生成二维码出错");
        }

    }

    /**
     * 创建分销单
     *
     * @param registerBillCode
     * @param separateDtoList
     * @return
     */
    private List<SeparateSalesRecord> createSeparateSalesRecord(String registerBillCode,
                                                                List<SeparateSalesRecord> separateDtoList) {
        return StreamEx.of(CollectionUtils.emptyIfNull(separateDtoList)).map(separateSalesRecord -> {
            separateSalesRecord.setId(null);
            separateSalesRecord.setRegisterBillCode(registerBillCode);
            separateSalesRecord.setCreated(new Date());
            separateSalesRecord.setModified(new Date());
            separateSalesRecord.setSalesCityId(0L);
            separateSalesRecord.setSalesCityName("");
//			separateSalesRecord.setSalesPlate("");
//			separateSalesRecord.setSalesUserId(0L);
//			separateSalesRecord.setSalesUserName("");
//			separateSalesRecord.setSalesWeight(0);
//			separateSalesRecord.setTradeNo("");
            this.separateSalesRecordService.insertSelective(separateSalesRecord);
            return separateSalesRecord;

        }).toList();

    }

    /**
     * 创建电商登记单
     *
     * @param bill
     * @param operatorUser
     * @return
     */
    // 创建电商登记单
    private Long createBill(RegisterBill bill, OperatorUser operatorUser) {
        if (bill.getWeight() == null || bill.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TraceBizException("总重量不能小于零");
        }

        bill.setRegisterSource(RegisterSourceEnum.OTHERS.getCode());
        if (!RegisterBilCreationSourceEnum.fromCode(bill.getCreationSource()).isPresent()) {
            throw new TraceBizException("登记单来源类型错误");
        }
        String code = uidRestfulRpcService.bizNumber(BizNumberType.ECOMMERCE_BILL);
        logger.debug("ECommerceBill.code={}", code);
        bill.setCode(code);
        List<ImageCert> imageCertList = StreamEx.ofNullable(bill.getImageCertList()).flatCollection(Function.identity()).nonNull().filter(img -> {
            return img.getCertType() != null && StringUtils.isNotBlank(img.getUid());
        }).toList();
        bill.setImageCertList(imageCertList);
//		bill.setState(RegisterBillStateEnum.WAIT_AUDIT.getCode());
        bill.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
        bill.setDetectStatus(DetectStatusEnum.NONE.getCode());
        bill.setCreated(new Date());
        bill.setModified(new Date());
        bill.setBillType(this.supportedBillType().getCode());
        bill.setRegistType(RegistTypeEnum.NONE.getCode());

        bill.setOperatorId(operatorUser.getId());
        bill.setOperatorName(operatorUser.getName());
        bill.setPlate(StringUtils.trimToEmpty(bill.getPlate()));
        bill.setHasHandleResult(YesOrNoEnum.NO.getCode());
        bill.setHasDetectReport(YesOrNoEnum.NO.getCode());
        bill.setHasOriginCertifiy(YesOrNoEnum.NO.getCode());

        bill.setMarketId(MarketUtil.returnMarket());
        this.billService.insertSelective(bill);
        this.billService.updateHasImage(bill.getBillId(), bill.getImageCertList());
        //同步uap商品、经营户
        this.syncCategoryService.saveAndSyncGoodInfo(bill.getProductId(), bill.getMarketId());
        this.syncUserInfoService.saveAndSyncUserInfo(bill.getUserId(), bill.getMarketId());
        return bill.getId();
    }

}
