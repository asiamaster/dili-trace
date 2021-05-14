package com.dili.trace.controller;

import com.dili.trace.domain.TraceCustomer;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.ExtCustomerService;
import com.dili.trace.service.UapRpcService;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.domain.UserPlate;
import com.dili.trace.service.UserPlateService;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 寿光sqlserver库中相关基础信息
 */
@RestController
@RequestMapping(value = "/trade/customer")
public class TradeInfoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeInfoController.class);

    @Autowired
    private CustomerRpcService customerRpcService;
    @Autowired
    private UserPlateService userPlateService;
    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    ExtCustomerService extCustomerService;

    /**
     * 根据客户账号获取
     *
     * @param customerCode
     * @return
     */
    @ApiOperation("根据客户账号获取")
    @RequestMapping(value = "/findCustomerByCode.action", method = {RequestMethod.GET})
    public BaseOutput<TraceCustomer> findCustomerById(@RequestParam(name = "customerCode", required = false) String customerCode) {

        return StreamEx.of(this.extCustomerService.queryCustomerByCustomerCode(customerCode, uapRpcService.getCurrentFirm().orElse(null)))
                .findFirst().map(c -> {
                    return BaseOutput.success().setData(c);
                }).orElse(BaseOutput.success());
    }

    /**
     * 根据客户账号获取
     *
     * @param cardNo
     * @return
     */
    @ApiOperation("根据客户账号获取")
    @RequestMapping(value = "/findCustomerByCardNo.action", method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<TraceCustomer> findCustomerByCardNo(@RequestParam(name = "cardNo", required = false) String cardNo) {

        return StreamEx.of(this.extCustomerService.queryCustomerByCardNo(cardNo, uapRpcService.getCurrentFirm().orElse(null)))
                .findFirst().map(c -> {
                    return BaseOutput.success().setData(c);
                }).orElse(BaseOutput.success());
    }

    /**
     * 根据客户账号获取
     *
     * @param tallyAreaNo
     * @return
     */
    @ApiOperation("根据理货区号获取客户获取")
    @RequestMapping(value = "/tallyAreaNo/{tallyAreaNo}", method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<UserInfo> findUserByTallyAreaNo(@PathVariable String tallyAreaNo) {

        return BaseOutput.failure();
    }

    /**
     * 根据客户ID获取车牌号
     *
     * @param userId
     * @return
     */
    @ApiOperation("根据理货区号获取客户获取")
    @RequestMapping(value = "/findUserPlateByUserId", method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<List<UserPlate>> findUserPlateByUserId(Long userId) {

        List<UserPlate> list = this.userPlateService.findUserPlateByUserId(userId);

        return BaseOutput.success().setData(list);
    }


}
