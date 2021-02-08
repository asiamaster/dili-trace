package com.dili.trace.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.ss.dto.IDTO;
import com.dili.ss.util.DateUtils;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.api.input.RegisterBillApiInputDto;
import com.dili.trace.api.input.UserQueryDto;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.domain.*;
import com.dili.trace.dto.*;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.glossary.UserQrStatusEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import com.dili.trace.util.NumUtils;
import com.dili.trace.util.RegUtils;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:34.
 */
@Service
@Transactional
public class RegisterBillServiceImpl extends BaseServiceImpl<RegisterBill, Long> implements RegisterBillService {
    private static final Logger logger = LoggerFactory.getLogger(RegisterBillServiceImpl.class);
    @Autowired
    com.dili.trace.rpc.service.UidRestfulRpcService uidRestfulRpcService;
    @Autowired
    ImageCertService imageCertService;
    @Autowired
    UserPlateService userPlateService;
    @Autowired
    UsualAddressService usualAddressService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    BrandService brandService;
    @Autowired
    RegisterBillHistoryService registerBillHistoryService;
    @Autowired
    UserService userService;
    @Autowired
    TradeService tradeService;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    CheckinOutRecordService checkinOutRecordService;
    @Autowired
    UserQrHistoryService userQrHistoryService;
    @Autowired
    MessageService messageService;
    @Autowired
    TradeRequestService tradeRequestService;

    @Autowired
    RegisterHeadService registerHeadService;
    @Autowired
    CustomerRpcService clientRpcService;
    @Autowired
    BillService billService;
    @Autowired
    ProcessService processService;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    BillVerifyHistoryService billVerifyHistoryService;
    @Autowired
    SyncUserInfoService syncUserInfoService;
    @Autowired
    SyncCategoryService syncCategoryService;

    public RegisterBillMapper getActualDao() {
        return (RegisterBillMapper) getDao();
    }

    @Transactional
    @Override
    public Optional<RegisterBill> selectByIdForUpdate(Long id) {
        return this.getActualDao().selectByIdForUpdate(id).map(billItem -> {
            if (YesOrNoEnum.YES.getCode().equals(billItem.getIsDeleted())) {
                throw new TraceBizException("登记单已经被删除");
            }
            return billItem;
        });
    }

    @Transactional
    @Override
    public Optional<RegisterBill> getAndCheckById(Long billId) {
        if (billId == null) {
            return Optional.empty();
        }
        RegisterBill billItem = this.get(billId);
        if (billItem == null) {
            return Optional.empty();
        }
        if (YesOrNoEnum.YES.getCode().equals(billItem.getIsDeleted())) {
            throw new TraceBizException("登记单已经被删除");
        }
        return Optional.of(billItem);
    }

    @Override
    public List<Long> createBillList(List<CreateRegisterBillInputDto> registerBills, Long customerId,
                                     Optional<OperatorUser> operatorUser, CreatorRoleEnum creatorRoleEnum) {


        return StreamEx.of(registerBills).nonNull().map(dto -> {
            logger.info("循环保存登记单:" + JSON.toJSONString(dto));
            CustomerExtendDto user = this.clientRpcService.findApprovedCustomerByIdOrEx(customerId, dto.getMarketId());
            RegisterBill registerBill = dto.build(user, dto.getMarketId());
            if (StringUtils.isNotBlank(registerBill.getPlate())) {
                if (!RegUtils.isPlate(registerBill.getPlate().trim())) {
                    throw new TraceBizException("车牌格式错误");
                }
            }

            registerBill.setCreatorRole(creatorRoleEnum.getCode());

            CustomerExtendDto customer = this.clientRpcService.findApprovedCustomerByIdOrEx(registerBill.getUserId(), dto.getMarketId());

            Customer cq = new Customer();
            cq.setCustomerId(customer.getCode());
            this.clientRpcService.findCustomer(cq, dto.getMarketId()).ifPresent(card -> {
                registerBill.setThirdPartyCode(card.getPrintingCard());
            });
            registerBill.setImageCertList(dto.getImageCertList());
            Long billId = this.createRegisterBill(registerBill, operatorUser);

            // 寿光管理端，新增完报备单的同时新增检测请求
//            OperatorUser oprUser = operatorUser.orElseThrow(() -> {
//                return new TraceBizException("用户未登录");
//            });

//            // 创建检测请求
//            DetectRequest item = this.detectRequestService.createByBillId(billId, DetectTypeEnum.NEW, new IdNameDto(oprUser.getId(),oprUser.getName()), Optional.empty());
//
//            // 如果管理创建登记单，更新检测状态和检测编号
//            if (CreatorRoleEnum.MANAGER.equalsToCode(creatorRoleEnum.getCode())) {
//                DetectRequest updatable = new DetectRequest();
//                updatable.setId(item.getId());
//                // 维护接单时间
//                updatable.setConfirmTime(new Date());
//                // 维护检测编号
//                updatable.setDetectCode(uidRestfulRpcService.detectRequestBizNumber(operatorUser.get().getMarketName()));
//                this.detectRequestService.updateSelective(updatable);
//
//                RegisterBill bill = this.billService.get(item.getBillId());
//                bill.setOperatorName(oprUser.getName());
//                bill.setOperatorId(oprUser.getId());
//                bill.setDetectStatus(DetectStatusEnum.NONE.getCode()); // 新增完为：待采样
//                this.billService.update(bill);
//            }

            return billId;
        }).toList();
    }

    @Transactional
    @Override
    public Long createRegisterBill(RegisterBill registerBill,
                                   Optional<OperatorUser> operatorUser) {
        this.checkBill(registerBill);

        registerBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        registerBill.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
        registerBill.setVerifyType(VerifyTypeEnum.NONE.getCode());
//        registerBill.setState(RegisterBillStateEnum.NEW.getCode());
        registerBill.setDetectStatus(DetectStatusEnum.NONE.getCode());
        String code = uidRestfulRpcService.bizNumber(BizNumberType.REGISTER_BILL);
        logger.debug("registerBill.code={}", code);
        registerBill.setCode(code);
        registerBill.setVersion(1);
        registerBill.setCreated(new Date());
//        registerBill.setIsCheckin(YesOrNoEnum.NO.getCode());
        registerBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        registerBill.setIsDeleted(YesOrNoEnum.NO.getCode());
        registerBill.setIsPrintCheckSheet(YesOrNoEnum.NO.getCode());
        operatorUser.ifPresent(op -> {
            registerBill.setOperatorName(op.getName());
            registerBill.setOperatorId(op.getId());
            registerBill.setOperationTime(new Date());
        });
        registerBill.setIdCardNo(StringUtils.trimToEmpty(registerBill.getIdCardNo()).toUpperCase());
        // 车牌转大写
        String plate = StreamEx.ofNullable(registerBill.getPlate()).nonNull().map(StringUtils::trimToNull).nonNull()
                .map(String::toUpperCase).findFirst().orElse(null);
        registerBill.setPlate(plate);
        registerBill.setModified(new Date());
//        registerBill.setOrderType(OrderTypeEnum.REGISTER_BILL.getCode());
        registerBill.setRegisterSource(RegisterSourceEnum.TALLY_AREA.getCode());

        // 补单直接进门状态
        if (RegistTypeEnum.SUPPLEMENT.equalsToCode(registerBill.getRegistType())) {
//            registerBill.setIsCheckin(YesOrNoEnum.YES.getCode());
            registerBill.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        } else {
//            registerBill.setIsCheckin(YesOrNoEnum.NO.getCode());
            registerBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        }

        // 保存车牌
//        this.userPlateService.checkAndInsertUserPlate(registerBill.getUserId(), plate);

        // 重构版本建单不允许没有市场
        if (Objects.isNull(registerBill.getMarketId())) {
            logger.error("登记单市场不存在！" + JSON.toJSONString(registerBill));
            throw new TraceBizException("登记单市场不存在");
        }
        if (registerBill.getPreserveType() == null) {
            registerBill.setPreserveType(PreserveTypeEnum.NONE.getCode());
        }
        if (registerBill.getTruckTareWeight() == null) {
            registerBill.setTruckTareWeight(BigDecimal.ZERO);
        }
        if (registerBill.getUnitPrice() == null) {
            registerBill.setUnitPrice(BigDecimal.ZERO);
        }
        logger.debug("判断是否是分批进场:registType={}", registerBill.getRegistType());
        //更新主台账单剩余重量
        if (RegistTypeEnum.PARTIAL.getCode().equals(registerBill.getRegistType())) {
            RegisterHead registerHead = new RegisterHead();
            registerHead.setCode(registerBill.getRegisterHeadCode());
            List<RegisterHead> registerHeadList = registerHeadService.listByExample(registerHead);
            if (CollectionUtils.isNotEmpty(registerHeadList)) {
                registerHead = registerHeadList.get(0);
            } else {
                throw new TraceBizException("未找到主台账单");
            }

            //主台账单的剩余重量小于进门登记单的总重量时给出提示
            BigDecimal remianWeight = registerHead.getRemainWeight();
            BigDecimal billWeight = registerBill.getWeight();

            logger.debug("remianWeight={},billWeight={}", remianWeight, billWeight);

            if (remianWeight == null || (remianWeight != null && remianWeight.compareTo(billWeight) == -1)) {
                throw new TraceBizException("进门登记单的总重量大于主台账单的剩余重量，不可新增");
            }

            registerHead.setRemainWeight(remianWeight.subtract(billWeight));
            registerHeadService.updateSelective(registerHead);
        }
        // 保存报备单
        int result = super.saveOrUpdate(registerBill);
        if (result == 0) {
            logger.error("新增登记单数据库执行失败" + JSON.toJSONString(registerBill));
            throw new TraceBizException("创建失败");
        }
        // 创建审核历史数据
        this.registerBillHistoryService.createHistory(registerBill.getBillId());
        // 保存图片
        List<ImageCert> imageCertList = StreamEx.ofNullable(registerBill.getImageCertList()).nonNull().flatCollection(Function.identity()).nonNull().toList();
        if (!imageCertList.isEmpty()) {
            imageCertService.insertImageCert(imageCertList, registerBill.getBillId());
            //更新报备单上图片标志位
            this.billService.updateHasImage(registerBill.getBillId(), imageCertList);
        }


        // 创建/更新品牌信息并更新brandId字段值
        this.brandService.createOrUpdateBrand(registerBill.getBrandName(), registerBill.getUserId(), registerBill.getMarketId())
                .ifPresent(brandId -> {
                    RegisterBill bill = new RegisterBill();
                    bill.setBrandId(brandId);
                    bill.setId(registerBill.getId());
                    this.updateSelective(bill);
                });
        this.updateUserQrStatusByUserId(registerBill.getBillId(), registerBill.getUserId());

        //报备单新增消息
        Integer businessType = MessageStateEnum.BUSINESS_TYPE_BILL.getCode();
        if (RegistTypeEnum.SUPPLEMENT.getCode().equals(registerBill.getRegistType())) {
            businessType = MessageStateEnum.BUSINESS_TYPE_FIELD_BILL.getCode();
        }
        addMessage(registerBill, MessageTypeEnum.BILLSUBMIT.getCode(), businessType, MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_MANAGER.getCode(), registerBill.getMarketId());
        //同步uap商品、经营户
        this.syncCategoryService.saveAndSyncGoodInfo(registerBill.getProductId(), registerBill.getMarketId());
        this.syncUserInfoService.saveAndSyncUserInfo(registerBill.getUserId(), registerBill.getMarketId());
        return registerBill.getId();
    }

    /**
     * 检查用户输入参数
     *
     * @param registerBill
     * @return
     */
    private BaseOutput checkBill(RegisterBill registerBill) {

        if (!BillTypeEnum.fromCode(registerBill.getBillType()).isPresent()) {
            throw new TraceBizException("单据类型错误");
        }
        if (!TruckTypeEnum.fromCode(registerBill.getTruckType()).isPresent()) {
            throw new TraceBizException("装车类型错误");
        }
//        if (registerBill.getUpStreamId() == null) {
//            throw new TraceBizException("上游企业不能为空");
//        }
        if (TruckTypeEnum.POOL.equalsToCode(registerBill.getTruckType())) {
            if (StringUtils.isBlank(registerBill.getPlate())) {
                throw new TraceBizException("车牌不能为空");
            }
        }
//        if (!OrderTypeEnum.REGISTER_FORM_BILL.getCode().equals(registerBill.getOrderType())) {
//            if (!PreserveTypeEnum.fromCode(registerBill.getPreserveType()).isPresent()) {
//                throw new TraceBusinessException("商品类型错误");
//            }
//        }
        if (StringUtils.isBlank(registerBill.getName())) {
            logger.error("业户姓名不能为空");
            throw new TraceBizException("业户姓名不能为空");
        }
        if (registerBill.getUserId() == null) {
            logger.error("业户ID不能为空");
            throw new TraceBizException("业户ID不能为空");
        }

        if (StringUtils.isBlank(registerBill.getProductName()) || registerBill.getProductId() == null) {
            logger.error("商品名称不能为空");
            throw new TraceBizException("商品名称不能为空");
        }
        if (StringUtils.isBlank(registerBill.getOriginName()) || registerBill.getOriginId() == null) {
            logger.error("商品产地不能为空");
            throw new TraceBizException("商品产地不能为空");
        }

        if (registerBill.getWeight() == null) {
            logger.error("商品重量不能为空");
            throw new TraceBizException("商品重量不能为空");
        }

        if (BigDecimal.ZERO.compareTo(registerBill.getWeight()) >= 0) {
            logger.error("商品重量不能小于0");
            throw new TraceBizException("商品重量不能小于0");
        }

        if (NumUtils.MAX_WEIGHT.compareTo(registerBill.getWeight()) < 0) {
            logger.error("商品重量不能大于" + NumUtils.MAX_WEIGHT.toString());
            throw new TraceBizException("商品重量不能大于" + NumUtils.MAX_WEIGHT.toString());
        }

        if (!NumUtils.isIntegerValue(registerBill.getWeight())) {
            logger.error("商品重量必须为整数");
            throw new TraceBizException("商品重量必须为整数");
        }

        // 计重类型，把件数和件重置空
        if (MeasureTypeEnum.COUNT_WEIGHT.equalsCode(registerBill.getMeasureType())) {
            registerBill.setPieceNum(null);
            registerBill.setPieceWeight(null);
        }

        // 计件类型，校验件数和件重
        if (MeasureTypeEnum.COUNT_UNIT.equalsCode(registerBill.getMeasureType())) {
            // 件数
            if (registerBill.getPieceNum() == null) {
                logger.error("商品件数不能为空");
                throw new TraceBizException("商品件数不能为空");
            }
            if (BigDecimal.ZERO.compareTo(registerBill.getPieceNum()) >= 0) {
                logger.error("商品件数不能小于0");
                throw new TraceBizException("商品件数不能小于0");
            }
            if (NumUtils.MAX_NUM.compareTo(registerBill.getPieceNum()) < 0) {
                logger.error("商品件数不能大于{}", NumUtils.MAX_NUM.toString());
                throw new TraceBizException("商品件数不能大于" + NumUtils.MAX_NUM.toString());
            }
            if (!NumUtils.isIntegerValue(registerBill.getPieceNum())) {
                logger.error("商品件数必须为整数");
                throw new TraceBizException("商品件数必须为整数");
            }

            // 件重
            if (registerBill.getPieceWeight() == null) {
                logger.error("商品件重不能为空");
                throw new TraceBizException("商品件重不能为空");
            }
            if (BigDecimal.ZERO.compareTo(registerBill.getPieceWeight()) >= 0) {
                logger.error("商品件重不能小于0");
                throw new TraceBizException("商品件重不能小于0");
            }
            if (NumUtils.MAX_WEIGHT.compareTo(registerBill.getPieceWeight()) < 0) {
                logger.error("商品件重不能大于{}", NumUtils.MAX_WEIGHT.toString());
                throw new TraceBizException("商品件重不能大于" + NumUtils.MAX_WEIGHT.toString());
            }
            if (!NumUtils.isIntegerValue(registerBill.getPieceWeight())) {
                logger.error("商品件重必须为整数");
                throw new TraceBizException("商品件重必须为整数");
            }
        }

        if (registerBill.getTruckTareWeight() != null) {
            if (NumUtils.MAX_WEIGHT.compareTo(registerBill.getTruckTareWeight()) < 0) {
                logger.error("车辆皮重不能大于" + NumUtils.MAX_WEIGHT.toString());
                throw new TraceBizException("车辆皮重不能大于" + NumUtils.MAX_WEIGHT.toString());
            }

            if (!NumUtils.isIntegerValue(registerBill.getTruckTareWeight())) {
                logger.error("车辆皮重必须为整数");
                throw new TraceBizException("车辆皮重必须为整数");
            }
        }


        if (Objects.nonNull(registerBill.getUnitPrice())
                && NumUtils.MAX_UNIT_PRICE.compareTo(registerBill.getUnitPrice()) < 0) {
            logger.error("商品单价不能大于" + NumUtils.MAX_UNIT_PRICE.toString());
            throw new TraceBizException("商品单价不能大于" + NumUtils.MAX_UNIT_PRICE.toString());
        }

        if (registerBill.getWeightUnit() == null) {
            logger.error("重量单位不能为空");
            throw new TraceBizException("重量单位不能为空");
        }

        if (registerBill.getMarketId() == null) {
            throw new TraceBizException("市场不能为空");
        }
        String specName = registerBill.getSpecName();
        if (StringUtils.isNotBlank(specName) && !RegUtils.isValidInput(specName)) {
            throw new TraceBizException("规格名称包含非法字符");
        }
        String remark = registerBill.getRemark();
        if (StringUtils.isNotBlank(remark) && !RegUtils.isValidInput(remark)) {
            throw new TraceBizException("备注包含非法字符");
        }
        return BaseOutput.success();
    }

    @Override
    public RegisterBill findByCode(String code) {
        RegisterBill registerBill = new RegisterBill();
        registerBill.setCode(code);
        List<RegisterBill> list = list(registerBill);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private UserTicket getOptUser() {
        return SessionContext.getSessionContext().getUserTicket();
    }

    @Transactional
    @Override
    public Long doEdit(RegisterBill input, List<ImageCert> imageCertList, Optional<OperatorUser> operatorUser) {
        if (input == null || input.getId() == null) {
            throw new TraceBizException("参数错误");
        }
        RegisterBill billItem = this.getAndCheckById(input.getId())
                .orElseThrow(() -> new TraceBizException("数据不存在"));

        if (BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(billItem.getVerifyStatus())
                || BillVerifyStatusEnum.RETURNED.equalsToCode(billItem.getVerifyStatus())) {
            // 待审核，或者已退回状态可以进行数据修改
        } else {
            throw new TraceBizException("当前审核状态不能修改数据");
        }
        if (!DetectStatusEnum.NONE.equalsToCode(billItem.getDetectStatus())) {
            throw new TraceBizException("当前检测状态不能修改数据");
        }

        TradeDetail tradeDetailItem = this.tradeDetailService.findBilledTradeDetailByBillId(billItem.getBillId())
                .orElse(null);
        // 车牌转大写
        String plate = StreamEx.ofNullable(input.getPlate()).filter(StringUtils::isNotBlank).map(p -> p.toUpperCase())
                .findFirst().orElse(null);
        input.setPlate(plate);
        // 保存车牌
//        this.userPlateService.checkAndInsertUserPlate(input.getUserId(), plate);
        input.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
        input.setModified(new Date());

        input.setOperatorName(null);
        input.setOperatorId(null);
        input.setOperationTime(null);
        input.setReason("");
        operatorUser.ifPresent(op -> {
            input.setOperatorName(op.getName());
            input.setOperatorId(op.getId());
            input.setOperationTime(new Date());
        });
        if (tradeDetailItem == null) {
            // 补单直接进门状态
            if (RegistTypeEnum.SUPPLEMENT.equalsToCode(input.getRegistType())) {
//                input.setIsCheckin(YesOrNoEnum.YES.getCode());
                input.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
            } else {
//                input.setIsCheckin(YesOrNoEnum.NO.getCode());
                input.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
            }
        } else if (CheckinStatusEnum.ALLOWED.equalsToCode(tradeDetailItem.getCheckinStatus())) {
//            input.setIsCheckin(YesOrNoEnum.YES.getCode());
            input.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        } else {
//            input.setIsCheckin(YesOrNoEnum.NO.getCode());
            input.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        }
        input.setCreated(new Date());
        this.updateSelective(input);
        this.registerBillHistoryService.createHistory(billItem.getBillId());

        imageCertList = StreamEx.ofNullable(imageCertList).nonNull().flatCollection(Function.identity()).nonNull().toList();
        if (!imageCertList.isEmpty()) {
            // 保存图片
            this.billService.updateHasImage(input.getId(), imageCertList);
        }

        // imageCertService.insertImageCert(imageCertList, input.getId());


        this.tradeDetailService.findBilledTradeDetailByBillId(billItem.getBillId()).ifPresent(td -> {
            TradeDetail updatableRecord = new TradeDetail();
            updatableRecord.setId(td.getId());
            updatableRecord.setModified(new Date());
            updatableRecord.setStockWeight(input.getWeight());
            updatableRecord.setTotalWeight(input.getWeight());
            this.tradeDetailService.updateSelective(updatableRecord);
        });

        this.brandService.createOrUpdateBrand(input.getBrandName(), billItem.getUserId(), input.getMarketId());
        this.updateUserQrStatusByUserId(billItem.getBillId(), billItem.getUserId());
        return input.getId();
    }

    @Transactional
    @Override
    public Long doDelete(Long billId, Long userId, Optional<OperatorUser> operatorUser) {
        if (billId == null || userId == null) {
            throw new TraceBizException("参数错误");
        }
        RegisterBill billItem = this.getAndCheckById(billId).orElseThrow(() -> new TraceBizException("数据不存在"));
        if (!userId.equals(billItem.getUserId())) {
            throw new TraceBizException("没有权限删除数据");
        }
        if (CheckinStatusEnum.ALLOWED.equalsToCode(billItem.getCheckinStatus())) {//YesOrNoEnum.YES.getCode().equals(billItem.getIsCheckin())) {
            throw new TraceBizException("不能删除已进门数据");
        }
        if (BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(billItem.getVerifyStatus()) && DetectStatusEnum.NONE.equalsToCode(billItem.getDetectStatus())) {
            //dothing
        } else {
            throw new TraceBizException("不能删除当前状态数据");
        }
        if (billItem.getDetectRequestId() != null) {
            throw new TraceBizException("不能删除已发起检测的登记单");
        }
        RegisterBill bill = new RegisterBill();
        bill.setId(billItem.getBillId());
        bill.setIsDeleted(YesOrNoEnum.YES.getCode());

        operatorUser.ifPresent(op -> {
            bill.setOperatorName(op.getName());
            bill.setOperatorId(op.getId());
            bill.setOperationTime(new Date());
            bill.setDeleteUser(op.getName());
            bill.setDeleteTime(new Date());
        });
        this.updateSelective(bill);
        this.registerBillHistoryService.createHistory(billItem.getBillId());
        this.userQrHistoryService.rollbackUserQrStatus(bill.getId(), billItem.getUserId());
        return billId;
    }

    private RegisterBillDto preBuildDTO(RegisterBillDto dto) {
        if (StringUtils.isNotBlank(dto.getAttrValue())) {
            switch (dto.getAttr()) {
                case "code":
                    dto.setCode(dto.getAttrValue());
                    break;
                // case "plate":
                // registerBill.setPlate(registerBill.getAttrValue());
                // break;
                // case "tallyAreaNo":
                //// registerBill.setTallyAreaNo(registerBill.getAttrValue());
                // registerBill.setLikeTallyAreaNo(registerBill.getAttrValue());
                // break;
                case "latestDetectOperator":
                    dto.setLatestDetectOperator(dto.getAttrValue());
                    break;
                case "name":
                    dto.setName(dto.getAttrValue());
                    break;
                case "productName":
                    dto.setProductName(dto.getAttrValue());
                    break;
                case "likeSampleCode":
                    dto.setLikeSampleCode(dto.getAttrValue());
                    break;
            }
        }

        StringBuilder sql = this.buildDynamicCondition(dto);
        if (sql.length() > 0) {
            dto.setMetadata(IDTO.AND_CONDITION_EXPR, sql.toString());
        }

        return dto;
    }

    @Override
    public RegisterBill findFirstWaitAuditRegisterBillCreateByCurrentUser(RegisterBillDto input) throws Exception {
        RegisterBillDto dto = new RegisterBillDto();
        UserTicket userTicket = getOptUser();
        dto.setOperatorId(userTicket.getId());
//        dto.setState(RegisterBillStateEnum.WAIT_AUDIT.getCode());
        dto.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
        dto.setRows(1);

        dto.setSort("code");
        dto.setOrder("desc");
        return this.listByExample(dto).stream().findFirst().orElse(new RegisterBill());
    }

    @Override
    public String listPage(RegisterBillDto input) throws Exception {
        RegisterBillDto dto = this.preBuildDTO(input);
        return this.listEasyuiPageByExample(dto, true).toString();
    }

    private StringBuilder buildDynamicCondition(RegisterBillDto registerBill) {
        StringBuilder sql = new StringBuilder();

        return sql;
    }

    @Transactional
    @Override
    public Long doVerifyBeforeCheckIn(RegisterBill input, Optional<OperatorUser> operatorUser) {
        if (input == null || input.getId() == null) {
            throw new TraceBizException("参数错误");
        }
        RegisterBill billItem = this.getAndCheckById(input.getId())
                .orElseThrow(() -> new TraceBizException("数据不存在"));

        if (//YesOrNoEnum.YES.getCode().equals(billItem.getIsCheckin())
                CheckinStatusEnum.ALLOWED.equalsToCode(billItem.getCheckinStatus())
                        || RegistTypeEnum.SUPPLEMENT.equalsToCode(billItem.getRegistType())) {
            throw new TraceBizException("补单或已进门报备单,只能场内审核");
        }
        BillVerifyStatusEnum toVerifyState = BillVerifyStatusEnum.fromCode(input.getVerifyStatus())
                .orElseThrow(() -> new TraceBizException("参数错误"));
        this.doVerify(billItem, toVerifyState, input.getReason(), operatorUser);

        if (BillVerifyStatusEnum.PASSED == toVerifyState) {
            processService.afterBillPassed(billItem.getId(), billItem.getMarketId(), operatorUser);
        }

        //新增消息
        addMessage(billItem, MessageTypeEnum.BILLPASS.getCode(), MessageStateEnum.BUSINESS_TYPE_BILL.getCode(), MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_NORMAL.getCode(), billItem.getMarketId());
        return billItem.getId();
    }

    @Transactional
    @Override
    public Long doVerifyAfterCheckIn(Long billId, Integer verifyStatus, String reason,
                                     Optional<OperatorUser> operatorUser) {
        if (billId == null || verifyStatus == null) {
            throw new TraceBizException("参数错误");
        }
        BillVerifyStatusEnum toVerifyState = BillVerifyStatusEnum.fromCode(verifyStatus)
                .orElseThrow(() -> new TraceBizException("参数错误"));
        RegisterBill billItem = this.getAndCheckById(billId).orElseThrow(() -> new TraceBizException("数据不存在"));

//        if (!YesOrNoEnum.YES.getCode().equals(billItem.getIsCheckin())
        if (!CheckinStatusEnum.ALLOWED.equalsToCode(billItem.getCheckinStatus())
                && !RegistTypeEnum.SUPPLEMENT.equalsToCode(billItem.getRegistType())) {
            throw new TraceBizException("补单或已进门报备单,才能场内审核");
        }
        if (BillVerifyStatusEnum.PASSED.equalsToCode(verifyStatus)) {
            this.checkinOutRecordService.doCheckin(operatorUser, Lists.newArrayList(billItem.getBillId()),
                    CheckinStatusEnum.ALLOWED);
        }
        this.doVerify(billItem, toVerifyState, reason, operatorUser);
        //新增消息
        addMessage(billItem, MessageTypeEnum.BILLPASS.getCode(), MessageStateEnum.BUSINESS_TYPE_BILL.getCode(), MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_NORMAL.getCode(), billItem.getMarketId());
        return billItem.getId();

    }


    private void addMessage(RegisterBill billItem, Integer messageType, Integer businessType, Integer receiverType, Long marketId) {

        Integer receiverNormal = 10;
        MessageInputDto messageInputDto = new MessageInputDto();
        messageInputDto.setSourceBusinessType(businessType);
        messageInputDto.setSourceBusinessId(billItem.getId());
        messageInputDto.setMessageType(messageType);
        messageInputDto.setCreatorId(billItem.getUserId());
        messageInputDto.setReceiverType(receiverType);
        messageInputDto.setEventMessageContentParam(new String[]{billItem.getName()});
        //管理员
        if (!receiverType.equals(receiverNormal)) {
            // 审核通过增加消息**已通过
            List<com.dili.uap.sdk.domain.User> manageList = new ArrayList<>();
            Set<Long> managerIdSet = new HashSet<>();
            StreamEx.of(manageList).nonNull().forEach(s -> {
                //没有判断用户状态
                managerIdSet.add(s.getId());
            });
            Long[] managerId = managerIdSet.toArray(new Long[managerIdSet.size()]);
            messageInputDto.setReceiverIdArray(managerId);
        } else {
            messageInputDto.setReceiverIdArray(new Long[]{billItem.getUserId()});
        }
        //审核通过/不通过/退回
        //增加卖家短信
        Map<String, Object> smsMap = getSmsMap(billItem);
        messageInputDto.setSmsContentParam(smsMap);
        messageService.addMessage(messageInputDto, marketId);
    }

    private Map<String, Object> getSmsMap(RegisterBill billItem) {
        Map<String, Object> smsMap = new HashMap<>();
        smsMap.put("userName", billItem.getName());
        smsMap.put("created", DateUtils.format(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
        smsMap.put("billNo", billItem.getCode());
        smsMap.put("productName", "商品:" + billItem.getProductName() + "    车号:" + billItem.getPlate());
        return smsMap;
    }

    private void doVerify(RegisterBill billItem, BillVerifyStatusEnum toVerifyState, String reason,
                          Optional<OperatorUser> operatorUser) {
        BillVerifyStatusEnum fromVerifyState = BillVerifyStatusEnum.fromCode(billItem.getVerifyStatus())
                .orElseThrow(() -> new TraceBizException("数据错误"));


        logger.info("审核: billId: {} from {} to {}", billItem.getBillId(), fromVerifyState.getName(),
                toVerifyState.getName());
        if (!BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(billItem.getVerifyStatus())) {
            throw new TraceBizException("当前状态不能进行数据操作");
        }
        if (BillVerifyStatusEnum.WAIT_AUDIT == toVerifyState) {
            throw new TraceBizException("不支持的操作");
        }
        if (fromVerifyState == toVerifyState) {
            throw new TraceBizException("状态不能相同");
        }

        // 更新当前报务单数据
        RegisterBill bill = new RegisterBill();
        bill.setId(billItem.getId());
        bill.setVerifyStatus(toVerifyState.getCode());
        operatorUser.ifPresent(op -> {
            bill.setOperatorId(op.getId());
            bill.setOperatorName(op.getName());
            bill.setOperationTime(new Date());
        });

        bill.setReason(StringUtils.trimToEmpty(reason));
        if (BillVerifyStatusEnum.PASSED == toVerifyState) {
            if (CheckinStatusEnum.ALLOWED.equalsToCode(billItem.getCheckinStatus())) {
//            if (YesOrNoEnum.YES.getCode().equals(billItem.getIsCheckin())) {
                bill.setVerifyType(VerifyTypeEnum.PASSED_AFTER_CHECKIN.getCode());
            } else {
                bill.setVerifyType(VerifyTypeEnum.PASSED_BEFORE_CHECKIN.getCode());
            }
        }
        bill.setModified(new Date());
        this.updateSelective(bill);
        // 创建审核历史数据
        this.registerBillHistoryService.createHistory(billItem.getId());
        this.billVerifyHistoryService.createVerifyHistory(fromVerifyState, bill.getBillId(), operatorUser);

        // 创建相关的tradeDetail及batchStock数据

        this.tradeService.createBatchStockAfterVerifiedAndCheckin(billItem.getId(),
                operatorUser);

        // 更新用户颜色码
        this.updateUserQrStatusByUserId(billItem.getBillId(), billItem.getUserId());

    }


    /**
     * 根据用户最新报备单审核状态更新颜色码
     *
     * @param userId
     */
    @Override
    public void updateUserQrStatusByUserId(Long billId, Long userId) {
        if (billId == null || userId == null) {
            return;
        }
        RegisterBill billItem = this.get(billId);
        this.userQrHistoryService.createUserQrHistoryForVerifyBill(billItem, userId);
    }

    /**
     * 根据报备单数量更新用户状态到黑码
     *
     * @param
     * @return
     */
    public void updateAllUserQrStatusByRegisterBillNum(Date createdStart, Date createdEnd) {
        UserQueryDto dto = new UserQueryDto();
        dto.setQrStatus(UserQrStatusEnum.BLACK.getCode());
        dto.setCreatedStart(createdStart);
        dto.setCreatedEnd(createdEnd);
        List<Long> userIdList = this.getActualDao().selectUserIdWithouBill(dto);
        if (userIdList == null) {
            userIdList = new ArrayList<>();
        }
        List<Long> buyerIdList = tradeRequestService.selectBuyerIdWithouTradeRequest(dto);
        if (buyerIdList != null) {
            userIdList.retainAll(buyerIdList);
        }
        StreamEx.of(userIdList).nonNull().forEach(uid -> {
            this.userQrHistoryService.createUserQrHistoryForWithousBills(uid);
        });

        // RegisterBillDto bq = new RegisterBillDto();
        // bq.setCreatedStart(DateUtil.format(createdStart, "yyyy-MM-dd HH:mm:ss"));
        // bq.setCreatedEnd(DateUtil.format(createdEnd, "yyyy-MM-dd HH:mm:ss"));
        // bq.setMetadata(IDTO.AND_CONDITION_EXPR,
        // "user_id in(select id from `user` where qr_status=" +
        // UserQrStatusEnum.BLACK.getCode() + ")");
        // StreamEx.of(this.listByExample(bq)).map(RegisterBill::getUserId).distinct().map(uid
        // -> {
        // return this.userService.get(uid);
        // }).nonNull().forEach(userItem -> {
        // this.updateUserQrStatusByUserId(userItem.getId());
        // });
    }

    @Override
    public BasePage<RegisterBill> listPageBeforeCheckinVerifyBill(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLBeforeCheckIn(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
//        query.setOrderType(OrderTypeEnum.REGISTER_BILL.getCode());
        return this.listPageByExample(query);
    }

    @Override
    public List<VerifyStatusCountOutputDto> countByVerifyStatuseBeforeCheckin(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLBeforeCheckIn(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        return this.countByVerifyStatus(query);
    }

    @Override
    public BasePage<RegisterBill> listPageAfterCheckinVerifyBill(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLAfterCheckIn(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
//        query.setOrderType(OrderTypeEnum.REGISTER_BILL.getCode());
        return this.listPageByExample(query);
    }

    @Override
    public List<VerifyStatusCountOutputDto> countByVerifyStatuseAfterCheckin(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLAfterCheckIn(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        return this.countByVerifyStatus(query);
    }

    @Override
    public Map<Integer, Map<String, List<RegisterBill>>> listPageCheckInData(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        String dynaWhere = " checkin_status=" + CheckinStatusEnum.NONE.getCode() + " and regist_type =" + RegistTypeEnum.NONE.getCode();

        query.setSort("created");
        query.setOrder("desc");
        query.setMetadata(IDTO.AND_CONDITION_EXPR, dynaWhere);
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        query.setTruckType(TruckTypeEnum.FULL.getCode());
        List<RegisterBill> list = this.listByExample(query);

        query.setTruckType(TruckTypeEnum.POOL.getCode());
        List<RegisterBill> userPoolList = this.listByExample(query);

        List<String> plateList = StreamEx.of(userPoolList).filter(bill -> {
            return TruckTypeEnum.POOL.equalsToCode(bill.getTruckType());
        }).map(RegisterBill::getPlate).distinct().toList();
        List<RegisterBill> samePlatePoolTruckTypeBillList = StreamEx.ofNullable(plateList).filter(l -> !l.isEmpty())
                .flatMap(l -> {
                    query.setPlateList(plateList);
                    query.setTruckType(TruckTypeEnum.POOL.getCode());
                    query.setUserId(null);
                    return StreamEx.of(this.listByExample(query));
                }).toList();

        Map<Integer, List<RegisterBill>> truckTypeBillMap = StreamEx.of(list).append(userPoolList)
                .append(samePlatePoolTruckTypeBillList).distinct(RegisterBill::getBillId)
                .groupingBy(RegisterBill::getTruckType);
        Map<Integer, Map<String, List<RegisterBill>>> resultMap = EntryStream.of(truckTypeBillMap)
                .flatMapToValue((k, v) -> {
                    if (TruckTypeEnum.FULL.equalsToCode(k)) {
                        return Stream.of(StreamEx.of(v).mapToEntry(item -> {
                            return DateUtil.format(item.getCreated(), "yyyy-MM-dd");
                        }, Function.identity())
                                .grouping(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER.reversed())));
                    }
                    if (TruckTypeEnum.POOL.equalsToCode(k)) {
                        return Stream.of(StreamEx.of(v).groupingBy(RegisterBill::getPlate));
                    }
                    return StreamEx.empty();
                }).toMap();
        return resultMap;

    }


    @Override
    public RegisterBillOutputDto viewTradeDetailBill(RegisterBillApiInputDto inputDto) {
        Long billId = inputDto.getBillId();
        Long tradeDetailId = inputDto.getTradeDetailId();
        Long marketId = inputDto.getMarketId();
        if (billId == null && tradeDetailId == null) {
            throw new TraceBizException("参数错误");
        }
//        if(marketId==null){
//            throw new TraceBizException("参数错误");
//        }

        TradeDetail tradeDetailItem = StreamEx.ofNullable(tradeDetailId).nonNull().map(tdId -> {
            return this.tradeDetailService.get(tdId);
        }).findFirst().orElse(new TradeDetail());

        RegisterBill registerBill = StreamEx.ofNullable(billId).append(tradeDetailItem.getBillId()).nonNull()
                .map(bId -> {
                    return this.get(bId);
                }).findFirst().orElse(new RegisterBill());

        List<ImageCert> imageCertList = StreamEx.ofNullable(registerBill).flatMap(bill -> {
            return imageCertService.findImageCertListByBillId(bill.getId(), BillTypeEnum.fromCode(bill.getBillType()).orElse(null)).stream();
        }).toList();

        String upStreamName = StreamEx.ofNullable(registerBill.getUpStreamId()).nonNull().map(upStreamId -> {
            return this.upStreamService.get(upStreamId);
        }).nonNull().findAny().map(UpStream::getName).orElse("");
        RegisterBillOutputDto outputdto = RegisterBillOutputDto.build(registerBill, Lists.newArrayList());
        outputdto.setImageCertList(imageCertList);
        outputdto.setUpStreamName(upStreamName);
        outputdto.setTruckTareWeight(registerBill.getTruckTareWeight());

        if (tradeDetailItem.getId() != null && registerBill.getId() != null) {
            outputdto.setWeight(tradeDetailItem.getTotalWeight());
            return outputdto;
        } else if (registerBill.getId() != null) {
            outputdto.setWeight(registerBill.getWeight());
            return outputdto;
        } else {
            throw new TraceBizException("没有数据");
        }

    }

    private Optional<String> buildLikeKeyword(RegisterBillDto query) {
        String sql = null;
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            sql = "( product_name like '%" + keyword + "%'  OR name like '%" + keyword + "%'  OR (register_source=" + RegisterSourceEnum.TALLY_AREA.getCode() + " and source_name like '%" + keyword + "%') )";
        }
        return Optional.ofNullable(sql);

    }

    private String dynamicSQLBeforeCheckIn(RegisterBillDto query) {
        List<String> sqlList = new ArrayList<>();
        this.buildLikeKeyword(query).ifPresent(sql -> {
            sqlList.add(sql);
        });
        // 正常进场或者分批进场 && 未进门或者进门审核通过
        sqlList.add("( (regist_type=" + RegistTypeEnum.NONE.getCode()
                + " or regist_type=" + RegistTypeEnum.PARTIAL.getCode() + ")  and (checkin_status=" + CheckinStatusEnum.NONE.getCode()
                + " OR (checkin_status=" + CheckinStatusEnum.ALLOWED.getCode() + " and verify_status="
                + BillVerifyStatusEnum.PASSED.getCode() + " and verify_type="
                + VerifyTypeEnum.PASSED_BEFORE_CHECKIN.getCode() + " ) ) )");

        return StreamEx.of(sqlList).joining(" AND ");
    }

    private String dynamicSQLAfterCheckIn(RegisterBillDto query) {
        List<String> sqlList = new ArrayList<>();
        this.buildLikeKeyword(query).ifPresent(sql -> {
            sqlList.add(sql);
        });
        sqlList.add("( regist_type=" + RegistTypeEnum.SUPPLEMENT.getCode() + " OR  (checkin_status=" + CheckinStatusEnum.ALLOWED.getCode()
                + " AND verify_status<>" + BillVerifyStatusEnum.PASSED.getCode() + ") OR(verify_status="
                + BillVerifyStatusEnum.PASSED.getCode() + " and verify_type="
                + VerifyTypeEnum.PASSED_AFTER_CHECKIN.getCode() + ") )");

        return StreamEx.of(sqlList).joining(" AND ");
    }

    private List<VerifyStatusCountOutputDto> countByVerifyStatus(RegisterBillDto query) {
        List<VerifyStatusCountOutputDto> dtoList = this.getActualDao().countByVerifyStatus(query);
        Map<Integer, Integer> verifyStatusNumMap = StreamEx.of(dtoList)
                .toMap(VerifyStatusCountOutputDto::getVerifyStatus, VerifyStatusCountOutputDto::getNum);
        return StreamEx.of(BillVerifyStatusEnum.values()).map(e -> {
            VerifyStatusCountOutputDto dto = VerifyStatusCountOutputDto.buildDefault(e);
            if (verifyStatusNumMap.containsKey(dto.getVerifyStatus())) {
                dto.setNum(verifyStatusNumMap.get(dto.getVerifyStatus()));
            }
            return dto;
        }).toList();
    }

    @Override
    public List<RegisterBill> createRegisterFormBillList(List<CreateRegisterBillInputDto> registerBills, Long customerId,
                                                         Optional<OperatorUser> operatorUser, Long marketId) {
        CustomerExtendDto customer = this.clientRpcService.findCustomerById(customerId, marketId).orElseThrow(() -> {
            return new TraceBizException("查询客户信息失败");
        });
        return StreamEx.of(registerBills).nonNull().map(dto -> {
            logger.info("循环保存进门登记单:" + JSON.toJSONString(dto));
            RegisterBill registerBill = dto.build(customer, marketId);
            registerBill.setMarketId(marketId);
//            registerBill.setOrderType(OrderTypeEnum.REGISTER_FORM_BILL.getCode());
            return this.createRegisterFormBill(registerBill, dto.getImageCertList(), operatorUser);
        }).toList();
    }

    @Transactional
    @Override
    public RegisterBill createRegisterFormBill(RegisterBill registerBill, List<ImageCert> imageCertList,
                                               Optional<OperatorUser> operatorUser) {
        this.checkBill(registerBill);

        registerBill.setVerifyType(VerifyTypeEnum.NONE.getCode());
        registerBill.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
//        registerBill.setState(RegisterBillStateEnum.NEW.getCode());
        registerBill.setDetectStatus(DetectStatusEnum.NONE.getCode());
        registerBill.setCode(uidRestfulRpcService.bizNumber(BizNumberType.REGISTER_BILL));
        registerBill.setVersion(1);
        registerBill.setCreated(new Date());
//        registerBill.setIsCheckin(YesOrNoEnum.NO.getCode());
        registerBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        registerBill.setIsDeleted(YesOrNoEnum.NO.getCode());
        registerBill.setIsPrintCheckSheet(YesOrNoEnum.NO.getCode());
        operatorUser.ifPresent(op -> {
            registerBill.setOperatorName(op.getName());
            registerBill.setOperatorId(op.getId());
            registerBill.setOperationTime(new Date());

            registerBill.setCreateUser(op.getName());
        });
        registerBill.setIdCardNo(StringUtils.trimToEmpty(registerBill.getIdCardNo()).toUpperCase());
        // 车牌转大写
        String plate = StreamEx.ofNullable(registerBill.getPlate()).nonNull().map(StringUtils::trimToNull).nonNull()
                .map(String::toUpperCase).findFirst().orElse(null);
        registerBill.setPlate(plate);
        registerBill.setModified(new Date());

        // 查验状态为不通过，进门状态设置为未进门，其他设置为已进门
        if (BillVerifyStatusEnum.NO_PASSED.equalsToCode(registerBill.getVerifyStatus())) {
//            registerBill.setIsCheckin(YesOrNoEnum.NO.getCode());
            registerBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        } else {
//            registerBill.setIsCheckin(YesOrNoEnum.YES.getCode());
            registerBill.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        }
        if (BillVerifyStatusEnum.PASSED.equalsToCode(registerBill.getVerifyStatus())) {
            registerBill.setVerifyType(VerifyTypeEnum.PASSED_BEFORE_CHECKIN.getCode());
        }

        if (registerBill.getRegisterSource() == null) {
            // 默认理货区
            registerBill.setRegisterSource(RegisterSourceEnum.TALLY_AREA.getCode());
        }

        // 保存车牌
        this.userPlateService.checkAndInsertUserPlate(registerBill.getUserId(), plate);

        // 保存进门登记单
        int result = super.saveOrUpdate(registerBill);
        if (result == 0) {
            logger.error("新增登记单数据库执行失败" + JSON.toJSONString(registerBill));
            throw new TraceBizException("创建失败");
        }
        // 创建审核历史数据
        this.registerBillHistoryService.createHistory(registerBill.getBillId());
        // 保存图片
        imageCertList = StreamEx.ofNullable(imageCertList).nonNull().flatCollection(Function.identity()).nonNull().toList();
        if (imageCertList.isEmpty() && !BillVerifyStatusEnum.RETURNED.equalsToCode(registerBill.getVerifyStatus())) {
            throw new TraceBizException("请上传凭证");
        }
        imageCertService.insertImageCert(imageCertList, registerBill.getBillId(), BillTypeEnum.REGISTER_BILL.getCode());

        // 创建/更新品牌信息并更新brandId字段值
        this.brandService.createOrUpdateBrand(registerBill.getBrandName(), registerBill.getUserId(), registerBill.getMarketId())
                .ifPresent(brandId -> {
                    RegisterBill bill = new RegisterBill();
                    bill.setBrandId(brandId);
                    bill.setId(registerBill.getId());
                    this.updateSelective(bill);
                });
        this.updateUserQrStatusByUserId(registerBill.getBillId(), registerBill.getUserId());

        //更新主台账单剩余重量
        if (RegistTypeEnum.PARTIAL.getCode().equals(registerBill.getRegistType())) {
            RegisterHead rhq = new RegisterHead();
            rhq.setCode(registerBill.getRegisterHeadCode());
            RegisterHead registerHeadItem = StreamEx.of(registerHeadService.listByExample(rhq)).findFirst().orElseThrow(() -> {
                return new TraceBizException("未找到主台账单");
            });

            //主台账单的剩余重量小于进门登记单的总重量时给出提示
            BigDecimal remianWeight = registerHeadItem.getRemainWeight();
            BigDecimal billWeight = registerBill.getWeight();
            if (remianWeight == null || (remianWeight != null && remianWeight.compareTo(billWeight) == -1)) {
                throw new TraceBizException("进门登记单的总重量大于主台账单的剩余重量，不可新增");
            }
            registerHeadItem.setRemainWeight(remianWeight.subtract(billWeight));
            if (registerBill.getOriginId() != registerHeadItem.getOriginId()
                    || registerBill.getProductId() != registerHeadItem.getProductId()) {
                throw new TraceBizException("分批进场登记单信息不能进行修改");
            }


            registerHeadService.updateSelective(registerHeadItem);
        }

        //进门登记单新增消息
        /*Integer businessType=MessageStateEnum.BUSINESS_TYPE_FORM_BILL.getCode();
        if(BillTypeEnum.SUPPLEMENT.getCode().equals(registerBill.getBillType())){
            businessType=MessageStateEnum.BUSINESS_TYPE_FIELD_BILL.getCode();
        }
        addMessage(registerBill, MessageTypeEnum.BILLSUBMIT.getCode(), businessType, MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_MANAGER.getCode());*/
        return registerBill;
    }

    @Transactional
    @Override
    public Long doEditFormBill(RegisterBill input, List<ImageCert> imageCertList, Optional<OperatorUser> operatorUser) {
        if (input == null || input.getId() == null) {
            throw new TraceBizException("参数错误");
        }
        RegisterBill billItem = this.getAndCheckById(input.getId())
                .orElseThrow(() -> new TraceBizException("数据不存在"));

        if (BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(billItem.getVerifyStatus())
                || BillVerifyStatusEnum.RETURNED.equalsToCode(billItem.getVerifyStatus())) {
            // 待审核，或者已退回状态可以进行数据修改
        } else {
            throw new TraceBizException("当前状态不能修改数据");
        }

        // 车牌转大写
        String plate = StreamEx.ofNullable(input.getPlate()).filter(StringUtils::isNotBlank).map(p -> p.toUpperCase())
                .findFirst().orElse(null);
        input.setPlate(plate);
        // 保存车牌
        this.userPlateService.checkAndInsertUserPlate(input.getUserId(), plate);
        input.setModified(new Date());

        input.setOperatorName(null);
        input.setOperatorId(null);
        input.setOperationTime(null);
        // input.setReason("");
        operatorUser.ifPresent(op -> {
            input.setOperatorName(op.getName());
            input.setOperatorId(op.getId());
            input.setOperationTime(new Date());
        });

        // 查验状态为不通过，进门状态设置为未进门，其他设置为已进门
        if (BillVerifyStatusEnum.NO_PASSED.equalsToCode(input.getVerifyStatus())) {
//            input.setIsCheckin(YesOrNoEnum.NO.getCode());
            input.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        } else {
//            input.setIsCheckin(YesOrNoEnum.YES.getCode());
            input.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        }
        if (BillVerifyStatusEnum.PASSED.equalsToCode(input.getVerifyStatus())) {
            input.setVerifyType(VerifyTypeEnum.PASSED_BEFORE_CHECKIN.getCode());
        }

        this.updateSelective(input);
        this.registerBillHistoryService.createHistory(billItem.getBillId());

        imageCertList = StreamEx.ofNullable(imageCertList).nonNull().flatCollection(Function.identity()).nonNull().toList();
        if (imageCertList.isEmpty() && !BillVerifyStatusEnum.RETURNED.equalsToCode(input.getVerifyStatus())) {
            throw new TraceBizException("请上传凭证");
        }
        // 保存图片
        imageCertService.insertImageCert(imageCertList, input.getId(), BillTypeEnum.REGISTER_BILL.getCode());

        this.brandService.createOrUpdateBrand(input.getBrandName(), billItem.getUserId(), input.getMarketId());
        this.updateUserQrStatusByUserId(billItem.getBillId(), billItem.getUserId());
        return input.getId();
    }

    @Transactional
    @Override
    public Long doVerifyFormCheckIn(RegisterBill input, Optional<OperatorUser> operatorUser) {
        if (input == null || input.getId() == null) {
            throw new TraceBizException("参数错误");
        }
        RegisterBill billItem = this.getAndCheckById(input.getId())
                .orElseThrow(() -> new TraceBizException("数据不存在"));

        this.doVerifyForm(billItem, input.getVerifyStatus(), input.getReason(), operatorUser);
        //新增消息
        //addMessage(billItem, MessageTypeEnum.BILLPASS.getCode(), MessageStateEnum.BUSINESS_TYPE_BILL.getCode(), MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_MANAGER.getCode());
        return billItem.getId();
    }

    private void doVerifyForm(RegisterBill billItem, Integer verifyStatus, String reason,
                              Optional<OperatorUser> operatorUser) {
        BillVerifyStatusEnum fromVerifyState = BillVerifyStatusEnum.fromCode(billItem.getVerifyStatus())
                .orElseThrow(() -> new TraceBizException("数据错误"));

        BillVerifyStatusEnum toVerifyState = BillVerifyStatusEnum.fromCode(verifyStatus)
                .orElseThrow(() -> new TraceBizException("参数错误"));

        logger.info("审核: billId: {} from {} to {}", billItem.getBillId(), fromVerifyState.getName(),
                toVerifyState.getName());
        if (!BillVerifyStatusEnum.RETURNED.equalsToCode(billItem.getVerifyStatus())) {
            throw new TraceBizException("当前状态不能进行数据操作");
        }
        if (BillVerifyStatusEnum.WAIT_AUDIT == toVerifyState) {
            throw new TraceBizException("不支持的操作");
        }
        if (fromVerifyState == toVerifyState) {
            throw new TraceBizException("状态不能相同");
        }

        // 更新当前报务单数据
        RegisterBill bill = new RegisterBill();
        bill.setId(billItem.getId());
        bill.setVerifyStatus(toVerifyState.getCode());
        operatorUser.ifPresent(op -> {
            bill.setOperatorId(op.getId());
            bill.setOperatorName(op.getName());
            bill.setOperationTime(new Date());
        });

        bill.setReason(StringUtils.trimToEmpty(reason));
        // 查验状态为不通过，进门状态设置为未进门，其他设置为已进门
        if (BillVerifyStatusEnum.NO_PASSED == toVerifyState) {
//            bill.setIsCheckin(YesOrNoEnum.NO.getCode());
            bill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        } else {
//            bill.setIsCheckin(YesOrNoEnum.YES.getCode());
            bill.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        }
        if (BillVerifyStatusEnum.PASSED == toVerifyState) {
            bill.setVerifyType(VerifyTypeEnum.PASSED_AFTER_CHECKIN.getCode());
        }
        bill.setModified(new Date());
        this.updateSelective(bill);
        // 创建审核历史数据
        this.registerBillHistoryService.createHistory(billItem.getId());

        // 更新用户颜色码
        this.updateUserQrStatusByUserId(billItem.getBillId(), billItem.getUserId());
    }

    @Override
    public BasePage<RegisterBill> listPageApi(RegisterBillDto input) {

        StringBuilder sql = new StringBuilder();
        buildFormLikeKeyword(input).ifPresent(sql::append);
        if (sql.length() > 0) {
            input.setMetadata(IDTO.AND_CONDITION_EXPR, sql.toString());
        }

        BasePage<RegisterBill> registerBillBasePage = listPageByExample(input);
        return registerBillBasePage;
    }

    private Optional<String> buildFormLikeKeyword(RegisterBillDto query) {
        String sql = null;
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            sql = "( product_name like '%" + keyword + "%'  OR user_id in(select id from `user` u where u.name like '%"
                    + keyword + "%' OR legal_person like '%" + keyword + "%' OR phone like '%"
                    + keyword + "%') OR third_party_code like '%" + keyword + "%' OR register_head_code LIKE '%" + keyword + "%' )";
        }
        return Optional.ofNullable(sql);
    }

    @Transactional
    @Override
    public Long doDelete(CreateRegisterBillInputDto dto, Long userId, Optional<OperatorUser> operatorUser) {
        if (dto == null || userId == null) {
            throw new TraceBizException("参数错误");
        }
        RegisterBill billItem = this.getAndCheckById(dto.getBillId()).orElseThrow(() -> new TraceBizException("数据不存在"));
//        if (!userId.equals(billItem.getUserId())) {
//            throw new TraceBusinessException("没有权限删除数据");
//        }
//        if (YesOrNoEnum.YES.equalsToCode(billItem.getIsCheckin())) {
//            throw new TraceBusinessException("不能删除已进门数据");
//        }
        if (BillVerifyStatusEnum.NO_PASSED.equalsToCode(billItem.getVerifyStatus())) {
            throw new TraceBizException("不能删除审核未通过数据");
        }
        RegisterBill bill = new RegisterBill();
        bill.setId(billItem.getBillId());
        bill.setIsDeleted(dto.getIsDeleted());

        operatorUser.ifPresent(op -> {
            bill.setOperatorName(op.getName());
            bill.setOperatorId(op.getId());
            bill.setOperationTime(new Date());
            bill.setDeleteUser(op.getName());
            bill.setDeleteTime(new Date());
        });
        this.updateSelective(bill);
        this.registerBillHistoryService.createHistory(billItem.getBillId());
        this.userQrHistoryService.rollbackUserQrStatus(bill.getId(), billItem.getUserId());
        return dto.getBillId();
    }

    @Override
    public List<VerifyStatusCountOutputDto> countBillsByVerifyStatus(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLFormBill(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        List<VerifyStatusCountOutputDto> countList = this.countByVerifyStatus(query);

        return countList;
    }

    private String dynamicSQLFormBill(RegisterBillDto query) {
        List<String> sqlList = new ArrayList<>();
        this.buildFormLikeKeyword(query).ifPresent(sql -> {
            sqlList.add(sql);
        });
        sqlList.add("(checkin_status=" + CheckinStatusEnum.NONE.getCode()
                + " OR (checkin_status=" + CheckinStatusEnum.ALLOWED.getCode() + " and verify_status="
                + BillVerifyStatusEnum.PASSED.getCode() + ") OR (checkin_status=" + CheckinStatusEnum.ALLOWED.getCode() + " and verify_status="
                + BillVerifyStatusEnum.RETURNED.getCode() + " ))");
        return StreamEx.of(sqlList).joining(" AND ");
    }


    @Override
    public RegisterBill findHighLightBill(RegisterBillDto input) throws Exception {
        RegisterBillDto dto = new RegisterBillDto();
        UserTicket userTicket = getOptUser();
        dto.setOperatorId(userTicket.getId());
//        dto.setState(RegisterBillStateEnum.WAIT_AUDIT.getCode());
        dto.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
        dto.setRows(1);
        dto.setSort("code");
        dto.setOrder("desc");
        return this.listByExample(dto).stream().findFirst().orElse(new RegisterBill());
    }
}