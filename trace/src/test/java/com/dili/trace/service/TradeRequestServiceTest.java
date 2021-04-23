package com.dili.trace.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.api.input.ProductStockInput;
import com.dili.trace.api.input.TradeDetailInputDto;
import com.dili.trace.api.input.TradeRequestInputDto;
import com.dili.trace.domain.ProductStock;
import com.dili.trace.domain.Brand;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.domain.TradeRequest;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.TradeDto;
import com.dili.trace.enums.*;
import com.google.common.collect.Lists;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

public class TradeRequestServiceTest extends AutoWiredBaseTest {
    @Autowired
    TradeRequestService tradeRequestService;
    @Autowired
    ProductStockService batchStockService;
    @Autowired
    AssetsRpcService categoryService;
    @Autowired
    BrandService brandService;
    @Autowired
    TradeDetailService tradeDetailService;

    private TradeDetail createTradeDetail(ProductStock batchStock) {

        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setStockWeight(BigDecimal.valueOf(120));
        tradeDetail.setTotalWeight(BigDecimal.valueOf(120));
        tradeDetail.setTradeType(TradeTypeEnum.NONE.getCode());
        tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetail.setBuyerId(batchStock.getUserId());
        tradeDetail.setBuyerName(batchStock.getUserName());
        tradeDetail.setProductName(batchStock.getProductName());
        tradeDetail.setProductStockId(batchStock.getId());
        tradeDetail.setBillId(1L);
        tradeDetail.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
        this.tradeDetailService.insertSelective(tradeDetail);
        return tradeDetail;
    }

    private ProductStock createBatchStock() {
        List<UserInfo> userList = super.findUsers();
        assertNotNull(userList);
        assertTrue(userList.size() >= 2);
        UserInfo seller = userList.get(0);
        UserInfo buyer = userList.get(1);

        Brand brand = new Brand();
        brand.setUserId(seller.getId());
        brand.setBrandName("demo");
        brandService.insertSelective(brand);

//        Category category = super.findCategory();
//        assertNotNull(category);
        ProductStock batchStock = new ProductStock();
        batchStock.setUserId(seller.getId());
        batchStock.setUserName(seller.getName());
//        batchStock.setProductId(category.getId());
//        batchStock.setProductName(category.getName());
        batchStock.setStockWeight(BigDecimal.valueOf(120));
        batchStock.setTotalWeight(BigDecimal.valueOf(120));
        batchStock.setPreserveType(PreserveTypeEnum.FRESH.getCode());
        batchStock.setBrandId(brand.getId());
        this.batchStockService.insertSelective(batchStock);

        assertNotNull(batchStock);
        return batchStock;

    }

    private TradeRequest createBuyTradeRequest(ProductStock batchStock) {
        List<UserInfo> userList = super.findUsers();
        assertNotNull(userList);
        assertTrue(userList.size() >= 2);
        UserInfo seller = userList.get(0);
        UserInfo buyer = userList.get(1);

        ProductStockInput input = new ProductStockInput();
        input.setProductStockId(batchStock.getId());
        input.setTradeWeight(BigDecimal.valueOf(77));

//        TradeRequest request = this.tradeRequestService.createTradeRequest(null, null, buyer.getId(), input, 11L);
//        assertNotNull(request);
//        return request;

        return null;
    }


    @Test
    public void createBuyRequest() {
        List<UserInfo> userList = super.findUsers();
        assertNotNull(userList);
        assertTrue(userList.size() >= 2);
        UserInfo seller = userList.get(0);
        UserInfo buyer = userList.get(1);

        Brand brand = new Brand();
        brand.setUserId(seller.getId());
        brand.setBrandName("demo");
        brandService.insertSelective(brand);
//
//        Category category = super.findCategory();
//        assertNotNull(category);
        ProductStock batchStock = new ProductStock();
        batchStock.setUserId(seller.getId());
        batchStock.setUserName(seller.getName());
//        batchStock.setProductId(category.getId());
//        batchStock.setProductName(category.getName());
        batchStock.setStockWeight(BigDecimal.valueOf(120));
        batchStock.setTotalWeight(BigDecimal.valueOf(120));
        batchStock.setPreserveType(PreserveTypeEnum.FRESH.getCode());
        batchStock.setBrandId(brand.getId());
        this.batchStockService.insertSelective(batchStock);

        TradeRequest request = new TradeRequest();
        request.setBuyerId(buyer.getId());
        request.setProductStockId(batchStock.getId());
        request.setTradeWeight(BigDecimal.TEN);
        TradeRequest tradeRequestItem = ReflectionTestUtils.invokeMethod(this.tradeRequestService, "createBuyRequest",
                request);
        assertNotNull(tradeRequestItem);

        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setProductStockId(batchStock.getId());
        tradeDetail.setBuyerId(batchStock.getUserId());
        tradeDetail.setBuyerName(batchStock.getUserName());
        tradeDetail.setBillId(1L);
        tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetail.setTradeType(TradeTypeEnum.NONE.getCode());
        tradeDetail.setStockWeight(BigDecimal.valueOf(120));
        tradeDetail.setTotalWeight(BigDecimal.valueOf(120));
        tradeDetail.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
        tradeDetail.setProductName(batchStock.getProductName());
        this.tradeDetailService.insertSelective(tradeDetail);
        assertNotNull(tradeDetail.getId());

    }

    @Test
    public void createSellRequest() {
        List<UserInfo> userList = super.findUsers();
        assertNotNull(userList);
        assertTrue(userList.size() >= 2);
        UserInfo ownedUser = userList.get(0);
        UserInfo buyer = userList.get(1);

        Brand brand = new Brand();
        brand.setUserId(ownedUser.getId());
        brand.setBrandName("demo");
        brandService.insertSelective(brand);

//        Category category = super.findCategory();
//        assertNotNull(category);
        ProductStock batchStock = new ProductStock();
        batchStock.setUserId(ownedUser.getId());
        batchStock.setUserName(ownedUser.getName());
//        batchStock.setProductId(category.getId());
//        batchStock.setProductName(category.getName());
        batchStock.setStockWeight(BigDecimal.valueOf(120));
        batchStock.setTotalWeight(BigDecimal.valueOf(120));
        batchStock.setPreserveType(PreserveTypeEnum.FRESH.getCode());
        batchStock.setBrandId(brand.getId());
        this.batchStockService.insertSelective(batchStock);

        TradeDetail tradeDetail = new TradeDetail();
        tradeDetail.setProductStockId(batchStock.getId());
        tradeDetail.setBuyerId(ownedUser.getId());
        tradeDetail.setBuyerName(ownedUser.getName());
        tradeDetail.setBillId(1L);
        tradeDetail.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
        tradeDetail.setTradeType(TradeTypeEnum.NONE.getCode());
        tradeDetail.setStockWeight(BigDecimal.valueOf(120));
        tradeDetail.setTotalWeight(BigDecimal.valueOf(120));
        tradeDetail.setCheckinStatus(CheckinStatusEnum.ALLOWED.getCode());
        tradeDetail.setCheckoutStatus(CheckoutStatusEnum.NONE.getCode());
        tradeDetail.setProductName(batchStock.getProductName());
        this.tradeDetailService.insertSelective(tradeDetail);
        assertNotNull(tradeDetail.getId());

        TradeRequest request = new TradeRequest();
        request.setBuyerId(buyer.getId());
        request.setBuyerName(buyer.getName());
        request.setProductStockId(batchStock.getId());
        request.setTradeWeight(BigDecimal.TEN);

        ProductStockInput input = new ProductStockInput();
        input.setProductStockId(1010L);
        input.setTradeWeight(BigDecimal.valueOf(88));

        // TradeDetailInputDto tdinput=new TradeDetailInputDto();
        // tdinput.setTradeDetailId(5016L);
        // tdinput.setTradeWeight(BigDecimal.valueOf(85));
        // input.setTradeDetailInputList(Lists.newArrayList(tdinput));
//        this.tradeRequestService.createSellRequest(30L, 29L, Lists.newArrayList(input), 11L, Optional.empty());

        // Long requestId =
        // this.tradeRequestService.createSellRequest(request,Lists.newArrayList());
        // assertNotNull(requestId);

    }

    @Test
    public void listPageTradeRequestByBuyerIdOrSellerId() {
        TradeRequestInputDto request = new TradeRequestInputDto();
        request.setLikeProductName("abc");
        request.setCreatedStart("2020-12-12 12:12:12");
        this.tradeRequestService.listPageTradeRequestByBuyerIdOrSellerId(request,2L);

    }
}