package com.dili.trace.api.manager;

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
import com.dili.trace.enums.BillVerifyStatusEnum;
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
 * 检测请求接口
 *
 * @author w
 */
@RestController
@RequestMapping(value = "/api/manager/managerDetectRquestApi")
@Api(value = "/api/manager/managerDetectRquestApi", description = "登记单相关接口")
@AppAccess(role = Role.Manager)
public class ManagerDetectRquestApi {
    private static final Logger logger = LoggerFactory.getLogger(ManagerDetectRquestApi.class);
    private static final Integer MAX_NORMAL_RESULT_LENGTH = 10;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    BillService billService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    SgRegisterBillService registerBillService;
    @Autowired
    private LoginSessionContext sessionContext;
    @Autowired
    UserRpcService userRpcService;

    /**
     * 查询检测请求列表
     *
     * @param detectRequestDto
     * @return
     */
    @RequestMapping("/listPagedDetectRequest.api")
    public BaseOutput<BasePage<DetectRequestOutDto>> listPagedDetectRequest(@RequestBody DetectRequestQueryDto detectRequestDto) {

        List<Integer>detectStatusList=StreamEx.ofNullable(detectRequestDto.getDetectStatusList())
                .flatCollection(Function.identity()).nonNull().toList();
        if (null == detectRequestDto.getDetectStatus()&&detectStatusList.isEmpty()) {
            return BaseOutput.failure("参数错误");
        }
        detectRequestDto.setIsDeleted(YesOrNoEnum.NO.getCode());
        detectRequestDto.setDetectStatusList(detectStatusList);
        detectRequestDto.setMarketId(this.sessionContext.getSessionData().getMarketId());
        if(StringUtils.isBlank(detectRequestDto.getSort())){
            detectRequestDto.setSort("created");
            detectRequestDto.setOrder("desc");
        }

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
        }catch (TraceBizException e){
            return BaseOutput.failure(e.getMessage());
        }catch (Exception e){
            logger.error(e.getMessage(),e);
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
            RegisterBill upBill = new RegisterBill();
            upBill.setDetectStatus(DetectStatusEnum.RETURN_DETECT.getCode());
            upBill.setId(bill.getId());
            billService.updateSelective(upBill);
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
            if(bill==null){
                return BaseOutput.failure("数据不存在");
            }
            if(BillVerifyStatusEnum.NO_PASSED.equalsToCode(bill.getVerifyStatus())||BillVerifyStatusEnum.DELETED.equalsToCode(bill.getVerifyStatus())){
                return BaseOutput.failure("当前登记单不能进行接单");
            }
//            //未填写时间时，设置为当前时间
//            if (null == detectRequestDto.getDetectTime()) {
//                detectRequestDto.setDetectTime(DateUtils.getCurrentDate());
//            }
//            //未填写检测人员时设置为当前登录人
//            if (null == detectRequestDto.getDetectorId()) {
//                detectRequestDto.setDetectorId(sessionContext.getAccountId());
//                detectRequestDto.setDetectorName(sessionContext.getUserName());
//            }
            detectRequestService.receiveDetectRequest(bill.getId(), detectRequestDto);
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
        queryInput.setIsDeleted(YesOrNoEnum.NO.getCode());
        queryInput.setMarketId(this.sessionContext.getSessionData().getMarketId());
        Map<Integer,Integer>statusCntMap= StreamEx.of(this.detectRequestService.countByDetectStatus(queryInput))
                .toMap(CountDetectStatusDto::getDetectStatus, CountDetectStatusDto::getCnt);

        List<CountDetectStatusDto> list = StreamEx.of(DetectStatusEnum.values()).map(e->{
            CountDetectStatusDto dto=new CountDetectStatusDto();
            dto.setCnt(statusCntMap.getOrDefault(e.getCode(),0));
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
            query.setMarketId(this.sessionContext.getSessionData().getMarketId());
            if(StringUtils.isBlank(query.getSort())){
                query.setSort("created");
                query.setOrder("desc");
            }
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
            if (detectRequest == null) {
                logger.error("检测单[{}]不存在，请联系管理员!", id);
                return BaseOutput.failure("检测单["+ id +"]不存在，请联系管理员!");
            }
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
//            input.setDetectResult(input.getDetectState());
//            DetectResultEnum detectResultEnum= DetectResultEnum.fromCode(input.getDetectResult()).orElseThrow(()->{
//                return  new TraceBizException("检测结果不正确");
//            });
//
//            DetectTypeEnum detectTypeEnum= DetectTypeEnum.fromCode(input.getDetectType()).orElseThrow(()->{
//                return  new TraceBizException("检测类型不正确");
//            });
            if (StringUtils.length(input.getNormalResult()) > MAX_NORMAL_RESULT_LENGTH){
                logger.error("标准值长度大于10");
                return BaseOutput.failure("标准值长度大于10");
            }
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


            int detectRecordId = this.detectRecordService.saveDetectRecordManually(input, this.sessionContext.getSessionData().getOptUser());

            return BaseOutput.success().setData(detectRecordId);
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
            registerBillService.autoCheckRegisterBillFromApp(id,sessionData.getOptUser().get());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
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
            registerBillService.samplingCheckRegisterBillFromApp(id, sessionData.getOptUser().get());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
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
            registerBillService.spotCheckRegisterBillFromApp(id, sessionData.getOptUser().get());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        }
        return BaseOutput.success("操作成功");
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
     * 退回
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/doReturn.api", method = RequestMethod.POST)
    public BaseOutput doReturn(@RequestBody RegisterBill input) {
        try {
            Long marketId = this.sessionContext.getSessionData().getMarketId();
            detectRequestService.doReturn(input);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }
}
