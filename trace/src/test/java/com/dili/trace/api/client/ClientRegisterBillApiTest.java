package com.dili.trace.api.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.ss.dto.DTOUtils;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.api.output.TradeDetailBillOutput;
import com.dili.trace.domain.Brand;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.CreateListBillParam;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.BillVerifyStatusEnum;
import com.dili.trace.enums.ImageCertTypeEnum;
import com.dili.trace.enums.PreserveTypeEnum;
import com.dili.trace.enums.TruckTypeEnum;
import com.dili.trace.enums.WeightUnitEnum;
import com.dili.trace.service.BrandService;
import com.dili.trace.service.RegisterBillService;
import com.dili.trace.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import one.util.streamex.StreamEx;

public class ClientRegisterBillApiTest extends AutoWiredBaseTest {
	@Autowired
	ClientRegisterBillApi clientRegisterBillApi;
	@Autowired
	RegisterBillService registerBillService;

	@Autowired
	BrandService brandService;
	// @MockBean
	// LoginSessionContext sessionContext;
	// private MockMvc mockMvc;
	// @Injectable
	private UserInfo userItem;


	@Test
	public void createRegisterBillList() {

		RegisterBill billItem = super.createRegisterBill(super.buildBill());

		CreateListBillParam createListBillParam = new CreateListBillParam();
		List<CreateRegisterBillInputDto> registerBills = new ArrayList<CreateRegisterBillInputDto>();
		createListBillParam.setRegisterBills(registerBills);

		CreateRegisterBillInputDto rb = new CreateRegisterBillInputDto();
		registerBills.add(rb);
		rb.setWeight(BigDecimal.TEN);
		rb.setSpecName("筐");
		rb.setBrandName("四川最好");
		rb.setPreserveType(PreserveTypeEnum.ICED.getCode());
		rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
		rb.setProductId(billItem.getProductId());
		rb.setProductName(billItem.getProductName());
		rb.setOriginId(billItem.getOriginId());
		rb.setOriginName(billItem.getOriginName());
//		rb.setBillType(BillTypeEnum.SUPPLEMENT.getCode());
		rb.setTruckType(TruckTypeEnum.FULL.getCode());
		rb.setImageCertList(new ArrayList<ImageCert>());
		ImageCert imageCert = new ImageCert();
		imageCert.setUid("imageurl");
		imageCert.setCertType(ImageCertTypeEnum.DETECT_REPORT.getCode());
		rb.getImageCertList().add(imageCert);
		BaseOutput out = this.clientRegisterBillApi.createRegisterBillList(createListBillParam);
		assertTrue(out.isSuccess());
		System.out.println(out.getData());

		Brand brandQuery = new Brand();
		brandQuery.setBrandName(rb.getBrandName());
		brandQuery.setUserId(userItem.getId());
		Brand brandItem = StreamEx.of(this.brandService.listByExample(brandQuery)).findFirst().orElse(null);
		assertNotNull(brandItem);
	}

	@Test
	public void doEdit() {
		RegisterBill query = new RegisterBill();
		query.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
		RegisterBill item = registerBillService.listPageByExample(query).getDatas().stream().findFirst().orElse(null);

		CreateRegisterBillInputDto rb = new CreateRegisterBillInputDto();
		rb.setBillId(item.getId());
		rb.setWeight(BigDecimal.TEN);
		rb.setSpecName("箱");
		rb.setBrandName("四川第二");
		rb.setPreserveType(PreserveTypeEnum.ICED.getCode());
		rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
		rb.setProductId(item.getProductId());
		rb.setProductName(item.getProductName());
		rb.setOriginId(item.getOriginId());
		rb.setOriginName(item.getOriginName());
//		rb.setBillType(BillTypeEnum.SUPPLEMENT.getCode());
		rb.setImageCertList(new ArrayList<ImageCert>());
		ImageCert imageCert = new ImageCert();
		imageCert.setUid("imageurl");
		imageCert.setCertType(ImageCertTypeEnum.DETECT_REPORT.getCode());
		rb.getImageCertList().add(imageCert);
		this.clientRegisterBillApi.doEditRegisterBill(rb);

		Brand brandQuery = new Brand();
		brandQuery.setBrandName(rb.getBrandName());
		brandQuery.setUserId(userItem.getId());
		Brand brandItem = StreamEx.of(this.brandService.listByExample(brandQuery)).findFirst().orElse(null);
		assertNotNull(brandItem);
	}

	@Test
	public void listPage() {
		RegisterBill bill = super.buildBill();
		bill.setUserId(this.userItem.getId());
		bill.setName(this.userItem.getName());
		RegisterBill billItem = super.createRegisterBill(bill);
		RegisterBillDto queryDto = new RegisterBillDto();
		// queryDto.setVerifyStatus(billItem.getVerifyStatus());
		BaseOutput<BasePage<TradeDetailBillOutput>> out = this.clientRegisterBillApi.listPage(queryDto);
		assertNotNull(out);
		assertTrue(out.isSuccess());

		BasePage<TradeDetailBillOutput> page = out.getData();
		assertNotNull(page);
		List<TradeDetailBillOutput> list = page.getDatas();
		TradeDetailBillOutput outBill = StreamEx.of(list).findFirst().orElse(null);
		assertNotNull(outBill);
		assertNotNull(outBill.getVerifyStatus());
		assertNotNull(outBill.getTradeType());
	}

	@Test
	public void listPage_2() {
		String sessionId="b9761afa0f1240e492ca369979f3a60e";
		MockHttpSession session=new MockHttpSession(null,sessionId);
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		request.setSession(session);
		RegisterBillDto queryDto = new RegisterBillDto();

		BaseOutput<BasePage<TradeDetailBillOutput>> out = this.clientRegisterBillApi.listPage(queryDto);
		assertNotNull(out);
		assertTrue(out.isSuccess());
	}

}
