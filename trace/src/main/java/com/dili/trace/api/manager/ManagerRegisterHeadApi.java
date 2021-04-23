package com.dili.trace.api.manager;

import com.alibaba.fastjson.JSON;
import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.domain.BaseDomain;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.trace.api.input.CreateRegisterHeadInputDto;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.domain.*;
import com.dili.trace.dto.CreateListRegisterHeadParam;
import com.dili.trace.dto.RegisterHeadDto;
import com.dili.trace.dto.query.RegisterHeadPlateQueryDto;
import com.dili.trace.dto.query.TallyAreaNoQueryDto;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.RegisgterHeadStatusEnum;
import com.dili.trace.enums.WeightUnitEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * (管理员)进门主台账单相关接口
 *
 * @author Lily
 */
@RestController
@RequestMapping(value = "/api/manager/managerRegisterHead")
@Api(value = "/api/manager/managerRegisterHead", description = "进门主台账单相关接口")
@AppAccess(role = Role.Manager)
public class ManagerRegisterHeadApi {
    private static final Logger logger = LoggerFactory.getLogger(ManagerRegisterHeadApi.class);

    @Autowired
    RegisterHeadService registerHeadService;

    @Autowired
    private LoginSessionContext sessionContext;

    @Autowired
    CustomerRpcService customerRpcService;

    @Autowired
    ImageCertService imageCertService;

    @Autowired
    RegisterBillService registerBillService;

    @Autowired
    UpStreamService upStreamService;
    @Autowired
    RegisterHeadPlateService registerHeadPlateService;
    @Autowired
    RegisterTallyAreaNoService registerTallyAreaNoService;

    /**
     * 获取进门主台账单列表
     *
     * @param input 查询条件
     * @return 主台账单列表
     */
    @ApiOperation(value = "获取进门主台账单列表")
    @ApiImplicitParam(paramType = "body", name = "RegisterHead", dataType = "RegisterHead", value = "获取进门主台账单列表")
    @RequestMapping(value = "/listPage.api", method = RequestMethod.POST)
    public BaseOutput<BasePage<RegisterHead>> listPage(@RequestBody RegisterHeadDto input) {
        logger.info("获取进门主台账单列表:{}", JSON.toJSONString(input));
        try {
//            if(input==null||input.getUserId()==null){
//                return BaseOutput.failure("参数错误");
//            }
            SessionData sessionData = this.sessionContext.getSessionData();

            logger.info("获取进门主台账单列表 操作用户:{}", sessionData.getUserId());
            input.setSort("created");
            input.setOrder("desc");
            input.setMarketId(sessionData.getMarketId());
            BasePage<RegisterHead> registerHeadBasePage = registerHeadService.listPageApi(input);
            List<Long> registerHeadIdList = StreamEx.of(registerHeadBasePage.getDatas()).map(RegisterHead::getId).toList();
            Map<Long, List<String>>plateListMap=   this.registerHeadPlateService.findPlateByRegisterHeadIdList(registerHeadIdList);
            Map<Long, List<String>>tallyAreaNoListMap=this.registerTallyAreaNoService. findTallyAreaNoByRegisterHeadIdList(registerHeadIdList);


            BasePage<RegisterHeadDto> registerHeadDtoBasePage = new BasePage<>();
            List<RegisterHeadDto> registerHeadDtoList = new ArrayList<>();
            if (null != registerHeadBasePage && CollectionUtils.isNotEmpty(registerHeadBasePage.getDatas())) {
                registerHeadBasePage.getDatas().forEach(e -> {
                    e.setWeightUnitName(WeightUnitEnum.toName(e.getWeightUnit()));
                    e.setBillTypeName(BillTypeEnum.fromCode(e.getBillType()).get().getName());
                    UpStream upStream = this.upStreamService.get(e.getUpStreamId());
                    if (upStream != null) {
                        e.setUpStreamName(upStream.getName());
                    }
                    e.setImageCertList(imageCertService.findImageCertListByBillId(e.getId(), BillTypeEnum.MASTER_BILL));

                    e.setPlateList(plateListMap.getOrDefault(e.getId(),Lists.newArrayList()));
                    e.setArrivalTallynos( tallyAreaNoListMap.getOrDefault(e.getId(),Lists.newArrayList()));
                    RegisterHeadDto registerHeadDto = new RegisterHeadDto();
                    BeanUtils.copyProperties(e, registerHeadDto);
                    registerHeadDto.setMarketName(this.sessionContext.getSessionData().getMarketName());
                    registerHeadDtoList.add(registerHeadDto);
                });
            }
            BeanUtils.copyProperties(registerHeadBasePage, registerHeadDtoBasePage);
            registerHeadDtoBasePage.setDatas(registerHeadDtoList);
            return BaseOutput.success().setData(registerHeadDtoBasePage);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }

    /**
     * 不同审核状态数据统计
     *
     * @param query 查询条件
     * @return 审核状态数据统计
     */
    @RequestMapping(value = "/countByStatus.api", method = {RequestMethod.POST})
    public BaseOutput<List<VerifyStatusCountOutputDto>> countByStatus(@RequestBody RegisterHeadDto query) {
        try {
            // 10: 已启用 20：已关闭 30：废弃
            List<VerifyStatusCountOutputDto> list = new ArrayList<>(3);
            List<RegisterHead> registerHeads = registerHeadService.listByExample(query);
            int activeCount = 0;
            int unactiveCount = 0;
            int deleteCount = 0;
            if (CollectionUtils.isNotEmpty(registerHeads)) {
                //Stream<RegisterHead> registerHeadStream = registerHeads.stream().filter(e -> (e.getActive() == null || e.getIsDeleted() == null));
                // 已启用
                activeCount = (int) registerHeads.stream().filter(e -> (e.getActive() == 1 && e.getIsDeleted() == 0)).count();
                unactiveCount = (int) registerHeads.stream().filter(e -> (e.getActive() == 0 && e.getIsDeleted() == 0)).count();
                deleteCount = (int) registerHeads.stream().filter(e -> e.getIsDeleted() == 1).count();
            }
            list.add(new VerifyStatusCountOutputDto(RegisgterHeadStatusEnum.ACTIVE.getCode(), activeCount));
            list.add(new VerifyStatusCountOutputDto(RegisgterHeadStatusEnum.UNACTIVE.getCode(), unactiveCount));
            list.add(new VerifyStatusCountOutputDto(RegisgterHeadStatusEnum.DELETED.getCode(), deleteCount));
            return BaseOutput.success().setData(list);

        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("操作失败：服务端出错");
        }
    }

    /**
     * 保存多个进门主台账单
     *
     * @param createListRegisterHeadParam 主台账单信息
     * @return 主台账单ID集合
     */
    @ApiOperation("保存多个进门主台账单")
    @RequestMapping(value = "/createRegisterHeadList.api", method = RequestMethod.POST)
    public BaseOutput<List<Long>> createRegisterHeadList(@RequestBody CreateListRegisterHeadParam createListRegisterHeadParam) {
        logger.info("保存多个进门主台账单:{}", JSON.toJSONString(createListRegisterHeadParam));
        if (createListRegisterHeadParam == null || createListRegisterHeadParam.getRegisterBills() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            Long userId = sessionData.getUserId();
            String userName = sessionData.getUserName();

            List<CreateRegisterHeadInputDto> registerHeads = StreamEx.of(createListRegisterHeadParam.getRegisterBills())
                    .nonNull().filter(dto -> {
                        return dto.getUserId() != null;
                    }).toList();
            if (registerHeads.isEmpty()) {
                return BaseOutput.failure("没有进门主台账单/参数错误");
            }

            logger.info("保存多个进门主台账单操作用户:userid={}，username={},marketid={}", userId, userName,sessionContext.getSessionData().getMarketId());
            List<Long> idList = this.registerHeadService.createRegisterHeadList(registerHeads, this.sessionContext.getSessionData().getOptUser(), sessionContext.getSessionData().getMarketId());
            return BaseOutput.success().setData(idList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
    }

    /**
     * 修改进门主台账单
     *
     * @param dto 主台账单信息
     * @return 修改结果
     */
    @ApiOperation("修改进门主台账单")
    @RequestMapping(value = "/doEditRegisterHead.api", method = RequestMethod.POST)
    public BaseOutput doEditRegisterBill(@RequestBody CreateRegisterHeadInputDto dto) {
        logger.info("修改进门主台账单:{}", JSON.toJSONString(dto));
        if (dto == null || dto.getId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            CustomerExtendDto customer = this.customerRpcService.findCustomerByIdOrEx(dto.getUserId(), sessionData.getMarketId());

            RegisterHead registerHead = dto.build(customer);
            logger.info("修改进门主台账单:{}", JSON.toJSONString(registerHead));
            this.registerHeadService.doEdit(registerHead, dto.getImageCertList(), this.sessionContext.getSessionData().getOptUser());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
        return BaseOutput.success();
    }

    /**
     * 作废进门主台账单
     *
     * @param dto 主台账单信息
     * @return 作废结果
     */
    @ApiOperation("作废进门主台账单")
    @RequestMapping(value = "/doDeleteRegisterHead.api", method = RequestMethod.POST)
    public BaseOutput doDeleteRegisterHead(@RequestBody CreateRegisterHeadInputDto dto) {
        logger.info("作废进门主台账单:{}", JSON.toJSONString(dto));
        if (dto == null || dto.getId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            logger.info("作废进门主台账单:billId:{},userId:{}", dto.getId(), sessionData.getUserId());
            this.registerHeadService.doDelete(dto, sessionData.getUserId(), this.sessionContext.getSessionData().getOptUser());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
        return BaseOutput.success();
    }

    /**
     * 启用/关闭进门主台账单
     *
     * @param dto 主台账单信息
     * @return 启用/关闭结果
     */
    @ApiOperation("启用/关闭进门主台账单")
    @RequestMapping(value = "/doUpdateActiveRegisterHead.api", method = RequestMethod.POST)
    public BaseOutput doUpdateActiveRegisterHead(@RequestBody CreateRegisterHeadInputDto dto) {
        logger.info("启用/关闭进门主台账单:{}", JSON.toJSONString(dto));
        if (dto == null || dto.getId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            logger.info("启用/关闭进门主台账单:billId:{},userId:{}", dto.getId(), sessionData.getUserId());
            this.registerHeadService.doUpdateActive(dto, sessionData.getUserId(), sessionData.getOptUser());
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务端出错");
        }
        return BaseOutput.success();
    }

    /**
     * 查看进门主台账单
     *
     * @param baseDomain 主台账单信息（ID）
     * @return 主台账单详情
     */
    @ApiOperation("查看进门主台账单")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/viewRegisterHead.api", method = {RequestMethod.POST})
    public BaseOutput<RegisterHead> viewRegisterHead(@RequestBody BaseDomain baseDomain) {
        try {
            RegisterHead registerHead = registerHeadService.get(baseDomain.getId());
            if (registerHead == null) {
                return BaseOutput.failure("数据不存在");
            }
            //修改重量返回的格式，只取整数部分
            if (registerHead.getWeight() != null) {
                registerHead.setWeight(weightTransform(registerHead.getWeight()));
            }
            if (registerHead.getPieceWeight() != null) {
                registerHead.setPieceWeight(weightTransform(registerHead.getPieceWeight()));
            }
            if (registerHead.getRemainWeight() != null) {
                registerHead.setRemainWeight(weightTransform(registerHead.getRemainWeight()));
            }
            List<ImageCert> imageCerts = imageCertService.findImageCertListByBillId(baseDomain.getId(), BillTypeEnum.MASTER_BILL);
            registerHead.setImageCertList(imageCerts);

            UpStream upStream = upStreamService.get(registerHead.getUpStreamId());
            registerHead.setUpStreamName(upStream.getName());

            RegisterBill registerBill = new RegisterBill();
            registerBill.setRegisterHeadCode(registerHead.getCode());
            List<RegisterBill> registerBills = registerBillService.listByExample(registerBill);
            registerHead.setRegisterBills(registerBills);

            List<String>plateList=   this.registerHeadPlateService.findPlateByRegisterHeadIdList(Arrays.asList(registerHead.getId())).getOrDefault(registerHead.getId(), Lists.newArrayList());
            registerHead.setPlateList(plateList);

            List<String> registerTallyAreaNoList = this.registerTallyAreaNoService.findTallyAreaNoByRegisterHeadIdList(Arrays.asList(registerHead.getId())).getOrDefault(registerHead.getId(), Lists.newArrayList());
            registerHead.setArrivalTallynos(registerTallyAreaNoList);


            return BaseOutput.success().setData(registerHead);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("查询进门主台账单数据出错", e);
            return BaseOutput.failure("查询进门主台账单数据出错");
        }
    }

    /**
     * 分批详情
     *
     * @param code 主台账单编号
     * @return 主台账单分批详情列表
     */
    @ApiOperation("分批详情")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/viewPartialRegisterHead.api", method = {RequestMethod.GET})
    public BaseOutput<RegisterHead> viewPartialRegisterHead(@RequestParam String code) {
        try {
            RegisterHead registerHead = new RegisterHead();
            registerHead.setCode(code);
            List<RegisterHead> registerHeadList = registerHeadService.listByExample(registerHead);
            if (CollectionUtils.isNotEmpty(registerHeadList)) {
                registerHead = registerHeadList.get(0);
            } else {
                return BaseOutput.failure("没有进门主台账单");
            }
            // 解析状态输出到前台
            if (registerHead.getActive() != null && YesOrNoEnum.YES.getCode().equals(registerHead.getActive())) {
                registerHead.setStatusStr(RegisgterHeadStatusEnum.ACTIVE.getDesc());
            } else if (registerHead.getActive() != null && YesOrNoEnum.NO.getCode().equals(registerHead.getActive())) {
                registerHead.setStatusStr(RegisgterHeadStatusEnum.UNACTIVE.getDesc());
            }
            if (registerHead.getIsDeleted() != null && YesOrNoEnum.YES.getCode().equals(registerHead.getIsDeleted())) {
                registerHead.setStatusStr(RegisgterHeadStatusEnum.DELETED.getDesc());
            }
            UpStream upStream = upStreamService.get(registerHead.getUpStreamId());
            registerHead.setUpStreamName(upStream.getName());
            RegisterBill registerBill = new RegisterBill();
            registerBill.setRegisterHeadCode(code);
            List<RegisterBill> registerBills = registerBillService.listByExample(registerBill);


            List<Long>billIdList=StreamEx.of(registerBills).map(RegisterBill::getBillId).toList();
            Map<Long,List<String>>billIdTallyMap=this.registerTallyAreaNoService.findTallyAreaNoByRegisterBillIdList(billIdList);
            if (null != registerBills && CollectionUtils.isNotEmpty(registerBills)) {
                registerBills.forEach(e -> {
                    List<ImageCert> imageCerts = imageCertService.findImageCertListByBillId(e.getBillId(), BillTypeEnum.REGISTER_BILL);
                    e.setImageCertList(imageCerts);
                    UpStream u = upStreamService.get(e.getUpStreamId());
                    e.setUpStreamName(u.getName());
                    e.setArrivalTallynos(billIdTallyMap.getOrDefault(e.getBillId(),Lists.newArrayList()));
                });
            }
            registerHead.setRegisterBills(registerBills);


            List<String>plateList=   this.registerHeadPlateService.findPlateByRegisterHeadIdList(Arrays.asList(registerHead.getId())).getOrDefault(registerHead.getId(), Lists.newArrayList());
            registerHead.setPlateList(plateList);

            List<String> registerTallyAreaNoList = this.registerTallyAreaNoService.findTallyAreaNoByRegisterHeadIdList(Arrays.asList(registerHead.getId())).getOrDefault(registerHead.getId(), Lists.newArrayList());
            registerHead.setArrivalTallynos(registerTallyAreaNoList);

            return BaseOutput.success().setData(registerHead);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("查询分批详情出错", e);
            return BaseOutput.failure("查询分批详情数据出错");
        }
    }

    /**
     * 重量去除小数部分
     *
     * @param weight
     * @return
     */
    private BigDecimal weightTransform(BigDecimal weight) {
        String weightString = weight.toString();
        if (weightString.indexOf(".") == -1) {
            return weight;
        }
        return new BigDecimal(weightString.substring(0, weightString.indexOf(".")));
    }



}
