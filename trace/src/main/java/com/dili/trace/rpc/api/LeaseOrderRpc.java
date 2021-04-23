package com.dili.trace.rpc.api;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.rpc.dto.AssetsParamsDto;
import com.dili.trace.rpc.dto.AssetsResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 分布式文件系统rpc
 *
 * @author yuehongbo
 * @Copyright 本软件源代码版权归农丰时代科技有限公司及其研发团队所有, 未经许可不得任意复制与传播.
 * @date 2020/8/17 18:37
 */
@FeignClient(name = "dili-ia", contextId = "dili-ia", url = "${diliIA.url:}")
public interface LeaseOrderRpc {

    /**
     * 查询用户的可用摊位
     *
     * @param assetsParamsDto
     * @return
     */
    @PostMapping(value = "/api/leaseOrder/findLease")
    BaseOutput<List<AssetsResultDto>> findLease(AssetsParamsDto assetsParamsDto);

}
