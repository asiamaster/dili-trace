package com.dili.trace.api;

import com.dili.common.entity.LoginSessionContext;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by laikui on 2019/7/26.
 * //交易区的客户信息
 */
@RestController
@RequestMapping(value = "/api/trade/customer")
//@InterceptConfiguration
@Api(value ="/api/trade/customer", description = "交易客户相关接口")
public class TradeCustomerApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeCustomerApi.class);
    @Autowired
    private CustomerRpcService customerService;
    @Autowired
    private UserService userService;
    @Autowired
    private LoginSessionContext loginSessionContext;

    /**
     * 根据客户账号获取
     * @param id
     * @return
     */
   /* @ApiOperation("根据客户账号获取")
    @RequestMapping(value = "/id/{id}",method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<Customer> findByCustomerId( @PathVariable String id){
        return customerService.findByCustomerId(id).map(c->{
            return BaseOutput.success().setData(c);
        }).orElse(BaseOutput.failure());
    }*/
    /**
     * 根据客户账号获取
     * @param printingCard
     * @return
     */
    /*@ApiOperation("根据客户账号获取")
    @RequestMapping(value = "/card/{printingCard}",method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<Customer> findByPrintingCard( @PathVariable String printingCard){
        return customerService.findByPrintingCard(cardNo).map(c->{
            return BaseOutput.success().setData(c);
        }).orElse(BaseOutput.failure());
    }*/


    /**
     * 根据客户账号获取
     * @param tallyAreaNo
     * @return
     */
    @ApiOperation("根据理货区号获取客户获取")
    @RequestMapping(value = "/tallyAreaNo/{tallyAreaNo}",method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<UserInfo> findTallyAreaNo(@PathVariable String tallyAreaNo){
        UserInfo customer = userService.findByTallyAreaNo(tallyAreaNo,loginSessionContext.getSessionData().getMarketId());
        if(customer!=null){
            return BaseOutput.success().setData(customer);
        }else {
            return BaseOutput.failure();
        }
    }

}
