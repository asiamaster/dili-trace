package com.dili.trace.service;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.util.DateUtils;
import com.dili.trace.dao.TruckEnterRecordMapper;
import com.dili.trace.domain.TruckEnterRecord;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 司机进门报备数据
 */
@Service
public class TruckEnterRecordService extends BaseServiceImpl<TruckEnterRecord, Long> {
    @Autowired
    TruckEnterRecordMapper truckEnterRecordMapper;
    @Autowired
    UidRestfulRpcService uidRestfulRpcService;

    /**
     * 新增
     * @param truckEnterRecord
     * @return
     */
    public BaseOutput addTruckEnterRecord(TruckEnterRecord truckEnterRecord) {
        try {
            if(StringUtils.isBlank(truckEnterRecord.getTruckTypeName())){
                return BaseOutput.failure("车型不能为空");
            }
            if(StringUtils.isBlank(truckEnterRecord.getTruckPlate())){
                return BaseOutput.failure("车牌不能为空");
            }
            truckEnterRecord.setCode(this.uidRestfulRpcService.bizNumber(BizNumberType.TRUCK_ENTER_RECORD_CODE));
            truckEnterRecord.setCreated(DateUtils.getCurrentDate());
            truckEnterRecordMapper.insertSelective(truckEnterRecord);

            return BaseOutput.success();
        } catch (Exception e){
            e.printStackTrace();
            return BaseOutput.failure();
        }
    }

    /**
     * 修改
     * @param truckEnterRecord
     * @return
     */
    public BaseOutput updateTruckEnterRecord(TruckEnterRecord truckEnterRecord) {
        try {
            truckEnterRecordMapper.updateByPrimaryKeySelective(truckEnterRecord);
            return BaseOutput.success();
        } catch (Exception e){
            e.printStackTrace();
            return BaseOutput.failure();
        }
    }
}
