package com.dili.trace.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.dili.customer.sdk.domain.TallyingArea;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.query.CustomerQueryInput;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.trace.dto.CustomerExtendOutPutDto;
import com.dili.trace.rpc.dto.CardResultDto;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.UapRpcService;
import com.dili.uap.sdk.domain.Firm;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    CustomerRpcService customerRpcService;

    /**
     * 通过摊位号查询客户信息
     *
     * @param tallyAreaNo
     * @return
     */
    @RequestMapping("/queryUserByTallyAreaNo.action")
    @ResponseBody
    public BaseOutput queryUserByTallyAreaNo(String tallyAreaNo) {
        Long marketId = this.uapRpcService.getCurrentFirm().get().getId();
        CustomerQueryInput queryInput = new CustomerQueryInput();
        queryInput.setMarketId(marketId);
        queryInput.setAssetsName(tallyAreaNo);
        return StreamEx.of(this.customerRpcService.list(queryInput)).findFirst()
                .map(data -> BaseOutput.successData(data))
                .orElseGet(() -> BaseOutput.failure("没有数据"));


    }

    /**
     * 查询经营户并带出卡号
     * @param query
     * @return
     */
    @RequestMapping(value = "/listSeller.action")
    @ResponseBody
    public BaseOutput<List<CustomerExtendOutPutDto>> listSeller(@RequestBody CustomerQueryInput query) {
        if (StrUtil.isBlank(query.getKeyword())&&query.getId()==null) {
            return BaseOutput.success().setData(new ArrayList<>(0));
        }
        Firm firm = uapRpcService.getCurrentFirm().orElse(DTOUtils.newDTO(Firm.class));
        query.setPage(1);
        query.setRows(50);
        PageOutput<List<CustomerExtendDto>> pageOutput = this.customerRpcService.listSeller(query, firm.getId());
        if (!pageOutput.isSuccess()) {
            return BaseOutput.failure(pageOutput.getMessage());
        }
        //转换并查询卡号
        List<CustomerExtendOutPutDto> items = convertWithCardInfo(firm, pageOutput.getData());
        return BaseOutput.success().setData(items);
    }

    /**
     * 查询经营户并带出卡号
     * @param query
     * @return
     */
    @RequestMapping(value = "/listSellerTallaryNoByUserId.action")
    @ResponseBody
    public BaseOutput<List<TallyingArea>> listSellerTallaryNoByUserId(@RequestBody CustomerQueryInput query) {
        if (query.getId()==null) {
            return BaseOutput.success().setData(new ArrayList<>(0));
        }
        Firm firm = uapRpcService.getCurrentFirm().orElse(DTOUtils.newDTO(Firm.class));
        query.setPage(1);
        query.setRows(50);
        PageOutput<List<CustomerExtendDto>> pageOutput = this.customerRpcService.listSeller(query, firm.getId());
        if (!pageOutput.isSuccess()) {
            return BaseOutput.failure(pageOutput.getMessage());
        }
        return StreamEx.of(pageOutput.getData()).findFirst().map(item->{
            return BaseOutput.success().setData(item.getTallyingAreaList());
        }).orElseGet(()->{
            return BaseOutput.successData(Lists.newArrayList());
        });

    }

    /**
     * 转换并查询出卡号信息
     * @param tempList
     * @return
     */
    private List<CustomerExtendOutPutDto> convertWithCardInfo(Firm firm, List<CustomerExtendDto> tempList) {
        if (CollUtil.isEmpty(tempList)) {
            return new ArrayList<>(0);
        }
        List<CustomerExtendOutPutDto> items = new ArrayList<>(tempList.size());
        tempList.forEach(temp -> {
            CustomerExtendOutPutDto item = new CustomerExtendOutPutDto();
            item.setId(temp.getId());
            item.setMarketId(firm.getId());
            item.setMarketName(firm.getName());
            item.setName(temp.getName());
            item.setPhone(StrUtil.isBlank(temp.getContactsPhone()) ? " " : StrUtil.replace(temp.getContactsPhone(), 3, 7 , '*'));
            CardResultDto cardResultDto = this.customerRpcService.queryCardInfoByCustomerCode(temp.getCode(), null, firm.getId()).orElse(null);
            item.setTradePrintingCard(cardResultDto == null ? " " : cardResultDto.getCardNo());
            items.add(item);
        });
        return items;
    }
}
