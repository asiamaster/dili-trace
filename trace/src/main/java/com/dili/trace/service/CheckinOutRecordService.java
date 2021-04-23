package com.dili.trace.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.api.input.CheckOutApiInput;
import com.dili.trace.domain.CheckinOutRecord;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.CheckinOutTypeEnum;
import com.dili.trace.enums.CheckinStatusEnum;
import com.dili.trace.enums.CheckoutStatusEnum;
import com.dili.trace.enums.SaleStatusEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import one.util.streamex.StreamEx;

/**
 * @author Alvin.Li
 */
@SuppressWarnings("deprecation")
@Service
public class CheckinOutRecordService extends BaseServiceImpl<CheckinOutRecord, Long> {

	@Autowired
	UserService userService;
	@Autowired
	RegisterBillService registerBillService;
	@Autowired
	UpStreamService upStreamService;
	@Autowired
	TradeDetailService tradeDetailService;
	@Autowired
	TradeService tradeService;
	@Autowired
	ProcessService processService;

	/**
	 * 出门操作
	 * @param operateUser
	 * @param checkOutApiInput
	 * @return
	 */
	@Transactional
	public List<CheckinOutRecord> doCheckout(OperatorUser operateUser, CheckOutApiInput checkOutApiInput) {
		if (checkOutApiInput == null || checkOutApiInput.getTradeDetailIdList() == null
				|| checkOutApiInput.getCheckoutStatus() == null) {
			throw new TraceBizException("参数错误");
		}
		CheckoutStatusEnum checkoutStatusEnum = CheckoutStatusEnum.fromCode(checkOutApiInput.getCheckoutStatus());

		if (checkoutStatusEnum == null) {
			throw new TraceBizException("参数错误");
		}
		return StreamEx.of(checkOutApiInput.getTradeDetailIdList()).nonNull().map(id -> {
			TradeDetail tradeDetailItem = this.tradeDetailService.get(id);
			if (tradeDetailItem == null) {
				throw new TraceBizException("请求出门的数据不存在");
			}
			if (SaleStatusEnum.FOR_SALE.equalsToCode(tradeDetailItem.getSaleStatus())
					&& CheckoutStatusEnum.NONE.equalsToCode(tradeDetailItem.getCheckoutStatus())) {

			} else {
				throw new TraceBizException("请求出门的数据状态错误");
			}
			return tradeDetailItem;

		}).nonNull().mapToEntry(tradeDetailItem -> {
			RegisterBill bill = this.registerBillService.get(tradeDetailItem.getBillId());
			return bill;

		}).map(e -> {
			TradeDetail tradeDetailItem = e.getKey();
			RegisterBill registerBillItem = e.getValue();
			UserInfo user = this.getUser(registerBillItem.getUserId())
					.orElseThrow(() -> new TraceBizException("用户信息不存在"));
			CheckinOutRecord checkoutRecord = new CheckinOutRecord();
			checkoutRecord.setStatus(checkoutStatusEnum.getCode());
			checkoutRecord.setInout(CheckinOutTypeEnum.OUT.getCode());
			checkoutRecord.setProductName(registerBillItem.getProductName());
			checkoutRecord.setInoutWeight(tradeDetailItem.getStockWeight());
			checkoutRecord.setWeightUnit(tradeDetailItem.getWeightUnit());
			checkoutRecord.setUserName(user.getName());
			checkoutRecord.setUserId(user.getId());

			checkoutRecord.setOperatorId(operateUser.getId());
			checkoutRecord.setOperatorName(operateUser.getName());
			checkoutRecord.setRemark(checkOutApiInput.getRemark());
			checkoutRecord.setCreated(new Date());
			checkoutRecord.setModified(new Date());
			checkoutRecord.setTradeDetailId(tradeDetailItem.getId());

			this.insertSelective(checkoutRecord);

			TradeDetail updatable = new TradeDetail();
			updatable.setId(tradeDetailItem.getId());
			updatable.setCheckoutRecordId(checkoutRecord.getId());

			updatable.setCheckoutStatus(checkoutStatusEnum.getCode());
			this.tradeDetailService.updateSelective(updatable);
			return checkoutRecord;
		}).toList();

	}

	/**
	 * 获取操作用户
	 * @param userId
	 * @return
	 */
	private Optional<UserInfo> getUser(Long userId) {
		UserInfo user = this.userService.get(userId);
		return Optional.ofNullable(user);
	}


	/**
	 * 单个进门
	 * 
	 * @param billId
	 * @param checkinStatusEnum
	 * @param operateUser
	 * @return
	 */
	public CheckinOutRecord doOneCheckin(Long billId, CheckinStatusEnum checkinStatusEnum,
			Optional<OperatorUser> operateUser) {
		RegisterBill billItem =registerBillService.getAndCheckById(billId).orElseThrow(()->new TraceBizException("数据不存在"));

		if (CheckinStatusEnum.NONE == checkinStatusEnum) {
			throw new TraceBizException("参数错误");
		}
		if (CheckinStatusEnum.NOTALLOWED == checkinStatusEnum) {
			return null;
		}

		// 补报单已经是进门状态(但是没有进门数据)，所以此处放弃用isCheckin来判断，而是用tradedetail来做判断
		// if (YnEnum.YES.equalsToCode(billItem.getIsCheckin())) {
		// throw new TraceBusinessException("当前报备单已进门");
		// }
		// CheckinOutRecord checkinOutRecordItem = this.tradeDetailService.findBilledTradeDetailByBillId(billId)
		// 		.map(td -> {
		// 			if (CheckinStatusEnum.ALLOWED.equalsToCode(td.getCheckinStatus())) {
		// 				return this.get(td.getCheckinRecordId());
		// 			}
		// 			return null;
		// 		}).filter(Objects::nonNull).orElse(null);

		// if (checkinOutRecordItem != null) {
		// 	return checkinOutRecordItem;
		// }
		if (CheckinStatusEnum.ALLOWED == checkinStatusEnum) {
//			TradeDetail tradeDetailItem = this.tradeDetailService.findBilledTradeDetailByBillId(billId).map(td->{
//				TradeDetail item = new TradeDetail();
//				item.setId(td.getId());
//				item.setCheckinStatus(checkinStatusEnum.getCode());
//				item.setStockWeight(billItem.getWeight());
//				item.setTotalWeight(billItem.getWeight());
//				item.setWeightUnit(billItem.getWeightUnit());
//				item.setProductName(billItem.getProductName());
//				item.setModified(new Date());
//				this.tradeDetailService.updateSelective(item);
//				return this.tradeDetailService.get(item.getId());
//			}).orElseGet(()->{
//			 	return this.tradeDetailService.createTradeDetailForCheckInBill(billItem);
//			});

//			CheckinOutRecord checkinRecordItem =Optional.ofNullable(tradeDetailItem.getCheckinRecordId()).map(cinId->{
//				return this.get(cinId);
//			}).map(cin->{
				CheckinOutRecord item=new CheckinOutRecord();
//				item.setId(cin.getId());
				item.setStatus(checkinStatusEnum.getCode());
				operateUser.ifPresent(op -> {
					// item.setOperatorId(op.getId());
					// item.setOperatorName(op.getName());
				});
				item.setModified(new Date());
				item.setProductName(billItem.getProductName());
				item.setInoutWeight(billItem.getWeight());
				item.setWeightUnit(billItem.getWeightUnit());
				item.setUserName(billItem.getName());
				item.setUserId(billItem.getUserId());
				item.setBillType(billItem.getBillType());
				// item.setVerifyStatus(billItem.getVerifyStatus());
				item.setBillId(billItem.getBillId());
				item.setInout(CheckinOutTypeEnum.IN.getCode());
//				item.setTradeDetailId(tradeDetailItem.getId());
//				this.updateSelective(item);
//				return this.get(item.getId());
				this.insertSelective(item);
//			}).orElseGet(()->{
//				 return this.createRecordForCheckin(billItem,tradeDetailItem.getTradeDetailId(), checkinStatusEnum, operateUser);
//			});
			

//			TradeDetail tradeDetail = new TradeDetail();
//			tradeDetail.setId(tradeDetailItem.getId());
//			tradeDetail.setCheckinStatus(checkinStatusEnum.getCode());
//			tradeDetail.setCheckinRecordId(checkinRecordItem.getId());
//			this.tradeDetailService.updateSelective(tradeDetail);
			// 更新报备单进门状态
			RegisterBill bill = new RegisterBill();
			bill.setId(billItem.getId());
			if (CheckinStatusEnum.ALLOWED == checkinStatusEnum) {
//				bill.setIsCheckin(YesOrNoEnum.YES.getCode());
				bill.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
			} else {
//				bill.setIsCheckin(YesOrNoEnum.NO.getCode());
				bill.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
			}
			this.registerBillService.updateSelective(bill);

			this.tradeService.createBatchStockAfterVerifiedAndCheckin(billItem.getId(),
					operateUser);

			// 本地库存处理完成后。同步库存到UAP
//			processService.afterCheckIn(billItem.getId(), billItem.getMarketId(),operateUser);

			return item;
		}
		return null;

	}

	/**
	 * 创建进门数据
	 */
	private CheckinOutRecord createRecordForCheckin(RegisterBill billItem,Long tradeDetailId, CheckinStatusEnum checkinStatusEnum,
			Optional<OperatorUser> operateUser) {
		CheckinOutRecord checkinRecord = new CheckinOutRecord();
		checkinRecord.setStatus(checkinStatusEnum.getCode());
		checkinRecord.setInout(CheckinOutTypeEnum.IN.getCode());
		operateUser.ifPresent(op -> {
			checkinRecord.setOperatorId(op.getId());
			checkinRecord.setOperatorName(op.getName());
		});

		// checkinRecord.setRemark(checkInApiInput.getRemark());
		checkinRecord.setCreated(new Date());
		checkinRecord.setModified(new Date());
		checkinRecord.setProductName(billItem.getProductName());
		checkinRecord.setInoutWeight(billItem.getWeight());
		checkinRecord.setWeightUnit(billItem.getWeightUnit());
		checkinRecord.setUserName(billItem.getName());
		checkinRecord.setUserId(billItem.getUserId());
		checkinRecord.setBillType(billItem.getBillType());
		checkinRecord.setVerifyStatus(billItem.getVerifyStatus());
		checkinRecord.setBillId(billItem.getBillId());
		checkinRecord.setTradeDetailId(tradeDetailId);
		checkinRecord.setMarketId(billItem.getMarketId());
		this.insertSelective(checkinRecord);
		return checkinRecord;
	}

	// public Optional<CheckInApiDetailOutput> getCheckInDetail(Long billId) {
	// 	RegisterBill registerBill = this.registerBillService.get(billId);
	// 	if (registerBill != null) {
	// 		CheckInApiDetailOutput output = new CheckInApiDetailOutput();
	// 		Long upstreamId = registerBill.getUpStreamId();
	// 		Long userId = registerBill.getUserId();
	// 		if (upstreamId != null) {
	// 			UpStream upStream = this.upStreamService.get(upstreamId);
	// 			output.setUpStream(upStream);
	// 		}
	// 		if (userId != null) {
	// 			User user = this.getUser(userId).orElseThrow(() -> new TraceBusinessException("用户信息不存在"));
	// 			output.setUser(user);
	// 		}
	// 		output.setId(registerBill.getId());
	// 		output.setState(registerBill.getState());
	// 		output.setDetectState(registerBill.getDetectState());
	// 		return Optional.of(output);
	// 	}
	// 	return Optional.empty();
	// }

	// public BasePage<CheckInApiListOutput> listCheckInApiListOutputPage(RegisterBillDto query) {

	// 	// RegisterBill condition = new RegisterBill();
	// 	List<String> sqlList = new ArrayList<>();

	// 	// if (query.getUserId() != null) {
	// 	//
	// 	// sqlList.add("user_id=" + query.getUserId());
	// 	//
	// 	// }
	// 	// if (query.getUpStreamId() != null) {
	// 	//
	// 	// sqlList.add("upstream_id=" + query.getUpStreamId());
	// 	//
	// 	// }
	// 	// if (query.getState() != null) {
	// 	//
	// 	// sqlList.add("state=" + query.getState());
	// 	// }
	// 	if (sqlList.size() > 0) {
	// 		// query.mset(IDTO.AND_CONDITION_EXPR, String.join("AND ", sqlList));
	// 	}
	// 	// query.mset(IDTO.AND_CONDITION_EXPR, String.join("AND ", sqlList));
	// 	// query.setState(RegisterBillStateEnum.WAIT_AUDIT.getCode());
	// 	query.setMetadata(IDTO.AND_CONDITION_EXPR,
	// 			" ( (id in(select bill_id from separate_sales_record where checkin_record_id  is null and sales_type="
	// 					+ TradeTypeEnum.NONE.getCode() + ") and state=" + RegisterBillStateEnum.WAIT_AUDIT.getCode()
	// 					+ " ) or  (id in(select bill_id from separate_sales_record where checkin_record_id  is not null and sales_type="
	// 					+ TradeTypeEnum.NONE.getCode() + " and checkout_record_id is null) ) )");

	// 	BasePage<RegisterBill> billPage = this.registerBillService.listPageByExample(query);
	// 	List<CheckInApiListOutput> dataList = billPage.getDatas().stream().map(bill -> {
	// 		CheckInApiListOutput out = new CheckInApiListOutput();
	// 		out.setBillId(bill.getId());
	// 		out.setCode(bill.getCode());
	// 		out.setProductName(bill.getProductName());
	// 		out.setPhone(bill.getPhone());
	// 		out.setState(bill.getState());
	// 		out.setCreated(bill.getCreated());
	// 		out.setWeight(bill.getWeight());

	// 		if (bill.getUpStreamId() != null) {
	// 			Optional.ofNullable(this.upStreamService.get(bill.getUpStreamId())).ifPresent(up -> {

	// 				out.setUpstreamName(up.getName());
	// 				out.setUpstreamTelphone(up.getTelphone());
	// 			});
	// 		}

	// 		return out;
	// 	}).collect(Collectors.toList());

	// 	BasePage<CheckInApiListOutput> result = BasePageUtil.convert(dataList, billPage);

	// 	return result;

	// }

	// public CheckoutApiDetailOutput getCheckoutDataDetail(Long tradeDetailId) {
	// 	if (tradeDetailId == null) {
	// 		throw new TraceBusinessException("参数错误");
	// 	}
	// 	TradeDetail tradeDetailItem = this.tradeDetailService.get(tradeDetailId);
	// 	if (tradeDetailItem == null) {
	// 		throw new TraceBusinessException("没有数据");
	// 	}

	// 	RegisterBill bill = this.registerBillService.get(tradeDetailItem.getBillId());
	// 	User user = this.getUser(bill.getUserId()).orElseThrow(() -> new TraceBusinessException("用户信息不存在"));
	// 	user.setPassword("");

	// 	CheckoutApiDetailOutput output = new CheckoutApiDetailOutput();
	// 	output.setId(tradeDetailItem.getId());
	// 	output.setState(bill.getState());
	// 	output.setUser(user);

	// 	if (tradeDetailItem.getParentId() == null && bill.getUpStreamId() != null) {
	// 		UpStream upStream = this.upStreamService.get(bill.getUpStreamId());
	// 		output.setUpStream(upStream);
	// 	} else if (tradeDetailItem.getParentId() != null) {
	// 		TradeDetail parentSalesRecord = this.tradeDetailService.get(tradeDetailItem.getParentId());
	// 		if (parentSalesRecord != null && parentSalesRecord.getBuyerId() != null) {
	// 			UpStream upStream = this.upStreamService.queryUpStreamBySourceUserId(parentSalesRecord.getBuyerId());
	// 			output.setUpStream(upStream);
	// 		}

	// 	}

	// 	return output;

	// }

	// public BaseOutput<BasePage<DTO>> listPagedAvailableCheckOutData(CheckoutApiListQuery query) {

	// 	if (query == null || query.getUserId() == null) {
	// 		return BaseOutput.failure("参数错误");
	// 	}

	// 	TradeDetail separateSalesRecord = new TradeDetail();
	// 	separateSalesRecord.setBuyerId(query.getUserId());

	// 	StringBuilder sql = new StringBuilder("( checkin_record_id in(select id from checkinout_record where `inout`="
	// 			+ CheckinOutTypeEnum.IN.getCode() + " and status=" + CheckinStatusEnum.ALLOWED.getCode()
	// 			+ "  and checkout_record_id is null) or checkout_record_id in (select id from checkinout_record where `inout`="
	// 			+ CheckinOutTypeEnum.OUT.getCode() + " and status=" + CheckinStatusEnum.NOTALLOWED.getCode() + " ) )");
	// 	if (StringUtils.isNotBlank(query.getLikeProductName())) {

	// 		sql.append(" AND bill_id in(select id from register_bill where product_name like '%"
	// 				+ query.getLikeProductName() + "%')");

	// 	}

	// 	separateSalesRecord.setMetadata(IDTO.AND_CONDITION_EXPR, sql.toString());

	// 	BasePage<TradeDetail> page = this.tradeDetailService.listPageByExample(separateSalesRecord);
	// 	List<DTO> dataList = page.getDatas().stream().map(sp -> {
	// 		Long id = sp.getId();
	// 		Long billId = sp.getBillId();
	// 		DTO dto = DTOUtils.go(sp);
	// 		dto.remove("billId");
	// 		dto.remove("id");
	// 		dto.put("separateSalesId", id);
	// 		RegisterBill rb = this.registerBillService.get(billId);
	// 		if (rb != null) {
	// 			RegisterBillStateEnum stateEnum = RegisterBillStateEnum.getRegisterBillStateEnum(rb.getState());
	// 			dto.put("state", rb.getState());
	// 			dto.put("stateName", stateEnum.getName());
	// 			dto.put("productName", rb.getProductName());
	// 		}

	// 		return dto;
	// 	}).collect(Collectors.toList());

	// 	BasePage<DTO> result = BasePageUtil.convert(dataList, page);
	// 	return BaseOutput.success().setData(result);
	// }

	// public BaseOutput<BasePage<Map<String, Object>>> listPagedData(CheckoutApiListQuery query, Long operatorId) {
	// 	// if (query == null || query.getUserId() == null) {
	// 	// return BaseOutput.failure("参数错误");
	// 	// }
	// 	CheckinOutRecord checkinOutRecord = new CheckinOutRecord();
	// 	checkinOutRecord.setOperatorId(operatorId);
	// 	if (query.getDate() != null) {
	// 		checkinOutRecord.setMetadata(IDTO.AND_CONDITION_EXPR,
	// 				" DATE_FORMAT(created,'%Y-%m-%d')='" + query.getDate() + "'");
	// 	}
	// 	BasePage<CheckinOutRecord> page = this.listPageByExample(checkinOutRecord);

	// 	List<Map<String, Object>> dataList = page.getDatas().stream().map(cr -> {
	// 		// RegisterBill billQuery = new RegisterBill();
	// 		// billQuery.mset(IDTO.AND_CONDITION_EXPR,
	// 		// " id in (select bill_id from separate_sales_record where checkin_record_id ="
	// 		// + cr.getId()
	// 		// + " or checkout_record_id=" + cr.getId() + ") ");
	// 		// RegisterBill billItem =
	// 		// this.registerBillService.listByExample(billQuery).stream().findFirst()
	// 		// .orElse(new RegisterBill());

	// 		TradeDetail separateSalesRecordQuery = new TradeDetail();
	// 		separateSalesRecordQuery.setMetadata(IDTO.AND_CONDITION_EXPR,
	// 				"  ( checkin_record_id =" + cr.getId() + " or checkout_record_id=" + cr.getId() + ") ");
	// 		TradeDetail separateSalesRecordItem = this.tradeDetailService.listByExample(separateSalesRecordQuery)
	// 				.stream().findFirst().orElse(new TradeDetail());
	// 		Map<String, Object> dto = BeanMapUtil.beanToMap(cr);
	// 		// Map<String,Object>dto=BeanMapUtil.beanToMap(cr);
	// 		// dto.remove("id");
	// 		// if (billItem != null) {
	// 		// dto.put("state", billItem.getState());
	// 		// }
	// 		return dto;

	// 	}).collect(Collectors.toList());
	// 	BasePage<Map<String, Object>> result = BasePageUtil.convert(dataList, page);
	// 	return BaseOutput.success().setData(result);
	// }
}
