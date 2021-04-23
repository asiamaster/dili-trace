package com.dili.trace.rpc.service;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.rpc.api.LeaseOrderRpc;
import com.dili.trace.rpc.dto.AssetsParamsDto;
import com.dili.trace.rpc.dto.AssetsResultDto;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * 调用rpc，查询可用摊位号
 */
@Service
public class LeaseOrderRpcService {
    private static final Logger logger = LoggerFactory.getLogger(LeaseOrderRpcService.class);
    @Autowired
    LeaseOrderRpc leaseOrderRpc;

    /**
     * 调用rpc，查询可用摊位号
     *
     * @param assetsParamsDto
     * @return
     */
    public List<AssetsResultDto> findLease(AssetsParamsDto assetsParamsDto) {
        try {
            BaseOutput<List<AssetsResultDto>>out= this.leaseOrderRpc.findLease(assetsParamsDto);
            if(out!=null&&out.isSuccess()){
                return StreamEx.ofNullable(out.getData()).flatCollection(Function.identity()).nonNull().toList();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return Lists.newArrayList();
    }
}
