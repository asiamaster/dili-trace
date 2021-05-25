package com.dili.trace.rpc.service;

import com.dili.customer.sdk.domain.TallyingArea;
import com.dili.customer.sdk.rpc.TallyingAreaRpc;
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
 * @author Alvin.Li
 */
@Service
public class TallyingAreaRpcService {

    private static final Logger logger = LoggerFactory.getLogger(TallyingAreaRpcService.class);

    @Autowired
    TallyingAreaRpc tallyingAreaRpc;

    /**
     * 查询理货区
     *
     * @return
     */
    public TallyingArea findCustomerByIdOrEx(String tallyAreaNo, Long marketId) {

        TallyingArea tallyingAreaQuery = new TallyingArea();
        tallyingAreaQuery.setAssetsName(tallyAreaNo);
        this.tallyingAreaRpc.listByExample(tallyingAreaQuery);
        return null;
    }


    /**
     * 根据市场id和customerid查询
     *
     * @param marketId
     * @param customerId
     * @return
     */
    public List<TallyingArea> findTallyingAreaByMarketIdAndCustomerId(Long marketId, Long customerId) {
        if (marketId == null || customerId == null) {
            return Lists.newArrayList();
        }

        try {
            BaseOutput<List<TallyingArea>> out = this.tallyingAreaRpc.listTallyingArea(customerId, marketId);
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
    public Map<Long, List<TallyingArea>> findTallyingAreaByMarketIdAndCustomerIdList(Long marketId, List<Long> customerIdList) {
        if (marketId == null || customerIdList == null || customerIdList.isEmpty()) {
            return Maps.newHashMap();
        }
        TallyingArea q = new TallyingArea();
        q.setMarketId(marketId);
        q.setCustomerIdSet(Sets.newHashSet(customerIdList));

        try {
            BaseOutput<List<TallyingArea>> out = this.tallyingAreaRpc.listByExample(q);
            if (out == null) {
                logger.error("查询返回BaseOutput为Null");
                return Maps.newHashMap();
            }
            if (!out.isSuccess()) {
                logger.error("查询返回Message为:{}", out.getMessage());
                return Maps.newHashMap();
            }
            return StreamEx.of(out.getData()).groupingBy(TallyingArea::getCustomerId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Maps.newHashMap();
        }
    }

}
