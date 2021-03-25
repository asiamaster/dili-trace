package com.dili.trace.controller;

import com.dili.common.entity.LoginSessionContext;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.UapRpcService;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 客户
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    CustomerRpcService customerRpcService;

    /**
     * 通过摊位号查询客户信息
     *
     * @param tallyAreaNo
     * @return
     */
    @RequestMapping("/queryUserByTallyAreaNo.action")
    @ResponseBody
    public BaseOutput queryUserByTallyAreaNo(String tallyAreaNo) {
        Long marketId = this.uapRpcService.getCurrentFirm().get().getId();
        CustomerQueryInput queryInput = new CustomerQueryInput();
        queryInput.setMarketId(marketId);
        queryInput.setAssetsName(tallyAreaNo);
        return StreamEx.of(this.customerRpcService.list(queryInput)).findFirst()
                .map(data -> BaseOutput.successData(data))
                .orElseGet(() -> BaseOutput.failure("没有数据"));


    }
}
