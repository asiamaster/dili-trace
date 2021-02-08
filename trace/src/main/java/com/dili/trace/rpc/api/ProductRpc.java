package com.dili.trace.rpc.api;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.retrofitful.annotation.VOBody;
import com.dili.trace.rpc.dto.RegCreateDto;
import com.dili.trace.rpc.dto.RegCreateResultDto;
import com.dili.trace.rpc.dto.StockReduceRequestDto;
import com.dili.trace.rpc.dto.StockReductResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 库存系统 rpc
 *
 * @author wangguofeng
 */
@FeignClient(
        name = "product-service",
        contextId = "productRpc",
        url = "${productService.url:}"
)
public interface ProductRpc {
    /**
     * 批次库存增加
     *
     * @param obj
     * @return
     */
    @PostMapping(value="/api/stock/incByStockIds",consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseOutput<List<StockReductResultDto>> incByStockIds(@VOBody StockReduceRequestDto obj);

    /**
     * 批次库存扣减
     *
     * @param obj
     * @return
     */
    @PostMapping(value="/api/stock/reduceByStockIds",consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseOutput<List<StockReductResultDto>> reduceByStockIds(@VOBody StockReduceRequestDto obj);

    /**
     * 创建批次库存
     * @param obj
     * @return
     */
    @PostMapping(value="/api/register/create",consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseOutput<RegCreateResultDto> create(@VOBody RegCreateDto obj);
}