package com.dili.trace.api.client;

import com.alibaba.fastjson.JSON;
import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.domain.BaseDomain;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.api.output.VerifyBillInputDto;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.domain.*;
import com.dili.trace.dto.CreateListBillParam;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.RegistTypeEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
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
 * 进门登记单相关接口
 *
 * @author Lily
// */
//@RestController
//@RequestMapping(value = "/api/client/clientRegisterFormBill")
//@Api(value = "/api/client/clientRegisterFormBill", description = "进门登记单相关接口")
//@AppAccess(role = Role.Client,url = "",subRoles = {CustomerEnum.CharacterType.经营户, CustomerEnum.CharacterType.买家})
public class ClientRegisterFormBillApi {
	private static final Logger logger = LoggerFactory.getLogger(ClientRegisterFormBillApi.class);
	@Autowired
	private RegisterBillService registerBillService;

	@Autowired
	private LoginSessionContext sessionContext;

	@Autowired
    CustomerRpcService customerRpcService;

	@Autowired
	ImageCertService imageCertService;

	@Autowired
	UpStreamService upStreamService;

	@Autowired
	RegisterHeadService registerHeadService;

	/**
	 * 获取进门登记单列表
	 * @param input
	 * @return
	 */
	@ApiOperation(value = "获取进门登记单列表")
	@ApiImplicitParam(paramType = "body", name = "RegisterBill", dataType = "RegisterBill", value = "获取登记单列表")
	@RequestMapping(value = "/listPage.api", method = RequestMethod.POST)
	public BaseOutput<BasePage<RegisterBill>> listPage(@RequestBody RegisterBillDto input) {
		logger.info("获取进门登记单列表:{}", JSON.toJSONString(input));
		try {
			SessionData sessionData=this.sessionContext.getSessionData();
			Long userId = sessionData.getUserId();

			logger.info("获取进门登记单列表 操作用户:{}", userId);
			input.setSort("created");
			input.setOrder("desc");
			BasePage<RegisterBill> basePage = this.registerBillService.listPageApi(input);
			return BaseOutput.success().setData(basePage);
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("查询进门登记单数据出错");
		}

	}


	/**
	 * 作废进门登记单
	 * @param dto
	 * @return
	 */
	@ApiOperation("作废进门登记单")
	@RequestMapping(value = "/doDeleteRegisterBill.api", method = RequestMethod.POST)
	public BaseOutput doDeleteRegisterBill(@RequestBody CreateRegisterBillInputDto dto) {
		logger.info("作废进门登记单:{}", JSON.toJSONString(dto));
		if (dto == null || dto.getBillId() == null) {
			return BaseOutput.failure("参数错误");
		}
		try {
			SessionData sessionData = this.sessionContext.getSessionData();

			OperatorUser operatorUser = new OperatorUser(sessionData.getUserId(),sessionData.getUserName());
			logger.info("作废进门登记单:billId:{},userId:{}", dto.getBillId(), operatorUser.getId());
			this.registerBillService.doDelete(dto, operatorUser.getId(), Optional.ofNullable(operatorUser));
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("服务端出错");
		}
		return BaseOutput.success();
	}
	/**
	 * 不同审核状态数据统计
	 */
	@RequestMapping(value = "/countByVerifyStatus.api", method = { RequestMethod.POST })
	public BaseOutput<List<VerifyStatusCountOutputDto>> countByVerifyStatus(@RequestBody RegisterBillDto query) {

		try {
			SessionData sessionData = this.sessionContext.getSessionData();

			OperatorUser operatorUser = new OperatorUser(sessionData.getUserId(),sessionData.getUserName());
			List<VerifyStatusCountOutputDto> list = this.registerBillService.countBillsByVerifyStatus(query);
			return BaseOutput.success().setData(list);
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("操作失败：服务端出错");
		}
	}
}
