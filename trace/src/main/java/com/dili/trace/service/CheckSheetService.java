package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.domain.*;
import com.dili.trace.dto.CheckSheetAliasInputDto;
import com.dili.trace.dto.CheckSheetInputDto;
import com.dili.trace.dto.CheckSheetPrintOutput;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.enums.DetectResultEnum;
import com.dili.trace.rpc.service.UidRestfulRpcService;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 打印检测报告信息
 */
@Service
public class CheckSheetService extends BaseServiceImpl<CheckSheet, Long> {
    @Autowired
    BillService billService;
    @Autowired
    CheckSheetDetailService checkSheetDetailService;
    @Autowired
    ApproverInfoService approverInfoService;
    @Autowired
    Base64SignatureService base64SignatureService;
    @Autowired
    DetectTaskService detectTaskService;
    @Autowired
    QrCodeService qrCodeService;
    @Autowired
    DetectRequestService detectRequestService;
    @Autowired
    UidRestfulRpcService uidRestfulRpcService;


    @Value("${current.baseWebPath}")
    private String baseWebPath;

    /**
     * 创建CheckSheet
     *
     * @param input
     * @return
     */
    @Transactional
    public CheckSheetPrintOutput createCheckSheet(CheckSheetInputDto input, OperatorUser operatorUser) {

        Triple<CheckSheet, List<CheckSheetDetail>, BillTypeEnum> triple = this.buildCheckSheet(input, operatorUser);
        CheckSheet checkSheet = triple.getLeft();
        // 生成编号，插入数据库
        String checkSheetCode = this.uidRestfulRpcService.nextCheckSheetCode(triple.getRight());
        checkSheet.setCode(checkSheetCode);
        checkSheet.setQrcodeUrl(this.baseWebPath + "/checkSheet/detail.html?checkSheetCode=" + checkSheetCode);
        checkSheet.setOperatorId(operatorUser.getId());
        checkSheet.setOperatorName(operatorUser.getName());
        this.insertExact(checkSheet);
        List<Long> detectTaskIdList = new ArrayList<>();
        // 生成详情并插入数据库
        List<CheckSheetDetail> checkSheetDetailList = triple.getMiddle().stream().map(detail -> {

            detail.setCheckSheetId(checkSheet.getId());
            detectTaskIdList.add(detail.getRegisterBillId());
            return detail;
        }).collect(Collectors.toList());

        List<RegisterBill> registerBillList = detectTaskIdList.stream().map(id -> {

            RegisterBill bill = new RegisterBill();
            bill.setId(id);
            bill.setCheckSheetId(checkSheet.getId());
            bill.setIsPrintCheckSheet(YesOrNoEnum.YES.getCode());
            this.billService.updateSelective(bill);
            return bill;

        }).collect(Collectors.toList());

        this.checkSheetDetailService.batchInsert(checkSheetDetailList);
        // 更新登记单信息
        return this.buildPrintDTOMap(checkSheet, checkSheetDetailList);
    }


    /**
     * 获取64位二维码
     *
     * @param content
     * @return
     */
    private String getBase64(String content) {
        try {
            return this.qrCodeService.getBase64QrCode(content, 200, 200);
        } catch (Exception e) {
            throw new TraceBizException("生成二维码出错");
        }
    }

    /**
     * 通过ID查询可调用打印接口的数据结构
     *
     * @param id
     * @return
     */
    public CheckSheetPrintOutput findPrintableCheckSheet(Long id) {
        if (id == null) {
            throw new TraceBizException("参数错误");
        }
        CheckSheet checkSheet = this.get(id);
        if (checkSheet == null) {
            throw new TraceBizException("数据不存在");
        }
        List<CheckSheetDetail> checkSheetDetailList = this.checkSheetDetailService
                .findCheckSheetDetailByCheckSheetId(checkSheet.getId());

        return this.buildPrintDTOMap(checkSheet, checkSheetDetailList);

    }

    /**
     * 构造CheckSheet
     *
     * @param checkSheet
     * @param checkSheetDetailList
     * @return
     */
    private CheckSheetPrintOutput buildPrintDTOMap(CheckSheet checkSheet, List<CheckSheetDetail> checkSheetDetailList) {
        String detailUrl = this.baseWebPath + "/checkSheet/detail.html?checkSheetCode=";
        ApproverInfo approverInfo = this.approverInfoService.get(checkSheet.getApproverInfoId());
        if (approverInfo == null) {
            throw new TraceBizException("提交的数据错误");
        }
        String base64Sign = this.base64SignatureService.findBase64SignatureByApproverInfoId(approverInfo.getId());
        CheckSheetPrintOutput checkSheetPrintOutput = CheckSheetPrintOutput.build(detailUrl, checkSheet, base64Sign,
                checkSheetDetailList, this::getBase64);

        return checkSheetPrintOutput;

    }

    /**
     * 打印预览CheckSheet
     *
     * @param input
     * @return
     */
    public CheckSheetPrintOutput prePrint(CheckSheetInputDto input, OperatorUser operatorUser) {
        Triple<CheckSheet, List<CheckSheetDetail>, BillTypeEnum> triple = this.buildCheckSheet(input, operatorUser);
        CheckSheetPrintOutput checkSheetMap = this.buildPrintDTOMap(triple.getLeft(), triple.getMiddle());

        input.setCheckSheetAliasInputDtoList(Collections.emptyList());

        return checkSheetMap;
    }

    /**
     * 构建 打印表单数据
     *
     * @param input
     * @param operatorUser
     * @return
     */
    private Triple<CheckSheet, List<CheckSheetDetail>, BillTypeEnum> buildCheckSheet(CheckSheetInputDto input, OperatorUser operatorUser) {
        Triple<List<RegisterBill>, Map<Long, String>, BillTypeEnum> triple = this.checkInputDto(input);
        Map<Long, String> billIdAliasNameMap = triple.getMiddle();

        List<RegisterBill> registerBillList = triple.getLeft();
        // 生成编号，插入数据库
        input.setDetectOperatorId(0L);

        input.setOperatorId(operatorUser.getId());
        input.setOperatorName(operatorUser.getName());
        input.setCode(this.uidRestfulRpcService.getMaskCheckSheetCode(triple.getRight()));

        input.setCreated(new Date());
        input.setModified(new Date());

        // 生成详情
        List<CheckSheetDetail> checkSheetDetailList = IntStream.range(0, registerBillList.size()).boxed().map(index -> {


            RegisterBill registerBill = registerBillList.get(index);
            if (input.getBillType() == null) {
                input.setBillType(registerBill.getBillType());
            }
            CheckSheetDetail detail = new CheckSheetDetail();
            DetectRequest detectRequest = detectRequestService.findDetectRequestByBillId(registerBill.getBillId()).orElse(null);

            detail.setRegisterBillId(registerBill.getId());
            detail.setCheckSheetId(input.getId());
            detail.setCreated(new Date());
            detail.setModified(new Date());
            detail.setOriginName(registerBill.getOriginName());
            detail.setProductName(registerBill.getProductName());
            detail.setProductAliasName(billIdAliasNameMap.get(registerBill.getId()));
            detail.setDetectResult(detectRequest.getDetectResult());
            detail.setDetectStatus(registerBill.getDetectStatus());
            detail.setVerifyStatus(registerBill.getVerifyStatus());

            detail.setOrderNumber(index + 1);
            detail.setLatestPdResult(registerBill.getLatestPdResult());
            return detail;

        }).collect(Collectors.toList());
        List<Integer>detectResultList=StreamEx.of(checkSheetDetailList).map(CheckSheetDetail::getDetectResult).distinct().toList();
        if(BillTypeEnum.REGISTER_BILL.equalsToCode(input.getBillType())){
            if(detectResultList.size()!=1||!detectResultList.contains(DetectResultEnum.PASSED.getCode())){
                throw new TraceBizException("不能打印不合格登记单");
            }
        }

        return MutableTriple.of(input, checkSheetDetailList, triple.getRight());

    }

    /**
     * 检查输入的数据及状态
     *
     * @param input
     * @return
     */
    private Triple<List<RegisterBill>, Map<Long, String>, BillTypeEnum> checkInputDto(CheckSheetInputDto input) {
        Map<Long, String> billIdAndAliasNameMap = CollectionUtils.emptyIfNull(input.getCheckSheetAliasInputDtoList())
                .stream().filter(Objects::nonNull).filter(item -> {

                    return item.getBillId() != null;

                }).collect(Collectors.toMap(CheckSheetAliasInputDto::getBillId, item -> item.getAliasName()));

        if (billIdAndAliasNameMap.isEmpty()) {
            throw new TraceBizException("提交的数据错误");
        }
        ApproverInfo approverInfo = this.approverInfoService.get(input.getApproverInfoId());
        if (approverInfo == null) {
            throw new TraceBizException("提交的数据错误");
        }
        // String base64Sign =
        // this.base64SignatureService.findBase64SignatureByApproverInfoId(approverInfo.getId());
        // input.setApproverBase64Sign(base64Sign);

        List<RegisterBill> registerBillList = this.billService.findByIdList(billIdAndAliasNameMap.keySet());

        if (registerBillList.isEmpty()) {
            throw new TraceBizException("提交的数据错误");
        }

        boolean withCheckSheetId = registerBillList.stream().anyMatch(bill -> bill.getCheckSheetId() != null);
        if (withCheckSheetId) {
            throw new TraceBizException("已经有登记单创建了打印报告");
        }
        boolean sameBillType = registerBillList.stream().map(RegisterBill::getBillType).distinct().count() == 1;
        if (!sameBillType) {
            throw new TraceBizException("所选登记(委托)单不属于相同类型");
        }
        BillTypeEnum billType = registerBillList.stream()
                .map(dto -> BillTypeEnum.fromCode(dto.getBillType()).orElse(null)).filter(Objects::nonNull)
                .findFirst().orElse(null);

        List<String> nameList = registerBillList.stream().map(RegisterBill::getName).filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        List<String> corporateNameList = registerBillList.stream().map(RegisterBill::getCorporateName)
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());

        if (nameList.isEmpty() && corporateNameList.isEmpty()) {
            throw new TraceBizException("登记单没有业户名/企业名称");
        }

        if (BillTypeEnum.COMMISSION_BILL == billType) {
            if (!corporateNameList.isEmpty()) {
                if (corporateNameList.stream().distinct().count() != 1) {
                    throw new TraceBizException("登记单不属于同一个企业");
                }
            } else if (!nameList.isEmpty()) {
                if (nameList.stream().distinct().count() != 1) {
                    throw new TraceBizException("登记单不属于同一个业户");
                }
            }

        } else if (BillTypeEnum.REGISTER_BILL == billType) {
            if (!nameList.isEmpty()) {
                if (nameList.stream().distinct().count() != 1) {
                    throw new TraceBizException("登记单不属于同一个业户");
                }
            }
        } else if (BillTypeEnum.E_COMMERCE_BILL == billType) {
            if (!nameList.isEmpty()) {
                if (nameList.stream().distinct().count() != 1) {
                    throw new TraceBizException("登记单不属于同一个业户");
                }
            }
        } else {
            throw new TraceBizException("类型错误");
        }

        boolean allChecked = registerBillList.stream().allMatch(bill -> {

            DetectRequest detectRequest = this.detectRequestService.findDetectRequestByBillId(bill.getBillId()).orElse(null);
            return DetectResultEnum.PASSED.equalsToCode(detectRequest.getDetectResult());
        });
        if (!allChecked) {
            throw new TraceBizException("登记单状态错误");
        }

        return MutableTriple.of(registerBillList, billIdAndAliasNameMap, billType);
    }

    /**
     * 通过code查询CheckSheet
     *
     * @param code
     * @return
     */
    public Optional<CheckSheet> findCheckSheetByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return Optional.empty();
        }
        CheckSheet query = new CheckSheet();
        query.setCode(code.trim());
        return this.listByExample(query).stream().findFirst();
    }
}