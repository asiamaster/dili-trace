package com.dili.trace.service;

import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.redis.service.RedisDistributedLock;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.DetectRecordParam;
import com.dili.trace.dto.DetectTaskApiOutputDto;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.TaskGetParam;
import com.dili.trace.enums.DetectResultEnum;
import com.dili.trace.enums.DetectStatusEnum;
import com.dili.trace.enums.DetectTypeEnum;
import com.dili.trace.glossary.RegisterBilCreationSourceEnum;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 检测任务接口
 */
@Service
public class DetectTaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DetectTaskService.class);

    @Autowired
    BillService billService;
    @Autowired
    RegisterBillMapper registerBillMapper;

    @Autowired
    private DetectRecordService detectRecordService;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    RedisDistributedLock redisDistributedLock;

    @Autowired
    ProductStockService productStockService;

    /**
     * 查询检测任务
     * @param exeMachineNo
     * @param taskCount
     * @param marketId
     * @return
     */
    private List<RegisterBill> findByExeMachineNo(String exeMachineNo, int taskCount, Long marketId) {
        LOGGER.info(">>>获得检测数据-参数:findByExeMachineNo(exeMachineNo={},taskCount={})", exeMachineNo, taskCount);
        String lockKey = "lock_trace_getTask";
        try {
            redisDistributedLock.tryGetLockSync(lockKey, lockKey, 10);
            List<DetectRequest> detectRequestList = this.registerBillMapper.selectDetectingOrWaitDetectBillId(exeMachineNo, taskCount, marketId);
            if (!detectRequestList.isEmpty()) {
                List<Long> billIdList = StreamEx.of(detectRequestList).map(DetectRequest::getBillId).toList();
                List<Long> designateNameBlankBillIdList = StreamEx.of(detectRequestList).filter(dr->{
                    return StringUtils.isBlank(dr.getDesignatedName());
                }).map(DetectRequest::getBillId).toList();

                RegisterBill billDomain = new RegisterBill();
                billDomain.setExeMachineNo(exeMachineNo);
                billDomain.setDetectStatus(DetectStatusEnum.DETECTING.getCode());

                RegisterBillDto billCondition = new RegisterBillDto();
                billCondition.setIdList(billIdList);
                billCondition.setDetectStatus(DetectStatusEnum.WAIT_DETECT.getCode());
                this.billService.updateSelectiveByExample(billDomain, billCondition);


//                if(!designateNameBlankBillIdList.isEmpty()){
//                    DetectRequest rqDomain = new DetectRequest();
//                    rqDomain.setDesignatedName(exeMachineNo);
//
//                    DetectRequestQueryDto rqCondition = new DetectRequestQueryDto();
//                    rqCondition.setBillIdList(designateNameBlankBillIdList);
//                    this.detectRequestService.updateSelectiveByExample(rqDomain, rqCondition);
//                }


                RegisterBillDto domain = new RegisterBillDto();
                domain.setIdList(billIdList);
                domain.setSort("bill_type,sample_code");
                domain.setOrder("DESC,ASC");
                List<RegisterBill> list = this.billService.listByExample(domain);
                try {
                    LOGGER.info(">>>获得检测数据-返回值:findByExeMachineNo({})", JSON.toJSONString(list));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return list;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            redisDistributedLock.releaseLock(lockKey, lockKey);
        }

        return Lists.newArrayList();
    }

    /**
     * 查询检测任务
     */
    @Transactional
    public List<DetectTaskApiOutputDto> findByExeMachineNo(TaskGetParam taskGetParam) {
        String exeMachineNo = taskGetParam.getExeMachineNo();
        int taskCount = taskGetParam.getPageSize();
        List<RegisterBill> billList = this.findByExeMachineNo(exeMachineNo, taskCount, taskGetParam.getMarketId());
        return DetectTaskApiOutputDto.build(billList);
    }
    /**
     * 更新检测状态
     *
     * @param detectRecord
     * @return
     */
    @Transactional
    public BaseOutput<Boolean> updateDetectTask(DetectRecordParam detectRecord) {
        try {
            LOGGER.info(">>>提交检测数据-参数:updateDetectTask(detectRecord={})", JSON.toJSONString(detectRecord));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }


//        RegisterBill billItem = this.billService.findBySampleCode(samplecode);
//        if (billItem == null) {
//            return BaseOutput.failure("找不到检测任务");
//        }
        try {
            this.updateRegisterBill(detectRecord);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure("服务端出错");
        }

        LOGGER.info(">>>提交检测数据-返回值:updateDetectTask({})", true);
        return BaseOutput.success().setData(true);

    }

    /**
     * 更新状态值
     * @param detectRecord
     */
    private void updateRegisterBill(DetectRecordParam detectRecord) {
        // 对code,samplecode进行互换(检测客户端程序那边他们懒得改,不要动这行代码.具体看findByExeMachineNo里面的代码及注释)
        String samplecode = detectRecord.getRegisterBillCode();
        RegisterBill registerBillItem = billService.findBySampleCode(samplecode);

        if (registerBillItem == null) {
            LOGGER.error("上传检测任务结果失败该采样单号无登记单");
            throw new TraceBizException("没有对应的登记单");
        }

        if (DetectStatusEnum.FINISH_DETECT.equalsToCode(registerBillItem.getDetectStatus())) {
            LOGGER.error("上传检测任务结果失败,该单号已完成检测");
            throw new TraceBizException("已完成检测");
        }
        if(!DetectStatusEnum.DETECTING.equalsToCode(registerBillItem.getDetectStatus())){
            LOGGER.error("上传检测任务结果失败,当前状态不能进行检测");
            throw new TraceBizException("当前状态错误");
        }
        if (StringUtils.isNoneBlank(registerBillItem.getExeMachineNo())
                && !registerBillItem.getExeMachineNo().equals(detectRecord.getExeMachineNo())) {
            LOGGER.error("上传检测任务结果失败，该仪器没有获取该登记单");
            throw new TraceBizException("该仪器无权操作该单据");
        }
        DetectRequest detectRequestItem = this.detectRequestService.findDetectRequestByBillId(registerBillItem.getBillId()).orElseThrow(() -> {
            LOGGER.error("上传检测任务结果失败，检测请求不存在");
            return new TraceBizException("检测请求不存在");
        });
        DetectRequest detectRequest = new DetectRequest();
        detectRequest.setId(detectRequestItem.getId());
        if (DetectResultEnum.FAILED.equalsToCode(detectRequestItem.getDetectResult())) {
            // 复检
            /// 1.第一次送检 2：复检 状态 1.合格 2.不合格
            detectRecord.setDetectType(DetectTypeEnum.RECHECK.getCode());
        } else if(DetectResultEnum.NONE.equalsToCode(detectRequestItem.getDetectResult())){
            // 第一次检测
            detectRecord.setDetectType(DetectTypeEnum.INITIAL_CHECK.getCode());
        }
        if (DetectTypeEnum.NEW.equalsToCode(detectRequestItem.getDetectType()) && DetectResultEnum.NONE.equalsToCode(detectRequestItem.getDetectResult())) {
            detectRequest.setDetectType(DetectTypeEnum.INITIAL_CHECK.getCode());
        } else if (DetectResultEnum.FAILED.equalsToCode(detectRequestItem.getDetectResult())) {
            detectRequest.setDetectType(DetectTypeEnum.RECHECK.getCode());
        }

        detectRecord.setRegisterBillCode(registerBillItem.getCode());
        detectRecord.setDetectRequestId(detectRequestItem.getId());
        detectRecord.setCreated(new Date());
        detectRecord.setModified(new Date());
        detectRecordService.saveDetectRecord(detectRecord);

        RegisterBill registerBill = new RegisterBill();
        registerBill.setId(registerBillItem.getBillId());
        registerBill.setDetectStatus(DetectStatusEnum.FINISH_DETECT.getCode());
//		registerBill.setState(RegisterBillStateEnum.ALREADY_CHECK.getCode());
        registerBill.setLatestDetectRecordId(detectRecord.getId());
        registerBill.setLatestDetectTime(detectRecord.getDetectTime());
        registerBill.setLatestPdResult(detectRecord.getPdResult());
        registerBill.setLatestDetectOperator(detectRecord.getDetectOperator());
        registerBill.setCreationSource(RegisterBilCreationSourceEnum.WX.getCode());

        DetectResultEnum detectResultEnum = DetectResultEnum.fromCode(detectRecord.getDetectState()).orElse(null);
        detectRequest.setDetectResult(detectResultEnum.getCode());
        detectRequest.setDetectorName(detectRecord.getDetectOperator());
        detectRequest.setDetectTime(detectRecord.getDetectTime());
        detectRequest.setDetectType(detectRecord.getDetectType());

        this.detectRequestService.updateSelective(detectRequest);

        this.billService.updateSelective(registerBill);

        this.productStockService.updateDetectFailedWeightByBillIdAfterDetect(registerBill.getBillId());

    }



    

}