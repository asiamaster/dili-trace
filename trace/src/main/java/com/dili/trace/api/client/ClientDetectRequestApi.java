package com.dili.trace.api.client;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.DateUtils;
import com.dili.trace.api.input.CreateDetectRequestInputDto;
import com.dili.trace.api.input.DetectRequestInputDto;
import com.dili.trace.api.input.DetectRequestQueryDto;
import com.dili.trace.api.output.CountDetectStatusDto;
import com.dili.trace.api.output.SampleSourceCountOutputDto;
import com.dili.trace.api.output.SampleSourceListOutputDto;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.domain.DetectRecord;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.DetectRecordInputDto;
import com.dili.trace.dto.DetectRequestOutDto;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.DetectResultEnum;
import com.dili.trace.enums.DetectStatusEnum;
import com.dili.trace.enums.DetectTypeEnum;
import com.dili.trace.glossary.SampleSourceEnum;
import com.dili.trace.service.*;
import com.dili.trace.util.DetectRecordUtil;
import com.dili.uap.sdk.domain.User;
import com.dili.uap.sdk.domain.UserTicket;
import io.swagger.annotations.Api;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 经营户(卖家)检测单接口
 */
@RestController()
@RequestMapping("/api/client/clientDetectRequest")
@AppAccess(role = Role.Client)
@Api(value = "/api/client/clientDetectRequest", description = "(客户)检测单接口")
public class ClientDetectRequestApi {
    private static final Logger logger = LoggerFactory.getLogger(ClientDetectRequestApi.class);

    @Autowired
    LoginSessionContext loginSessionContext;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    BillService billService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    UserRpcService userRpcService;
    @Autowired
    private LoginSessionContext sessionContext;
    @Autowired
    SgRegisterBillService registerBillService;
    @Autowired
    CommissionBillService commissionBillService;

//    /**
//     * 用户创建场外委托单
//     *
//     * @param createListBillParam 小程序创建委托单信息
//     * @return 创建结果
//     */
//    @RequestMapping(value = "/createCommissionBill.api", method = RequestMethod.POST)
//    public BaseOutput<?> createCommissionBill(@RequestBody CreateListBillParam createListBillParam) {
//
//        try {
//            SessionData sessionData = this.sessionContext.getSessionData();
//
//            Long userId = sessionData.getUserId();
//            List<CreateRegisterBillInputDto> inputList = StreamEx.ofNullable(createListBillParam).filter(Objects::nonNull).map(CreateListBillParam::getRegisterBills).nonNull().flatCollection(Function.identity()).map(bill -> {
//                bill.setCreationSource(RegisterBilCreationSourceEnum.WX.getCode());
//                bill.setUserId(userId);
//                return bill;
//            }).toList();
//            if (inputList.isEmpty()) {
//                return BaseOutput.failure("参数错误");
//            }
//           List<RegisterBill>billList= StreamEx.of(inputList).map(input->{
//                logger.info("循环保存登记单:" + JSON.toJSONString(input));
//                RegisterBill registerBill = new RegisterBill();
//
//                registerBill.setUserId(this.sessionContext.getSessionData().getUserId());
//                registerBill.setName(this.sessionContext.getSessionData().getUserName());
//
//                if (registerBill.getRegisterSource() == null) {
//                    // 小程序默认理货区
//                    registerBill.setRegisterSource(RegisterSourceEnum.TALLY_AREA.getCode());
//                }
//
//                registerBill.setCreationSource(RegisterBilCreationSourceEnum.WX.getCode());
//
//               registerBill.setIsPrintCheckSheet(input.getIsPrintCheckSheet());
//                return registerBill;
//            }).toList();
//
//            List<RegisterBill> outlist = this.commissionBillService.createCommissionBillByUser(billList);
//            return BaseOutput.success();
//        } catch (TraceBizException e) {
//            return BaseOutput.failure(e.getMessage());
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return BaseOutput.failure("服务端出错");
//        }
//
//    }

    /**
     * 创建检测单
     *
     * @param input
     * @return
     */
    @RequestMapping("/createDetectRequest.api")
    public BaseOutput<Long> createDetectRequest(@RequestBody DetectRequestInputDto input) {
        if (input == null || input.getBillId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            Optional<OperatorUser> optUser = this.sessionContext.getSessionData().getOptUser();
            OperatorUser operatorUser = optUser.orElseThrow(() -> {
                return new TraceBizException("你还未登录");
            });
            DetectRequest item = this.detectRequestService.createDetectRequestForBill(input, operatorUser);

            return BaseOutput.successData(item.getId());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

//    /**
//     * 创建场外委托检测单
//     *
//     * @param input
//     * @return
//     */
//    @RequestMapping("/createOffSiteDetectRequest.api")
//    public BaseOutput<Long> createOffSiteDetectRequest(@RequestBody DetectRequestDto input) {
//        if (input == null) {
//            return BaseOutput.failure("参数错误");
//        }
//        try {
//            Long userId = loginSessionContext.getSessionData().getUserId();
//            String userName = loginSessionContext.getSessionData().getUserName();
//            Long marketId = loginSessionContext.getSessionData().getMarketId();
//
//            if (null == userId) {
//                return BaseOutput.failure("未登录或登录过期");
//            }
//            input.setCreatorId(userId);
//            input.setCreatorName(userName);
//            input.setMarketId(marketId);
//            DetectRequest item = this.detectRequestService.createOffSiteDetectRequest(input, Optional.empty());
//            return BaseOutput.successData(item.getId());
//        } catch (TraceBizException e) {
//            return BaseOutput.failure(e.getMessage());
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return BaseOutput.failure("服务端出错");
//        }
//
//    }

    /**
     * 创建检测单前报备单详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getBillDetailById.api", method = RequestMethod.GET)
    public BaseOutput<RegisterBill> getBillDetailById(Long id) {
        if (id == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            return BaseOutput.successData(billService.get(id));
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 检测单详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getDetectRequestDetail.api", method = RequestMethod.GET)
    public String getDetectRequestDetail(Long id) {
        BaseOutput<DetectRequestOutDto>output=null;
        try {
            DetectRequestQueryDto detectRequest = new DetectRequestQueryDto();
            detectRequest.setId(id);
            DetectRequestOutDto detail = detectRequestService.getDetectRequestDetail(detectRequest);

            output= BaseOutput.successData(detail);
        } catch (TraceBizException e) {
            output= BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            output= BaseOutput.failure("服务端出错");
        }
        return JSONObject.toJSONString(output, SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * 查询市场检测人员
     *
     * @param likeUserName
     * @return
     */
    @RequestMapping(value = "/getDetectUsers.api", method = RequestMethod.GET)
    public BaseOutput getDetectUsers(@RequestParam(name = "likeUserName") String likeUserName) {
        try {
            Long marketId = this.sessionContext.getSessionData().getMarketId();
            List<User> users = userRpcService.findDetectDepartmentUsers(likeUserName, marketId);
            return BaseOutput.successData(users);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 查询检测请求列表
     *
     * @param detectRequestDto
     * @return
     */
    @RequestMapping("/listPagedDetectRequest.api")
    public BaseOutput<BasePage<DetectRequestOutDto>> listPagedDetectRequest(@RequestBody DetectRequestQueryDto detectRequestDto) {

        List<Integer> detectStatusList = StreamEx.ofNullable(detectRequestDto.getDetectStatusList())
                .flatCollection(Function.identity()).nonNull().toList();
//        if (null == detectRequestDto.getDetectStatus() && detectStatusList.isEmpty()) {
//            return BaseOutput.failure("参数错误");
//        }
        detectRequestDto.setMarketId(this.sessionContext.getSessionData().getMarketId());
        detectRequestDto.setUserId(this.sessionContext.getSessionData().getUserId());
        detectRequestDto.setIsDeleted(YesOrNoEnum.NO.getCode());
        detectRequestDto.setDetectStatusList(detectStatusList);

        BasePage<DetectRequestOutDto> basePage = detectRequestService.listPageByUserCategory(detectRequestDto);
        return BaseOutput.success().setData(basePage);
    }

    /**
     * 查询检测请求详情
     *
     * @param detectRequestDto
     * @return
     */
    @RequestMapping("/getDetectRequest.api")
    public BaseOutput getDetectRequest(@RequestBody DetectRequestQueryDto detectRequestDto) {
        if (null == detectRequestDto.getId()) {
            return BaseOutput.failure("参数错误");
        }
        try {
            return BaseOutput.success().setData(detectRequestService.getDetectRequestDetail(detectRequestDto));
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询出错");
        }

    }

    /**
     * 退回检测请求
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/returnDetectRequest.api", method = RequestMethod.GET)
    public BaseOutput returnDetectRequest(Long id) {
        if (null == id) {
            return BaseOutput.failure("参数错误");
        }
        try {
            RegisterBill bill = getBillByDetectRequestId(id);
            if (bill == null) {
                return BaseOutput.failure("数据不存在");
            }
            if (!DetectStatusEnum.NONE.equalsToCode(bill.getDetectStatus())
                    && !DetectStatusEnum.WAIT_DESIGNATED.equalsToCode(bill.getDetectStatus())) {
                return BaseOutput.failure("状态已变更,不能退回");
            }
            RegisterBill upBill = new RegisterBill();
            upBill.setDetectStatus(DetectStatusEnum.RETURN_DETECT.getCode());
            upBill.setId(bill.getId());
            billService.updateSelective(upBill);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
        return BaseOutput.success();
    }

    /**
     * 检测请求接单
     *
     * @param detectRequestDto
     * @return
     */
    @RequestMapping("/receiveDetectRequest.api")
    public BaseOutput receiveDetectRequest(@RequestBody DetectRequest detectRequestDto) {
        if (null == detectRequestDto.getId()) {
            return BaseOutput.failure("参数错误");
        }
        try {
            RegisterBill bill = getBillByDetectRequestId(detectRequestDto.getId());
            //未填写时间时，设置为当前时间
            if (null == detectRequestDto.getDetectTime()) {
                detectRequestDto.setDetectTime(DateUtils.getCurrentDate());
            }
            SessionData sessionData = this.sessionContext.getSessionData();

            //未填写检测人员时设置为当前登录人
            if (null == detectRequestDto.getDetectorId()) {
                detectRequestDto.setDetectorId(sessionData.getUserId());
                detectRequestDto.setDetectorName(sessionData.getUserName());
            }
            detectRequestService.receiveDetectRequest(bill.getId(), detectRequestDto);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
        return BaseOutput.success();
    }

    /**
     * 统计不同状态数量
     *
     * @param queryInput
     * @return
     */
    @RequestMapping("/countByDetectStatus.api")
    public BaseOutput<List<CountDetectStatusDto>> countByDetectStatus(@RequestBody DetectRequestQueryDto queryInput) {
        queryInput.setMarketId(this.sessionContext.getSessionData().getMarketId());
        queryInput.setUserId(this.sessionContext.getSessionData().getUserId());

        Map<Integer, Integer> statusCntMap = StreamEx.of(this.detectRequestService.countByDetectStatus(queryInput))
                .toMap(CountDetectStatusDto::getDetectStatus, CountDetectStatusDto::getCnt);

        List<CountDetectStatusDto> list = StreamEx.of(DetectStatusEnum.values()).map(e -> {
            CountDetectStatusDto dto = new CountDetectStatusDto();
            dto.setCnt(statusCntMap.getOrDefault(e.getCode(), 0));
            dto.setDetectStatus(e.getCode());
            return dto;
        }).toList();

        return BaseOutput.successData(list);
    }

    /**
     * 根据检测请求获取报备单信息，并校验状态是否为待接单
     *
     * @param id
     * @return
     */
    private RegisterBill getBillByDetectRequestId(Long id) {
        RegisterBill queBill = new RegisterBill();
        queBill.setDetectRequestId(id);
        List<RegisterBill> billList = billService.listByExample(queBill);
        if (CollectionUtils.isEmpty(billList)) {
            throw new TraceBizException("操作失败，报备单据不存在");
        }
        RegisterBill bill = billList.get(0);
        if (!DetectStatusEnum.WAIT_DESIGNATED.equalsToCode(bill.getDetectStatus())) {
            throw new TraceBizException("操作失败，单据非待接单状态");
        }
        return bill;
    }

    /**
     * 处理检测请求列表
     *
     * @param input
     * @return
     */
    @RequestMapping("/handleDetectRequest.api")
    public BaseOutput handleDetectRequest(@RequestBody CreateDetectRequestInputDto input) {
        if (input == null || input.getDetectRequest() == null) {
            return BaseOutput.failure("参数错误");
        }
        DetectRequest detectRequest = input.getDetectRequest();
        if (detectRequest.getDetectSource() == null || detectRequest.getBillId() == null) {
            return BaseOutput.failure("参数错误");
        }
        SampleSourceEnum sampleSourceEnum = SampleSourceEnum.fromCode(input.getDetectRequest().getDetectSource()).orElseThrow(() -> {
            return new TraceBizException("检测来源错误");
        });
        if (sampleSourceEnum == SampleSourceEnum.MANUALLY) {
            if (input.getDetectRecord() == null) {
                return BaseOutput.failure("参数错误");
            }
        }
        try {
            RegisterBill billItem = this.billService.getAvaiableBill(detectRequest.getBillId()).orElseThrow(() -> {
                throw new TraceBizException("数据不存在");
            });

            detectRequest.setDetectResult(DetectResultEnum.NONE.getCode());
            detectRequest.setDetectType(DetectTypeEnum.NEW.getCode());
            detectRequest.setCreated(new Date());
            detectRequest.setModified(new Date());
            this.detectRequestService.insertSelective(detectRequest);

            //处理人工检测结果
            if (sampleSourceEnum == SampleSourceEnum.MANUALLY) {
                DetectRecord detectRecord = input.getDetectRecord();
                detectRecord.setRegisterBillCode(billItem.getCode());
                this.detectRecordService.insertSelective(detectRecord);
                RegisterBill bill = new RegisterBill();
            }

            return BaseOutput.success();
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }


    }

    /**
     * 采样检测-查询
     *
     * @param query
     * @return
     */
    @RequestMapping("/listPagedSampleSourceDetect.api")
    public BaseOutput<BasePage<SampleSourceListOutputDto>> listPagedSampleSourceDetect(@RequestBody DetectRequestQueryDto query) {
        try {
            query.setIsDeleted(YesOrNoEnum.NO.getCode());
            query.setUserId(this.sessionContext.getSessionData().getUserId());
            query.setMarketId(this.sessionContext.getSessionData().getMarketId());
            BasePage<SampleSourceListOutputDto> basePage = this.detectRequestService.listPagedSampleSourceDetect(query);
            return BaseOutput.success().setData(basePage);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("操作失败：服务端出错");
        }
    }

    /**
     * 采样检测-采样来源数量统计
     */
    @RequestMapping(value = "/countBySampleSource.api", method = {RequestMethod.POST})
    public BaseOutput<List<VerifyStatusCountOutputDto>> countBySampleSource(@RequestBody DetectRequestQueryDto query) {
        try {
            query.setMarketId(this.sessionContext.getSessionData().getMarketId());
            query.setUserId(this.sessionContext.getSessionData().getUserId());
            List<SampleSourceCountOutputDto> list = this.detectRequestService.countBySampleSource(query);
            return BaseOutput.success().setData(list);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("操作失败：服务端出错");
        }
    }

    /**
     * 采样检测-查看详情
     */
    @RequestMapping(value = "/getSampleSourceDetectDetail.api", method = {RequestMethod.GET})
    public BaseOutput<RegisterBill> getSampleSourceDetectDetail(@RequestParam(name = "id", required = true) Long id) {
        try {
            DetectRequest detectRequest = this.detectRequestService.get(id);
            RegisterBill registerBill = this.billService.getAvaiableBill(detectRequest.getBillId()).orElse(null);
            return BaseOutput.success().setData(registerBill);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("操作失败：服务端出错");
        }
    }

    /**
     * 采样检测-手动填写检测结果
     */
    @RequestMapping(value = "/createDetectRecord.api", method = {RequestMethod.POST})
    public BaseOutput<Long> createDetectRecord(@RequestBody DetectRecordInputDto input) {
        try {
            if (StringUtils.isBlank(input.getRegisterBillCode())) {
                logger.error("上传检测任务结果失败无编号");
                return BaseOutput.failure("没有对应的登记单");
            }
            if (StringUtils.isBlank(input.getDetectOperator())) {
                logger.error("上传检测任务结果失败无检测人员");
                return BaseOutput.failure("没有对应的检测人员");
            }
            DetectResultEnum detectResultEnum = DetectResultEnum.fromCode(input.getDetectResult()).orElseThrow(() -> {
                return new TraceBizException("检测结果不正确");
            });

            DetectTypeEnum detectTypeEnum = DetectTypeEnum.fromCode(input.getDetectType()).orElseThrow(() -> {
                return new TraceBizException("检测类型不正确");
            });

            if (input.getDetectTime() == null) {
                logger.error("上传检测任务结果失败无检测时间");
                return BaseOutput.failure("没有对应的检测时间");
            }
            if (StringUtils.isBlank(input.getPdResult())) {
                logger.error("上传检测任务结果失败无检测值");
                return BaseOutput.failure("没有对应的检测值");
            }
            if (input.getDetectRequestId() == null) {
                logger.error("上传检测任务结果失败无检测任务");
                return BaseOutput.failure("没有对应的检测检测任务");
            }
            input.setCreated(new Date());
            input.setModified(new Date());

            int i = this.detectRecordService.saveDetectRecordManually(input, this.sessionContext.getSessionData().getOptUser());
            return BaseOutput.success().setData(i);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("操作失败：服务端出错");
        }
    }

    /**
     * 采样检测-主动送检
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/doAutoCheck.api", method = RequestMethod.GET)
    public BaseOutput doAutoCheck(@RequestParam(name = "id", required = true) Long id) {
        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            if (sessionData == null) {
                throw new TraceBizException("用户未登录");
            }
            registerBillService.autoCheckRegisterBillFromApp(id, sessionData);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 采样检测-采样检测
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/doSamplingCheck.api", method = RequestMethod.GET)
    public BaseOutput doSamplingCheck(@RequestParam(name = "id", required = true) Long id) {
        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            if (sessionData == null) {
                throw new TraceBizException("用户未登录");
            }
            registerBillService.samplingCheckRegisterBillFromApp(id, sessionData);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }

    /**
     * 采样检测-抽检
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/doSpotCheck.api", method = RequestMethod.GET)
    public BaseOutput doSpotCheck(@RequestParam(name = "id", required = true) Long id) {
        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            if (sessionData == null) {
                throw new TraceBizException("用户未登录");
            }
            registerBillService.spotCheckRegisterBillFromApp(id, sessionData);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
        return BaseOutput.success("操作成功");
    }
}
