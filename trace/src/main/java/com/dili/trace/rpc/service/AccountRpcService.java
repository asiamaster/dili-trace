package com.dili.trace.rpc.service;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.rpc.api.AccountRpc;
import com.dili.trace.rpc.dto.AccountGetListQueryDto;
import com.dili.trace.rpc.dto.AccountGetListResultDto;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountRpcService {
    private static final Logger logger = LoggerFactory.getLogger(AccountRpcService.class);
    @Autowired
    AccountRpc accountRpc;

    public List<AccountGetListResultDto> getList(AccountGetListQueryDto obj) {

        try {
            BaseOutput<List<AccountGetListResultDto>> out = this.accountRpc.getList(obj);
            if(out==null){
                logger.error("BaseOutput为Null");
                return Lists.newArrayList();
            }
            if (!out.isSuccess()) {
                logger.error("BaseOutput.isSuccess=:{}",out.isSuccess());
                return Lists.newArrayList();
            }
            return out.getData();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Lists.newArrayList();
        }

    }
}
