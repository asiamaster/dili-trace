package com.dili.trace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Optional;

import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.domain.CheckinOutRecord;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.CheckinOutTypeEnum;
import com.dili.trace.enums.CheckinStatusEnum;
import com.dili.trace.enums.CheckoutStatusEnum;
import com.dili.trace.enums.SaleStatusEnum;
import com.dili.trace.enums.TradeTypeEnum;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@TestInstance(Lifecycle.PER_CLASS)
public class CheckinOutServiceTest extends AutoWiredBaseTest {
	@Autowired
	private TradeDetailService tradeDetailService;
	@Autowired
	CheckinOutRecordService checkinOutRecordService;

	@Test
	@Transactional
	public void doCheckinBeforeVerify() {
		// ------createRegisterBill--------
		Long billId = super.createRegisterBill(super.buildBill()).getId();
		assertNotNull(billId);
		// ------doCheckIn--------
		Pair<CheckinOutRecord, TradeDetail> pair = super.doCheckIn(billId, CheckinStatusEnum.ALLOWED);
		CheckinOutRecord inRecord = pair.getLeft();
		TradeDetail tradeDetailItem = pair.getRight();
		assertEquals(tradeDetailItem.getSaleStatus(), SaleStatusEnum.NONE.getCode());
		// ------doVerifyAfterCheckIn--------
		super.doVerifyAfterCheckIn(billId, BillVerifyStatusEnum.PASSED);

		TradeDetail sepQuery = new TradeDetail();
		sepQuery.setBillId(billId);
		sepQuery.setTradeType(TradeTypeEnum.NONE.getCode());
		TradeDetail tradeDetail = this.tradeDetailService.listByExample(sepQuery).stream().findFirst().orElse(null);

		assertNotNull(tradeDetail);
		assertEquals(tradeDetail.getSaleStatus(), SaleStatusEnum.FOR_SALE.getCode());
		assertEquals(tradeDetail.getCheckinRecordId(), inRecord.getId());
		assertEquals(inRecord.getTradeDetailId(), tradeDetail.getId());

		// =======doCheckOut=============
		CheckinOutRecord outrecord = super.doCheckOut(tradeDetail.getId(), CheckoutStatusEnum.ALLOWED);
		assertEquals(outrecord.getInout(), CheckinOutTypeEnum.OUT.getCode());
		assertEquals(outrecord.getStatus(), CheckoutStatusEnum.ALLOWED.getCode());
		assertEquals(outrecord.getTradeDetailId(), tradeDetailItem.getId());

	}

	@Test
	@Transactional
	public void doCheckinAferVerify() {
		Long billId = super.createRegisterBill(super.buildBill()).getId();
		// ------doVerifyBeforeCheckIn--------
		super.doVerifyBeforeCheckIn(billId, BillVerifyStatusEnum.PASSED);

		TradeDetail sepQuery = new TradeDetail();
		sepQuery.setBillId(billId);
		sepQuery.setTradeType(TradeTypeEnum.NONE.getCode());
		TradeDetail tradeDetail = this.tradeDetailService.listByExample(sepQuery).stream().findFirst().orElse(null);
		assertNull(tradeDetail);

		// ------doCheckIn--------
		Pair<CheckinOutRecord, TradeDetail> pair = super.doCheckIn(billId, CheckinStatusEnum.ALLOWED);
		CheckinOutRecord inRecord = pair.getLeft();
		TradeDetail tradeDetailItem = pair.getRight();

		assertEquals(tradeDetailItem.getSaleStatus(), SaleStatusEnum.FOR_SALE.getCode());
		assertEquals(tradeDetailItem.getCheckinRecordId(), inRecord.getId());
		assertEquals(inRecord.getTradeDetailId(), tradeDetailItem.getId());

		// =======doCheckOut=============
		CheckinOutRecord outrecord = super.doCheckOut(tradeDetailItem.getId(), CheckoutStatusEnum.ALLOWED);
		assertEquals(outrecord.getInout(), CheckinOutTypeEnum.OUT.getCode());
		assertEquals(outrecord.getStatus(), CheckoutStatusEnum.ALLOWED.getCode());
		assertEquals(outrecord.getTradeDetailId(), tradeDetailItem.getId());

	}


		

}
