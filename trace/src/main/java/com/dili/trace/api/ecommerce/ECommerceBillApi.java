package com.dili.trace.api.ecommerce;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.trace.api.enums.LoginIdentityTypeEnum;
import com.dili.trace.dto.ECommerceBillInputDto;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.SeparateSalesRecord;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.common.exception.TraceBizException;
import com.dili.trace.glossary.RegisterBilCreationSourceEnum;
import com.dili.trace.service.BillService;
import com.dili.trace.service.ECommerceBillService;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.service.SeparateSalesRecordService;
import com.dili.trace.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.ss.dto.DTO;

import io.swagger.annotations.ApiOperation;

/**
 * 电商登记单接口
 */
@SuppressWarnings("deprecation")
@RestController
@RequestMapping(value = "/api/ecommerceBillApi")
@AppAccess(role = Role.Client,url = "",subRoles = {CustomerEnum.CharacterType.经营户, CustomerEnum.CharacterType.买家})
public class ECommerceBillApi {
    private static final Logger logger = LoggerFactory.getLogger(ECommerceBillApi.class);
    @Autowired
    ECommerceBillService eCommerceBillService;
    @Autowired
    BillService billService;
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    LoginSessionContext sessionContext;

    /**
     * 当前用户
     *
     * @param
     * @return
     */
    private OperatorUser getCurrentOperatorUser() {
        SessionData sessionData = this.sessionContext.getSessionData();

        OperatorUser operatorUser = new OperatorUser(sessionData.getUserId(), sessionData.getUserName());
        return operatorUser;
    }

    /**
     * 创建电商登记单
     *
     * @param input 电商登记单
     * @return 电商登记单ID
     */
    @ApiOperation("创建记录")
    @RequestMapping(value = "/createEcommerceBill.api", method = RequestMethod.POST)
    public BaseOutput<Long> createEcommerceBill(@RequestBody ECommerceBillInputDto input) {

        try {
            OperatorUser operatorUser = this.getCurrentOperatorUser();
            if (input == null || input.getBill() == null) {
                return BaseOutput.failure("参数错误");
            }

            RegisterBill bill = input.getBill();
            bill.setCreationSource(RegisterBilCreationSourceEnum.WX.getCode());

            List<SeparateSalesRecord> separateList = CollectionUtils.emptyIfNull(input.getSeparateSalesRecordList())
                    .stream().filter(Objects::nonNull).collect(Collectors.toList());

            Long billId = this.eCommerceBillService.createECommerceBill(bill, separateList, operatorUser);
            return BaseOutput.success("新增成功").setData(billId);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }

    }

    /**
     * 电商登记单列表
     *
     * @param input 查询条件
     * @return 电商登记单列表
     */
    @ApiOperation("电商登记单列表")
    @RequestMapping(value = "/listEcommerceBill.api", method = RequestMethod.POST)
    public BaseOutput<BasePage<RegisterBill>> listEcommerceBill(@RequestBody RegisterBillDto input) {
        try {
            OperatorUser operatorUser = this.getCurrentOperatorUser();
            if (input == null) {
                return BaseOutput.failure("参数错误");
            }
            input.setBillType(this.eCommerceBillService.supportedBillType().getCode());
            if (StringUtils.isBlank(input.getSort())) {
                input.setSort("created");
                input.setOrder("desc");
            }
            BasePage<RegisterBill> data = this.billService.listPageByExample(input);
            return BaseOutput.success().setData(data);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }

    }

    /**
     * 查看电商登记单详情
     *
     * @param input 查询条件
     * @return 电商登记单详情
     */
    @ApiOperation("查看电商登记单详情")
    @RequestMapping(value = "/detailEcommerceBill.api", method = RequestMethod.POST)
    public BaseOutput<RegisterBillOutputDto> detailEcommerceBill(@RequestBody RegisterBillDto input) {
        try {
            OperatorUser operatorUser = this.getCurrentOperatorUser();
            if (input == null || input.getId() == null) {
                return BaseOutput.failure("参数错误");
            }
            RegisterBillOutputDto data = this.eCommerceBillService.detailEcommerceBill(input.getId());
            return BaseOutput.success().setData(data);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }

    }

    /**
     * 撤销电商登记单
     *
     * @param input 操作条件
     * @return 撤销结果
     */
    @ApiOperation("撤销电商登记单")
    @RequestMapping(value = "/deleteEcommerceBill.api", method = RequestMethod.POST)
    public BaseOutput<?> deleteEcommerceBill(@RequestBody RegisterBillDto input) {
        try {
            OperatorUser operatorUser = this.getCurrentOperatorUser();
            Long id = this.eCommerceBillService.doDeleteEcommerceBill(input, operatorUser);
            return BaseOutput.success().setData(id);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错，请重试");
        }

    }

}
