package com.dili.trace.service;

import com.dili.orders.dto.WeighingBillClientListDto;
import com.dili.orders.rpc.OrderServiceRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.trace.domain.TradeType;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.glossary.QualityTraceTradeBillMatchStatusEnum;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 订单接口
 */
@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Autowired(required = false)
    OrderServiceRpc orderRpc;
    @Autowired
    TradeTypeService tradeTypeService;

    /**
     * 查询订单信息
     *
     * @param startId
     * @param limit
     * @return
     */
    public List<QualityTraceTradeBill> fetchOrderData(Long startId, Integer limit) {
        try {
            Map<String, TradeType> tradeTypeMap = this.tradeTypeService.queryTradeTypeMap();
            return StreamEx.ofNullable(this.orderRpc.sourceSync(startId, limit)).nonNull()
                    .filter(BaseOutput::isSuccess).map(BaseOutput::getData).nonNull()
                    .flatCollection(Function.identity()).nonNull().map(this::build).map(bill -> {
                        if (StringUtils.isBlank(bill.getTradetypeName())) {
                            String tradeTypeName=Optional.ofNullable(tradeTypeMap.get(bill.getTradetypeId())).map(TradeType::getTypeName).orElse("");
                            bill.setTradetypeName(tradeTypeName);
                        }
                        return bill;
                    }).sortedByLong(QualityTraceTradeBill::getBillId).toList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Lists.newArrayList();


    }

    /**
     * 数据结构转换
     *
     * @param dto
     * @return
     */
    private QualityTraceTradeBill build(WeighingBillClientListDto dto) {
        QualityTraceTradeBill bill = new QualityTraceTradeBill();
        bill.setBillId(dto.getId());
        bill.setOrderId(dto.getSerialNo());
        bill.setBuyerAccount(String.valueOf(dto.getBuyerAccount()));
        bill.setBuyerName(dto.getBuyerName());
        bill.setBuyerIDNo(dto.getBuyerCertificateNumber());
        bill.setOrderPayDate(Date.from(dto.getSettlementTime().atZone(ZoneId.systemDefault()).toInstant()));
        bill.setOrderCreateDate(Date.from(dto.getCreatedTime().atZone(ZoneId.systemDefault()).toInstant()));
        bill.setProductName(dto.getGoodsName());
//        bill.setCateName(dto.getca);
        bill.setPrice(dto.getUnitPrice());
        if (dto.getUnitAmount() != null) {
            bill.setPieceQuantity(dto.getUnitAmount().longValue());
        } else {
            bill.setPieceQuantity(0L);
        }
        if (dto.getUnitWeight() != null) {
            bill.setPieceWeight(new BigDecimal(dto.getUnitWeight()).divide(BigDecimal.valueOf(100)).longValue());
        } else {
            bill.setPieceWeight(0L);
        }

        bill.setSaleUnit("1".equals(dto.getMeasureType()) ? 1 : 2);
        if (dto.getNetWeight() != null) {
            bill.setNetWeight(new BigDecimal(dto.getNetWeight()).divide(BigDecimal.valueOf(100)).longValue());
        } else {
            bill.setNetWeight(0L);
        }

        bill.setSellerName(dto.getSellerName());
        bill.setSellerAccount(String.valueOf(dto.getSellerAccount()));
        bill.setSellerIDNo(dto.getSellerCertificateNumber());
        bill.setTradetypeId(dto.getTradeType());
        bill.setTradetypeName(dto.getTradeTypeName());
        bill.setBillActive(0);
        bill.setMatchStatus(QualityTraceTradeBillMatchStatusEnum.INITED.getCode());
        return bill;
    }

}
