package com.dili.trace.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.api.input.CheckInApiInput;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.api.input.RegisterBillApiInputDto;
import com.dili.trace.domain.CheckinOutRecord;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.enums.*;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.google.common.collect.Lists;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.Transactional;

import one.util.streamex.StreamEx;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnableDiscoveryClient
public class RegisterBillServiceTest extends AutoWiredBaseTest {
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    CheckinOutRecordService checkinOutRecordService;
    @Autowired
    TradeDetailService tradeDetailService;

    @SpyBean
    CustomerRpcService clientRpcService;
    @Autowired
    ProcessService processService;


    @Test
    public void createRegisterBillList() {
        CreateRegisterBillInputDto inputDto = new CreateRegisterBillInputDto();
        inputDto.setRegistType(RegistTypeEnum.NONE.getCode());

        Long marketId = 8L;
        List<CreateRegisterBillInputDto> inputBillDtoList = Lists.newArrayList(inputDto);

        Long customerId = 1L;
        CustomerExtendDto customerExtendDto = new CustomerExtendDto();
        customerExtendDto.setId(customerId);
        customerExtendDto.setName("testuser");
        Mockito.doReturn(customerExtendDto).when(this.clientRpcService)
                .findApprovedCustomerByIdOrEx(Mockito.anyLong(), Mockito.anyLong());

        Optional<OperatorUser> operatorUser = Optional.empty();
        CreatorRoleEnum creatorRoleEnum = CreatorRoleEnum.MANAGER;

        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "商品重量不能为空");

        inputDto.setWeight(BigDecimal.valueOf(1.23D));

        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "商品重量必须为整数");


        inputDto.setWeight(BigDecimal.valueOf(-1));


        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "商品重量不能小于0");


        inputDto.setWeight(BigDecimal.valueOf(99999999L + 1));

        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "商品重量不能大于99999999");

        inputDto.setWeight(BigDecimal.TEN);


        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "重量单位不能为空");

        inputDto.setWeightUnit(WeightUnitEnum.KILO.getCode());
        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "商品名称不能为空");

        inputDto.setProductId(1L);
        inputDto.setProductName("白菜");

        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "商品产地不能为空");
        inputDto.setOriginId(2L);
        inputDto.setOriginName("四川成都");
        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "备注不能为空");
        inputDto.setRemark("备注信息");

        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "皮重不能为空");
        inputDto.setTruckTareWeight(BigDecimal.ONE);


        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "是否拼车不能为空");

        inputDto.setTruckType(TruckTypeEnum.POOL.getCode());


        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "车牌不能为空");

        inputDto.setPlate("川A12345");

        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "商品单价不能为空");

        inputDto.setUnitPrice(BigDecimal.ONE);
        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "商品规格不能为空");

        inputDto.setSpecName("箱子");
        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "品牌不能为空");

        inputDto.setBrandName("好巴适");

        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "上游企业不能为空");

        inputDto.setUpStreamId(10L);
        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "到场时间不能为空");
        inputDto.setArrivalDatetime(new Date());

        assertThrows(TraceBizException.class,
                () -> {
                    this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
                }, "到货摊位不能为空");
        inputDto.setArrivalTallynos(Lists.newArrayList("222"));
        List<Long> idList = this.registerBillService.createRegisterBillList(marketId, inputBillDtoList, customerId, operatorUser, creatorRoleEnum);
        assertNotNull(idList);
        System.out.println(idList);
    }

    @Test
    public void getBillDetail() {
        RegisterBill registerBill = registerBillService.get(5L);
        System.out.println(JSON.toJSONString(registerBill));
    }

    @Test
    public void doVerifyBeforeCheckIn() {
        RegisterBill query = new RegisterBill();
        query.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
        RegisterBill input = StreamEx.of(this.registerBillService.listByExample(query)).findFirst().orElse(null);
        assertNotNull(input);
        OperatorUser operatorUser = new OperatorUser(1L, "test");
        input.setVerifyStatus(BillVerifyStatusEnum.PASSED.getCode());
        Long billId = this.registerBillService.doVerifyBeforeCheckIn(input, Optional.ofNullable(operatorUser));
        assertNotNull(billId);
    }

    @Test
    public void doVerifyAfterCheckIn() {

        RegisterBill query = new RegisterBill();
        query.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
        RegisterBill input = StreamEx.of(this.registerBillService.listByExample(query)).findFirst().orElse(null);
        assertNotNull(input);
        assertTrue(BillVerifyStatusEnum.WAIT_AUDIT.equalsToCode(input.getVerifyStatus()));
        CheckInApiInput checkInApiInput = new CheckInApiInput();
        checkInApiInput.setBillIdList(Lists.newArrayList(input.getId()));
        checkInApiInput.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        List<CheckinOutRecord> checkInList = this.processService.doCheckIn(
                Optional.ofNullable(new OperatorUser(1L, "test")), checkInApiInput.getBillIdList(),
                CheckinStatusEnum.ALLOWED);
        assertTrue(checkInList.size() == 1);
        CheckinOutRecord recordItem = checkInList.get(0);
        assertTrue(CheckinOutTypeEnum.IN.equalsToCode(recordItem.getInout()));
        assertTrue(CheckinStatusEnum.ALLOWED.equalsToCode(recordItem.getStatus()));

        OperatorUser operatorUser = new OperatorUser(1L, "test");
        input.setVerifyStatus(BillVerifyStatusEnum.PASSED.getCode());
        Long billId = this.registerBillService.doVerifyAfterCheckIn(input.getId(), input.getVerifyStatus(),
                input.getReason(), Optional.ofNullable(operatorUser));
        assertNotNull(billId);

    }

    @Test
    @Transactional
    public void selectByIdForUpdate() {
        this.registerBillService.selectByIdForUpdate(5L).ifPresent(bill -> {
            System.out.println(bill);
        });
    }

    @Test
    public void viewTradeDetailBill() {
        RegisterBillOutputDto dto = this.registerBillService.viewTradeDetailBill(new RegisterBillApiInputDto());
        System.out.println(dto);
    }

    @Test
    public void listPageCheckInData() {
        RegisterBillDto query = new RegisterBillDto();
//        query.setCreatedStart("2020-07-15 00:00:00");
//        query.setCreatedEnd("2020-07-21 23:59:59");
        query.setUserId(458L);
        Map<Integer, Map<String, List<RegisterBill>>> mapdata = this.registerBillService.listPageCheckInData(query);
        System.out.println(mapdata);
    }
}