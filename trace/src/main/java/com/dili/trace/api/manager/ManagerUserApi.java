package com.dili.trace.api.manager;

import com.dili.assets.sdk.dto.CarTypeDTO;
import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.VehicleInfo;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.dto.CustomerSimpleExtendDto;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.trace.dto.*;
import com.dili.trace.enums.ClientTypeEnum;
import com.dili.trace.rpc.dto.AccountGetListResultDto;
import com.dili.trace.rpc.dto.CustomerQueryDto;
import com.dili.trace.rpc.service.CarTypeRpcService;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.rpc.service.VehicleRpcService;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 管理用户接口
 */
@RestController
@RequestMapping(value = "/api/manager/user")
@Api(value = "/api/manager/user", description = "用户管理相关接口")
@AppAccess(role = Role.Manager, url = "dili-trace-app-auth", subRoles = {})
public class ManagerUserApi {
    private static final Logger logger = LoggerFactory.getLogger(ManagerUserApi.class);
    @Autowired
    private LoginSessionContext sessionContext;
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    CarTypeRpcService carTypeRpcService;
    @Autowired
    VehicleRpcService vehicleRpcService;

//    /**
//     * 商户审核统计概览
//     *
//     * @param user
//     * @return
//     */
//    @ApiOperation(value = "商户审核统计概览")
//    @RequestMapping(value = "/userCertCount.api", method = RequestMethod.POST)
//    public BaseOutput<List<UserOutput>> countGroupByValidateState(@RequestBody User user) {
//        try {
////            sessionContext.getLoginUserOrException(LoginIdentityTypeEnum.SYS_MANAGER);
//            return userService.countGroupByValidateState(user);
//        } catch (TraceBizException e) {
//            return BaseOutput.failure(e.getMessage());
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return BaseOutput.failure("操作失败：服务端出错");
//        }
//    }
//
//    /**
//     * 获得用户审核列表
//     *
//     * @param user
//     * @return
//     */
//    @ApiOperation(value = "获得用户审核列表")
//    @RequestMapping(value = "/listUserCertByQuery.api", method = RequestMethod.POST)
//    public BaseOutput<BasePage<UserOutput>> listUserCertByQuery(@RequestBody UserInput user) {
//        try {
////            sessionContext.getLoginUserOrException(LoginIdentityTypeEnum.SYS_MANAGER);
//            BasePage<UserOutput> data = userService.pageUserByQuery(user);
//            return BaseOutput.success().setData(data);
//        } catch (TraceBizException e) {
//            return BaseOutput.failure(e.getMessage());
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return BaseOutput.failure("操作失败：服务端出错");
//        }
//    }
//
//    /**
//     * 获得用户资料详情
//     *
//     * @param input
//     * @return
//     */
//    @ApiOperation(value = "获得用户资料详情")
//    @RequestMapping(value = "/userCertDetail.api", method = RequestMethod.POST)
//    public BaseOutput<User> userCertDetail(@RequestBody UserInput input) {
//        if (input == null || input.getId() == null) {
//            return BaseOutput.failure("参数错误");
//        }
//        try {
////            sessionContext.getLoginUserOrException(LoginIdentityTypeEnum.SYS_MANAGER);
//            User data = userService.get(input.getId());
//            data.setPassword(null);
//            return BaseOutput.success().setData(data);
//
//        } catch (TraceBizException e) {
//            return BaseOutput.failure(e.getMessage());
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return BaseOutput.failure("操作失败：服务端出错");
//        }
//    }
//
//    /**
//     * 审核用户资料
//     *
//     * @param input
//     * @return
//     */
//    @ApiOperation(value = "审核用户资料")
//    @RequestMapping(value = "/verifyUserCert.api", method = RequestMethod.POST)
//    public BaseOutput<Long> verifyUserCert(@RequestBody UserInput input) {
//        try {
//            SessionData sessionData = this.sessionContext.getSessionData();
//            OperatorUser operatorUser =new OperatorUser(sessionData.getUserId(),sessionData.getUserName());
//            return userService.verifyUserCert(input, operatorUser);
//        } catch (TraceBizException e) {
//            return BaseOutput.failure(e.getMessage());
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return BaseOutput.failure("操作失败：服务端出错");
//        }
//
//    }

    /**
     * 查询经营户信息
     *
     * @param input
     * @return
     */
    @ApiOperation(value = "查询经营户信息")
    @RequestMapping(value = "/listSeller.api", method = RequestMethod.POST)
    public PageOutput<List<CustomerExtendOutPutDto>> listSeller(@RequestBody CustomerQueryDto input) {
        try {
            Long marketId = this.sessionContext.getSessionData().getMarketId();
            input.setMarketId(marketId);
            PageOutput<List<CustomerSimpleExtendDto>> pageOutput = this.customerRpcService.listSeller(input);

            // UAP 内置对象缺少市场名称、园区卡号，只能重新构建返回对象
            return getListPageOutput(marketId, pageOutput, ClientTypeEnum.SELLER);
        } catch (TraceBizException e) {
            return PageOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return PageOutput.failure("操作失败：服务端出错");
        }
    }

    /**
     * 查询买家信息
     *
     * @param input
     * @return
     */
    @ApiOperation(value = "查询买家信息")
    @RequestMapping(value = "/listBuyer.api", method = RequestMethod.POST)
    public PageOutput<List<CustomerExtendOutPutDto>> listBuyer(@RequestBody CustomerQueryDto input) {
        try {
            Long marketId = this.sessionContext.getSessionData().getMarketId();
            input.setMarketId(marketId);
            PageOutput<List<CustomerSimpleExtendDto>> pageOutput = this.customerRpcService.listBuyer(input);
            // UAP 内置对象缺少市场名称、园区卡号，只能重新构建返回对象
            return getListPageOutput(marketId, pageOutput, ClientTypeEnum.BUYER);
        } catch (TraceBizException e) {
            return PageOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return PageOutput.failure("操作失败：服务端出错");
        }
    }

    /**
     * 查询司机信息
     *
     * @param input
     * @return
     */
    @ApiOperation(value = "查询司机信息")
    @RequestMapping(value = "/listDriver.api", method = RequestMethod.POST)
    public BaseOutput<List<CustomerExtendOutPutDto>> listDriver(@RequestBody CustomerQueryDto input) {
        try {
            Long marketId = this.sessionContext.getSessionData().getMarketId();
            input.setMarketId(marketId);
            PageOutput<List<CustomerSimpleExtendDto>> pageOutput = this.customerRpcService.listDriver(input, marketId);
            // UAP 内置对象缺少市场名称、园区卡号，只能重新构建返回对象
            PageOutput<List<CustomerExtendOutPutDto>> pgData = getListPageOutput(marketId, pageOutput, ClientTypeEnum.DRIVER);

            List<Long> customerIdList = StreamEx.of(pgData.getData()).nonNull().map(CustomerExtendOutPutDto::getId).toList();
            Map<Long, List<VehicleInfo>> vehicleInfoMap = this.vehicleRpcService.findVehicleInfoByMarketIdAndCustomerIdList(marketId, customerIdList);

            Map<Long, String> carTypeMap = StreamEx.ofNullable(this.carTypeRpcService.listCarType()).flatCollection(Function.identity())
                    .mapToEntry(CarTypeDTO::getId, CarTypeDTO::getName).toMap();
            List<CustomerExtendOutPutDto> dataList = StreamEx.of(pgData.getData()).map(o -> {
                List<VehicleInfoDto> vehicleInfoList = StreamEx.of(vehicleInfoMap.getOrDefault(o.getId(), Lists.newArrayList())).map(v -> {
                    String carType = carTypeMap.getOrDefault(v.getTypeNumber(), "");
                    return VehicleInfoDto.build(v, carType);

                }).toList();
                o.setVehicleInfoList(vehicleInfoList);
                return o;

            }).toList();
            pgData.setData(dataList);
            return pgData;
        } catch (TraceBizException e) {
            return PageOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return PageOutput.failure("操作失败：服务端出错");
        }
    }

    /**
     * 构建卖家、买家、司机查询返回对象
     *
     * @param marketId
     * @param pageOutput
     * @return
     */
    private PageOutput<List<CustomerExtendOutPutDto>> getListPageOutput(Long marketId, PageOutput<List<CustomerSimpleExtendDto>> pageOutput, ClientTypeEnum clientTypeEnum) {
        PageOutput<List<CustomerExtendOutPutDto>> page = new PageOutput<>();
        if (null != pageOutput) {

            List<CustomerSimpleExtendDto> customerList = pageOutput.getData();
            List<CustomerExtendOutPutDto> customerOutputList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(customerList)) {
                customerList.forEach(c -> {
                    CustomerExtendOutPutDto customerOutput = new CustomerExtendOutPutDto();
                    customerOutput.setMarketId(marketId);
                    customerOutput.setMarketName(this.sessionContext.getSessionData().getMarketName());
                    customerOutput.setId(c.getId());
                    customerOutput.setName(c.getName());

                    customerOutput.setPhone(c.getContactsPhone());
                    customerOutput.setClientType(clientTypeEnum.getCode());

                    Optional<AccountGetListResultDto> cardResultDto = this.customerRpcService.queryCardInfoByCustomerCode(c.getCode(), null, marketId);
                    cardResultDto.ifPresent(cardInfo -> {
                        customerOutput.setCardNo(cardInfo.getCardNo());
                    });
                    customerOutputList.add(customerOutput);
                });
            }
            BeanUtils.copyProperties(pageOutput, page);
            page.setData(customerOutputList);
        }
        return page;
    }

}
