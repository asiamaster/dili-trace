package com.dili.trace.service;

import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.trace.async.AsyncService;
import com.dili.trace.domain.TraceCustomer;
import com.dili.trace.rpc.dto.AccountGetListQueryDto;
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
    public List<TraceCustomer> queryCustomerByCustomerId(Long id, Firm firm) {
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
    public List<TraceCustomer> queryCustomerByKeyWord(String keyword, Firm firm) {
        if (StringUtils.isBlank(keyword) || firm == null) {
            return Lists.newArrayList();
        }
        List<Long> customerIdList = this.findCustomerIdListByKeyword(keyword, firm);
        return this.findByCustomerIdList(customerIdList, firm);
    }


    /**
     * 查询
     *
     * @param keyword
     * @param firm
     */
    public List<Long> findCustomerIdListByKeyword(String keyword, Firm firm) {
        if (StringUtils.isBlank(keyword) || firm == null) {
            return Lists.newArrayList();
        }
        List<Future<List<Long>>> futures = Lists.newArrayList(this.asyncService.executeAsync(() -> {
                    return this.customerRpcService.findCustomerIdListByKeywordFromCustomer(keyword, firm);
                }),
                this.asyncService.executeAsync(() -> {
                    return this.customerRpcService.findCustomerIdListByKeywordFromAccount(keyword, firm);
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

        CustomerQueryInput query = new CustomerQueryInput();
        query.setCodeList(list);
        return StreamEx.of(this.customerRpcService.listSeller(query, firm.getId()).getData()).map(customerExtendDto -> {
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
            CustomerQueryInput query = new CustomerQueryInput();
            query.setIdSet(Sets.newHashSet(customerIdList));
            return StreamEx.of(this.customerRpcService.listSeller(query, firm.getId()).getData()).toMap(c -> {
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
        Future<List<TraceCustomer>> custListFuture =
                this.asyncService.executeAsync(() -> {

                    AccountGetListQueryDto accountGetListQueryDto = new AccountGetListQueryDto();
                    accountGetListQueryDto.setCustomerIds(customerIdList);
                    accountGetListQueryDto.setFirmId(firm.getId());
                    return StreamEx.of(this.accountRpcService.getList(accountGetListQueryDto)).map(accountGetListResultDto -> {
                        TraceCustomer customer = new TraceCustomer();
                        customer.setId(accountGetListResultDto.getCustomerId());
                        customer.setCode(accountGetListResultDto.getCustomerCode());
                        customer.setCardNo(accountGetListResultDto.getCardNo());
                        customer.setName(accountGetListResultDto.getCustomerName());
                        return customer;

                    }).toList();
                });

        Map<Long, TraceCustomer> idCustMap = Maps.newHashMap();
        List<TraceCustomer> customerList = Lists.newArrayList();

        try {
            idCustMap.putAll(idCustMapFuture.get());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            customerList.addAll(custListFuture.get());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return StreamEx.of(customerList).filter(customer -> {
            //过滤非经营户
            return idCustMap.containsKey(customer.getId());
        }).map(customer -> {
            customer.setPhone(idCustMap.getOrDefault(customer.getId(), customer).getPhone());
            customer.setName(idCustMap.getOrDefault(customer.getId(), customer).getName());
            customer.setMarketId(firm.getId());
            customer.setMarketName(firm.getName());
            return customer;

        }).toList();
    }
}
