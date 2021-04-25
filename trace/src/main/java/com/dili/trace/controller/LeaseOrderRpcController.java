package com.dili.trace.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.rpc.dto.AssetsParamsDto;
import com.dili.trace.rpc.dto.AssetsResultDto;
import com.dili.trace.rpc.service.LeaseOrderRpcService;
import com.dili.trace.service.UapRpcService;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 调用rpc，查询可用摊位号
 */
@Controller
@RequestMapping("/leaseOrderRpc")
public class LeaseOrderRpcController {
    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    LeaseOrderRpcService leaseOrderRpcService;

    /**
     * 查询可用摊位号
     *
     * @param assetsParamsDto
     * @return
     */
    @RequestMapping("/findLease.action")
    @ResponseBody
    public BaseOutput<List<String>> findLease(@RequestBody AssetsParamsDto assetsParamsDto) {
        if (assetsParamsDto.getCustomerId() == null) {
            return BaseOutput.failure("参数错误");
        }
        assetsParamsDto.setBizType(1);
        assetsParamsDto.setMarketId(this.uapRpcService.getCurrentFirm().get().getId());
        try {
            List<AssetsResultDto> assetsResultDtoList = this.leaseOrderRpcService.findLease(assetsParamsDto);
            List<String> dataList = StreamEx.of(assetsResultDtoList).map(AssetsResultDto::getAssetsName).toList();
//            dataList.add("sss");
//            dataList.add("bbb");
            return BaseOutput.successData(dataList);
        } catch (Exception e) {
            return BaseOutput.failure("后端出错");
        }
    }

}
