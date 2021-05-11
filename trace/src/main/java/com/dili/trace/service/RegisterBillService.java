package com.dili.trace.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BasePage;
import com.dili.ss.dto.IDTO;
import com.dili.ss.util.DateUtils;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.api.input.RegisterBillApiInputDto;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.domain.*;
import com.dili.trace.dto.MessageInputDto;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.dto.ret.FieldConfigDetailRetDto;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import com.dili.trace.util.JsonPathUtil;
import com.dili.trace.util.NumUtils;
import com.dili.trace.util.RegUtils;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import de.cronn.reflection.util.PropertyUtils;
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
public class RegisterBillService extends BaseServiceImpl<RegisterBill, Long> {
    private static final Logger logger = LoggerFactory.getLogger(RegisterBillService.class);
    @Autowired
    UidRestfulRpcService uidRestfulRpcService;
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

    @Autowired
    FieldConfigDetailService fieldConfigDetailService;

    @Autowired
    RegisterTallyAreaNoService registerTallyAreaNoService;
    @Autowired
    ProcessConfigService processConfigService;


    /**
     * 返回mapper
     *
     * @return
     */
    public RegisterBillMapper getActualDao() {
        return (RegisterBillMapper) getDao();
    }


    /**
     * 通过ID悲观锁定数据
     */
    @Transactional
    public Optional<RegisterBill> selectByIdForUpdate(Long id) {
        return this.getActualDao().selectByIdForUpdate(id).map(billItem -> {
            if (YesOrNoEnum.YES.getCode().equals(billItem.getIsDeleted())) {
                throw new TraceBizException("登记单已经被删除");
            }
            return billItem;
        });
    }

    /**
     * 查询并检查报备单子是否删除
     */
    @Transactional
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

    /**
     * 创建多个报备单
     *
     * @param inputBillDtoList
     * @param customerId
     * @param operatorUser
     * @return
     */
    public List<Long> createRegisterBillList(Long marketId, List<CreateRegisterBillInputDto> inputBillDtoList, Long customerId,
                                             Optional<OperatorUser> operatorUser, CreatorRoleEnum creatorRoleEnum) {
        List<CreateRegisterBillInputDto> registerBills = StreamEx.ofNullable(inputBillDtoList).flatCollection(Function.identity())
                .nonNull().toList();
        if (registerBills.isEmpty()) {
            throw new TraceBizException("没有登记单");
        }
        ProcessConfig processConfig = this.processConfigService.findByMarketId(marketId);
        CustomerExtendDto user = this.clientRpcService.findApprovedCustomerByIdOrEx(customerId, marketId);
        String printingCard = this.findPrintingCard(user, marketId).orElse(null);
        List<FieldConfigDetailRetDto> fieldConfigDetailRetDtoList = this.fieldConfigDetailService.findByMarketIdAndModuleType(marketId, FieldConfigModuleTypeEnum.REGISTER);
        Map<String, FieldConfigDetailRetDto> fieldConfigDetailRetDtoMap = StreamEx.of(fieldConfigDetailRetDtoList).nonNull().toMap(item -> item.getDefaultFieldDetail().getFieldName(), Function.identity());
        return StreamEx.of(registerBills).nonNull().map(dto -> {
            dto.setMarketId(marketId);
            logger.info("循环保存登记单:" + JSON.toJSONString(dto));

            RegisterBill registerBill = dto.build(user, dto.getMarketId());
            if (StringUtils.isNotBlank(registerBill.getPlate())) {
                if (!RegUtils.isPlate(registerBill.getPlate().trim())) {
                    throw new TraceBizException("车牌格式错误");
                }
            }

            registerBill.setCreatorRole(creatorRoleEnum.getCode());
            registerBill.setThirdPartyCode(printingCard);
            registerBill.setImageCertList(dto.getImageCertList());


            Long billId = this.createRegisterBill(registerBill, fieldConfigDetailRetDtoMap, processConfig, operatorUser);

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

    /**
     * 查询卡号
     *
     * @param user
     * @param marketId
     * @return
     */
    private Optional<String> findPrintingCard(CustomerExtendDto user, Long marketId) {
        Customer cq = new Customer();
        cq.setCustomerId(user.getCode());
        return this.clientRpcService.findCustomer(cq, marketId).map(Customer::getPrintingCard);
    }

    /**
     * 为进门称重创建报备单
     *
     * @param registerBill
     * @param operatorUser
     * @return
     */
    public Long createWeightingRegisterBill(RegisterBill registerBill,
                                            Optional<OperatorUser> operatorUser) {
        Long marketId = registerBill.getMarketId();
        List<FieldConfigDetailRetDto> fieldConfigDetailRetDtoList = this.fieldConfigDetailService.findByMarketIdAndModuleType(marketId, FieldConfigModuleTypeEnum.REGISTER);
        Map<String, FieldConfigDetailRetDto> fieldConfigDetailRetDtoMap = StreamEx.of(fieldConfigDetailRetDtoList).nonNull().toMap(item -> item.getDefaultFieldDetail().getFieldName(), Function.identity());

        ProcessConfig processConfig = this.processConfigService.findByMarketId(marketId);
        processConfig.setIsNeedVerify(YesOrNoEnum.NO.getCode());
        Long billId = this.createRegisterBill(registerBill, fieldConfigDetailRetDtoMap, processConfig, operatorUser);

        return billId;
    }

    /**
     * 创建单个报备单
     *
     * @param registerBill
     * @return
     */
    private Long createRegisterBill(RegisterBill registerBill
            , Map<String, FieldConfigDetailRetDto> fieldConfigDetailRetDtoMap, ProcessConfig processConfig,
                                    Optional<OperatorUser> operatorUser) {

        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL);

        String json = JSON.toJSONString(registerBill);
//        StreamEx.of(fieldConfigDetailRetDtoList).forEach(fcd -> {
//            String jsonPath = fcd.getDefaultFieldDetail().getJsonPath();
//            if (StringUtils.isBlank(jsonPath)) {
//                return;
//            }
//            Object jsonValue = JsonPath.using(conf).parse(json).read(jsonPath.trim());
//            logger.debug("jsonpath={},jsonvalue={}", jsonPath, jsonValue);
//        });
        this.checkAndSetDefaultValue(registerBill, fieldConfigDetailRetDtoMap);


        registerBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        registerBill.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
        registerBill.setVerifyType(VerifyTypeEnum.NONE.getCode());
//        registerBill.setState(RegisterBillStateEnum.NEW.getCode());
        registerBill.setDetectStatus(DetectStatusEnum.NONE.getCode());
        String code = uidRestfulRpcService.bizNumber(BizNumberType.REGISTER_BILL);
        logger.debug("registerBill.code={}", code);
        registerBill.setCode(code);
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
        registerBill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
        registerBill.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
        // 保存车牌
//        this.userPlateService.checkAndInsertUserPlate(registerBill.getUserId(), plate);


        if (registerBill.getPreserveType() == null) {
            registerBill.setPreserveType(PreserveTypeEnum.NONE.getCode());
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
            List<String>arrivalTallynos=StreamEx.of(registerTallyAreaNoService.findTallyAreaNoByBillIdAndType(registerHead.getId(),BillTypeEnum.MASTER_BILL)).map(RegisterTallyAreaNo::getTallyareaNo).toList();
            registerBill.setArrivalTallynos(arrivalTallynos);
        }

        // 保存报备单
        int result = super.saveOrUpdate(registerBill);
        if (result == 0) {
            logger.error("新增登记单数据库执行失败" + JSON.toJSONString(registerBill));
            throw new TraceBizException("创建失败");
        }
        this.registerTallyAreaNoService.insertTallyAreaNoList(registerBill.getArrivalTallynos(), registerBill.getBillId(), BillTypeEnum.REGISTER_BILL);


        // 保存图片
        List<ImageCert> imageCertList = StreamEx.ofNullable(registerBill.getImageCertList()).nonNull().flatCollection(Function.identity()).nonNull().toList();
        if (!imageCertList.isEmpty()) {
            imageCertService.insertImageCert(imageCertList, registerBill.getBillId());
            //更新报备单上图片标志位
            this.billService.updateHasImage(registerBill.getBillId(), imageCertList);
        }

        this.processService.afterCreateBill(registerBill.getId(), registerBill.getMarketId(), processConfig, operatorUser);

        // 创建审核历史数据
        this.registerBillHistoryService.createHistory(registerBill.getBillId());


        // 创建/更新品牌信息并更新brandId字段值
        this.brandService.createOrUpdateBrand(registerBill.getBrandName(), registerBill.getUserId(), registerBill.getMarketId())
                .ifPresent(brandId -> {
                    RegisterBill bill = new RegisterBill();
                    bill.setBrandId(brandId);
                    bill.setId(registerBill.getId());
                    this.updateSelective(bill);
                });


        //报备单新增消息
        Integer businessType = MessageStateEnum.BUSINESS_TYPE_BILL.getCode();
        if (RegistTypeEnum.SUPPLEMENT.getCode().equals(registerBill.getRegistType())) {
            businessType = MessageStateEnum.BUSINESS_TYPE_FIELD_BILL.getCode();
        }
        addMessage(registerBill, MessageTypeEnum.BILLSUBMIT.getCode(), businessType, MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_MANAGER.getCode(), registerBill.getMarketId());
        //同步uap商品、经营户
        this.syncCategoryService.saveAndSyncGoodInfo(registerBill.getProductId(), registerBill.getMarketId());
        this.syncUserInfoService.saveAndSyncUserInfo(registerBill.getUserId(), registerBill.getMarketId());
        this.updateUserQrStatusByUserId(registerBill.getBillId(), registerBill.getUserId());


        return registerBill.getId();
    }
    /**
     * 检查用户输入参数
     *
     * @param registerBill
     * @return
     */
    private RegisterBill simpleCheck(RegisterBill registerBill) {
        if (registerBill.getMarketId() == null) {
            throw new TraceBizException("登记单市场不存在");
        }
        BigDecimal weight = registerBill.getWeight();
        if (weight == null) {
            logger.error("商品重量不能为空");
            throw new TraceBizException("商品重量不能为空");
        }

        if (!NumUtils.isIntegerValue(weight)) {
            logger.error("商品重量必须为整数");
            throw new TraceBizException("商品重量必须为整数");
        }

        if (BigDecimal.ZERO.compareTo(weight) >= 0) {
            logger.error("商品重量不能小于0");
            throw new TraceBizException("商品重量不能小于0");
        }

        if (NumUtils.MAX_WEIGHT.compareTo(weight) < 0) {
            logger.error("商品重量不能大于" + NumUtils.MAX_WEIGHT.toString());
            throw new TraceBizException("商品重量不能大于" + NumUtils.MAX_WEIGHT.toString());
        }

        String remark = StringUtils.trimToNull(registerBill.getRemark());
        if(Objects.nonNull(remark)){
            if (!RegUtils.isValidInput(remark)) {
                throw new TraceBizException("备注包含非法字符");
            }
            if (remark.length()>200) {
                throw new TraceBizException("备注不能超过200字符");
            }
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
        return  registerBill;
    }
    /**
     * 检查用户输入参数
     *
     * @param registerBill
     * @return
     */
    private RegisterBill checkAndSetDefaultValue(RegisterBill registerBill, Map<String, FieldConfigDetailRetDto> fieldConfigDetailRetDtoMap) {
        if (registerBill.getMarketId() == null) {
            throw new TraceBizException("登记单市场不存在");
        }
        BigDecimal weight = registerBill.getWeight();
        if (weight == null) {
            logger.error("商品重量不能为空");
            throw new TraceBizException("商品重量不能为空");
        }

        if (!NumUtils.isIntegerValue(weight)) {
            logger.error("商品重量必须为整数");
            throw new TraceBizException("商品重量必须为整数");
        }

        if (BigDecimal.ZERO.compareTo(weight) >= 0) {
            logger.error("商品重量不能小于0");
            throw new TraceBizException("商品重量不能小于0");
        }

        if (NumUtils.MAX_WEIGHT.compareTo(weight) < 0) {
            logger.error("商品重量不能大于" + NumUtils.MAX_WEIGHT.toString());
            throw new TraceBizException("商品重量不能大于" + NumUtils.MAX_WEIGHT.toString());
        }

        if (registerBill.getWeightUnit() == null) {
            logger.error("重量单位不能为空");
            throw new TraceBizException("重量单位不能为空");
        }

        if (StringUtils.isBlank(registerBill.getProductName()) || registerBill.getProductId() == null) {
            logger.error("商品名称不能为空");
            throw new TraceBizException("商品名称不能为空");
        }

        //客户相关字段
        if (StringUtils.isBlank(registerBill.getName())) {
            logger.error("业户姓名不能为空");
            throw new TraceBizException("业户姓名不能为空");
        }
        if (registerBill.getUserId() == null) {
            logger.error("业户ID不能为空");
            throw new TraceBizException("业户ID不能为空");
        }


        if(StringUtils.isNotBlank(registerBill.getName())){
            if(!RegUtils.isValidInput(registerBill.getName())){
                throw new TraceBizException("业户名称不能有特殊字符");
            }
            if(StringUtils.trimToEmpty(registerBill.getName()).length()>40){
                throw new TraceBizException("业户名称不能超过40字符");
            }
        }
        if(StringUtils.isNotBlank(registerBill.getCorporateName())){
            if(!RegUtils.isValidInput(registerBill.getCorporateName())){
                throw new TraceBizException("企业名称不能有特殊字符");
            }
            if(StringUtils.trimToEmpty(registerBill.getProductAliasName()).length()>40){
                throw new TraceBizException("企业名称不能超过40字符");
            }
        }
        
        if(StringUtils.trimToEmpty(registerBill.getProductAliasName()).length()>40){
            throw new TraceBizException("商品别名不能超过40字符");
        }
        //登记单类型字段
        if (!BillTypeEnum.fromCode(registerBill.getBillType()).isPresent()) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getBillType).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("登记单类型不能为空");
            }
            registerBill.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
        }


        //产地字段
        if (StringUtils.isBlank(registerBill.getOriginName()) || registerBill.getOriginId() == null) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getOriginId).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("商品产地不能为空");
            }
        }

        //备注字段
        if (StringUtils.isBlank(registerBill.getRemark())) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getRemark).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("备注不能为空");
            }
        }
        String remark = StringUtils.trimToNull(registerBill.getRemark());
        if(Objects.nonNull(remark)){
            if (!RegUtils.isValidInput(remark)) {
                throw new TraceBizException("备注包含非法字符");
            }
            if (remark.length()>200) {
                throw new TraceBizException("备注不能超过200字符");
            }
        }

        //皮重字段
        if (registerBill.getTruckTareWeight() == null) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getTruckTareWeight).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("皮重不能为空");
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

        //是否拼车/车牌字段
        if (!TruckTypeEnum.fromCode(registerBill.getTruckType()).isPresent()) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getTruckType).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("是否拼车不能为空");
            }
            registerBill.setTruckType(TruckTypeEnum.FULL.getCode());
        }

        if (TruckTypeEnum.POOL.equalsToCode(registerBill.getTruckType())) {
            if (StringUtils.isBlank(registerBill.getPlate())) {
                throw new TraceBizException("车牌不能为空");
            }
        }

        //商品单价
        if (registerBill.getUnitPrice() == null) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getUnitPrice).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("商品单价不能为空");
            }
        }

        //商品规格
        if (StringUtils.isBlank(registerBill.getSpecName())) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getSpecName).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("商品规格不能为空");
            }
        }
        String specName = registerBill.getSpecName();
        if (StringUtils.isNotBlank(specName) && !RegUtils.isValidInput(specName)) {
            throw new TraceBizException("规格名称包含非法字符");
        }


        //品牌
        if (StringUtils.isBlank(registerBill.getBrandName())) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getBrandName).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("品牌不能为空");
            }
        }

        String brandName = StringUtils.trimToNull(registerBill.getBrandName());
        if(Objects.nonNull(brandName)){
            if (!RegUtils.isValidInput(brandName)) {
                throw new TraceBizException("品牌包含非法字符");
            }
            if (brandName.length()>20) {
                throw new TraceBizException("品牌不能超过20字符");
            }
        }


        //上游企业
        if (registerBill.getUpStreamId() == null) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getUpStreamId).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("上游企业不能为空");
            }
        }

        //预计到场时间
        if (registerBill.getArrivalDatetime() == null) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getArrivalDatetime).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("预计到场时间不能为空");
            }
        }

        //到货摊位
        List<String> arrivalTallynos = StreamEx.ofNullable(registerBill.getArrivalTallynos()).flatCollection(Function.identity()).filter(StringUtils::isNotBlank).toList();
        if (arrivalTallynos.isEmpty()) {
            String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getArrivalTallynos).getName();
            FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
            if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
                throw new TraceBizException("到货摊位不能为空");
            }
            boolean inValid=StreamEx.of(arrivalTallynos).anyMatch(no->{
                return !RegUtils.isValidInput(no);
            });
            if(inValid){
                throw  new TraceBizException("到货摊位包含非法字符");
            }
        }
        //图片凭证
        String propName = PropertyUtils.getPropertyDescriptor(registerBill, RegisterBill::getImageCertList).getName();
        FieldConfigDetailRetDto retDto = fieldConfigDetailRetDtoMap.getOrDefault(propName, null);
        if (retDto != null && YesOrNoEnum.YES.getCode().equals(retDto.getDisplayed()) && YesOrNoEnum.YES.getCode().equals(retDto.getRequired())) {
            if(CollectionUtils.isEmpty(registerBill.getImageCertList())){
                throw new TraceBizException("凭证不能为空");
            }else{

                String json = JSON.toJSONString(registerBill);
                List<Integer>certTypeList= JsonPathUtil.parse(json,retDto.getDefaultFieldDetail().getJsonPath(),Integer.class);
                List<Integer>availableValueList=   StreamEx.ofNullable(retDto.getAvailableValueList()).flatCollection(Function.identity()).nonNull().map(String::valueOf).map(v->{
                    try{
                        return Integer.parseInt(v);
                    }catch (Exception e){
                        return null;
                    }
                }).nonNull().toList();
                if(!availableValueList.containsAll(certTypeList)){
                    throw new TraceBizException("凭证类型错误");
                }
            }
        }



        // 计重类型，把件数和件重置空
//        if (MeasureTypeEnum.COUNT_WEIGHT.equalsCode(registerBill.getMeasureType())) {
//            registerBill.setPieceNum(BigDecimal.ZERO);
//            registerBill.setPieceWeight(BigDecimal.ZERO);
//        }

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


        if (Objects.nonNull(registerBill.getUnitPrice())
                && NumUtils.MAX_UNIT_PRICE.compareTo(registerBill.getUnitPrice()) < 0) {
            logger.error("商品单价不能大于" + NumUtils.MAX_UNIT_PRICE.toString());
            throw new TraceBizException("商品单价不能大于" + NumUtils.MAX_UNIT_PRICE.toString());
        }


        return registerBill;
    }

    /**
     * 查询当前登录用户
     *
     * @return
     */
    private UserTicket getOptUser() {
        return SessionContext.getSessionContext().getUserTicket();
    }

    /**
     * 修改单个报备单
     *
     * @param
     * @return
     */
    @Transactional
    public Long doEdit(RegisterBill input, List<ImageCert> imageCertList, Optional<OperatorUser> operatorUser) {
        if (input == null || input.getId() == null) {
            throw new TraceBizException("参数错误");
        }
        this.simpleCheck(input);
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

    /**
     * 删除报备单
     *
     * @param userId
     * @param operatorUser
     * @return
     */
    @Transactional
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
        this.userQrHistoryService.rollbackByBill(billItem);
        return billId;
    }

    /**
     * 预处理dto
     *
     * @param dto
     * @return
     */
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

    /**
     * 查找第一条由当前用户创建的待审核报备单
     *
     * @param input
     * @return
     * @throws Exception
     */
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

    /**
     * 分页查询
     *
     * @param input
     * @return
     * @throws Exception
     */
    public String listPage(RegisterBillDto input) throws Exception {
        RegisterBillDto dto = this.preBuildDTO(input);
        return this.listEasyuiPageByExample(dto, true).toString();
    }

    /**
     * 增加动态条件
     *
     * @param registerBill
     * @return
     */
    private StringBuilder buildDynamicCondition(RegisterBillDto registerBill) {
        StringBuilder sql = new StringBuilder();

        return sql;
    }

    /**
     * 进门前审核
     *
     * @param input
     * @param operatorUser
     * @return
     */
    @Transactional
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

    /**
     * 进门后审核
     *
     * @param billId
     * @param operatorUser
     * @return
     */
    @Transactional
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
            this.processService.doCheckIn(operatorUser, Lists.newArrayList(billItem.getBillId()),
                    CheckinStatusEnum.ALLOWED);
        }
        this.doVerify(billItem, toVerifyState, reason, operatorUser);
        //新增消息
        addMessage(billItem, MessageTypeEnum.BILLPASS.getCode(), MessageStateEnum.BUSINESS_TYPE_BILL.getCode(), MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_NORMAL.getCode(), billItem.getMarketId());
        return billItem.getId();

    }


    /**
     * 增加站内信
     *
     * @param billItem
     * @param messageType
     * @param businessType
     * @param receiverType
     * @param marketId
     */
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

    /**
     * 构造短信数据
     *
     * @param billItem
     * @return
     */
    private Map<String, Object> getSmsMap(RegisterBill billItem) {
        Map<String, Object> smsMap = new HashMap<>();
        smsMap.put("userName", billItem.getName());
        smsMap.put("created", DateUtils.format(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
        smsMap.put("billNo", billItem.getCode());
        smsMap.put("productName", "商品:" + billItem.getProductName() + "    车号:" + billItem.getPlate());
        return smsMap;
    }

    /**
     * 审核
     *
     * @param billItem
     * @param toVerifyState
     * @param reason
     * @param operatorUser
     */
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
        this.billVerifyHistoryService.createVerifyHistory(Optional.of(fromVerifyState), bill.getBillId(), operatorUser);

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
    public void updateUserQrStatusByUserId(Long billId, Long userId) {
        if (billId == null || userId == null) {
            return;
        }
        RegisterBill billItem = this.get(billId);
        this.userQrHistoryService.createUserQrHistoryForVerifyBill(billItem, userId);
    }


    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    public BasePage<RegisterBill> listPageBeforeCheckinVerifyBill(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLBeforeCheckIn(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
//        query.setOrderType(OrderTypeEnum.REGISTER_BILL.getCode());
        return this.listPageByExample(query);
    }

    /**
     * 统计不同审核状态报备单数据
     *
     * @param query
     * @return
     */
    public List<VerifyStatusCountOutputDto> countByVerifyStatuseBeforeCheckin(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLBeforeCheckIn(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        return this.countByVerifyStatus(query);
    }

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    public BasePage<RegisterBill> listPageAfterCheckinVerifyBill(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLAfterCheckIn(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
//        query.setOrderType(OrderTypeEnum.REGISTER_BILL.getCode());
        return this.listPageByExample(query);
    }

    /**
     * 统计不同审核状态报备单数据
     *
     * @param query
     * @return
     */
    public List<VerifyStatusCountOutputDto> countByVerifyStatuseAfterCheckin(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLAfterCheckIn(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        return this.countByVerifyStatus(query);
    }

    /**
     * 查询进门数据
     *
     * @param query
     * @return
     */
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


    /**
     * 查询详情
     *
     * @param inputDto
     * @return
     */

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

    /**
     * 增加like查询参数
     *
     * @param query
     * @return
     */
    private Optional<String> buildLikeKeyword(RegisterBillDto query) {
        String sql = null;
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            sql = "( product_name like '%" + keyword + "%'  OR name like '%" + keyword + "%'  OR (register_source=" + RegisterSourceEnum.TALLY_AREA.getCode() + " and source_name like '%" + keyword + "%') )";
        }
        return Optional.ofNullable(sql);

    }

    /**
     * 增加动态参数
     *
     * @param query
     * @return
     */
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

    /**
     * 增加动态参数
     *
     * @param query
     * @return
     */
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

    /**
     * 处理统计数据
     *
     * @param query
     * @return
     */
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

    /**
     * @return
     * @Description 给小程序提供查询接口
     * * @Date 2020/10/21 16:04
     */
    public BasePage<RegisterBill> listPageApi(RegisterBillDto input) {

        StringBuilder sql = new StringBuilder();
        buildFormLikeKeyword(input).ifPresent(sql::append);
        if (sql.length() > 0) {
            input.setMetadata(IDTO.AND_CONDITION_EXPR, sql.toString());
        }

        BasePage<RegisterBill> registerBillBasePage = listPageByExample(input);
        return registerBillBasePage;
    }

    /**
     * 构造Like查询
     *
     * @param query
     * @return
     */
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

    /**
     * 删除进门登记单
     *
     * @param dto
     * @param userId
     * @param operatorUser
     * @return
     */
    @Transactional
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
        this.userQrHistoryService.rollbackByBill(bill);
        return dto.getBillId();
    }

    /**
     * 统计不同审核状态进门登记单数据
     *
     * @param query
     * @return
     */
    public List<VerifyStatusCountOutputDto> countBillsByVerifyStatus(RegisterBillDto query) {
        if (query == null || query.getMarketId() == null) {
            throw new TraceBizException("参数错误");
        }
        query.setMetadata(IDTO.AND_CONDITION_EXPR, this.dynamicSQLFormBill(query));
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        List<VerifyStatusCountOutputDto> countList = this.countByVerifyStatus(query);

        return countList;
    }

    /**
     * 构造动态查询
     *
     * @param query
     * @return
     */
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


    /**
     * 查询用户最近操作数据
     *
     * @param input
     * @return
     * @throws Exception
     */
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