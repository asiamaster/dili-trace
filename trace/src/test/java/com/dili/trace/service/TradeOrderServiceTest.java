package com.dili.trace.service;

import com.dili.customer.sdk.domain.CustomerMarket;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.api.input.ProductStockInput;
import com.dili.trace.api.input.TradeRequestHandleDto;
import com.dili.trace.domain.*;
import com.dili.trace.dto.TradeDto;
import com.dili.trace.enums.BuyerTypeEnum;
import com.dili.trace.enums.TradeOrderStatusEnum;
import com.dili.trace.enums.TradeOrderTypeEnum;
import com.google.common.collect.Lists;
import mockit.*;
import one.util.streamex.StreamEx;
import org.apache.commons.beanutils.BeanMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableDiscoveryClient
//@DirtiesContext(classMode = AFTER_CLASS)
public class TradeOrderServiceTest extends AutoWiredBaseTest {
    @Autowired
    TradeOrderService tradeOrderService;
    @Autowired
    TradeRequestService tradeRequestService;
    @Autowired
    ProductStockService productStockService;
    @Autowired
    TradeRequestDetailService tradeRequestDetailService;
    @Autowired
    TradeDetailService tradeDetailService;

    //对真实的bean进行一次spy之后再注入。
    //相当于 @Autowired之后进行Spy然后再替换这个Service (@Autowired+@Spy)
//    @SpyBean
//    CustomerRpcService customerRpcService;
    //用mock的bean来代码本次testcase相关的所有依赖的bean(@Autowired+@Mocked)
    @MockBean
    CustomerRpc customerRpc;
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    CheckinOutRecordService checkinOutRecordService;
    @Autowired
    BrandService brandService;

//    @BeforeAll
//    public static void applySpringIntegration() {
//        new FakeBeanFactory();
//    }

    //@Mock标注的Bean并没有被初始化，需要对当前对象进行初始化init(MockitoAnnotations.initMocks(this);)
    //@Spy的bean一定要有真实的属性值
//    @Mock
//    private CustomerRpc t; //MockitoAnnotations.initMocks(this);相当于t= Mockito.mock(CustomerRpc.class)
//    @Spy
//    private CustomerRpc t=new CustomerRpc();
    private CustomerExtendDto buildCustExt(Long marketId, Long userId) {
        CustomerExtendDto dto = new CustomerExtendDto();
        dto.setName("test-user-" + userId);
        dto.setId(userId);
        dto.setCustomerMarket(new CustomerMarket());
        dto.getCustomerMarket().setMarketId(marketId);
        dto.setCreateTime(LocalDateTime.now());
        dto.setModifyTime(LocalDateTime.now());
        dto.getCustomerMarket().setApprovalStatus(CustomerEnum.ApprovalStatus.PASSED.getCode());
        return dto;
    }

    @Test
    public void findCustomerById() {
        //当调用方法时直接调用真实方法,之后返回thenreturn的值(只对mockbean有效,不能对spy的bean使用)?真的吗？
        Mockito.when(this.customerRpc.get(1L, 8L)).thenReturn(BaseOutput.failure());
        System.out.println(this.customerRpc);
        //当调用方法时直接返回doreturn的值 ，而不调用真实方法
        Mockito.doReturn(Optional.empty()).when(customerRpcService).findCustomerById(1L, 8L);
        this.customerRpcService.findCustomerById(1L, 8L).ifPresent(c -> {
            System.out.println(c);
        });
        //调用真实方法
        Mockito.doCallRealMethod().when(customerRpcService).findCustomerById(1L, 8L);
        this.customerRpcService.findCustomerById(1L, 8L).ifPresent(c -> {
            System.out.println(c);
        });


    }

    @Test
    public void createBuy_test1() {
        Long marketId = 8L;
        Long userId = 1L;
        Mockito.doAnswer(invocation -> {
            CustomerExtendDto dto = new CustomerExtendDto();
            Long uid = (Long) invocation.getArguments()[0];
            Long marketid = (Long) invocation.getArguments()[1];
            dto.setName("user-" + uid);
            dto.setId(uid);
            dto.setCustomerMarket(new CustomerMarket());
            dto.getCustomerMarket().setMarketId(marketid);
            dto.getCustomerMarket().setApprovalStatus(CustomerEnum.ApprovalStatus.PASSED.getCode());
            return Optional.ofNullable(dto);
        }).when(customerRpcService).findCustomerById(Mockito.anyLong(), Mockito.anyLong());

        CustomerExtendDto userDto = this.customerRpcService.findCustomerById(userId, marketId).orElse(null);
        this.deleteAllDatas(userDto);
        BigDecimal weight = BigDecimal.valueOf(110);
        Long billId = super.baseCreateRegisterBill(marketId, userId, weight);
        super.doVerifyBeforeCheckIn(billId);
        super.doOneCheckin(billId);

        RegisterBill registerBill = this.registerBillService.get(billId);

        ProductStock q = new ProductStock();
        q.setUserId(registerBill.getUserId());
        q.setProductId(registerBill.getProductId());
        q.setMarketId(registerBill.getMarketId());
        ProductStock productStock = StreamEx.of(this.productStockService.listByExample(q)).findFirst().orElse(null);

        TradeDto tradeDto = new TradeDto();
        tradeDto.setMarketId(marketId);
        tradeDto.setTradeOrderType(TradeOrderTypeEnum.SELL);

        tradeDto.getBuyer().setBuyerId(100L);
        tradeDto.getBuyer().setBuyerName("错位时空");
        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);


        List<ProductStockInput> batchStockInputList = new ArrayList<>();
        ProductStockInput input = new ProductStockInput();
        input.setProductStockId(productStock.getProductStockId());
        input.setTradeWeight(BigDecimal.valueOf(2));
        batchStockInputList.add(input);


        TradeOrder tradeOrder = this.tradeOrderService.createBuyTrade(tradeDto, batchStockInputList);
        Assertions.assertNotNull(tradeOrder);


        TradeRequest trq = new TradeRequest();
        trq.setTradeOrderId(tradeOrder.getId());
        StreamEx.of(this.tradeRequestService.listByExample(trq)).forEach(tr -> {
            TradeRequestHandleDto handleDto = new TradeRequestHandleDto();
            handleDto.setTradeRequestId(tr.getId());
            handleDto.setHandleStatus(TradeOrderStatusEnum.FINISHED.getCode());
            this.tradeOrderService.handleBuyerRequest(handleDto);

        });

    }

    @Test
    public void createBuyTrade() {

        TradeDto tradeDto = new TradeDto();
        tradeDto.setMarketId(8L);
        tradeDto.setTradeOrderType(TradeOrderTypeEnum.BUY);

        tradeDto.getSeller().setSellerId(1L);
        tradeDto.getSeller().setSellerName("zhangsan");

        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);
        tradeDto.getBuyer().setBuyerId(2L);
        tradeDto.getBuyer().setBuyerName("lisi");


        TradeOrder tradeOrderItem = new TradeOrder();
        tradeOrderItem.setId(10L);


        List<ProductStockInput> batchStockInputList = new ArrayList<>();
        ProductStockInput input = new ProductStockInput();
        input.setProductStockId(1L);
        input.setTradeWeight(BigDecimal.ONE);
        batchStockInputList.add(input);

        TradeOrder tradeOrder = this.tradeOrderService.createBuyTrade(tradeDto, batchStockInputList);
        Assertions.assertNotNull(tradeOrder);


    }

    @DisplayName("配送交易")
    @Test
    public void createSeperateTrade() {

        CustomerExtendDto cust = new CustomerExtendDto();
        cust.setId(31L);
        cust.setName("zhangsan");
        Mockito.doReturn(Optional.of(cust)).when(customerRpcService).findCustomerById(31L, 8L);
//        this.customerRpcService.findCustomerById(31L, 8L).ifPresent(c -> {
//            System.out.println(c);
//        });


        TradeDto tradeDto = new TradeDto();
        tradeDto.setMarketId(8L);
        tradeDto.setTradeOrderType(TradeOrderTypeEnum.SELL);

        tradeDto.getSeller().setSellerId(31L);
        tradeDto.getSeller().setSellerName("zhangsan");

        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.DOWNSTREAM_BUYER);
        tradeDto.getBuyer().setBuyerId(32L);
        tradeDto.getBuyer().setBuyerName("lisi");


        List<ProductStockInput> batchStockInputList = new ArrayList<>();
        ProductStockInput input = new ProductStockInput();
        input.setProductStockId(33L);
        input.setTradeWeight(BigDecimal.valueOf(430));
        batchStockInputList.add(input);
        TradeOrder tradeOrder = this.tradeOrderService.createSeperateTrade(tradeDto, batchStockInputList);
        Assertions.assertNotNull(tradeOrder);
    }

    private void deleteAllDatas(CustomerExtendDto userDto) {
        RegisterBill del = new RegisterBill();
        del.setMarketId(userDto.getCustomerMarket().getMarketId());
        del.setUserId(userDto.getId());
        List<Long> billIdList = StreamEx.of(this.registerBillService.listByExample(del)).map(RegisterBill::getId).toList();
        this.registerBillService.deleteByExample(del);

        StreamEx.of(billIdList).forEach(billId -> {
            TradeDetail td = new TradeDetail();
            td.setBillId(billId);
            this.tradeDetailService.deleteByExample(td);
        });
        ProductStock productStock = new ProductStock();
        productStock.setUserId(userDto.getId());
        productStock.setMarketId(userDto.getCustomerMarket().getMarketId());
        this.productStockService.deleteByExample(productStock);

    }

    @Transactional
    @Test
//    @Commit
    public void createSellTrade() {

        Long marketId = 8L;
        Long userId = 1L;
        Mockito.doAnswer(invocation -> {
            CustomerExtendDto dto = new CustomerExtendDto();
            Long uid = (Long) invocation.getArguments()[0];
            Long marketid = (Long) invocation.getArguments()[1];
            dto.setName("user-" + uid);
            dto.setId(uid);
            dto.setCustomerMarket(new CustomerMarket());
            dto.getCustomerMarket().setMarketId(marketid);
            dto.getCustomerMarket().setApprovalStatus(CustomerEnum.ApprovalStatus.PASSED.getCode());
            return Optional.ofNullable(dto);
        }).when(customerRpcService).findCustomerById(Mockito.anyLong(), Mockito.anyLong());

        CustomerExtendDto userDto = this.customerRpcService.findCustomerById(userId, marketId).orElse(null);
        this.deleteAllDatas(userDto);
        BigDecimal weight = BigDecimal.valueOf(110);

        Long billId = super.baseCreateRegisterBill(marketId, userId, weight);
        RegisterBill registerBill = this.registerBillService.get(billId);

        super.doVerifyBeforeCheckIn(billId);
        super.doOneCheckin(billId);


        ProductStock q = new ProductStock();
        q.setUserId(userDto.getId());
        q.setMarketId(userDto.getCustomerMarket().getMarketId());
        q.setProductId(registerBill.getProductId());

        ProductStock productStock = StreamEx.of(this.productStockService.listByExample(q)).findFirst().orElse(null);


        TradeDto tradeDto = new TradeDto();
        tradeDto.setMarketId(userDto.getCustomerMarket().getMarketId());
        tradeDto.setTradeOrderType(TradeOrderTypeEnum.SELL);

        tradeDto.getSeller().setSellerId(userDto.getId());
        tradeDto.getSeller().setSellerName(userDto.getName());

        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);
        tradeDto.getBuyer().setBuyerId(2L);
        tradeDto.getBuyer().setBuyerName("lisi");


        List<ProductStockInput> batchStockInputList = new ArrayList<>();
        ProductStockInput input = new ProductStockInput();
        input.setProductStockId(productStock.getProductStockId());
        input.setTradeWeight(BigDecimal.valueOf(11));
        batchStockInputList.add(input);
        TradeOrder tradeOrder = this.tradeOrderService.createSellTrade(tradeDto, batchStockInputList);
        Assertions.assertNotNull(tradeOrder);

    }

    @Test
    public void createTradeFromRegisterBill() {
        Optional<TradeOrder> tradeOrder = this.tradeOrderService.createTradeFromRegisterBill(this.registerBillService.get(4L));
        Assertions.assertNotNull(tradeOrder);
    }

    @Test
    @Transactional
    public void testBuyTrade() {

        Long marketId = 8L;
        Long userId = 100L;
        List<BigDecimal> weightList = Lists.newArrayList(BigDecimal.valueOf(100L), BigDecimal.valueOf(80L));
        Mockito.doAnswer(invocation -> {

            Long uid = (Long) invocation.getArguments()[0];
            Long mid = (Long) invocation.getArguments()[1];
            return Optional.ofNullable(this.buildCustExt(mid, uid));
        }).when(customerRpcService).findCustomerById(Mockito.anyLong(), Mockito.anyLong());

        Mockito.doAnswer(invocation -> {
            return Lists.newArrayList();
        }).when(assetsRpcService).listCusCategory(Mockito.any(), Mockito.anyLong());

        ProductStock ps = super.buildProductStock(marketId, userId, weightList);
        BigDecimal totalWeight = StreamEx.of(weightList).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(ps.getStockWeight().compareTo(totalWeight), 0, "报备进门总重量与库存总重量不相等");

        TradeDetail td = new TradeDetail();
        td.setProductStockId(ps.getProductStockId());
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(td);
        BigDecimal sumWeight = StreamEx.of(tradeDetailList).map(TradeDetail::getStockWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(sumWeight.compareTo(totalWeight), 0, "报备进门总重量与批次库存总重量不相等");

        TradeDto tradeDto = new TradeDto();
        tradeDto.setMarketId(marketId);
        tradeDto.setTradeOrderType(TradeOrderTypeEnum.BUY);

        tradeDto.getSeller().setSellerId(ps.getUserId());
        tradeDto.getSeller().setSellerName(ps.getUserName());

        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);
        tradeDto.getBuyer().setBuyerId(2L);
        tradeDto.getBuyer().setBuyerName("lisi");



        //clean buyer ps

        ProductStock q = new ProductStock();
        q.setUserId(tradeDto.getBuyer().getBuyerId());
        q.setMarketId(marketId);
        q.setProductId(ps.getProductId());
        q.setWeightUnit(ps.getWeightUnit());
        q.setSpecName(ps.getSpecName());
        q.setBrandId(ps.getBrandId());
        this.productStockService.deleteByExample(q);


        BigDecimal tradeWeight = BigDecimal.valueOf(110);
        List<ProductStockInput> batchStockInputList = new ArrayList<>();
        ProductStockInput input = new ProductStockInput();
        input.setProductStockId(ps.getProductStockId());
        input.setTradeWeight(tradeWeight);
        batchStockInputList.add(input);

        TradeOrder tradeOrder = this.tradeOrderService.createBuyTrade(tradeDto, batchStockInputList);
        Assertions.assertNotNull(tradeOrder);

        ProductStock afterTradePs = this.productStockService.get(ps.getProductStockId());

        List<TradeDetail> afterTradeDetailList = StreamEx.of(tradeDetailList).map(TradeDetail::getId).map(this.tradeDetailService::get).toList();
        BigDecimal afterTradeSumWeight = StreamEx.of(afterTradeDetailList).map(TradeDetail::getStockWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal afterTradeSumSoftWeight = StreamEx.of(afterTradeDetailList).map(TradeDetail::getSoftWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(afterTradePs.getStockWeight().compareTo(afterTradeSumWeight), 0, "剩余总库存与剩余批次总库存不一致");

        assertEquals(afterTradeSumSoftWeight.add(afterTradeSumWeight).compareTo(totalWeight), 0, "锁定库存+剩余批次总库存之和不等于总进门重量");


        TradeRequest treq = new TradeRequest();
        treq.setTradeOrderId(tradeOrder.getTradeOrderId());
        List<TradeRequest> tradeRequestList = this.tradeRequestService.listByExample(treq);


        List<TradeRequestDetail> tradeRequestDetailList = StreamEx.of(tradeRequestList).map(TradeRequest::getId).flatCollection(treqId -> {
            TradeRequestDetail trdQ = new TradeRequestDetail();
            trdQ.setTradeRequestId(treqId);
            return this.tradeRequestDetailService.listByExample(trdQ);

        }).toList();
        BigDecimal tradeWeightTotal = StreamEx.of(tradeRequestDetailList).map(TradeRequestDetail::getTradeWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(tradeWeightTotal.compareTo(afterTradeSumSoftWeight), 0, "锁定库存与交易重量不一致");
        this.tradeOrderService.dealTradeOrder(tradeOrder, TradeOrderStatusEnum.FINISHED, tradeRequestList);

        ProductStock buyerPs = StreamEx.of(this.productStockService.listByExample(q)).findFirst().orElse(null);
        assertNotNull(buyerPs);
        assertEquals(buyerPs.getStockWeight().compareTo(tradeWeightTotal), 0);

        TradeDetail tdq = new TradeDetail();
        tdq.setProductStockId(buyerPs.getProductStockId());
        List<TradeDetail> buyerTradeDetailList = this.tradeDetailService.listByExample(tdq);


        long count = StreamEx.of(buyerTradeDetailList).count();
        assertEquals(count, 2);
        BigDecimal totalBuyedWeight = StreamEx.of(buyerTradeDetailList).map(TradeDetail::getStockWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(totalBuyedWeight.compareTo(tradeWeightTotal), 0);

    }

    @Test
    @Transactional
    public void testSellTrade() {

        Long marketId = 8L;
        Long sellerId = 100L;

        Long buyerId = 2L;


        List<BigDecimal> weightList = Lists.newArrayList(BigDecimal.valueOf(100L), BigDecimal.valueOf(80L));
        Mockito.doAnswer(invocation -> {
            Long uid = (Long) invocation.getArguments()[0];
            Long mid = (Long) invocation.getArguments()[1];
            return Optional.ofNullable(this.buildCustExt(mid, uid));
        }).when(customerRpcService).findCustomerById(Mockito.anyLong(), Mockito.anyLong());

        Mockito.doAnswer(invocation -> {
            return Lists.newArrayList();
        }).when(assetsRpcService).listCusCategory(Mockito.any(), Mockito.anyLong());

        ProductStock ps = super.buildProductStock(marketId, sellerId, weightList);
        BigDecimal totalWeight = StreamEx.of(weightList).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(ps.getStockWeight().compareTo(totalWeight), 0, "报备进门总重量与库存总重量不相等");

        TradeDetail td = new TradeDetail();
        td.setProductStockId(ps.getProductStockId());
        List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(td);
        BigDecimal sumWeight = StreamEx.of(tradeDetailList).map(TradeDetail::getStockWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(sumWeight.compareTo(totalWeight), 0, "报备进门总重量与批次库存总重量不相等");

        TradeDto tradeDto = new TradeDto();
        tradeDto.setMarketId(marketId);
        tradeDto.setTradeOrderType(TradeOrderTypeEnum.SELL);

        tradeDto.getSeller().setSellerId(ps.getUserId());
        tradeDto.getSeller().setSellerName(ps.getUserName());


        tradeDto.getBuyer().setBuyerType(BuyerTypeEnum.NORMAL_BUYER);
        tradeDto.getBuyer().setBuyerId(buyerId);
        tradeDto.getBuyer().setBuyerName("lisi");


        //clean buyer ps

        ProductStock q = new ProductStock();
        q.setUserId(buyerId);
        q.setMarketId(marketId);
        q.setProductId(ps.getProductId());
        q.setWeightUnit(ps.getWeightUnit());
        q.setSpecName(ps.getSpecName());
        q.setBrandId(ps.getBrandId());
        this.productStockService.deleteByExample(q);


        BigDecimal tradeWeight = BigDecimal.valueOf(110);
        List<ProductStockInput> batchStockInputList = new ArrayList<>();
        ProductStockInput input = new ProductStockInput();
        input.setProductStockId(ps.getProductStockId());
        input.setTradeWeight(tradeWeight);
        batchStockInputList.add(input);
        System.out.println(this.tradeOrderService);
        System.out.println(this.tradeOrderService.tradeRequestService);

        AtomicBoolean fakeDealTradeOrder = new AtomicBoolean(true);

        new MockUp<TradeOrderService>() {
            @Mock
            protected void dealTradeOrder(Invocation inv, TradeOrder tradeOrderItem, TradeOrderStatusEnum tradeOrderStatusEnum, List<TradeRequest> tradeRequestList) {
                if (fakeDealTradeOrder.get()) {
                    System.out.println("fake dealTradeOrder");
                } else {
                    inv.proceed(tradeOrderItem, tradeOrderStatusEnum, tradeRequestList);
                }
            }
        };
        fakeDealTradeOrder.set(true);
        TradeOrder tradeOrder = this.tradeOrderService.createSellTrade(tradeDto, batchStockInputList);


        Assertions.assertNotNull(tradeOrder);
//        Mockito.doReturn(null).when(this.tradeOrderService);

        ProductStock afterTradePs = this.productStockService.get(ps.getProductStockId());

        List<TradeDetail> afterTradeDetailList = StreamEx.of(tradeDetailList).map(TradeDetail::getId).map(this.tradeDetailService::get).toList();
        BigDecimal afterTradeSumWeight = StreamEx.of(afterTradeDetailList).map(TradeDetail::getStockWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal afterTradeSumSoftWeight = StreamEx.of(afterTradeDetailList).map(TradeDetail::getSoftWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(afterTradePs.getStockWeight().compareTo(afterTradeSumWeight), 0, "剩余总库存与剩余批次总库存不一致");

        assertEquals(afterTradeSumSoftWeight.add(afterTradeSumWeight).compareTo(totalWeight), 0, "锁定库存+剩余批次总库存之和不等于总进门重量");


        TradeRequest treq = new TradeRequest();
        treq.setTradeOrderId(tradeOrder.getTradeOrderId());
        List<TradeRequest> tradeRequestList = this.tradeRequestService.listByExample(treq);


        List<TradeRequestDetail> tradeRequestDetailList = StreamEx.of(tradeRequestList).map(TradeRequest::getId).flatCollection(treqId -> {
            TradeRequestDetail trdQ = new TradeRequestDetail();
            trdQ.setTradeRequestId(treqId);
            return this.tradeRequestDetailService.listByExample(trdQ);

        }).toList();
        BigDecimal tradeWeightTotal = StreamEx.of(tradeRequestDetailList).map(TradeRequestDetail::getTradeWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(tradeWeightTotal.compareTo(afterTradeSumSoftWeight), 0, "锁定库存与交易重量不一致");


        fakeDealTradeOrder.set(false);
        this.tradeOrderService.dealTradeOrder(tradeOrder, TradeOrderStatusEnum.FINISHED, tradeRequestList);

        ProductStock buyerPs = StreamEx.of(this.productStockService.listByExample(q)).findFirst().orElse(null);
        assertNotNull(buyerPs);
        assertEquals(buyerPs.getStockWeight().compareTo(tradeWeightTotal), 0);

        TradeDetail tdq = new TradeDetail();
        tdq.setProductStockId(buyerPs.getProductStockId());
        List<TradeDetail> buyerTradeDetailList = this.tradeDetailService.listByExample(tdq);


        long count = StreamEx.of(buyerTradeDetailList).count();
        assertEquals(count, 2);
        BigDecimal totalBuyedWeight = StreamEx.of(buyerTradeDetailList).map(TradeDetail::getStockWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(totalBuyedWeight.compareTo(tradeWeightTotal), 0);

    }


}
