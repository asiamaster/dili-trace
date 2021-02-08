package com.dili.trace.api.manager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.api.enums.LoginIdentityTypeEnum;
import com.dili.trace.api.input.CheckInApiInput;
import com.dili.trace.api.input.CheckOutApiInput;
import com.dili.trace.domain.CheckinOutRecord;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.CheckinStatusEnum;
import com.dili.trace.glossary.TFEnum;
import com.dili.trace.service.CheckinOutRecordService;
import com.dili.trace.service.RegisterBillService;
import com.dili.trace.service.SeparateSalesRecordService;
import com.dili.trace.service.UpStreamService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import one.util.streamex.StreamEx;

/**
 * (管理员)进出门操作接口
 */
@SuppressWarnings("deprecation")
@Api(value = "/api/manager/managerCheckinOutRecordApi")
@RestController
@AppAccess(role = Role.Manager,url = "dili-trace-app-auth",subRoles = {})
@RequestMapping(value = "/api/manager/managerCheckinOutRecordApi")
public class ManagerCheckinOutRecordApi {
	private static final Logger logger = LoggerFactory.getLogger(ManagerCheckinOutRecordApi.class);

	@Autowired
	private LoginSessionContext sessionContext;
	@Autowired
	RegisterBillService registerBillService;
	@Autowired
	CheckinOutRecordService checkinOutRecordService;
	@Autowired
	SeparateSalesRecordService separateSalesRecordService;
	@Autowired
	UpStreamService upStreamService;

	/**
	 * 进门验货
	 */
	@RequestMapping(value = "/doCheckin.api", method = { RequestMethod.POST, RequestMethod.GET })
	public BaseOutput<Long> doCheckin(@RequestBody CheckInApiInput input) {
		if (input == null || input.getBillIdList() == null) {
			return BaseOutput.failure("参数错误");
		}
		List<Long> billIdList = StreamEx.of(input.getBillIdList()).nonNull().toList();
		if (billIdList.isEmpty()) {
			return BaseOutput.failure("参数错误");
		}
		try {
			SessionData sessionData = this.sessionContext.getSessionData();

			OperatorUser operatorUser = new OperatorUser(sessionData.getUserId(), sessionData.getUserName());


			RegisterBillDto query = new RegisterBillDto();
			query.setIdList(billIdList);
			query.setIsDeleted(YesOrNoEnum.NO.getCode());
			StreamEx.of(this.registerBillService.listByExample(query)).forEach(billItem -> {
				if (!BillVerifyStatusEnum.PASSED.equalsToCode(billItem.getVerifyStatus())
						&& !BillVerifyStatusEnum.RETURNED.equalsToCode(billItem.getVerifyStatus())) {
					throw new TraceBizException("当前状态不能进行进门操作");
				}
			});

			List<CheckinOutRecord> checkinRecordList = this.checkinOutRecordService
					.doCheckin(Optional.ofNullable(operatorUser), billIdList, CheckinStatusEnum.ALLOWED);
			return BaseOutput.success();
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("服务端出错");
		}
	}

	/**
	 * 出门验货
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/doCheckout.api", method = { RequestMethod.POST, RequestMethod.GET })
	public BaseOutput doCheckout(@RequestBody CheckOutApiInput input) {
		try {
			SessionData sessionData = this.sessionContext.getSessionData();

			OperatorUser operatorUser = new OperatorUser(sessionData.getUserId(), sessionData.getUserName());

			List<CheckinOutRecord> checkinRecordList = this.checkinOutRecordService
					.doCheckout(new OperatorUser(sessionData.getUserId(), sessionData.getUserName()), input);
			return BaseOutput.success();
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("服务端出错");
		}

	}

	/**
	 * 分页查询需要出场查询的信息
	 */
	@RequestMapping(value = "/listPageCheckInData.api", method = { RequestMethod.POST })
	public BaseOutput<Map<Integer, Map<String,List<RegisterBill>>>> listPageCheckInData(@RequestBody RegisterBillDto query) {
		try {
			if (query == null || query.getUserId() == null) {
				return BaseOutput.failure("参数错误");
			}
			SessionData sessionData = this.sessionContext.getSessionData();

			Long operatorUserId = sessionData.getUserId();
			Map<Integer, Map<String,List<RegisterBill>>> resultMap = this.registerBillService.listPageCheckInData(query);
			return BaseOutput.success().setData(resultMap);
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("服务端出错");
		}
	}

}