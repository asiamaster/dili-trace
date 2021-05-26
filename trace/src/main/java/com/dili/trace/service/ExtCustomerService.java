package com.dili.trace.service;

import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.trace.async.AsyncService;
import com.dili.trace.domain.TraceCustomer;
import com.dili.trace.rpc.dto.AccountGetListQueryDto;
import com.dili.trace.rpc.dto.AccountGetListResultDto;
import com.dili.trace.rpc.dto.CustomerQueryDto;
import com.dili.trace.rpc.service.AccountRpcService;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.rpc.service.FirmRpcService;
import com.dili.uap.sdk.domain.Firm;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * 扩展
 */
@Service
public class ExtCustomerService {
    private static final Logger logger = LoggerFactory.getLogger(ExtCustomerService.class);
    @Autowired
    CustomerRpcService customerRpcService;
    @Autowired
    AccountRpcService accountRpcService;
    @Autowired
    FirmRpcService firmRpcService;
    @Autowired
    AsyncService asyncService;

    /**
     * 根据客户idlist查询卡号
     *
     * @param marketId
     * @param customerIdList
     * @return
     */
    public Map<Long, AccountGetListResultDto> findCardInfoByCustomerIdList(Long marketId, List<Long> customerIdList) {

        AccountGetListQueryDto accountGetListQueryDto = new AccountGetListQueryDto();
        accountGetListQueryDto.setCustomerIds(customerIdList);
        accountGetListQueryDto.setFirmId(marketId);
        return StreamEx.of(this.accountRpcService.getList(accountGetListQueryDto)).toMap(accountGetListResultDto -> accountGetListResultDto.getCustomerId(),
                accountGetListResultDto -> {
                    return accountGetListResultDto;
                });


    }

    /**
     * 根据卡号查询信息
     *
     * @param cardNo
     * @param firm
     */
    public List<TraceCustomer> queryCustomerByCardNo(String cardNo, Firm firm) {
        if (StringUtils.isBlank(cardNo) || firm == null) {
            return Lists.newArrayList();
        }
        List<Long> customerIdList = this.findCustomerIdListByCardNoList(Lists.newArrayList(cardNo.trim()), firm);
        return this.findByCustomerIdList(customerIdList, firm);
    }

    /**
     * 根据customer code查询信息
     *
     * @param customerCode
     * @param firm
     */
    public List<TraceCustomer> queryCustomerByCustomerCode(String customerCode, Firm firm) {
        if (StringUtils.isBlank(customerCode) || firm == null) {
            return Lists.newArrayList();
        }
        List<Long> customerIdList = this.findCustomerIdListByCustomerCode(Lists.newArrayList(customerCode.trim()), firm);
        return this.findByCustomerIdList(customerIdList, firm);
    }

    /**
     * 根据id查询信息
     *
     * @param id
     * @param firm
     */
    public List<TraceCustomer> querySellersByCustomerId(Long id, Firm firm) {
        if (id == null || firm == null) {
            return Lists.newArrayList();
        }
        List<Long> customerIdList = Lists.newArrayList(id);
        return this.findByCustomerIdList(Lists.newArrayList(id), firm);
    }

    /**
     * 查询
     *
     * @param keyword
     * @param firm
     */
    public List<TraceCustomer> querySellersByKeyWord(String keyword, Firm firm) {
        if (StringUtils.isBlank(keyword) || firm == null) {
            return Lists.newArrayList();
        }
        List<Long> customerIdList = this.findSellerIdListByKeyword(keyword, firm);
        return this.findByCustomerIdList(customerIdList, firm);
    }


    /**
     * 查询
     *
     * @param keyword
     * @param firm
     */
    public List<Long> findSellerIdListByKeyword(String keyword, Firm firm) {
        if (StringUtils.isBlank(keyword) || firm == null) {
            return Lists.newArrayList();
        }
        List<Future<List<Long>>> futures = Lists.newArrayList(this.asyncService.executeAsync(() -> {
                    return this.customerRpcService.findSellerIdListByKeywordFromCustomer(keyword, firm);
                }),
                this.asyncService.executeAsync(() -> {
                    return this.customerRpcService.findSellerIdListByKeywordFromAccount(keyword, firm);
                })

        );
        List<Long> customerIdList = StreamEx.of(futures).flatCollection(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return Lists.newArrayList();
        }).toList();
        return customerIdList;
    }

    /**
     * 根据卡号集合查询idlist
     *
     * @param cardNoList
     * @param firm
     * @return
     */
    private List<Long> findCustomerIdListByCardNoList(List<String> cardNoList, Firm firm) {
        List<String> list = StreamEx.ofNullable(cardNoList).flatCollection(Function.identity()).filter(StringUtils::isNotBlank).map(String::trim).toList();
        if (list.isEmpty()) {
            return Lists.newArrayList();
        }
        AccountGetListQueryDto accountGetListQueryDto = new AccountGetListQueryDto();
        accountGetListQueryDto.setCardNos(list);
        accountGetListQueryDto.setFirmId(firm.getId());

        return StreamEx.of(this.accountRpcService.getList(accountGetListQueryDto)).map(accountGetListResultDto -> {
            return accountGetListResultDto.getCustomerId();
        }).toList();
    }

    /**
     * 根据customercodelist查询customerid
     *
     * @param customerCodeList
     * @param firm
     * @return
     */
    private List<Long> findCustomerIdListByCustomerCode(List<String> customerCodeList, Firm firm) {
        List<String> list = StreamEx.ofNullable(customerCodeList).flatCollection(Function.identity()).filter(StringUtils::isNotBlank).map(String::trim).toList();
        if (list.isEmpty()) {
            return Lists.newArrayList();
        }

        CustomerQueryDto query = new CustomerQueryDto();
        query.setCodeList(list);
        query.setMarketId(firm.getId());
        return StreamEx.of(this.customerRpcService.listSeller(query).getData()).map(customerExtendDto -> {
            return customerExtendDto.getId();
        }).toList();

    }

    /**
     * 根据id查询数据
     *
     * @param customerIdList
     * @param firm
     * @return
     */
    private List<TraceCustomer> findByCustomerIdList(List<Long> customerIdList, Firm firm) {
        if (customerIdList.isEmpty() || firm == null) {
            return Lists.newArrayList();
        }
        Future<Map<Long, TraceCustomer>> idCustMapFuture = this.asyncService.executeAsync(() -> {
            CustomerQueryDto query = new CustomerQueryDto();
            query.setMarketId(firm.getId());
            query.setIdSet(Sets.newHashSet(customerIdList));
            return StreamEx.of(this.customerRpcService.listSeller(query).getData()).toMap(c -> {
                return c.getId();
            }, c -> {
                TraceCustomer customer = new TraceCustomer();
                customer.setId(c.getId());
                customer.setPhone(c.getContactsPhone());
                customer.setCode(c.getCode());
                customer.setName(c.getName());
                return customer;
            });
        });
        Future<Map<Long, TraceCustomer>> custListFuture =
                this.asyncService.executeAsync(() -> {

                    AccountGetListQueryDto accountGetListQueryDto = new AccountGetListQueryDto();
                    accountGetListQueryDto.setCustomerIds(customerIdList);
                    accountGetListQueryDto.setFirmId(firm.getId());
                    return StreamEx.of(this.accountRpcService.getList(accountGetListQueryDto)).toMap(accountGetListResultDto -> accountGetListResultDto.getCustomerId(),
                            accountGetListResultDto -> {
                                TraceCustomer customer = new TraceCustomer();
                                customer.setId(accountGetListResultDto.getCustomerId());
                                customer.setCode(accountGetListResultDto.getCustomerCode());
                                customer.setCardNo(accountGetListResultDto.getCardNo());
                                customer.setName(accountGetListResultDto.getCustomerName());
                                return customer;

                            });
                });

        Map<Long, TraceCustomer> idCustMap = Maps.newHashMap();
        Map<Long, TraceCustomer> customerListWithCardNo = Maps.newHashMap();

        try {
            idCustMap.putAll(idCustMapFuture.get());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            customerListWithCardNo.putAll(custListFuture.get());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return StreamEx.of(customerIdList).filter(custId -> {
            return idCustMap.containsKey(custId);
        }).map(custId -> {
            TraceCustomer traceCustomer = idCustMap.get(custId);
            TraceCustomer cust = customerListWithCardNo.getOrDefault(custId, traceCustomer);

            traceCustomer.setCardNo(cust.getCardNo());
            traceCustomer.setMarketName(firm.getName());
            return traceCustomer;

        }).toList();

    }
}
