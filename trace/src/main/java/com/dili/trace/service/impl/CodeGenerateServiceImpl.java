package com.dili.trace.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.dili.common.exception.TraceBizException;
import com.dili.ss.redis.service.RedisDistributedLock;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.sg.trace.glossary.CodeGenerateEnum;
import com.dili.trace.service.BillService;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.dao.CodeGenerateMapper;
import com.dili.trace.domain.CodeGenerate;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.service.CodeGenerateService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeGenerateServiceImpl extends BaseServiceImpl<CodeGenerate, Long>
        implements CodeGenerateService, CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateServiceImpl.class);
//    private static final String TRADE_REQUEST_CODE_TYPE = "TRADE_REQUEST_CODE";
    @Autowired
    RedisDistributedLock redisDistributedLock;

    private CodeGenerateMapper getMapper() {
        return (CodeGenerateMapper) this.getDao();
    }

//    private boolean checkAndInitTradeRequestCode() {
//        CodeGenerate codeGenerate = this.getMapper().selectByTypeForUpdate(TRADE_REQUEST_CODE_TYPE).stream().findFirst()
//                .orElse(new CodeGenerate());
//        codeGenerate.setPattern("yyyyMMddHH");
//        codeGenerate.setType(TRADE_REQUEST_CODE_TYPE);
//        codeGenerate.setPrefix("HZSY");
//        if (codeGenerate.getId() == null) {
//            LocalDateTime now = LocalDateTime.now();
//
//            String nextSegment = now.format(DateTimeFormatter.ofPattern(codeGenerate.getPattern()));
//            codeGenerate.setSegment(nextSegment);
//            codeGenerate.setSeq(0L);
//            this.insertSelective(codeGenerate);
//        }
//        return true;
//    }


//    @Override
//    @Transactional(propagation = Propagation.REQUIRED)
//    public String nextTradeRequestCode() {
//        return this.nextCode(TRADE_REQUEST_CODE_TYPE, 5);
//    }

//    private String nextCode(String codeType, int paddingSize) {
//
//        CodeGenerate codeGenerate = this.getMapper().selectByTypeForUpdate(TRADE_REQUEST_CODE_TYPE).stream().findFirst()
//                .orElse(null);
//        if (codeGenerate == null) {
//            throw new TraceBizException("生成编号错误");
//        }
//
//        // 时间比较
//        LocalDateTime now = LocalDateTime.now();
//
//        String nextSegment = now.format(DateTimeFormatter.ofPattern(codeGenerate.getPattern()));
//        if (!nextSegment.equals(codeGenerate.getSegment())) {
//
//            codeGenerate.setSeq(1L);
//            codeGenerate.setSegment(nextSegment);
//            codeGenerate.setModified(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
//
//        } else {
//
//            codeGenerate.setSeq(codeGenerate.getSeq() + 1);
//            codeGenerate.setModified(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
//
//        }
//
//        this.updateSelective(codeGenerate);
//
//        return StringUtils.trimToEmpty(codeGenerate.getPrefix()).concat(nextSegment)
//                .concat(StringUtils.leftPad(String.valueOf(codeGenerate.getSeq()), paddingSize, "0"));
//    }

    @Override
    public void run(String... args) throws Exception {
//        this.checkAndInitTradeRequestCode();
    }

    //==============================寿光合并=========================
    @Autowired
    BillService billService;

    @Override
    public void init() {
//        this.checkAndInitRegisterBillSCode();
        this.checkAndInitRegisterBillSampleCode();
        this.checkAndInitCodeGenerate(CodeGenerateEnum.REGISTER_BILL_CHECKSHEET_CODE);
        this.checkAndInitCodeGenerate(CodeGenerateEnum.COMMISSION_BILL_CHECKSHEET_CODE);
//        this.checkAndInitCodeGenerate(CodeGenerateEnum.COMMISSION_BILL_CODE);
        this.checkAndInitCodeGenerate(CodeGenerateEnum.COMMISSION_BILL_SAMPLECODE);
        this.checkAndInitCodeGenerate(CodeGenerateEnum.ECOMMERCE_BILL_CODE);
        this.checkAndInitCodeGenerate(CodeGenerateEnum.ECOMMERCE_BILL_SAMPLECODE);
        this.checkAndInitCodeGenerate(CodeGenerateEnum.ECOMMERCE_BILL_SEPERATE_REPORT_CODE);

    }

    private boolean checkAndInitRegisterBillSampleCode() {

        RegisterBill domain = new RegisterBill();
        domain.setOrder("desc");
        domain.setSort("sample_code");
        domain.setPage(1);
        domain.setRows(1);
        domain.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
        RegisterBill registerBill = this.billService.listPageByExample(domain).getDatas().stream().findFirst()
                .orElse(new RegisterBill());
        String maxSampleCode = registerBill.getSampleCode();
        CodeGenerate codeGenerate = this.getMapper()
                .selectByTypeForUpdate(CodeGenerateEnum.REGISTER_BILL_SAMPLECODE.getType()).stream().findFirst()
                .orElse(new CodeGenerate());
        codeGenerate.setPattern(CodeGenerateEnum.REGISTER_BILL_SAMPLECODE.getPattern());
        codeGenerate.setType(CodeGenerateEnum.REGISTER_BILL_SAMPLECODE.getType());
        codeGenerate.setPrefix(CodeGenerateEnum.REGISTER_BILL_SAMPLECODE.getPrefix());

        if (codeGenerate.getId() == null) {
            if (StringUtils.isNotBlank(maxSampleCode)) {
                codeGenerate.setSegment(maxSampleCode.substring(1, 9));
                codeGenerate.setSeq(Long.valueOf(maxSampleCode.substring(9, 14)));
            }
            this.insertSelective(codeGenerate);
        } else {
            if (StringUtils.isNotBlank(maxSampleCode)) {
                String segment = maxSampleCode.substring(1, 9);
                Long seq = Long.valueOf(maxSampleCode.substring(9, 14));
                if (!segment.equals(codeGenerate.getSegment()) || !seq.equals(codeGenerate.getSeq())) {
                    codeGenerate.setSegment(segment);
                    codeGenerate.setSeq(seq);
                    this.updateSelective(codeGenerate);
                }

            }

        }
        return true;
    }

//    private boolean checkAndInitRegisterBillSCode() {
//
//
//        RegisterBill domain = new RegisterBill();
//        domain.setOrder("desc");
//        domain.setSort("code");
//        domain.setPage(1);
//        domain.setRows(1);
//        domain.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
//        RegisterBill registerBill = this.billService.listPageByExample(domain).getDatas().stream().findFirst()
//                .orElse(new RegisterBill());
//        String maxCode = registerBill.getCode();
//        CodeGenerate codeGenerate = this.getMapper()
//                .selectByTypeForUpdate(CodeGenerateEnum.REGISTER_BILL_CODE.getType()).stream().findFirst()
//                .orElse(new CodeGenerate());
//        codeGenerate.setPattern(CodeGenerateEnum.REGISTER_BILL_CODE.getPattern());
//        codeGenerate.setType(CodeGenerateEnum.REGISTER_BILL_CODE.getType());
//        codeGenerate.setPrefix(CodeGenerateEnum.REGISTER_BILL_CODE.getPrefix());
//
//        if (codeGenerate.getId() == null) {
//            if (StringUtils.isNotBlank(maxCode)) {
//                codeGenerate.setSegment(maxCode.substring(1, 9));
//                codeGenerate.setSeq(Long.valueOf(maxCode.substring(9, 14)));
//            }
//            this.insertSelective(codeGenerate);
//        } else {
//            if (StringUtils.isNotBlank(maxCode)) {
//                String segment = maxCode.substring(1, 9);
//                Long seq = Long.valueOf(maxCode.substring(9, 14));
//                if (!segment.equals(codeGenerate.getSegment()) || !seq.equals(codeGenerate.getSeq())) {
//                    codeGenerate.setSegment(segment);
//                    codeGenerate.setSeq(seq);
//                    this.updateSelective(codeGenerate);
//                }
//
//            }
//
//        }
//        return true;
//
//    }


    private boolean checkAndInitCodeGenerate(CodeGenerateEnum codeGenerateEnum) {

        CodeGenerate codeGenerate = this.getMapper()
                .selectByTypeForUpdate(codeGenerateEnum.getType()).stream().findFirst()
                .orElse(new CodeGenerate());
        codeGenerate.setPattern(codeGenerateEnum.getPattern());
        codeGenerate.setType(codeGenerateEnum.getType());
        codeGenerate.setPrefix(codeGenerateEnum.getPrefix());

        if (codeGenerate.getId() == null) {
            LocalDateTime now = LocalDateTime.now();

            String nextSegment = now.format(DateTimeFormatter.ofPattern(codeGenerate.getPattern()));
            codeGenerate.setSegment(nextSegment);
            codeGenerate.setSeq(0L);
            this.insertSelective(codeGenerate);
        }

        return true;

    }


//
//    @Override
//    @Transactional(propagation = Propagation.REQUIRED)
//    public String nextRegisterBillSampleCode() {
//        String lockKey = "lock_trace_registerbill_samplecode";
//        try {
//            redisDistributedLock.tryGetLockSync(lockKey, lockKey, 10);
//            return this.nextCode(CodeGenerateEnum.REGISTER_BILL_SAMPLECODE);
//        } catch (Exception e) {
//            throw new TraceBizException("生成编号错误");
//        } finally {
//            redisDistributedLock.releaseLock(lockKey, lockKey);
//        }
//
//    }


    @Transactional(propagation = Propagation.REQUIRED)
    public String nextECOMMERCE_BILL_SEPERATE_REPORT_CODE() {

        return this.nextCode(CodeGenerateEnum.ECOMMERCE_BILL_SEPERATE_REPORT_CODE);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String nextCheckSheetCode(BillTypeEnum taskTypeEnum) {
        if (BillTypeEnum.REGISTER_BILL == taskTypeEnum) {
            return this.nextCode(CodeGenerateEnum.REGISTER_BILL_CHECKSHEET_CODE);
        } else if (BillTypeEnum.COMMISSION_BILL == taskTypeEnum) {
            return this.nextCode(CodeGenerateEnum.COMMISSION_BILL_CHECKSHEET_CODE);
        } else {
            throw new TraceBizException("不支持的类型");
        }
    }

    @Override
    public String getMaskCheckSheetCode(BillTypeEnum taskTypeEnum) {

        if (BillTypeEnum.REGISTER_BILL == taskTypeEnum) {
            return CodeGenerateEnum.REGISTER_BILL_CHECKSHEET_CODE.getPrefix() + "******";
        } else if (BillTypeEnum.COMMISSION_BILL == taskTypeEnum) {
            return CodeGenerateEnum.COMMISSION_BILL_CHECKSHEET_CODE.getPrefix() + "******";
        } else {
            throw new TraceBizException("不支持的类型");
        }
    }




    @Override
    public String nextSampleCode(BillTypeEnum billTypeEnum) {
        if (BillTypeEnum.REGISTER_BILL == billTypeEnum) {
            return this.nextCode(CodeGenerateEnum.REGISTER_BILL_SAMPLECODE);
        } else if (BillTypeEnum.COMMISSION_BILL == billTypeEnum) {
            return this.nextCode(CodeGenerateEnum.COMMISSION_BILL_SAMPLECODE);
        } else {
            throw new TraceBizException("不能生成采样单号");
        }
    }

//    @Override
//    public String nextCommissionBillSampleCode() {
//
//        return this.nextCode(CodeGenerateEnum.COMMISSION_BILL_SAMPLECODE);
//    }


    private String nextCode(CodeGenerateEnum codeGenerateEnum) {
        CodeGenerate codeGenerate = this.getMapper().selectByTypeForUpdate(codeGenerateEnum.getType()).stream()
                .findFirst().orElse(null);
        if (codeGenerate == null) {
            throw new TraceBizException("生成编号错误");
        }
        ;
        // 时间比较
        LocalDateTime now = LocalDateTime.now();

        String nextSegment = now.format(DateTimeFormatter.ofPattern(codeGenerate.getPattern()));
        if (!nextSegment.equals(codeGenerate.getSegment())) {

            codeGenerate.setSeq(1L);
            codeGenerate.setSegment(nextSegment);
            codeGenerate.setModified(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));

        } else {

            codeGenerate.setSeq(codeGenerate.getSeq() + 1);
            codeGenerate.setModified(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));

        }

        this.updateSelective(codeGenerate);

        String code = StringUtils.trimToEmpty(codeGenerate.getPrefix()).concat(nextSegment)
                .concat(StringUtils.leftPad(String.valueOf(codeGenerate.getSeq()), codeGenerateEnum.getLen(), "0"));


        logger.debug("生成的{}的code为{}", codeGenerateEnum.getName(), code);
        return code;
    }

    @Override
    public String nextECommerceBillSampleCode() {
        return this.nextCode(CodeGenerateEnum.ECOMMERCE_BILL_SAMPLECODE);
    }

    @Override
    public String nextECommerceBillCode() {
        return this.nextCode(CodeGenerateEnum.ECOMMERCE_BILL_CODE);
    }

}
