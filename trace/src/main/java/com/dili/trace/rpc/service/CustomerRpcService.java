package com.dili.trace.rpc.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.dili.common.annotation.AppAccess;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.CharacterType;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.customer.sdk.rpc.CustomerMarketRpc;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.trace.domain.Customer;
import com.dili.trace.rpc.dto.CardQueryInput;
import com.dili.trace.rpc.dto.CardResultDto;
import com.dili.trace.service.GlobalVarService;
import com.dili.trace.util.NumUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * web请求上下文环境变量接口
 */
@Service
public class CustomerRpcService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerRpcService.class);


    @Autowired
    CustomerRpc customerRpc;
    @Autowired
    CustomerMarketRpc customerMarketRpc;
    @Autowired
    FirmRpcService firmRpcService;

    @Autowired
    GlobalVarService globalVarService;

    @Value("${accountService.url}")
    private String accountServiceUrl;

    /**
     * 查询当前登录用户信息
     *
     * @return
     */
    public Optional<SessionData> getCurrentCustomer() {
        //两个方法在没有使用JSF的项目中是没有区别的
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        if (requestAttributes == null) {
            throw new TraceBizException("当前运行环境不是web请求环境");
        }
        //RequestContextHolder.getRequestAttributes();
        //从session里面获取对应的值
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) requestAttributes).getResponse();
        if (request == null) {
            throw new TraceBizException("当前运行环境不是web请求环境");
        }


        try {
            Long userId = NumUtils.toLong(request.getHeader("userId")).orElse(null);
            Long marketId = NumUtils.toLong(request.getHeader("marketId")).orElse(null);
            if (marketId == null || userId == null) {
                return Optional.empty();
            }

            try {

                if (!marketId.equals(0L)) {
                    CustomerQueryInput query = new CustomerQueryInput();
                    query.setId(userId);
                    query.setMarketId(marketId);
                    BaseOutput<List<CustomerExtendDto>> out = this.customerRpc.list(query);

                    if (out.isSuccess()) {
                        return StreamEx.ofNullable(out.getData()).flatCollection(Function.identity()).nonNull().map(c -> {
                            SessionData sessionData = new SessionData();
                            sessionData.setUserId(c.getId());
                            sessionData.setUserName(c.getName());
                            sessionData.setMarketId(marketId);
                            this.firmRpcService.getFirmById(marketId).ifPresent(m -> {
                                sessionData.setMarketName(m.getName());
                            });
                            sessionData.setSubRoles(this.convert(c.getCharacterTypeList()));
                            return sessionData;
                        }).findFirst();

                    }
                } else {
                    BaseOutput<com.dili.customer.sdk.domain.Customer> out = this.customerRpc.getById(userId);
                    if (out.isSuccess()) {
                        return StreamEx.ofNullable(out.getData()).nonNull().map(c -> {
                            SessionData sessionData = new SessionData();
                            sessionData.setUserId(c.getId());
                            sessionData.setUserName(c.getName());
                            sessionData.setMarketId(marketId);
                            return sessionData;
                        }).findFirst();

                    } else {
                        logger.error("userId={},marketId={},message:{}", userId, marketId, out.getMessage());
                    }

                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * 角色转换
     *
     * @param characterTypeList
     * @return
     */
    private List<CustomerEnum.CharacterType> convert(List<CharacterType> characterTypeList) {
        List<CustomerEnum.CharacterType> subRoles = StreamEx.ofNullable(characterTypeList)
                .flatCollection(Function.identity()).nonNull()
                .map(CharacterType::getCharacterType)
                .filter(StringUtils::isNotBlank)
                .map(CustomerEnum.CharacterType::getInstance).nonNull().toList();
        return subRoles;
    }

    /**
     * 是否有访问权限
     *
     * @param access
     * @return
     */
    public boolean hasAccess(AppAccess access) {
        return this.getCurrentCustomer().map(c -> {
            List<CustomerEnum.CharacterType> subRoleList = StreamEx.ofNullable(access.subRoles()).flatArray(Function.identity()).toList();
            return c.getSubRoles().containsAll(subRoleList);
        }).orElse(false);
    }

    /**
     * 查询客户
     *
     * @param customerId
     * @return
     */
    public com.dili.customer.sdk.domain.Customer findCustByIdOrEx(Long customerId) {
        return this.findCustById(customerId).orElseThrow(() -> {
            return new TraceBizException("查询客户信息失败");
        });
    }

    /**
     * 查询客户
     *
     * @param customerId
     * @return
     */
    public Optional<com.dili.customer.sdk.domain.Customer> findCustById(Long customerId) {

        BaseOutput<com.dili.customer.sdk.domain.Customer> out = this.customerRpc.getById(customerId);
        if (out != null && out.isSuccess() && out.getData() != null) {
            return Optional.ofNullable(out.getData());
        }
        return Optional.empty();
    }

    /**
     * 查询客户
     *
     * @param customerId
     * @return
     */
    public CustomerExtendDto findCustomerByIdOrEx(Long customerId, Long marketId) {

        return this.findCustomerById(customerId, marketId).orElseThrow(() -> {
            return new TraceBizException("查询客户信息失败");
        });
    }

    /**
     * 查询相同市场的用户
     *
     * @param custIdList
     * @param marketId
     * @return
     */
    public Map<Long, CustomerExtendDto> findCustomersByIdList(List<Long> custIdList, Long marketId) {
        if (marketId == null || custIdList == null || custIdList.isEmpty()) {
            return new HashMap<>();
        }
        Set<Long> idSet = StreamEx.of(custIdList).nonNull().distinct().toSet();

        if (idSet.isEmpty()) {
            return new HashMap<>();
        }
        CustomerQueryInput queyrQueryInput = new CustomerQueryInput();
        queyrQueryInput.setMarketId(marketId);
        queyrQueryInput.setIdSet(idSet);
        return StreamEx.ofNullable(this.list(queyrQueryInput)).flatCollection(Function.identity()).nonNull()
                .mapToEntry(CustomerExtendDto::getId, Function.identity()).toMap();


    }

    /**
     * 查询客户
     *
     * @param customerId
     * @return
     */
    public Optional<CustomerExtendDto> findCustomerById(Long customerId, Long marketId) {
        logger.debug("findCustomerById: customerId={},marketId={}", customerId, marketId);
        if (customerId == null || marketId == null) {
            return Optional.empty();
        }
        try {
            BaseOutput<CustomerExtendDto> out = this.customerRpc.get(customerId, marketId);
            if (out.isSuccess()) {
                return Optional.ofNullable(out.getData());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * 转换为溯源用户返回
     *
     * @param customerId
     * @return
     */


    /**
     * 查询客户
     *
     * @param customerId
     * @return
     */
    public CustomerExtendDto findApprovedCustomerByIdOrEx(Long customerId, Long marketId) {
        CustomerExtendDto customer = this.findCustomerByIdOrEx(customerId, marketId);
        if (!CustomerEnum.ApprovalStatus.PASSED.getCode().equals(customer.getCustomerMarket().getApprovalStatus())) {
            throw new TraceBizException("用户未审核通过不能创建报备单");
        }
        return customer;
    }
    /**
     * 查询客户
     *
     * @param customerId
     * @return
     */
    public CustomerExtendDto findApprovedCustomerByIdOrEx(Long customerId, Long marketId,String errorMsg) {
        CustomerExtendDto customer = this.findCustomerByIdOrEx(customerId, marketId);
        if (!CustomerEnum.ApprovalStatus.PASSED.getCode().equals(customer.getCustomerMarket().getApprovalStatus())) {
            throw new TraceBizException(errorMsg);
        }
        return customer;
    }

    /**
     * return Optional.empty();
     * }
     * 分页查询
     *
     * @param queryInput
     * @return
     */
    public PageOutput<List<CustomerExtendDto>> listPage(CustomerQueryInput queryInput) {
        PageOutput<List<CustomerExtendDto>> page = this.customerRpc.listPage(queryInput);
        return page;
    }

    /**
     * 查询经营户
     *
     * @param queryInput
     * @param marketId
     * @return
     */
    public PageOutput<List<CustomerExtendDto>> listSeller(CustomerQueryInput queryInput, Long marketId) {

        CharacterType seller = new CharacterType();
        seller.setCharacterType(CustomerEnum.CharacterType.经营户.getCode());
        queryInput.setCharacterType(seller);
        queryInput.setMarketId(marketId);
        PageOutput<List<CustomerExtendDto>> pageOutput = this.listPage(queryInput);
        return pageOutput;
    }

    /**
     * 查询买家
     *
     * @param queryInput
     * @param marketId
     * @return
     */
    public PageOutput<List<CustomerExtendDto>> listBuyer(CustomerQueryInput queryInput, Long marketId) {

        CharacterType buyer = new CharacterType();
        buyer.setCharacterType(CustomerEnum.CharacterType.买家.getCode());
        queryInput.setCharacterType(buyer);
        queryInput.setMarketId(marketId);
        logger.debug("listBuyer:marketId={},queryInput={}", marketId, JSON.toJSONString(queryInput));
        PageOutput<List<CustomerExtendDto>> pageOutput = this.listPage(queryInput);
        return pageOutput;
    }

    /**
     * 查询经营户
     *
     * @param queryInput
     * @param marketId
     * @return
     */
    public PageOutput<List<CustomerExtendDto>> listDriver(CustomerQueryInput queryInput, Long marketId) {

        CharacterType driver = new CharacterType();
        driver.setCharacterType(CustomerEnum.CharacterType.其他类型.getCode());
        queryInput.setCharacterType(driver);
        queryInput.setMarketId(marketId);
        PageOutput<List<CustomerExtendDto>> pageOutput = this.listPage(queryInput);
        return pageOutput;
    }

    /**
     * 查询客户信息
     *
     * @param cust
     * @return
     */
    public Optional<com.dili.trace.domain.Customer> findCustomer(com.dili.trace.domain.Customer cust, Long marketId) {
        return this.listCustomers(cust.getCustomerId(), cust.getPrintingCard(), marketId);
    }

    /**
     * 查询用户信息
     *
     * @param customerCode
     * @param cardNo
     * @return
     */
    private Optional<com.dili.trace.domain.Customer> listCustomers(String customerCode, String cardNo, Long marketId) {
        if (StringUtils.isAllBlank(customerCode, cardNo)) {
            return Optional.empty();
        }
        return this.queryCardInfoByCustomerCode(customerCode, cardNo, marketId).map(card -> {

            com.dili.trace.domain.Customer customer = new Customer();
            customer.setPrintingCard(card.getCardNo());
            customer.setCustomerId(customerCode);
            customer.setName(card.getCustomerName());
            customer.setIdNo(card.getCustomerCertificateNumber());
            customer.setId(card.getCustomerId());
            Long customerId = card.getCustomerId();
            this.findCustomerById(customerId, marketId).ifPresent(cust -> {
                customer.setPhone(cust.getContactsPhone());
                customer.setAddress(cust.getCertificateAddr());
                customer.setCustomerId(cust.getCode());
            });
            return customer;

        });

    }

    /**
     * 查询卡信息
     *
     * @param customerCode
     * @throws IOException
     */
    public Optional<CardResultDto> queryCardInfoByCustomerCode(String customerCode, String cardNo, Long marketId) {
        CardQueryInput input = new CardQueryInput();
        input.setFirmId(marketId);
        input.setCustomerCode(StringUtils.trimToNull(customerCode));
        if (StringUtils.isNotBlank(cardNo)) {
            input.setCardNos(Lists.newArrayList(cardNo.trim()));
        }


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String jsonBody = mapper.writeValueAsString(input);
//            System.out.println(jsonBody);
            String respBody = HttpUtil.post(this.accountServiceUrl + "/api/account/getList", jsonBody);
//            System.out.println(respBody);
            BaseOutput<List<CardResultDto>> out = mapper.readValue(respBody, new TypeReference<BaseOutput<List<CardResultDto>>>() {
            });
            if (!"200".equals(out.getCode())) {
                return Optional.empty();
            }
            return StreamEx.ofNullable(out.getData()).nonNull().flatCollection(Function.identity()).nonNull().findFirst();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * 根据车牌查询绑定的客户
     *
     * @param plate
     * @return
     */
    public List<CustomerExtendDto> findCustomerByPlate(String plate, Long marketId) {
        CustomerQueryInput queryInput = new CustomerQueryInput();
        queryInput.setVehicleNumber(plate);
        queryInput.setMarketId(marketId);
        PageOutput<List<CustomerExtendDto>> pageOutput = this.listPage(queryInput);

        return StreamEx.ofNullable(pageOutput).filter(PageOutput::isSuccess)
                .map(PageOutput::getData).nonNull()
                .flatCollection(Function.identity()).nonNull()
                .toList();

    }

    /**
     * list查询
     *
     * @param customer
     * @return
     */
    public List<CustomerExtendDto> list(CustomerQueryInput customer) {
        BaseOutput<List<CustomerExtendDto>> out = this.customerRpc.list(customer);
        return StreamEx.ofNullable(out)
                .filter(BaseOutput::isSuccess)
                .map(BaseOutput::getData)
                .nonNull()
                .flatCollection(Function.identity())
                .nonNull().toList();

    }
}
