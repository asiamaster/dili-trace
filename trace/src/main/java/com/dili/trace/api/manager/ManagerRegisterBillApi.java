package com.dili.trace.api.manager;

import com.alibaba.fastjson.JSON;
import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.RegisterHead;
import com.dili.trace.dto.CreateListBillParam;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.CreatorRoleEnum;
import com.dili.trace.enums.RegistTypeEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.ImageCertService;
import com.dili.trace.service.RegisterBillService;
import com.dili.trace.service.RegisterHeadService;
import com.dili.trace.service.UpStreamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * (管理员)登记单接口
 */
@RestController
@RequestMapping(value = "/api/manager/managerRegisterBill")
@Api(value = "/api/manager/managerRegisterBill", description = "登记单相关接口")
@AppAccess(role = Role.Manager, url = "dili-trace-app-auth", subRoles = {})
public class ManagerRegisterBillApi {
    private static final Logger logger = LoggerFactory.getLogger(ManagerRegisterBillApi.class);
    @Autowired
    private RegisterBillService registerBillService;
    @Autowired
    private LoginSessionContext sessionContext;
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    UpStreamService upStreamService;
    @Autowired
    ImageCertService imageCertService;
    @Autowired
    RegisterHeadService registerHeadService;

    // @ApiOperation(value = "获得登记单详情")
    // @RequestMapping(value = "/viewRegisterBill.api", method = RequestMethod.POST)
    // public BaseOutput<BasePage<RegisterBill>> viewRegisterBill(@RequestBody RegisterBillDto input) {
    // 	if (input == null || input.getBillId() == null) {
    // 		return BaseOutput.failure("参数错误");
    // 	}
    // 	try {
    // 		OperatorUser operatorUser = sessionContext.getLoginUserOrException(LoginIdentityTypeEnum.SYS_MANAGER);
    // 		RegisterBill billItem = this.registerBillService.get(input.getBillId());
    // 		if(billItem==null){
    // 			return BaseOutput.failure("数据不存在");
    // 		}

    // 		RegisterBillOutput dto = RegisterBillOutput.build(billItem);

    // 		String upStreamName=StreamEx.ofNullable(billItem.getUpStreamId()).nonNull()
    // 		.map(upId->{return this.upStreamService.get(upId);})
    // 		.nonNull().map(UpStream::getName).findFirst().orElse("");
    // 		dto.setUpStreamName(upStreamName);
    // 		dto.setImageCertList(this.imageCertService.findImageCertListByBillId(billItem.getId()));
    // 		return BaseOutput.success().setData(dto);

    // 	} catch (TraceBusinessException e) {
    // 		return BaseOutput.failure(e.getMessage());
    // 	} catch (Exception e) {
    // 		logger.error(e.getMessage(), e);
    // 		return BaseOutput.failure("操作失败：服务端出错");
    // 	}
    // }

    /**
     * 为经营户创建登记单
     *
     * @return
     */
    @ApiOperation(value = "为经营户创建登记单")
    @RequestMapping(value = "/createRegisterBillForSeller.api", method = RequestMethod.POST)
    public BaseOutput createRegisterBillForSeller(@RequestBody CreateListBillParam input) {
        logger.info("保存多个登记单:{}", JSON.toJSONString(input));
        if (input == null || input.getRegisterBills() == null) {
            return BaseOutput.failure("参数错误");
        }
        SessionData sessionData = this.sessionContext.getSessionData();
        try {

            logger.info("保存多个登记单操作用户:{}，{}", sessionData.getUserId(), sessionData.getUserName());
            List<Long> idList = this.registerBillService.createRegisterBillList(sessionData.getMarketId(),input.getRegisterBills(), input.getUserId()
                    , sessionData.getOptUser(),
                    CreatorRoleEnum.MANAGER);
            return BaseOutput.success().setData(idList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 统计不同审核状态下报备单数量
     *
     * @return
     */
    @RequestMapping(value = "/countBillByVerifyStatus.api", method = RequestMethod.POST)
    public BaseOutput<List<VerifyStatusCountOutputDto>> countBillByVerifyStatus(@RequestBody RegisterBillDto query) {
        query.setMarketId(this.sessionContext.getSessionData().getMarketId());
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        this.registerBillService.countBillsByVerifyStatus(query);

        return BaseOutput.success();

    }

    /**
     * 分页查询报备单
     *
     * @param query
     * @return
     */
    @RequestMapping("/listPagedBill.api")
    public BaseOutput listPagedBill(@RequestBody RegisterBillDto query) {
        query.setMarketId(this.sessionContext.getSessionData().getMarketId());
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        BasePage<RegisterBill> page = this.registerBillService.listPageByExample(query);

        return BaseOutput.successData(page);
    }

    /**
     * 查看登记单详情信息
     *
     * @return
     */
    @RequestMapping(value = "/viewRegisterBill.api", method = RequestMethod.POST)
    public BaseOutput<RegisterBill> viewRegisterBill(@RequestBody RegisterBillDto query) {
        if (query == null || query.getId() == null) {
            return BaseOutput.failure("参数错误");
        }
        query.setMarketId(this.sessionContext.getSessionData().getMarketId());
        query.setIsDeleted(YesOrNoEnum.NO.getCode());
        return StreamEx.of(this.registerBillService.listByExample(query)).findFirst().map(bill -> {
            List<ImageCert> imageCerts = imageCertService.findImageCertListByBillId(bill.getId(), BillTypeEnum.fromCode(bill.getBillType()).orElse(null));
            bill.setImageCertList(imageCerts);

            Optional.ofNullable(upStreamService.get(bill.getUpStreamId())).ifPresent(up -> {
                bill.setUpStreamName(up.getName());
            });
            if (RegistTypeEnum.PARTIAL.equalsToCode(bill.getRegistType())) {
                RegisterHead registerHead = new RegisterHead();
                registerHead.setCode(bill.getRegisterHeadCode());
                Optional<RegisterHead> rhOpt = StreamEx.of(registerHeadService.listByExample(registerHead)).findFirst();
                if (rhOpt.isEmpty()) {
                    return BaseOutput.<RegisterBill>failure("未找到主台账单");
                } else {
                    rhOpt.ifPresent(rh -> {
                        bill.setHeadWeight(rh.getWeight());
                        bill.setRemainWeight(rh.getRemainWeight());
                    });
                }
            }

            return BaseOutput.successData(bill);
        }).orElseGet(() -> {
            return BaseOutput.failure("没有找到数据");
        });
    }
}

