package com.dili.trace.service;

import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.domain.ProductStock;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.rpc.service.ProductRpcService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.math.BigDecimal;
import java.util.Optional;

@EnableDiscoveryClient
public class ProductRpcServiceTest extends AutoWiredBaseTest {


    @Autowired
    ProductRpcService productRpcService;
    @Autowired
    BillService billService;
    @Autowired
    TradeDetailService tradeDetailService;
    @Autowired
    ProductStockService productStockService;

    /**
     * test
     */
    @Test
    public void createRegCreate() {
        RegisterBill rb = new RegisterBill();
        rb.setUserId(287L);
        rb.setWeight(BigDecimal.valueOf(100));
        rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
        rb.setProductId(61L);
        rb.setProductName("玉米");
        rb.setCode(String.valueOf(System.currentTimeMillis()));
        rb.setVerifyStatus(BillVerifyStatusEnum.PASSED.getCode());
        rb.setRegisterSource(RegisterSourceEnum.OTHERS.getCode());

        this.billService.insertSelective(rb);
        ProductStock ps = new ProductStock();
        ps.setStockWeight(BigDecimal.valueOf(100));
        ps.setUserId(287L);
        ps.setUserName("德华物流信息");
        ps.setWeightUnit(WeightUnitEnum.KILO.getCode());
        ps.setProductId(61L);
        ps.setProductName("玉米");

        this.productStockService.insertSelective(ps);


        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBuyerId(287L);
        tradeDetail.setProductStockId(ps.getProductStockId());
        tradeDetail.setBillId(rb.getBillId());
        tradeDetail.setWeightUnit(WeightUnitEnum.KILO.getCode());
        tradeDetail.setStockWeight(BigDecimal.valueOf(100));
        tradeDetail.setTotalWeight(BigDecimal.valueOf(100));
        tradeDetail.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
        tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetail.setTradeType(TradeTypeEnum.NONE.getCode());
        tradeDetail.setProductName("玉米");
        this.tradeDetailService.insertSelective(tradeDetail);

        this.productRpcService.createRegCreate(tradeDetail.getId(), 8L, Optional.empty());

    }

    @Test
    public void deductRegDetail() {

        RegisterBill rb = new RegisterBill();
        rb.setUserId(287L);
        rb.setWeight(BigDecimal.valueOf(30));
        rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
        rb.setProductId(61L);
        rb.setProductName("玉米");
        rb.setCode(String.valueOf(System.currentTimeMillis()));
        rb.setVerifyStatus(BillVerifyStatusEnum.PASSED.getCode());
        rb.setRegisterSource(RegisterSourceEnum.OTHERS.getCode());

        this.billService.insertSelective(rb);
        ProductStock ps = new ProductStock();
        ps.setStockWeight(BigDecimal.valueOf(30));
        ps.setUserId(287L);
        ps.setUserName("德华物流信息");
        ps.setWeightUnit(WeightUnitEnum.KILO.getCode());
        ps.setProductId(61L);
        ps.setProductName("玉米");

        this.productStockService.insertSelective(ps);


        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBuyerId(287L);
        tradeDetail.setProductStockId(ps.getProductStockId());
        tradeDetail.setBillId(rb.getBillId());
        tradeDetail.setWeightUnit(WeightUnitEnum.KILO.getCode());
        tradeDetail.setStockWeight(BigDecimal.valueOf(10));
        tradeDetail.setTotalWeight(BigDecimal.valueOf(100));
        tradeDetail.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
        tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetail.setTradeType(TradeTypeEnum.NONE.getCode());
        tradeDetail.setProductName("玉米");
        tradeDetail.setThirdPartyStockId(20210207000007L);
        this.tradeDetailService.insertSelective(tradeDetail);

        this.productRpcService.deductRegDetail(tradeDetail.getTradeDetailId(), 8L, BigDecimal.valueOf(10), Optional.empty());
    }

    @Test
    public void increaseRegDetail() {

        RegisterBill rb = new RegisterBill();
        rb.setUserId(287L);
        rb.setWeight(BigDecimal.valueOf(30));
        rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
        rb.setProductId(61L);
        rb.setProductName("玉米");
        rb.setCode(String.valueOf(System.currentTimeMillis()));
        rb.setVerifyStatus(BillVerifyStatusEnum.PASSED.getCode());
        rb.setRegisterSource(RegisterSourceEnum.OTHERS.getCode());

        this.billService.insertSelective(rb);
        ProductStock ps = new ProductStock();
        ps.setStockWeight(BigDecimal.valueOf(30));
        ps.setUserId(287L);
        ps.setUserName("德华物流信息");
        ps.setWeightUnit(WeightUnitEnum.KILO.getCode());
        ps.setProductId(61L);
        ps.setProductName("玉米");

        this.productStockService.insertSelective(ps);


        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setBuyerId(287L);
        tradeDetail.setProductStockId(ps.getProductStockId());
        tradeDetail.setBillId(rb.getBillId());
        tradeDetail.setWeightUnit(WeightUnitEnum.KILO.getCode());
        tradeDetail.setStockWeight(BigDecimal.valueOf(5));
        tradeDetail.setTotalWeight(BigDecimal.valueOf(100));
        tradeDetail.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
        tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetail.setTradeType(TradeTypeEnum.NONE.getCode());
        tradeDetail.setProductName("玉米");
        tradeDetail.setThirdPartyStockId(20210207000007L);
        this.tradeDetailService.insertSelective(tradeDetail);

        this.productRpcService.increaseRegDetail(tradeDetail.getTradeDetailId(), 8L, BigDecimal.valueOf(5), Optional.empty());
    }


}
