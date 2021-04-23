package com.dili.trace.service;

import com.dili.trace.events.DetectRequestMessageEvent;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BasePage;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.api.input.DetectRequestInputDto;
import com.dili.trace.api.input.DetectRequestQueryDto;
import com.dili.trace.api.output.CountDetectStatusDto;
import com.dili.trace.api.output.SampleSourceCountOutputDto;
import com.dili.trace.api.output.SampleSourceListOutputDto;
import com.dili.trace.dao.DetectRequestMapper;
import com.dili.trace.domain.DetectRecord;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.*;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.SampleSourceEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.UserTicket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 检测请求service
 */
@Service
public class DetectRequestService extends TraceBaseService<DetectRequest, Long> {

    @Autowired
    BillService billService;
    @Autowired
    DetectRequestMapper detectRequestMapper;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    ImageCertService imageCertService;
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    MarketService marketService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    BillVerifyHistoryService verifyHistoryService;
    @Autowired
    com.dili.trace.rpc.service.UidRestfulRpcService uidRestfulRpcService;
    @Autowired
    CodeGenerateService codeGenerateService;

    /**
     * 创建检测请求
     *
     * @param inputDto     报备单ID
     * @param operatorUser 操作用户
     * @return
     */
    public DetectRequest createDetectRequestForBill(DetectRequestInputDto inputDto, OperatorUser operatorUser) {
        Long billId = inputDto.getBillId();
        RegisterBill billItem = this.billService.get(billId);
        if (billItem == null) {
            throw new TraceBizException("登记单不存在");
        }

        boolean canCreateDetectRequest = StreamEx.of(BillVerifyStatusEnum.WAIT_AUDIT, BillVerifyStatusEnum.PASSED, BillVerifyStatusEnum.RETURNED)
                .map(BillVerifyStatusEnum::getCode)
                .toList()
                .contains(billItem.getVerifyStatus());

        if (!canCreateDetectRequest) {
            throw new TraceBizException("当前状态不能创建检测");
        }
        DetectRequest detectRequest = new DetectRequest();
        detectRequest.setDetectType(DetectTypeEnum.NEW.getCode());
        detectRequest.setDetectSource(SampleSourceEnum.AUTO_CHECK.getCode());
        detectRequest.setDetectResult(DetectResultEnum.NONE.getCode());
        detectRequest.setDetectReservationTime(new Date());
        detectRequest.setDetectCode(uidRestfulRpcService.detectRequestBizNumber(operatorUser.getMarketName()));

        detectRequest.setBillId(billId);
        detectRequest.setCreated(new Date());
        detectRequest.setModified(new Date());
        this.insert(detectRequest);

        detectRequest.setCreatorId(operatorUser.getId());
        detectRequest.setCreatorName(operatorUser.getName());
        RegisterBill registerBill = new RegisterBill();
        registerBill.setId(billId);
        registerBill.setDetectStatus(DetectStatusEnum.WAIT_DESIGNATED.getCode());
        registerBill.setDetectRequestId(detectRequest.getId());
//        registerBill.setProductAliasName(inputDto.getProductAliasName());
//        registerBill.setIsPrintCheckSheet(inputDto.getIsPrintCheckSheet());
        this.billService.updateSelective(registerBill);
        return detectRequest;

    }

    /**
     * 创建默认检测请求
     *
     * @param billId       报备单ID
     * @param operatorUser 操作用户
     * @return
     */
    public DetectRequest createDefault(Long billId, Optional<OperatorUser> operatorUser) {
        DetectRequest detectRequest = new DetectRequest();
        detectRequest.setDetectType(DetectTypeEnum.NEW.getCode());
        detectRequest.setDetectSource(SampleSourceEnum.NONE.getCode());
        detectRequest.setDetectResult(DetectResultEnum.NONE.getCode());


        detectRequest.setBillId(billId);
        detectRequest.setCreated(new Date());
        detectRequest.setModified(new Date());
        this.insert(detectRequest);


        operatorUser.ifPresent(opt -> {
            detectRequest.setCreatorId(opt.getId());
            detectRequest.setCreatorName(opt.getName());
        });
        RegisterBill registerBill = new RegisterBill();
        registerBill.setId(billId);
        registerBill.setDetectRequestId(detectRequest.getId());
        this.billService.updateSelective(registerBill);
        return detectRequest;

    }

    /**
     * 查询检测请求
     *
     * @param billId
     * @return
     */
    public Optional<DetectRequest> findDetectRequestByBillId(Long billId) {
        if (billId == null) {
            return Optional.empty();
        }

        RegisterBill registerBill = this.billService.get(billId);
        if (registerBill == null) {
            return Optional.empty();
        }
        Long detectRequestId = registerBill.getDetectRequestId();
        return Optional.ofNullable(this.get(detectRequestId));
    }

    /**
     * 创建检测请求
     *
     * @param billId
     * @param creatorDto
     * @return
     */
    public Long createOtherPassedRequest(@NotNull Long billId, @NotNull IdNameDto creatorDto) {

        DetectRequest item = this.createByBillId(billId, DetectTypeEnum.NEW, creatorDto, Optional.empty());

        DetectRequest detectRequest = new DetectRequest();
        detectRequest.setId(item.getId());
        detectRequest.setDetectType(DetectTypeEnum.OTHERS.getCode());
        detectRequest.setDetectSource(SampleSourceEnum.OTHERS.getCode());
        detectRequest.setDetectResult(DetectResultEnum.PASSED.getCode());

        this.updateSelective(detectRequest);
        return item.getId();
    }

    /**
     * 更新检测结果
     *
     * @param detectRequestId
     * @param resultEnum
     * @return
     */
    public Long updateRequest(Long detectRequestId, DetectResultEnum resultEnum) {
        DetectRequest item = this.get(detectRequestId);
        if (item == null) {
            throw new TraceBizException("数据不存在");
        }
        if (DetectResultEnum.NONE == resultEnum) {
            throw new TraceBizException("检测值错误");
        }
        DetectRequest detectRequest = new DetectRequest();
        detectRequest.setId(detectRequestId);
        detectRequest.setDetectResult(resultEnum.getCode());
        this.updateSelective(detectRequest);
        return detectRequestId;
    }

    /**
     * 根据报备单创建检测请求数据
     *
     * @param billId
     * @return
     */
    public DetectRequest createByBillId(@NotNull Long billId, DetectTypeEnum detectTypeEnum, @NotNull IdNameDto creatorDto, @NotNull Optional<IdNameDto> designatedDto) {
        RegisterBill billItem = this.billService.get(billId);
        if (billItem == null) {
            throw new TraceBizException("报备单不存在");
        }

        DetectRequest detectRequest = new DetectRequest();
        detectRequest.setDetectType(detectTypeEnum.getCode());
        detectRequest.setDetectSource(SampleSourceEnum.NONE.getCode());
        detectRequest.setDetectResult(DetectResultEnum.NONE.getCode());


        detectRequest.setBillId(billItem.getBillId());
        detectRequest.setCreated(new Date());
        detectRequest.setModified(new Date());

        designatedDto.ifPresent(idname -> {
            detectRequest.setDesignatedId(idname.getId());
            detectRequest.setDesignatedName(idname.getName());
        });
        detectRequest.setCreatorId(creatorDto.getId());
        detectRequest.setCreatorName(creatorDto.getName());
        this.insert(detectRequest);

        RegisterBill registerBill = new RegisterBill();
        registerBill.setId(billId);
        registerBill.setDetectRequestId(detectRequest.getId());
        this.billService.updateSelective(registerBill);
        return detectRequest;
    }

    /**
     * 根据id集合查询
     *
     * @param detectRequestIdList
     * @return
     */
    public Map<Long, DetectRequest> findDetectRequestByIdList(List<Long> detectRequestIdList) {
        if (detectRequestIdList.isEmpty()) {
            return Maps.newHashMap();
        }

        Example example = new Example(DetectRequest.class);
        example.and().andIn("id", detectRequestIdList);
        return StreamEx.of(this.detectRequestMapper.selectByExample(example)).toMap(DetectRequest::getId, Function.identity());
    }


    /**
     * 分页查询数据
     *
     * @param dto 查询条件
     * @return
     * @throws Exception
     */
    public EasyuiPageOutput listEasyuiPageByExample(DetectRequestQueryDto dto) throws Exception {
        EasyuiPageOutput out = this.listEasyuiPageByExample(dto, true);

        // 查询报备单信息
        List<Map<?, ?>> requests = out.getRows();
        if (CollectionUtils.isNotEmpty(requests)) {
            for (Map r : requests) {
                RegisterBill registerBill = billService.get((Long) r.get("billId"));
                r.put("detectStatus", registerBill.getDetectStatus());
                r.put("detectStatusName", registerBill.getDetectStatusName());
                r.put("billCode", registerBill.getCode());
            }
        }

        out.setRows(requests);
        return out;
    }

    /**
     * 分页查询采样检测列表
     *
     * @param query
     * @return
     */
    public EasyuiPageOutput listBasePageByExample(DetectRequestWithBillDto query) throws Exception {
        BasePage<DetectRequestWithBillDto> page = super.buildQuery(query).listPageByFun(q -> {
            return this.detectRequestMapper.queryListByExample(q);
        });

        EasyuiPageOutput out = new EasyuiPageOutput();
        List results = ValueProviderUtils.buildDataByProvider(query, page.getDatas());
        out.setRows(results);
        out.setTotal(page.getTotalItem());

        return out;
    }

    /**
     * 检测请求-接单
     *
     * @param billId         登记单ID
     * @param designatedId   检测员ID
     * @param designatedName 检测员姓名
     * @param detectTime     检测时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long billId, Long designatedId, String designatedName, Date detectTime) {
        RegisterBill registerBill = this.billService.getAvaiableBill(billId).orElse(null);
        if (registerBill == null) {
            throw new TraceBizException("检测请求关联的报备单据不存在。");
        }
        DetectRequest detectRequest = this.get(registerBill.getDetectRequestId());
        if (detectRequest == null) {
            throw new TraceBizException("检测请求不存在。");
        }

        if (registerBill.getDetectStatus() != null && registerBill.getDetectStatus() >= DetectStatusEnum.WAIT_SAMPLE.getCode()) {
            throw new TraceBizException("检测请求已接单。");
        }
        // 更新报备单检测状态
        RegisterBill billParam = new RegisterBill();
        billParam.setId(registerBill.getId());
        billParam.setDetectStatus(DetectStatusEnum.WAIT_SAMPLE.getCode());
        billService.updateSelective(billParam);
        // 更新检测请求
        DetectRequest updateParam = new DetectRequest();
        updateParam.setId(detectRequest.getId());
        updateParam.setDesignatedId(designatedId);
        updateParam.setDesignatedName(designatedName);
        updateParam.setConfirmTime(new Date());
        updateParam.setScheduledDetectTime(detectTime);
        updateParam.setModified(new Date());
        updateParam.setDetectSource(SampleSourceEnum.WAIT_HANDLE.getCode());
        this.updateSelective(updateParam);
    }

    /**
     * 检测请求-退回
     *
     * @param registerBill 登记单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void doReturn(RegisterBill registerBill) {
        if (null == registerBill || registerBill.getId() == null || StringUtils.isBlank(registerBill.getReturnReason())) {
            throw new TraceBizException("参数错误");
        }
        RegisterBill billItem = billService.getAvaiableBill(registerBill.getId()).orElseThrow(() -> {
            throw new TraceBizException("登记单不存在");
        });
        if (!DetectStatusEnum.NONE.equalsToCode(billItem.getDetectStatus())
                && !DetectStatusEnum.WAIT_DESIGNATED.equalsToCode(billItem.getDetectStatus())) {
            throw new TraceBizException("登记单状态已变");
        }
        RegisterBill bill = new RegisterBill();
        bill.setId(billItem.getId());
        bill.setReturnReason(registerBill.getReturnReason());
        bill.setDetectStatus(DetectStatusEnum.RETURN_DETECT.getCode());
        billService.updateSelective(bill);
    }

    /**
     * 查询检测请i去
     *
     * @param domain
     * @return
     */
    public BasePage<DetectRequestOutDto> listPageByUserCategory(DetectRequestQueryDto domain) {
        BasePage<DetectRequestOutDto> result = super.buildQuery(domain).listPageByFun(q -> {
            List<DetectRequestOutDto> list = this.detectRequestMapper.selectListPageByUserCategory(q);
            return list;
        });
        return result;
    }

    /**
     * 根据检测状态统计
     *
     * @param queryDto
     * @return
     */
    public List<CountDetectStatusDto> countByDetectStatus(DetectRequestQueryDto queryDto) {


        return this.detectRequestMapper.countByDetectStatus(queryDto);
    }

    /**
     * 查询检测详情
     *
     * @param detectRequestDto
     * @return
     */
    public DetectRequestOutDto getDetectRequestDetail(DetectRequestQueryDto detectRequestDto) {
        DetectRequestOutDto dto = detectRequestMapper.getDetectRequestDetail(detectRequestDto);
        if (dto == null) {
            throw new TraceBizException("数据不存在");
        }
        this.verifyHistoryService.findVerifyHistoryByBillId(dto.getBillId()).ifPresent(vh -> {
            dto.setVerifyDateTime(vh.getVerifyDateTime());
            dto.setVerifyOperatorName(vh.getVerifyOperatorName());
        });
        //设置最新检测记录
        if (StringUtils.isNotBlank(dto.getBillCode())) {
            List<DetectRecord> detectRecordList = detectRecordService.findTop2AndLatest(dto.getBillCode());
            dto.setDetectRecordList(detectRecordList);
            StreamEx.ofNullable(detectRecordList).flatCollection(Function.identity()).nonNull().sortedBy(DetectRecord::getId).unordered().findFirst().ifPresent(dr -> {
                dto.setLatestDetectRecord(dr);
            });
        }
        List<ImageCert> imageCertList = this.imageCertService.findImageCertListByBillId(dto.getBillId(), BillTypeEnum.fromCode(dto.getBillType()).orElse(null));
        dto.setImageCertList(imageCertList);
        return dto;
    }

    /**
     * 检测请求接单
     *
     * @param detectRequestDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void receiveDetectRequest(Long billId, DetectRequest detectRequestDto) {
        detectRequestDto.setDetectSource(SampleSourceEnum.WAIT_HANDLE.getCode());
        detectRequestDto.setConfirmTime(new Date());
        this.updateSelective(detectRequestDto);
        RegisterBill upBill = new RegisterBill();
        upBill.setId(billId);
        upBill.setDetectStatus(DetectStatusEnum.WAIT_SAMPLE.getCode());
        billService.updateSelective(upBill);
    }

    /**
     * @param query
     * @return
     */
    public List<SampleSourceCountOutputDto> countBySampleSource(DetectRequestQueryDto query) {
        if (query == null) {
            throw new TraceBizException("参数错误");
        }
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        List<SampleSourceCountOutputDto> countList = this.doCountBySampleSource(query);
        return countList;
    }

    /**
     * @param query
     * @return
     */
    private List<SampleSourceCountOutputDto> doCountBySampleSource(DetectRequestQueryDto query) {
        List<SampleSourceCountOutputDto> dtoList = this.detectRequestMapper.countBySampleSource(query);
        Map<Integer, Integer> sampleSourceNumMap = StreamEx.of(dtoList)
                .toMap(SampleSourceCountOutputDto::getSampleSource, SampleSourceCountOutputDto::getNum);
        return StreamEx.of(SampleSourceEnum.values()).map(e -> {
            SampleSourceCountOutputDto dto = SampleSourceCountOutputDto.buildDefault(e);
            if (sampleSourceNumMap.containsKey(dto.getSampleSource())) {
                dto.setNum(sampleSourceNumMap.get(dto.getSampleSource()));
            }
            return dto;
        }).toList();
    }

    /**
     * 分页查询采样检测列表
     *
     * @param query
     * @return
     */
    public BasePage<SampleSourceListOutputDto> listPagedSampleSourceDetect(DetectRequestQueryDto query) {

        BasePage<SampleSourceListOutputDto> result = super.buildQuery(query).listPageByFun(q -> {
            return this.detectRequestMapper.queryListBySampleSource(q);
        });
        return result;
    }

    /* *//**
     * 创建场外委托单
     *
     * @param input
     * @param empty
     * @return
     *//*
    @Transactional(rollbackFor = Exception.class)
    public DetectRequest createOffSiteDetectRequest(DetectRequestDto input, Optional<OperatorUser> empty) {

        DetectRequest resultDetectRequest = new DetectRequest();
        try {
            Long upStreamId = null;
            String upName = null;
            //创建上游企业
            if (StringUtils.isNotBlank(input.getUpStreamName())) {
                Integer upCode = 10;
                OperatorUser operatorUser = new OperatorUser(input.getCreatorId(), input.getCreatorName());
                UpStreamDto upStreamDto = new UpStreamDto();
                upStreamDto.setName(input.getUpStreamName());
                upStreamDto.setUpORdown(upCode);
                upStreamDto.setSourceUserId(input.getCreatorId());
                upStreamDto.setUpstreamType(UpStreamTypeEnum.USER.getCode());
                upStreamDto.setTelphone("''");
                upStreamService.addUpstream(upStreamDto, operatorUser);
                upStreamId = upStreamDto.getId();
                upName = upStreamDto.getName();
            }
            //创建报备单
            RegisterBill registerBill = preCreateRegisterBill(input, upStreamId, upName);
            ImageCert imageCert = new ImageCert();
            List imageCerts = new ArrayList<ImageCert>();
            imageCerts.add(imageCert);
            Long billId = registerBillService.createRegisterBill(registerBill, imageCerts,
                    Optional.ofNullable(new OperatorUser(input.getCreatorId(), input.getCreatorName())));
            //创建检测请求
            resultDetectRequest = createDetectRequest(input, billId, empty);
        } catch (TraceBizException e) {
            throw new TraceBizException(e.getMessage());

        } catch (Exception e) {
            throw new TraceBizException(e.getMessage());

        }
        return resultDetectRequest;
    }*/

    /**
     * 初始化报备单参数-创建场外委托单
     *
     * @param input
     * @param upStreamId
     * @param upName
     * @return
     */
//    private RegisterBill preCreateRegisterBill(DetectRequestDto input, Long upStreamId, String upName) {
//        CustomerExtendDto user=this.customerRpcService.findCustomerByIdOrEx(input.getCreatorId(),input.getMarketId());
////        User user = userRpcService.userRpc.findUserById(input.getCreatorId()).getData();
//        RegisterBill registerBill = new RegisterBill();
//        registerBill.setName(user.getName());
////        registerBill.setIdCardNo(user);
//        registerBill.setUserId(input.getCreatorId());
//        registerBill.setBillType(BillTypeEnum.CHECK_ORDER.getCode());
//        registerBill.setTruckType(TruckTypeEnum.FULL.getCode());
//        registerBill.setUpStreamId(upStreamId);
//        registerBill.setUpStreamName(upName);
//        registerBill.setProductId(input.getProductId());
//        registerBill.setProductName(input.getProductName());
//        registerBill.setProductAliasName(input.getProductAliasName());
//        registerBill.setOriginId(input.getOriginId());
//        registerBill.setOriginName(input.getOriginName());
//        registerBill.setWeight(input.getWeight());
//        registerBill.setWeightUnit(input.getWeightUnit());
//        registerBill.setMarketId(input.getMarketId());
//        registerBill.setPreserveType(PreserveTypeEnum.NONE.getCode());
//        return registerBill;
//    }

    /**
     * 创建委托请求单并修改报备单状态
     *
     * @param input
     * @param billId
     * @param operatorUser
     */
    private DetectRequest createDetectRequest(DetectRequestInputDto input, Long billId, Optional<OperatorUser> operatorUser) {
        DetectRequest detectRequest = new DetectRequest();
        detectRequest.setDetectType(DetectTypeEnum.NEW.getCode());
        detectRequest.setDetectSource(SampleSourceEnum.AUTO_CHECK.getCode());
        detectRequest.setDetectResult(DetectResultEnum.NONE.getCode());

        detectRequest.setBillId(billId);
        detectRequest.setCreated(new Date());
        detectRequest.setModified(new Date());
        this.insert(detectRequest);

        operatorUser.ifPresent(opt -> {
            detectRequest.setCreatorId(opt.getId());
            detectRequest.setCreatorName(opt.getName());
        });
        RegisterBill registerBill = new RegisterBill();
        registerBill.setId(billId);
        registerBill.setDetectStatus(DetectStatusEnum.WAIT_DESIGNATED.getCode());
        registerBill.setDetectRequestId(detectRequest.getId());
        this.billService.updateSelective(registerBill);
        return detectRequest;
    }

    /**
     * 查询可操作事件
     *
     * @param billId
     * @return
     */
    public List<DetectRequestMessageEvent> queryEvents(Long billId) {
        if (billId == null) {
            return Lists.newArrayList();
        }
        RegisterBill item = this.billService.getAvaiableBill(billId).orElse(null);
        if (item == null) {
            return Lists.newArrayList();
        }

        List<DetectRequestMessageEvent> msgStream = Lists.newArrayList();
        //已进场、未预约检测
        boolean canSpot = CheckinStatusEnum.ALLOWED.equalsToCode(item.getCheckinStatus())
                && (DetectStatusEnum.RETURN_DETECT.equalsToCode(item.getDetectStatus()) || DetectStatusEnum.NONE.equalsToCode(item.getDetectStatus()));
        if (canSpot) {
            msgStream.add(DetectRequestMessageEvent.spotCheck);
        }
        // 只有待审核且待预约状态的报备单才可以撤销
        if (DetectStatusEnum.NONE.equalsToCode(item.getDetectStatus())
                && BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(item.getVerifyStatus())) {
            msgStream.addAll(Lists.newArrayList(DetectRequestMessageEvent.undo));
        }
        // 待审核：可以预约申请（弹框二次确认）和撤销和预约检测
        if (DetectStatusEnum.NONE.equalsToCode(item.getDetectStatus())) {

            if (BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(item.getVerifyStatus())
                    || BillVerifyStatusEnum.PASSED.equalsToCode(item.getVerifyStatus())
                    || BillVerifyStatusEnum.RETURNED.equalsToCode(item.getVerifyStatus())) {
                msgStream.add(DetectRequestMessageEvent.booking);
            }
        }

        //待审核，且未预约检测或者已退回：可以预约检测
        boolean canApp = BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(item.getVerifyStatus())
                && (DetectStatusEnum.RETURN_DETECT.equalsToCode(item.getDetectStatus()) || DetectStatusEnum.NONE.equalsToCode(item.getDetectStatus()));
        if (canApp && !BillTypeEnum.COMMISSION_BILL.equalsToCode(item.getBillType())) {
            msgStream.add(DetectRequestMessageEvent.appointment);
        }
        // 待接单：可以接单
        if (DetectStatusEnum.WAIT_DESIGNATED.equalsToCode(item.getDetectStatus())) {
            msgStream.add(DetectRequestMessageEvent.assign);
            // 待采样：可以采样检测、主动送检、人工检测
        } else if (DetectStatusEnum.WAIT_SAMPLE.equalsToCode(item.getDetectStatus())) {
            msgStream.addAll(Lists.newArrayList(DetectRequestMessageEvent.auto, DetectRequestMessageEvent.sampling));
            msgStream.add(DetectRequestMessageEvent.manual);
        }
        if (item.getDetectRequestId() != null) {
            DetectRequest detectRequest = this.get(item.getDetectRequestId());
            if (detectRequest != null) {
                // 检测不合格
                if (DetectResultEnum.FAILED.equalsToCode(detectRequest.getDetectResult())) {
                    //初检不合格且已检测，复检不合格且已检测可以进行复检
                    if (DetectTypeEnum.INITIAL_CHECK.equalsToCode(detectRequest.getDetectType())
                            && DetectStatusEnum.FINISH_DETECT.equalsToCode(item.getDetectStatus())) {
                        msgStream.add(DetectRequestMessageEvent.review);
                        msgStream.add(DetectRequestMessageEvent.batchReview);
                    } else if (DetectTypeEnum.RECHECK.equalsToCode(detectRequest.getDetectType())) {
                        if (DetectStatusEnum.FINISH_DETECT.equalsToCode(item.getDetectStatus())) {
                            msgStream.add(DetectRequestMessageEvent.review);
                            msgStream.add(DetectRequestMessageEvent.batchReview);
                        }
                        if (item.getHasHandleResult() == 0) {
                            msgStream.add(DetectRequestMessageEvent.uploadHandleResult);
                        }
                    }
                    //初检、复检不合格可以不合格处置
                    if (DetectStatusEnum.FINISH_DETECT.equalsToCode(item.getDetectStatus()) && item.getHasHandleResult() == 0) {
                        msgStream.add(DetectRequestMessageEvent.unqualifiedHandle);
                    }
                }
                // 检测合格，生成检测报告。
                else if (DetectResultEnum.PASSED.equalsToCode(detectRequest.getDetectResult())) {
                    if (item.getCheckSheetId() == null) {
                        msgStream.add(DetectRequestMessageEvent.createSheet);
                    }
                }
            }
        }
        return msgStream;
    }

    /**
     * 撤销检测请求
     *
     * @param billId
     */
    @Transactional(rollbackFor = Exception.class)
    public void undoDetectRequest(Long billId) {
        //更新报备单未已删除
        this.billService.getAvaiableBill(billId).ifPresent(rb -> {
            // 校验待审核并且待预约才能撤销
            if (!(DetectStatusEnum.NONE.equalsToCode(rb.getDetectStatus()) && BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(rb.getVerifyStatus()))) {
                throw new TraceBizException("操作失败，数据状态已改变");
            }
            RegisterBill bill = new RegisterBill();
            bill.setId(rb.getId());
            bill.setIsDeleted(YesOrNoEnum.YES.getCode());
            this.billService.updateSelective(bill);
//            if(rb.getDetectRequestId()!=null){
//                //更新检测请求
//                doUndoDetectRequest(rb.getDetectRequestId());
//            }
        });

    }

    /**
     * 更新检测请求
     *
     * @param detectRequestId
     */
    private void doUndoDetectRequest(Long detectRequestId) {
        DetectRequest detectRequest = this.get(detectRequestId);
        if (Objects.isNull(detectRequest)) {
            throw new TraceBizException("检测请求不存在");
        }
        RegisterBill registerBill = billService.get(detectRequest.getBillId());
        if (Objects.isNull(registerBill)) {
            throw new TraceBizException("检测请求对应的报备单不存在");
        }
        // 待接单才可退回
        if (DetectStatusEnum.WAIT_DESIGNATED.equalsToCode(registerBill.getDetectStatus())) {
            RegisterBill bill = new RegisterBill();
            bill.setId(registerBill.getId());
            bill.setDetectStatus(DetectStatusEnum.RETURN_DETECT.getCode());
            bill.setModified(new Date());
            this.billService.updateSelective(bill);
        } else {
            throw new TraceBizException("操作失败，数据状态已改变");
        }
    }

    /**
     * 预约申请
     *
     * @param billId
     */
    public void bookingRequest(Long billId, UserTicket userTicket) {

        this.createByBillId(billId, DetectTypeEnum.NEW, new IdNameDto(userTicket.getId(), userTicket.getUserName()), Optional.empty());

        RegisterBill registerBill = this.billService.getAvaiableBill(billId).orElseThrow(() -> {
            throw new TraceBizException("操作失败，登记单已撤销！");
        });
        // 初始【待预约】状态才可以预约申请
        if (!DetectStatusEnum.NONE.equalsToCode(registerBill.getDetectStatus())) {
            throw new TraceBizException("操作失败，数据状态已改变");
        }
        // 待审核：可以预约申请（弹框二次确认）和撤销和预约检测
        if (DetectStatusEnum.NONE.equalsToCode(registerBill.getDetectStatus())) {
            if (BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(registerBill.getVerifyStatus())
                    || BillVerifyStatusEnum.PASSED.equalsToCode(registerBill.getVerifyStatus())
                    || BillVerifyStatusEnum.RETURNED.equalsToCode(registerBill.getVerifyStatus())) {
                //donothing
            } else {
                throw new TraceBizException("操作失败，当前状态不能进行预约");
            }
        } else {
            throw new TraceBizException("操作失败，当前状态不能进行预约");
        }
//
//        if(BillVerifyStatusEnum.NO_PASSED.equalsToCode(registerBill.getVerifyStatus())){
//            throw new TraceBizException("操作失败，审核不通过不能进行预约");
//        }
        if (registerBill.getDetectRequestId() == null) {
            throw new TraceBizException("操作失败，检测请求不存在，请联系管理员！");
        }

        // 更新检测请求预约时间
        DetectRequest updateParam = new DetectRequest();
        updateParam.setId(registerBill.getDetectRequestId());
        updateParam.setDetectReservationTime(new Date());
        // 维护检测编号
        updateParam.setDetectCode(uidRestfulRpcService.detectRequestBizNumber(userTicket.getFirmName()));
        this.updateSelective(updateParam);

        // 检测状态：待预约 --> 待接单
        RegisterBill updateBill = new RegisterBill();
        updateBill.setId(billId);
        updateBill.setDetectStatus(DetectStatusEnum.WAIT_DESIGNATED.getCode());
        updateBill.setOperatorId(userTicket.getId());
        updateBill.setOperatorName(userTicket.getUserName());
        updateBill.setModified(new Date());
        this.billService.updateSelective(updateBill);
    }

    /**
     * 人工直接检测通过或者退回
     *
     * @param billId
     * @param operatorUser
     * @param detectTypeEnum
     * @param detectResultEnum
     */
    @Transactional(rollbackFor = Exception.class)
    public void manualCheck(Long detectRecordId, Long billId, Optional<OperatorUser> operatorUser, DetectTypeEnum detectTypeEnum, DetectResultEnum detectResultEnum, Date detectTime) {


        RegisterBill registerBill = this.billService.getAvaiableBill(billId).orElseThrow(() -> {
            throw new TraceBizException("操作失败，登记单已撤销！");
        });
        if (BillVerifyStatusEnum.NO_PASSED.equalsToCode(registerBill.getVerifyStatus()) || BillVerifyStatusEnum.DELETED.equalsToCode(registerBill.getVerifyStatus())) {
            throw new TraceBizException("当前登记单不能进行接单");
        }
        // 审核状态为【待采样】状态并且管理员创建的报备单才可以人工检测
        if (!DetectStatusEnum.WAIT_SAMPLE.equalsToCode(registerBill.getDetectStatus())) {
            throw new TraceBizException("操作失败，检测状态已改变！");
        }
        if (registerBill.getDetectRequestId() == null) {
            throw new TraceBizException("操作失败，检测请求不存在，请联系管理员！");
        }

        // 人工检测-报备单
        manualCheckBill(detectRecordId, registerBill, operatorUser, detectTypeEnum, detectResultEnum);

        // 人工检测-检测请求
        manualCheckDetectRequest(detectRecordId, registerBill.getDetectRequestId(), detectTypeEnum, detectResultEnum, detectTime);
    }

    /**
     * 人工检测-报备单
     *
     * @param registerBill
     * @param operatorUser
     */
    private void manualCheckBill(Long detectRecordId, RegisterBill registerBill, Optional<OperatorUser> operatorUser, DetectTypeEnum detectTypeEnum, DetectResultEnum detectResultEnum) {
        DetectRecord detectRecord = detectRecordService.get(detectRecordId);
        if (!Objects.nonNull(detectRecord)) {
            throw new TraceBizException("操作失败，检测记录不存在，请联系管理员！");
        }
        RegisterBill updateBill = new RegisterBill();
        updateBill.setId(registerBill.getId());
        updateBill.setDetectStatus(DetectStatusEnum.FINISH_DETECT.getCode());
        operatorUser.ifPresent(opt->{
            updateBill.setOperatorId(opt.getId());
            updateBill.setOperatorName(opt.getName());
        });

        updateBill.setModified(new Date());
        updateBill.setLatestDetectTime(detectRecord.getDetectTime());
        updateBill.setLatestPdResult(detectRecord.getPdResult());
        updateBill.setLatestDetectOperator(detectRecord.getDetectOperator());
        updateBill.setLatestDetectRecordId(detectRecordId);

        BillTypeEnum billTypeEnum = BillTypeEnum.fromCode(registerBill.getBillType()).orElse(null);
//        if (BillTypeEnum.REGISTER_BILL.equalsToCode(registerBill.getBillType())) {
        updateBill.setSampleCode(this.codeGenerateService.nextSampleCode(billTypeEnum));
//        } else if (BillTypeEnum.COMMISSION_BILL.equalsToCode(registerBill.getBillType())) {
//            updateBill.setSampleCode(this.codeGenerateService.nextCommissionBillSampleCode());
//        }
//        updateBill.setSampleCode(this.codeGenerateService.nextRegisterBillSampleCode());
        this.billService.updateSelective(updateBill);
    }

    /**
     * 人工检测-检测请求
     *
     * @param detectRequestId
     */
    private void manualCheckDetectRequest(Long detectRecordId, Long detectRequestId, DetectTypeEnum detectTypeEnum, DetectResultEnum detectResultEnum, Date detectTime) {
        DetectRecord detectRecord = detectRecordService.get(detectRecordId);
        if (!Objects.nonNull(detectRecord)) {
            throw new TraceBizException("操作失败，检测记录不存在，请联系管理员！");
        }
        DetectRequest updateRequest = new DetectRequest();
        updateRequest.setId(detectRequestId);
        // 采样来源
        updateRequest.setDetectSource(SampleSourceEnum.MANUALLY.getCode());
        // 采样时间
        updateRequest.setSampleTime(new Date());
        // 检测时间
        updateRequest.setDetectTime(detectTime);
        // 检测结果
        updateRequest.setDetectResult(detectResultEnum.getCode());
        // 初检
        updateRequest.setDetectType(detectTypeEnum.getCode());
        updateRequest.setModified(new Date());
        updateRequest.setDetectorName(detectRecord.getDetectOperator());
        this.updateSelective(updateRequest);
    }

    /**
     * 执行预约检测
     *
     * @param detectRequest
     */
    @Transactional(rollbackFor = Exception.class)
    public void doAppointment(DetectRequest detectRequest) {
        Long billId = detectRequest.getBillId();
        if (null == billId) {
            throw new TraceBizException("预约检测单据Id为空");
        }
        RegisterBill bill = billService.get(billId);
        Long marketId = bill.getMarketId();
        Firm firm = marketService.getMarketById(marketId).orElse(null);
        if (null == firm) {
            throw new TraceBizException("预约检测单据marketId为空");
        }
        //更新报备单检测状态
        updateBillStatus(detectRequest.getBillId(), DetectStatusEnum.WAIT_DESIGNATED.getCode());

        //生成检测单号
        String detectCode = uidRestfulRpcService.detectRequestBizNumber(firm.getName());
        detectRequest.setId(bill.getDetectRequestId());
        detectRequest.setDetectCode(detectCode);
        detectRequest.setDetectReservationTime(new Date());
        updateSelective(detectRequest);
    }

    /**
     * 更新报备单状态
     *
     * @param billId
     * @param code
     */
    private void updateBillStatus(Long billId, Integer code) {
        if (null == billId) {
            LOGGER.error("=====>>>> billId is Null");
            return;
        }
        if (null == code) {
            LOGGER.error("=====>>>> status code is Null");
            return;
        }
        RegisterBill registerBill = new RegisterBill();
        registerBill.setId(billId);
        registerBill.setDetectStatus(code);
        billService.updateSelective(registerBill);
    }

    /**
     * 抽检
     *
     * @param record
     * @param userTicket
     */
    @Transactional(rollbackFor = Exception.class)
    public void spotCheckBill(DetectRecordParam record, UserTicket userTicket) {
        if (Objects.isNull(record.getBillId())) {
            throw new TraceBizException("检测单据Id为空");
        }
        RegisterBill registerBillItem = this.billService.getAvaiableBill(record.getBillId()).orElseThrow(() -> {
            throw new TraceBizException("数据不存在或已删除");
        });

        boolean isCantSpot = BillVerifyStatusEnum.NO_PASSED.equalsToCode(registerBillItem.getVerifyStatus())
                || BillVerifyStatusEnum.DELETED.equalsToCode(registerBillItem.getVerifyStatus())
                || DetectStatusEnum.WAIT_DETECT.equalsToCode(registerBillItem.getDetectStatus())
                || DetectStatusEnum.DETECTING.equalsToCode(registerBillItem.getDetectStatus())
                || DetectStatusEnum.FINISH_DETECT.equalsToCode(registerBillItem.getDetectStatus());
        if (isCantSpot) {
            throw new TraceBizException("当前登记单状态不能进行抽检");
        }
        if (SpotTypeStatusEnum.NORMAL.equalsToCode(record.getSpotCheckType())) {
            doAutoSpotCheckBill(registerBillItem, userTicket);
        } else {
            doManualSpotCheckBill(record, registerBillItem, userTicket);
        }
    }

    /**
     * 手动录入抽检结果
     *
     * @param record
     * @param registerBillItem
     * @param userTicket
     */
    private void doManualSpotCheckBill(DetectRecordParam record, RegisterBill registerBillItem, UserTicket userTicket) {
        //更新报备单检测状态
        RegisterBill registerBill = updateBillDetectStatus(DetectStatusEnum.FINISH_DETECT.getCode(), registerBillItem, userTicket);
        billService.updateSelective(registerBill);
        // 更新检测请求
        DetectRequest updateParam = new DetectRequest();
        updateParam.setId(registerBillItem.getDetectRequestId());
        updateParam.setDetectSource(SampleSourceEnum.SPOT_CHECK.getCode());
        updateParam.setDetectorId(record.getDetectOperatorId());
        updateParam.setDetectorName(record.getDetectOperator());
        updateParam.setDetectResult(record.getDetectState());
        updateParam.setDetectTime(DateUtils.getCurrentDate());
        updateParam.setDetectFee(record.getDetectFee());
        updateParam.setSampleTime(DateUtils.getCurrentDate());
        this.updateSelective(updateParam);

        //更新检测记录
        DetectRecord newRecord = new DetectRecord();
        BeanUtils.copyProperties(record, newRecord);
        newRecord.setDetectRequestId(registerBillItem.getDetectRequestId());
        detectRecordService.insertSelective(newRecord);
    }

    /**
     * 抽检检测
     *
     * @param registerBillItem
     * @param userTicket
     */
    private void doAutoSpotCheckBill(RegisterBill registerBillItem, UserTicket userTicket) {
        //更新报备单检测状态
        RegisterBill bill = updateBillDetectStatus(DetectStatusEnum.WAIT_DETECT.getCode(), registerBillItem, userTicket);
        billService.updateSelective(bill);
        // 更新检测请求
        DetectRequest updateParam = new DetectRequest();
        updateParam.setId(registerBillItem.getDetectRequestId());
        updateParam.setDetectSource(SampleSourceEnum.SPOT_CHECK.getCode());
        this.updateSelective(updateParam);
    }

    /**
     * 处理库存
     *
     * @param billId
     */
    private void handleStock(Long billId) {
        if (Objects.isNull(billId)) {
            throw new TraceBizException("处理库存参数异常");
        }
        RegisterBill bill = billService.get(billId);
        DetectRecord record = new DetectRecord();
        record.setDetectRequestId(bill.getDetectRequestId());
        List<DetectRecord> detectRecords = detectRecordService.listByExample(record);
        List<DetectRecord> records = StreamEx.of(detectRecords).nonNull().filter(dr ->
                DetectTypeEnum.SPOT_CHECK.equalsToCode(dr.getDetectType())).sorted(Comparator.comparing(DetectRecord::getDetectTime).reversed()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(records)) {
            DetectRecord detectRecord = records.get(0);
            if (Objects.isNull(detectRecord.getDetectState())) {
                throw new TraceBizException("检测记录结果为空");
            }
            //不合格
            if (DetectRecordStateEnum.UNQUALIFIED.equalsToCode(detectRecord.getDetectState())) {
                //销毁库存
            } else {
                //释放库存
            }
        }
    }

    /**
     * 更新报备单为待检测，并生成采样编号
     *
     * @param registerBillItem
     * @param userTicket
     * @return
     */
    private RegisterBill updateBillDetectStatus(Integer opt, RegisterBill registerBillItem, UserTicket userTicket) {
        RegisterBill upBill = new RegisterBill();
        upBill.setId(registerBillItem.getId());
        upBill.setOperatorName(userTicket.getRealName());
        upBill.setOperatorId(userTicket.getId());
        upBill.setDetectStatus(opt);

        BillTypeEnum billTypeEnum = BillTypeEnum.fromCode(registerBillItem.getBillType()).orElse(null);
//        if (BillTypeEnum.REGISTER_BILL.equalsToCode(registerBill.getBillType())) {
        upBill.setSampleCode(this.codeGenerateService.nextSampleCode(billTypeEnum));

//        if (BillTypeEnum.REGISTER_BILL.equalsToCode(registerBillItem.getBillType())) {
//            upBill.setSampleCode(this.codeGenerateService.nextRegisterBillSampleCode());
//        } else if (BillTypeEnum.COMMISSION_BILL.equalsToCode(registerBillItem.getBillType())) {
//            upBill.setSampleCode(this.codeGenerateService.nextCommissionBillSampleCode());
//        }
        return upBill;
    }

    /**
     * 上传处理结果
     *
     * @param bill
     * @param userTicket
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadUnqualifiedHandle(RegisterBillOutputDto bill, UserTicket userTicket) {
        if (Objects.isNull(bill)) {
            throw new TraceBizException("上传处理结果为空");
        }
        //上传处理图片
        List<ImageCert> imageCerts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(bill.getImageCertList())) {
            imageCerts = StreamEx.of(bill.getImageCertList()).nonNull().filter(img -> {
                // 只取uid不为空，并且类型为处理结果的照片
                return StringUtils.isNotBlank(img.getUid()) && ImageCertTypeEnum.Handle_Result.equalsToCode(img.getCertType());
            }).toList();
        }
        if (CollectionUtils.isEmpty(imageCerts)) {
            throw new TraceBizException("请上传报告");
        }
        imageCertService.insertImageCert(imageCerts, bill.getId());
        if (bill.getHandleResult().trim().length() > 1000) {
            throw new TraceBizException("处理结果不能超过1000");
        }
        RegisterBill item = this.billService.getAvaiableBill(bill.getId()).orElseThrow(() -> {
            throw new TraceBizException("数据不存在或已删除");
        });
        RegisterBill example = new RegisterBill();
        example.setId(item.getId());
        example.setHandleResult(bill.getHandleResult());
        this.billService.updateSelective(example);
        this.billService.updateHasImage(item.getBillId(), imageCerts);

        //抽检类型处置销毁库存
        if (DetectTypeEnum.SPOT_CHECK.equalsToCode(bill.getDetectType())) {
            //
        }
    }
}
