package com.dili.trace.controller;

import com.dili.trace.domain.Customer;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.UapRpcService;
import com.dili.trace.util.MarketUtil;
import com.dili.trace.util.MaskUserInfo;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.domain.UserPlate;
import com.dili.trace.service.UserPlateService;
import com.dili.trace.service.UserService;
import com.dili.uap.sdk.session.SessionContext;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    @Resource
    private UserService userService;
    @Resource
    private UserPlateService userPlateService;
    @Autowired
    UapRpcService uapRpcService;

    /**
     * 根据客户账号获取
     *
     * @param customerCode
     * @return
     */
    @ApiOperation("根据客户账号获取")
    @RequestMapping(value = "/findCustomerByCode/{customerCode}", method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<Customer> findCustomerById(@PathVariable String customerCode) {
        Customer cust = new Customer();
        cust.setCustomerId(customerCode);
        return customerRpcService.findCustomer(cust,uapRpcService.getCurrentFirm().get().getId()).map(c -> {
            return BaseOutput.success().setData(c);
        }).orElse(BaseOutput.failure());
    }

    /**
     * 根据客户账号获取
     *
     * @param printingCard
     * @return
     */
    @ApiOperation("根据客户账号获取")
    @RequestMapping(value = "/findCustomerByCardNo/{printingCard}", method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<Customer> findCustomerByCardNo(@PathVariable String printingCard) {
        Customer cust = new Customer();
        cust.setPrintingCard(printingCard);
        return customerRpcService.findCustomer(cust,uapRpcService.getCurrentFirm().get().getId()).map(c -> {
            return BaseOutput.success().setData(c);
        }).orElse(BaseOutput.failure());
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
        UserInfo user = userService.findByTallyAreaNo(tallyAreaNo, MarketUtil.returnMarket());
        if (user != null) {
            return BaseOutput.success().setData(this.maskUser(user));
        } else {
            return BaseOutput.failure();
        }
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

    /**
     * 对用户敏感信息进行遮罩
     *
     * @param user
     * @return
     */
    private UserInfo maskUser(UserInfo user) {
        if (this.uapRpcService.hasAccess("registerBill/create.html#user")) {
            return user;
        } else {
            user.setCardNo(MaskUserInfo.maskIdNo(user.getCardNo()));
            user.setAddr(MaskUserInfo.maskAddr(user.getAddr()));
            return user;
        }

    }

}
