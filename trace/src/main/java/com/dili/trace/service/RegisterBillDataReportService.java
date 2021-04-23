package com.dili.trace.service;

import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.IDTO;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trace.dao.RegisterBillMapper;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillStaticsDto;
import com.dili.trace.enums.BillTypeEnum;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 寿光登记单数据报表
 */
@Service
public class RegisterBillDataReportService {
    @Autowired
    RegisterBillMapper registerBillMapper;

    /**
     * 查询统计页面分页数据
     */
    public String listStaticsPage(RegisterBillDto dto) throws Exception {
        if (StringUtils.isNotBlank(dto.getAttrValue())) {
            switch (dto.getAttr()) {
                case "code":
                    dto.setCode(dto.getAttrValue());
                    break;
                case "plate":
                    dto.setLikePlate(dto.getAttrValue());
                    break;
                case "tallyAreaNo":
                    // registerBill.setTallyAreaNo(registerBill.getAttrValue());
                    dto.setLikeTallyAreaNo(dto.getAttrValue());
                    break;
                case "latestDetectOperator":
                    dto.setLatestDetectOperator(dto.getAttrValue());
                    break;
                case "name":
                    dto.setName(dto.getAttrValue());
                    break;
                case "productName":
                    dto.setLikeProductName(dto.getAttrValue());
                    break;
                case "likeSampleCode":
                    dto.setLikeSampleCode(dto.getAttrValue());
                    break;
            }
        }
        StringBuilder sql = this.buildDynamicCondition(dto);
        if (sql.length() > 0) {
            dto.setMetadata(IDTO.AND_CONDITION_EXPR, sql.toString());
        }
        dto.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
//        dto.setLatestDetectTimeTimeStart(StringUtils.trimToNull(dto.getLatestDetectTimeTimeStart()));
//        dto.setLatestDetectTimeTimeEnd(StringUtils.trimToNull(dto.getLatestDetectTimeTimeEnd()));

//        dto.setCreatedStart(StringUtils.trimToNull(dto.getCreatedStart()));
//        dto.setCreatedEnd(StringUtils.trimToNull(dto.getCreatedEnd()));
        if (dto.getPage() == null || dto.getPage() < 0) {
            dto.setPage(1);
        }
        if (dto.getRows() == null || dto.getRows() <= 0) {
            dto.setRows(10);
        }
        PageHelper.startPage(dto.getPage(), dto.getRows());
        PageHelper.orderBy(dto.getSort() + " " + dto.getOrder());
        List<RegisterBillDto> list = this.registerBillMapper.queryListByExample(dto);
        Page<RegisterBillDto> page = (Page) list;

        EasyuiPageOutput out = new EasyuiPageOutput();
        List results = ValueProviderUtils.buildDataByProvider(dto, list);
        out.setRows(results);
        out.setTotal(page.getTotal());
        return out.toString();
    }

    /**
     * 构造动态条件
     * @param registerBill
     * @return
     */
    private StringBuilder buildDynamicCondition(RegisterBillDto registerBill) {
        StringBuilder sql = new StringBuilder();
        if (registerBill.getHasDetectReport() != null) {
//            if (registerBill.getHasDetectReport()) {
//                sql.append("  (detect_report_url is not null AND detect_report_url<>'') ");
//            } else {
//                sql.append("  (detect_report_url is  null or detect_report_url='') ");
//            }
        }

        if (registerBill.getHasOriginCertifiy() != null) {
            if (sql.length() > 0) {
                sql.append(" AND ");
            }
//            if (registerBill.getHasOriginCertifiy()) {
//                sql.append("  (origin_certifiy_url is not null AND origin_certifiy_url<>'') ");
//            } else {
//                sql.append("  (origin_certifiy_url is  null or origin_certifiy_url='') ");
//            }
        }

        if (registerBill.getHasHandleResult() != null) {
            if (sql.length() > 0) {
                sql.append(" AND ");
            }
//            if (registerBill.getHasHandleResult()) {
//                sql.append("  (handle_result is not null AND handle_result<>'') ");
//            } else {
//                sql.append("  (handle_result is  null or handle_result='') ");
//            }
        }
        if (registerBill.getHasCheckSheet() != null) {
            if (sql.length() > 0) {
                sql.append(" AND ");
            }
            if (registerBill.getHasCheckSheet()) {
                sql.append("  (check_sheet_id is not null) ");
            } else {
                sql.append("  (check_sheet_id is null) ");
            }
        }
        return sql;
    }
    /**
     * 根据状态统计数据
     *
     * @param dto
     * @return
     */
    public RegisterBillStaticsDto groupByState(RegisterBillDto dto) {
        dto.setBillType(BillTypeEnum.REGISTER_BILL.getCode());
        return this.registerBillMapper.groupByState(dto);
    }
}
