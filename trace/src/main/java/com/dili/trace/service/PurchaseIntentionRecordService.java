package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.Customer;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.util.DateUtils;
import com.dili.trace.domain.PurchaseIntentionRecord;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.DetectStatusEnum;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.glossary.RegisterBilCreationSourceEnum;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import com.dili.trace.util.MarketUtil;
import com.dili.uap.sdk.domain.User;
import com.dili.uap.sdk.rpc.UserRpc;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author wgf
 */
@Service
public class PurchaseIntentionRecordService extends BaseServiceImpl<PurchaseIntentionRecord, Long> {

    @Autowired
    private BillService billService;
    @Autowired
    private UidRestfulRpcService uidRestfulRpcService;
    @Resource
    private CustomerRpc customerRpc;
    /**
     * 买家报备新增
     *
     * @param record
     */
    @Transactional(rollbackFor = Exception.class)
    public void doAddPurchaseIntentionRecord(PurchaseIntentionRecord record, OperatorUser operatorUser) {
        //新增报备单
        //RegisterBill bill = buildBillRecord(record, operatorUser);
        //createAddBill(bill);
        //record.setCode(bill.getCode());
        //新增买家报备记录
        record.setCode(this.uidRestfulRpcService.bizNumber(BizNumberType.PURCHASE_INTENTION_RECORD_CODE));
        record.setCreated(DateUtils.getCurrentDate());
        record.setModified(DateUtils.getCurrentDate());
        insertSelective(record);
    }

    /**
     * 构建报备单
     *
     * @param record
     * @return
     */
    private RegisterBill buildBillRecord(PurchaseIntentionRecord record, OperatorUser operatorUser) {
        if (record.getProductWeight() == null || record.getProductWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TraceBizException("重量不能小于零");
        }
        if (StringUtils.isBlank(record.getBuyerName())||null==record.getBuyerId()) {
            throw new TraceBizException("买家名称不能为空");
        }
        BaseOutput<Customer> byId = customerRpc.getById(record.getBuyerId());
        RegisterBill bill = new RegisterBill();
        if(null!=byId){
            Customer user = byId.getData();
            if(null!=user){
                bill.setIdCardNo(user.getCertificateNumber());
                bill.setPhone(user.getContactsPhone());
            }
        }
        bill.setCode(this.uidRestfulRpcService.bizNumber(BizNumberType.PURCHASE_INTENTION_RECORD_CODE));
        bill.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
        bill.setDetectStatus(DetectStatusEnum.WAIT_DESIGNATED.getCode());
        bill.setRegisterSource(RegisterSourceEnum.OTHERS.getCode());
        bill.setProductId(record.getProductId());
        bill.setProductName(record.getProductName());
        bill.setWeight(record.getProductWeight());
        bill.setWeightUnit(record.getWeightUnit());
        bill.setCreated(new Date());
        bill.setModified(new Date());
        bill.setCreationSource(RegisterBilCreationSourceEnum.PC.getCode());
        bill.setOperatorId(operatorUser.getId());
        bill.setOperatorName(operatorUser.getName());
        bill.setPlate(record.getPlate());
        bill.setMarketId(record.getMarketId());
        bill.setName(record.getBuyerName());
        bill.setIsPrintCheckSheet(YesOrNoEnum.NO.getCode());
        return bill;
    }

    /**
     * 新增报备单
     *
     * @param bill
     */
    private void createAddBill(RegisterBill bill) {
        this.billService.insertSelective(bill);
    }

    /**
     * 修改买家报备
     *
     * @param record
     */
    @Transactional(rollbackFor = Exception.class)
    public void doUpdate(PurchaseIntentionRecord record) {
        //修改报备记录
        updateSelective(record);
        //修改报备信息
        updateRecordBill(record);
    }

    /**
     * 修改报备信息
     *
     * @param record
     */
    private void updateRecordBill(PurchaseIntentionRecord record) {
        RegisterBill bill = new RegisterBill();
        bill.setCode(record.getCode());
        List<RegisterBill> billList = billService.listByExample(bill);
        if (CollectionUtils.isEmpty(billList)) {
            throw new TraceBizException("未关联到报备信息");
        }
        if (null != record.getProductId()) {
            bill.setProductId(record.getProductId());
            bill.setProductName(record.getProductName());
        }
        bill.setWeight(record.getProductWeight());
        bill.setWeightUnit(record.getWeightUnit());
        bill.setPlate(record.getPlate());
        bill.setUserId(record.getBuyerId());
        bill.setName(record.getBuyerName());
        billService.updateSelective(bill);
    }
}
