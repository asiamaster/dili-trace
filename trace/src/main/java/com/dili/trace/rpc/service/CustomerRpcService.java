package com.dili.trace.rpc.service;

import com.alibaba.fastjson.JSON;
import com.dili.common.annotation.AppAccess;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.domain.*;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.dto.CustomerSimpleExtendDto;
import com.dili.customer.sdk.domain.query.CustomerBaseQueryInput;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.customer.sdk.rpc.CustomerMarketRpc;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.trace.domain.TraceCustomer;
import com.dili.trace.rpc.dto.AccountGetListQueryDto;
import com.dili.trace.rpc.dto.AccountGetListResultDto;
import com.dili.trace.rpc.dto.CustomerQueryDto;
import com.dili.trace.service.GlobalVarService;
import com.dili.trace.util.NumUtils;
import com.dili.uap.sdk.domain.Firm;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    @Autowired
    private AccountRpcService accountRpcService;
    @Autowired
    BusinessCategoryRpcService businessCategoryRpcService;
    @Autowired
    VehicleRpcService vehicleRpcService;
    @Autowired
    TallyingAreaRpcService tallyingAreaRpcService;
    @Autowired
    AttachmentRpcService attachmentRpcService;


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
            if (out != null && out.isSuccess()) {
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
    public CustomerExtendDto findApprovedCustomerByIdOrEx(Long customerId, Long marketId, String errorMsg) {
        CustomerExtendDto customer = this.findCustomerByIdOrEx(customerId, marketId);
        if (!CustomerEnum.ApprovalStatus.PASSED.getCode().equals(customer.getCustomerMarket().getApprovalStatus())) {
            throw new TraceBizException(errorMsg);
        }
        return customer;
    }

    /**
     * 查询经营户
     *
     * @param queryInput
     * @param characterType
     * @return
     */
    private PageOutput<List<CustomerSimpleExtendDto>> queryCustomerByCustomerQueryDto(CustomerQueryDto queryInput, CustomerEnum.CharacterType characterType) {
        if (queryInput == null || queryInput.getMarketId() == null) {
            return PageOutput.failure("参数错误");
        }
        CharacterType seller = new CharacterType();
        seller.setCharacterType(characterType.getCode());
        queryInput.setCharacterType(seller);

        PageOutput<List<CustomerSimpleExtendDto>> page = this.queryCustomerByCustomerQueryDto(queryInput);
        return page;
    }


    /**
     * 查询经营户
     *
     * @param queryInput
     * @return
     */
    public PageOutput<List<CustomerSimpleExtendDto>> listSeller(CustomerQueryDto queryInput) {
        PageOutput<List<CustomerSimpleExtendDto>> pageOutput = this.queryCustomerByCustomerQueryDto(queryInput, CustomerEnum.CharacterType.经营户);
        return pageOutput;
    }

    /**
     * 查询买家
     *
     * @param queryInput
     * @return
     */
    public PageOutput<List<CustomerSimpleExtendDto>> listBuyer(CustomerQueryDto queryInput) {
        logger.debug("listBuyer:marketId={},queryInput={}", queryInput.getMarketId(), JSON.toJSONString(queryInput));
        PageOutput<List<CustomerSimpleExtendDto>> pageOutput = this.queryCustomerByCustomerQueryDto(queryInput, CustomerEnum.CharacterType.买家);
        return pageOutput;
    }

    /**
     * 查询经营户
     *
     * @param queryInput
     * @param marketId
     * @return
     */
    public PageOutput<List<CustomerSimpleExtendDto>> listDriver(CustomerQueryDto queryInput, Long marketId) {
        queryInput.setMarketId(marketId);
        PageOutput<List<CustomerSimpleExtendDto>> pageOutput = this.queryCustomerByCustomerQueryDto(queryInput, CustomerEnum.CharacterType.其他类型);
        return pageOutput;
    }

    /**
     * 查询客户信息
     *
     * @param cust
     * @return
     */
    public Optional<TraceCustomer> findCustomer(TraceCustomer cust, Long marketId) {
        return this.listCustomers(cust.getCode(), cust.getCardNo(), marketId);
    }

    /**
     * 查询用户信息
     *
     * @param customerCode
     * @param cardNo
     * @return
     */
    private Optional<TraceCustomer> listCustomers(String customerCode, String cardNo, Long marketId) {
        if (StringUtils.isAllBlank(customerCode, cardNo)) {
            return Optional.empty();
        }
        return this.queryCardInfoByCustomerCode(customerCode, cardNo, marketId).map(card -> {


            TraceCustomer traceCustomer = new TraceCustomer();
            traceCustomer.setCardNo(card.getCardNo());
            traceCustomer.setCode(customerCode);
            traceCustomer.setName(card.getCustomerName());
            traceCustomer.setIdNo(card.getCustomerCertificateNumber());
            traceCustomer.setId(card.getCustomerId());
            Long customerId = card.getCustomerId();
            this.findCustomerById(customerId, marketId).ifPresent(cust -> {
                traceCustomer.setPhone(cust.getContactsPhone());
                traceCustomer.setAddress(cust.getCertificateAddr());
                traceCustomer.setCode(cust.getCode());
            });
            return traceCustomer;

        });

    }

    /**
     * 查询卡信息
     *
     * @param customerCode
     * @throws IOException
     */
    public Optional<AccountGetListResultDto> queryCardInfoByCustomerCode(String customerCode, String cardNo, Long marketId) {

        AccountGetListQueryDto queryDto = new AccountGetListQueryDto();
        queryDto.setFirmId(marketId);
        queryDto.setCustomerCode(StringUtils.trimToNull(customerCode));
        if (StringUtils.isNotBlank(cardNo)) {
            queryDto.setCardNos(Lists.newArrayList(cardNo.trim()));
        }
        return StreamEx.of(this.accountRpcService.getList(queryDto)).findFirst();
    }

    /**
     * 根据车牌查询绑定的客户
     *
     * @param plate
     * @return
     */
    public List<CustomerSimpleExtendDto> findCustomerByPlate(String plate, Long marketId) {
        CustomerQueryDto queryInput = new CustomerQueryDto();
        queryInput.setVehicleNumber(plate);
        queryInput.setMarketId(marketId);
        PageOutput<List<CustomerSimpleExtendDto>> pageOutput = this.queryCustomerByCustomerQueryDto(queryInput, CustomerEnum.CharacterType.经营户);

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


    /**
     * 查询
     *
     * @param keyword
     * @param firm
     */
    public List<Long> findSellerIdListByKeywordFromCustomer(String keyword, Firm firm) {
        if (StringUtils.isBlank(keyword) || firm == null) {
            return Lists.newArrayList();
        }

        CustomerQueryDto query = new CustomerQueryDto();
        query.setKeyword(keyword);
        query.setMarketId(firm.getId());
        query.setPage(1);
        query.setRows(50);
        List<Long> customerIdList = StreamEx.of(this.listSeller(query).getData()).map(customerExtendDto -> {
            return customerExtendDto.getId();
        }).toList();

        return customerIdList;
    }

    /**
     * 查询
     *
     * @param keyword
     * @param firm
     */
    public List<Long> findSellerIdListByKeywordFromAccount(String keyword, Firm firm) {
        if (StringUtils.isBlank(keyword) || firm == null) {
            return Lists.newArrayList();
        }

        AccountGetListQueryDto accountGetListQueryDto = new AccountGetListQueryDto();
        accountGetListQueryDto.setKeyword(keyword);
        accountGetListQueryDto.setFirmId(firm.getId());

        List<Long> accountCustomerIdList = StreamEx.of(this.accountRpcService.getList(accountGetListQueryDto)).map(accountGetListResultDto -> {
            return accountGetListResultDto.getCustomerId();
        }).toList();

        return accountCustomerIdList;
    }

    /**
     * 查询客户信息
     *
     * @param queryDto
     * @return
     */
    public PageOutput<List<CustomerSimpleExtendDto>> queryCustomerByCustomerQueryDto(CustomerQueryDto queryDto) {
        if (queryDto == null || queryDto.getMarketId() == null) {
            return PageOutput.success();
        }

        if (queryDto.getPage() == null || queryDto.getPage() < 1) {
            queryDto.setPage(1);
        }
        if (queryDto.getRows() == null || queryDto.getRows() < 1) {
            queryDto.setRows(20);
        }
        return this.queryCustomer(queryDto);
    }

    /**
     * 查询客户信息
     *
     * @param queryDto
     * @return
     */
    private PageOutput<List<CustomerSimpleExtendDto>> queryCustomer(CustomerQueryDto queryDto) {
        if (queryDto == null||queryDto.getMarketId()==null) {
            return PageOutput.success();
        }
        return this.customerRpc.listSimpleNormalPage(queryDto);

    }

}
