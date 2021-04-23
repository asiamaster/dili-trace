package com.dili.trace.api;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.IDTO;
import com.dili.trace.api.input.RegisterBillQueryInputDto;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.CreateListBillParam;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillInputDto;
import com.dili.trace.dto.ret.FieldConfigDetailRetDto;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.FieldConfigModuleTypeEnum;
import com.dili.trace.enums.TradeOrderTypeEnum;
import com.dili.trace.service.FieldConfigDetailService;
import com.dili.trace.service.RegisterBillService;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 进门称重接口
 */
@RestController
@RequestMapping(value = "/api/weightingBill")
@AppAccess(role = Role.Manager)
public class WeightingBillApi {
    private static final Logger logger = LoggerFactory.getLogger(WeightingBillApi.class);
    @Autowired
    FieldConfigDetailService fieldConfigDetailService;

    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    LoginSessionContext loginSessionContext;


    /**
     * 进门称重应用远程调用此接口，创建登记单
     *
     * @param inputDto
     * @return
     */
    @RequestMapping("/createRegisterBill.api")
    public BaseOutput createRegisterBill(@RequestBody RegisterBill inputDto) {
        if (inputDto == null || inputDto.getMarketId() == null || inputDto.getUserId() == null || inputDto.getWeightingBillId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {

            Long billId = this.registerBillService.createWeightingRegisterBill(inputDto, this.loginSessionContext.getSessionData().getOptUser());

            return BaseOutput.successData(billId);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }

    }

    /**
     * 进门称重应用远程调用此接口，查询登记单(审核通过)
     *
     * @param inputDto
     * @return
     */
    @RequestMapping("/queryRegisterBill.api")
    public BaseOutput queryRegisterBill(@RequestBody RegisterBillDto inputDto) {

        if (inputDto == null || inputDto.getUserId() == null || inputDto.getProductId() == null) {
            return BaseOutput.failure("参数错误");
        }
        if (StringUtils.isNotBlank(inputDto.getPlate())) {
            inputDto.setPlate(inputDto.getPlate().trim().toUpperCase());
        } else {
            inputDto.setPlate(null);
        }
        try {
            inputDto.setPage(1);
            inputDto.setRows(100);
            inputDto.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
            inputDto.setIsDeleted(YesOrNoEnum.NO.getCode());
            inputDto.setVerifyStatus(BillVerifyStatusEnum.PASSED.getCode());
            inputDto.setMetadata(IDTO.AND_CONDITION_EXPR, "  weighting_bill_id is null ");
            List<RegisterBill> data = this.registerBillService.listPageByExample(inputDto).getDatas();
            return BaseOutput.successData(data);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 进门称重应用远程调用此接口，绑定数据到登记单
     *
     * @param inputDto
     * @return
     */
    @RequestMapping("/updateRegisterBill.api")
    public BaseOutput updateRegisterBill(@RequestBody RegisterBillDto inputDto) {

        if (inputDto == null || inputDto.getBillId() == null || inputDto.getWeightingBillId() == null) {
            return BaseOutput.failure("参数错误");
        }

        try {
            RegisterBill q = new RegisterBill();
            q.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
            q.setIsDeleted(YesOrNoEnum.NO.getCode());
            q.setVerifyStatus(BillVerifyStatusEnum.PASSED.getCode());
            q.setId(inputDto.getBillId());

            RegisterBill item = StreamEx.of(this.registerBillService.listByExample(q)).findFirst().orElse(null);
            if (item == null) {
                return BaseOutput.failure("数据不存在");
            }
            if (item.getWeightingBillId() != null) {
                return BaseOutput.failure("已有进门称重绑定到当前登记单");
            }

            RegisterBill updatable = new RegisterBill();
            updatable.setId(item.getId());
            updatable.setWeightingBillId(inputDto.getWeightingBillId());
            this.registerBillService.updateSelective(updatable);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

}
