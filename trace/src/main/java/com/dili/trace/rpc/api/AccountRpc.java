package com.dili.trace.rpc.api;

import cn.hutool.http.HttpUtil;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.retrofitful.annotation.VOBody;
import com.dili.trace.rpc.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@FeignClient(
        name = "account-service",
        contextId = "accountRpc",
        url = "${accountService.url:}"
)
public interface AccountRpc {
    /**
     * 批次库存增加
     *
     * @param obj
     * @return
     */
    @PostMapping(value = "/api/account/getList", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseOutput<List<AccountGetListResultDto>> getList(AccountGetListQueryDto obj);



}
