package com.dili.trace.controller;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.sg.trace.glossary.SalesTypeEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.trace.domain.*;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.dto.CreateListBillParam;
import com.dili.trace.dto.DetectRequestWithBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.enums.*;
import com.dili.trace.glossary.RegisterBilCreationSourceEnum;
import com.dili.trace.glossary.RegisterSourceEnum;
import com.dili.trace.glossary.UsualAddressTypeEnum;
import com.dili.trace.service.*;
import com.dili.trace.util.MarketUtil;
import com.dili.trace.util.MaskUserInfo;
import com.dili.uap.sdk.domain.User;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 场外委托检测管理
 */
@Api("/commissionDetectRequest")
@Controller
@RequestMapping("/commissionDetectRequest")
public class CommissionDetectRequestController {
    private static final Logger logger = LoggerFactory.getLogger(CommissionDetectRequestController.class);

    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    UserRpcService userRpcService;
    @Autowired
    BillService billService;
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;
    @Autowired
    QualityTraceTradeBillService qualityTraceTradeBillService;
    @Autowired
    DetectRecordService detectRecordService;
    @Autowired
    SgRegisterBillService registerBillService;
    @Autowired
    TradeTypeService tradeTypeService;
    @Autowired
    UapRpcService uapRpcService;
    @Autowired
    CommissionBillService commissionBillService;
    @Autowired
    UsualAddressService usualAddressService;

    /**
     * 跳转到DetectRequest页面
     *
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到commissionDetectRequest页面")
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap, HttpServletRequest req) {
        return "commissionDetectRequest/index";
    }

    /**
     * 分页查询DetectRequest
     *
     * @param detectRequestDto 查询条件
     * @return DetectRequest 列表
     * @throws Exception
     */
    @ApiOperation(value = "分页查询commissionDetectRequest", notes = "分页查询commissionDetectRequest，返回easyui分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commissionDetectRequest", paramType = "form", value = "commissionDetectRequest的form信息", required = false, dataType = "string")})
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    String listPage(@RequestBody DetectRequestWithBillDto detectRequestDto) throws Exception {
        detectRequestDto.setMarketId(MarketUtil.returnMarket());
        detectRequestDto.setBillType(BillTypeEnum.COMMISSION_BILL.getCode());
        detectRequestDto.setIsDeleted(YesOrNoEnum.NO.getCode());
        EasyuiPageOutput out = this.detectRequestService.listBasePageByExample(detectRequestDto);
        return out.toString();
    }

    /**
     * 接单页面
     *
     * @param modelMap
     * @param billId
     * @return
     */
    @RequestMapping(value = "/confirm.html", method = RequestMethod.GET)
    public String assign(ModelMap modelMap, @RequestParam(name = "billId", required = true) Long billId) {

        RegisterBill registerBill = this.billService.getAvaiableBill(billId).orElse(null);
        DetectRequest detectRequest = StreamEx.ofNullable(registerBill).map(RegisterBill::getDetectRequestId).nonNull().map(this.detectRequestService::get).findFirst().orElse(null);
        modelMap.put("detectRequest", detectRequest);
        modelMap.put("registerBill", registerBill);

        return "commissionDetectRequest/confirm";
    }

    /**
     * 登记单录查看页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/view.html", method = RequestMethod.GET)
    public String view(ModelMap modelMap, @RequestParam(required = true, name = "id") Long id
            , @RequestParam(required = false, name = "displayWeight") Boolean displayWeight) {
        RegisterBill item = billService.getAvaiableBill(id).orElse(null);
        if (item == null) {
            modelMap.put("registerBill", item);
            return "customerDetectRequest/view";
        }

        if (null != item.getPieceNum()) {
            modelMap.put("pieceNum", item.getPieceNum().setScale(0, BigDecimal.ROUND_DOWN));
        }else{
            modelMap.put("pieceNum", null);
        }
        if (null != item.getPieceWeight()) {
            modelMap.put("pieceWeight", item.getPieceWeight().setScale(0, BigDecimal.ROUND_DOWN));
        }else{
            modelMap.put("pieceWeight", null);
        }

        DetectRequest detectRequest = detectRequestService.get(item.getDetectRequestId());
        modelMap.put("detectRequest", detectRequest);
        if (displayWeight == null) {
            displayWeight = false;
        }
        if (RegisterSourceEnum.TALLY_AREA.getCode().equals(item.getRegisterSource())) {
            // 分销信息
            if (item.getSalesType() != null
                    && item.getSalesType().intValue() == SalesTypeEnum.SEPARATE_SALES.getCode().intValue()) {
                // 分销
                List<SeparateSalesRecord> records = separateSalesRecordService
                        .findByRegisterBillCode(item.getCode());
                modelMap.put("separateSalesRecords", records);
            }
        } else {
            QualityTraceTradeBill condition = DTOUtils.newDTO(QualityTraceTradeBill.class);
            condition.setRegisterBillCode(item.getCode());
            modelMap.put("qualityTraceTradeBills", qualityTraceTradeBillService.listByExample(condition));
        }

        List<DetectRecord> detectRecordList = this.detectRecordService.findTop2AndLatest(item.getCode());
        modelMap.put("detectRecordList", detectRecordList);
        modelMap.put("displayWeight", displayWeight);

        RegisterBillOutputDto registerBill = buildRegisterBill(id);
        modelMap.put("registerBill", registerBill);

        return "customerDetectRequest/view";
    }
    /**
     * 构建报备信息
     * @param billId
     * @return
     */
    private RegisterBillOutputDto buildRegisterBill(Long billId) {
        RegisterBill item = billService.get(billId);
        RegisterBillOutputDto registerBill = new RegisterBillOutputDto();
        BeanUtils.copyProperties(this.maskRegisterBillOutputDto(item), registerBill);
        List<ImageCert> imageCerts = this.registerBillService.findImageCertListByBillId(item.getBillId());
        registerBill.setImageCertList(imageCerts);
        return registerBill;
    }
    /**
     * 权限判断并保护敏感信息
     *
     * @param dto
     * @return
     */
    private RegisterBill maskRegisterBillOutputDto(RegisterBill dto) {
        if (Objects.isNull(dto)) {
            return dto;
        }
        if (SessionContext.hasAccess("post", "registerBill/create.html#user")) {
            return dto;
        } else {
            dto.setIdCardNo(MaskUserInfo.maskIdNo(dto.getIdCardNo()));
            dto.setAddr(MaskUserInfo.maskAddr(dto.getAddr()));
            return dto;
        }

    }
    /**
     * 登记单录入页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/create.html")
    public String create(ModelMap modelMap) {
        modelMap.put("citys", this.queryCitys());
        return "commissionDetectRequest/create";
    }

    /**
     * 查询产地
     *
     * @return
     */
    private List<UsualAddress> queryCitys() {
        return usualAddressService.findUsualAddressByType(UsualAddressTypeEnum.REGISTER);
    }

    /**
     * 新增CommissionBill
     *
     * @param input
     * @return
     */
    @ApiOperation("新增CommissionBill")
    @RequestMapping(value = "/insert.action", method = RequestMethod.POST)
    public @ResponseBody
    BaseOutput insert(@RequestBody CreateListBillParam input) {
        if (input == null) {
            return BaseOutput.failure("参数错误");
        }
        List<RegisterBill> billList = StreamEx.ofNullable(input.getRegisterBills()).flatCollection(Function.identity())
                .nonNull()
                .map(rbInputDto -> {
                    CustomerExtendDto customer = new CustomerExtendDto();
                    RegisterBill rb = rbInputDto.build(customer, this.uapRpcService.getCurrentFirm().orElse(null).getId());
                    rb.setIdCardNo(input.getIdCardNo());
                    rb.setName(input.getName());
                    rb.setCorporateName(input.getCorporateName());
                    rb.setPhone(input.getPhone());
                    rb.setPlate(input.getPlate());
                    rb.setAddr(input.getAddr());
                    rb.setUserId(input.getUserId());
                    List<ImageCert> imageList = StreamEx.ofNullable(input.getGlobalImageCertList()).flatCollection(Function.identity()).nonNull().toList();
                    rb.setImageCertList(imageList);
                    rb.setWeightUnit(WeightUnitEnum.KILO.getCode());
                    rb.setCreationSource(RegisterBilCreationSourceEnum.PC.getCode());
                    rb.setRegisterSource(RegisterSourceEnum.getRegisterSourceEnum(input.getRegisterSource()).orElse(RegisterSourceEnum.OTHERS).getCode());
                    rb.setSourceName(input.getSourceName());
                    rb.setSourceId(input.getSourceId());
                    rb.setVerifyStatus(BillVerifyStatusEnum.WAIT_AUDIT.getCode());
                    rb.setPreserveType(PreserveTypeEnum.NONE.getCode());
                    rb.setVerifyType(VerifyTypeEnum.NONE.getCode());
                    rb.setTruckType(TruckTypeEnum.FULL.getCode());
//                    rb.setIsCheckin(YesOrNoEnum.NO.getCode());
                    rb.setCheckinStatus(CheckinStatusEnum.NONE.getCode());
                    rb.setIsDeleted(YesOrNoEnum.NO.getCode());
                    rb.setMeasureType(MeasureTypeEnum.COUNT_WEIGHT.getCode());
                    rb.setIsPrintCheckSheet(YesOrNoEnum.NO.getCode());
                    // 理货类型为交易区时才保存交易区号和id
                    if (RegisterSourceEnum.TRADE_AREA.getCode().equals(input.getRegisterSource())) {
                        rb.setSourceName(input.getSourceName());
                        rb.setSourceId(input.getSourceId());
                    }
                    return rb;
                }).toList();
        try {
            this.commissionBillService.createCommissionBillByManager(billList, this.uapRpcService.getCurrentOperator().get());
            return BaseOutput.success("新增成功").setData(billList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("服务器出错,请重试").setErrorData(e.getMessage());
        }
    }

    /**
     * 进入预约检测页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/appointment.html")
    public String audit(ModelMap modelMap, Long billId) {
        RegisterBill item = this.billService.get(billId);
        modelMap.put("registerBill", item);
        return "commissionDetectRequest/appointment";
    }


    /**
     * 预约检测
     *
     * @param detectRequest
     * @return
     */
    @RequestMapping(value = "/doAppointment.action")
    @ResponseBody
    public BaseOutput doAppointment(@RequestBody  DetectRequest detectRequest) {
        try {
            detectRequestService.doAppointment(detectRequest);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure();
        }
    }

    /**
     * 查询市场检测人员
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/getDetectUsers.action")
    @ResponseBody
    public Map<String, ?> getDetectUsers(@RequestParam(name = "name") String name) {
        try {
            List<User> users = userRpcService.findDetectDepartmentUsers(name, MarketUtil.returnMarket());
            List<Map<String, Object>> list = Lists.newArrayList();
            if (users != null && !users.isEmpty()) {
                for (User c : users) {
                    Map<String, Object> obj = Maps.newHashMap();
                    obj.put("id", c.getId());
                    obj.put("data", name);
                    obj.put("value", c.getRealName());
                    list.add(obj);
                }
            }
            Map map = Maps.newHashMap();
            map.put("suggestions", list);
            return map;
        } catch (TraceBizException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}