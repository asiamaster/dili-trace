package com.dili.trace.api.manager;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.ss.dto.IDTO;
import com.dili.trace.domain.PurchaseIntentionRecord;
import com.dili.trace.dto.query.PurchaseIntentionRecordQueryDto;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import com.dili.trace.service.PurchaseIntentionRecordService;
import com.dili.trace.util.NumUtils;
import com.dili.trace.util.RegUtils;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

/**
 * (管理员)买家进门报备
 */
@RestController
@RequestMapping(value = "/api/manager/managerPurchaseIntentionRecordApi", method = RequestMethod.POST)
@Api(value = "/api/manager/managerPurchaseIntentionRecordApi", description = "登记单相关接口")
@AppAccess(role = Role.Manager, url = "dili-trace-app-auth", subRoles = {})
public class ManagerPurchaseIntentionRecordApi {

    @Autowired
    LoginSessionContext sessionContext;
    @Autowired
    PurchaseIntentionRecordService purchaseIntentionRecordService;
    @Autowired
    UidRestfulRpcService uidRestfulRpcService;

    /**
     * 查询买家进门报备信息
     *
     * @param queryInput
     * @return
     */
    @RequestMapping(value = "/listPage.api")
    public BaseOutput<BasePage<PurchaseIntentionRecord>> listPage(@RequestBody PurchaseIntentionRecordQueryDto queryInput) {
        SessionData sessionData = this.sessionContext.getSessionData();
        queryInput.setMarketId(sessionData.getMarketId());
        if(StringUtils.isNotBlank(queryInput.getKeyword())){
            queryInput.setMetadata(IDTO.AND_CONDITION_EXPR,
                    ( "( buyer_name like '" + queryInput.getKeyword().trim()
                    + "%'  or buyer_phone like '" + queryInput.getKeyword().trim() + "%')" ));
        }
        BasePage<PurchaseIntentionRecord> data = this.purchaseIntentionRecordService.listPageByExample(queryInput);

        return BaseOutput.successData(data);

    }

    /**
     * 为买家创建进门报备信息
     *
     * @param input
     * @return
     */
    @RequestMapping(value = "/createPurchaseIntentionRecord.api")
    public BaseOutput createTruckEnterRecord(@RequestBody PurchaseIntentionRecord input) {
        if (input.getProductWeight() != null){
            if (input.getProductWeight().compareTo(NumUtils.MAX_WEIGHT) == 1){
                return BaseOutput.failure("商品重量不能大于" + NumUtils.MAX_WEIGHT.toString());
            }
        }
        SessionData sessionData = this.sessionContext.getSessionData();

        if(StringUtils.isNotBlank(input.getPlate())&&!RegUtils.isPlate(input.getPlate())){
            return BaseOutput.failure("车牌格式错误");
        }
        input.setMarketId(sessionData.getMarketId());
        input.setCreated(new Date());
        input.setModified(new Date());
        input.setCode(uidRestfulRpcService.bizNumber(BizNumberType.PURCHASE_INTENTION_RECORD_CODE));
        this.purchaseIntentionRecordService.insertSelective(input);
        return BaseOutput.successData(input.getId());
    }

}
