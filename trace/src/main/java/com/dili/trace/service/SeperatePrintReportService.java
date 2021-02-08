package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.SeperatePrintReport;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.SeperatePrintReportOutputDto;
import com.dili.trace.glossary.BizNumberType;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * SeperatePrintReportService
 */
@Service
public class SeperatePrintReportService extends BaseServiceImpl<SeperatePrintReport, Long> {
	@Autowired
	BillService billService;
	@Autowired
	Base64SignatureService base64SignatureService;
	@Autowired
	QrCodeService qrCodeService;
	@Autowired
	UidRestfulRpcService uidRestfulRpcService;

	@Value("${current.baseWebPath}")
	private String baseWebPath;

	/**
	 *
	 * @param id
	 * @return
	 */
	public Optional<SeperatePrintReportOutputDto> findPrintReportOutDtoById(Long id) {

		SeperatePrintReport item = this.get(id);
		if(item==null) {
			return Optional.empty();
		}
		SeperatePrintReportOutputDto dto=this.buildOutdto(item);
		return Optional.ofNullable(dto);
	}

	/**
	 *
	 * @param reportList
	 * @param operatorUser
	 * @return
	 */
	public List<SeperatePrintReportOutputDto> saveSeperatePrintReportList(List<SeperatePrintReport> reportList,
			OperatorUser operatorUser) {
		if (reportList.isEmpty()) {
			return Lists.newArrayList();
		}

		return StreamEx.of(reportList).map(r -> {
			return this.findBySeperateRecocrdId(r.getSeperateRecocrdId()).orElseGet(() -> {
				r.setCreated(new Date());
				r.setModified(new Date());
				r.setOperatorId(operatorUser.getId());
				r.setOperatorName(operatorUser.getName());
				r.setCode(this.uidRestfulRpcService.bizNumber(BizNumberType.ECOMMERCE_BILL_SEPERATE_REPORT_CODE));
				this.insertSelective(r);
				return r;
			});
		}).map(r -> {
			return this.buildOutdto(r);

		}).toList();

	}

	/**
	 *
	 * @param reportList
	 * @param operatorUser
	 * @return
	 */
	public List<SeperatePrintReportOutputDto> buildPreViewOutputList(List<SeperatePrintReport> reportList,
			OperatorUser operatorUser) {

		if (reportList.isEmpty()) {
			return Lists.newArrayList();
		}

		return StreamEx.of(reportList).map(r -> {
			return this.findBySeperateRecocrdId(r.getSeperateRecocrdId()).orElseGet(() -> {
				r.setCreated(new Date());
				r.setModified(new Date());
				r.setOperatorId(operatorUser.getId());
				r.setOperatorName(operatorUser.getName());
				r.setCode("SGDSR*****");
				return r;
			});
		}).map(r -> {
			return this.buildOutdto(r);

		}).toList();
	}

	/**
	 *
	 * @param r
	 * @return
	 */
	private SeperatePrintReportOutputDto buildOutdto(SeperatePrintReport r) {
		RegisterBill bill = billService.get(r.getBillId());
		String approverBase64Sign = this.base64SignatureService
				.findBase64SignatureByApproverInfoId(r.getApproverInfoId());
		String base64Qrcode = this.buildBase64QrCode(r.getId());
		SeperatePrintReportOutputDto out = SeperatePrintReportOutputDto.build(r, bill, approverBase64Sign,
				base64Qrcode);
		return out;

	}

	/**
	 *
	 * @param id
	 * @return
	 */
	private String buildBase64QrCode(Long id) {
		try {
			String base64Qrcode = this.qrCodeService.getBase64QrCode(this.baseWebPath
					+ "/seperatePrintReport/seperateReportDetail.html?id=" + (id == null ? "" : String.valueOf(id)),
					200, 200);
			return base64Qrcode;
		} catch (Exception e) {
			throw new TraceBizException("生成二维码错误");
		}
	}

	/**
	 *
	 * @param seperateRecocrdId
	 * @return
	 */
	private Optional<SeperatePrintReport> findBySeperateRecocrdId(Long seperateRecocrdId) {
		SeperatePrintReport condition = new SeperatePrintReport();
		condition.setSeperateRecocrdId(seperateRecocrdId);
		return this.listByExample(condition).stream().findFirst();

	}

}
