package com.dili.trace.rpc.service;

import com.dili.customer.sdk.domain.BusinessCategory;
import com.dili.customer.sdk.rpc.BusinessCategoryRpc;
import com.dili.ss.domain.BaseOutput;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 查询BusinessCategoryRpcService
 */
@Service
public class BusinessCategoryRpcService {
    private static final Logger logger = LoggerFactory.getLogger(BusinessCategoryRpcService.class);
    @Autowired
    BusinessCategoryRpc businessCategoryRpc;

    /**
     * 根据市场id和customerid查询
     *
     * @param marketId
     * @param customerId
     * @return
     */
    public List<BusinessCategory> findBusinessCategoryByMarketIdAndCustomerId(Long marketId, Long customerId) {
        if (marketId == null || customerId == null) {
            return Lists.newArrayList();
        }

        try {
            BaseOutput<List<BusinessCategory>> out = this.businessCategoryRpc.list(customerId, marketId);
            if (out == null) {
                logger.error("查询返回BaseOutput为Null");
                return Lists.newArrayList();
            }
            if (!out.isSuccess()) {
                logger.error("查询返回Message为:{}", out.getMessage());
                return Lists.newArrayList();
            }
            return out.getData();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    /**
     * 根据市场id和多个customerid查询
     *
     * @param marketId
     * @param customerIdList
     * @return
     */
    public Map<Long, List<BusinessCategory>> findBusinessCategoryByMarketIdAndCustomerIdList(Long marketId, List<Long> customerIdList) {
        if (marketId == null || customerIdList == null || customerIdList.isEmpty()) {
            return Maps.newHashMap();
        }
        try {
            BaseOutput<Map<Long, List<BusinessCategory>>> out = this.businessCategoryRpc.batchQuery(Sets.newHashSet(customerIdList), marketId);
            if (out == null) {
                logger.error("查询返回BaseOutput为Null");
                return Maps.newHashMap();
            }
            if (!out.isSuccess()) {
                logger.error("查询返回Message为:{}", out.getMessage());
                return Maps.newHashMap();
            }
            return out.getData();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Maps.newHashMap();
        }


    }
}
