package com.dili.trace.api;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.trace.api.input.QualityTraceTradeBillInputDto;
import com.dili.trace.api.output.QualityTraceTradeBillOutput;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.service.BillService;
import com.dili.trace.service.QrCodeService;
import com.dili.trace.service.QualityTraceTradeBillService;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 查询订单相关的检测信息
 * @author wangguofeng
 * @version 1.0
 */
@RestController
@RequestMapping("/api/qualityTraceTradeBillApi")
@AppAccess(role = Role.NONE, url = "", subRoles = {})
public class QualityTraceTradeBillApi {
    private static final Logger logger = LoggerFactory.getLogger(QualityTraceTradeBillApi.class);
    @Autowired
    QualityTraceTradeBillService qualityTraceTradeBillService;
    @Autowired
    BillService billService;
    @Autowired
    QrCodeService qrCodeService;

    /**
     * 查询检测结果
     * @param inputTradeBillIdList
     * @return
     */
    @RequestMapping("/queryByOrderIdList.api")
    public BaseOutput<List<QualityTraceTradeBillOutput>> queryByOrderIdList(@RequestBody List<Long> inputTradeBillIdList) {
        List<Long> tradeBillIdList = StreamEx.of(CollectionUtils.emptyIfNull(inputTradeBillIdList)).nonNull().toList();
        if (tradeBillIdList.isEmpty()) {
            return BaseOutput.success().setData(new ArrayList<>());
        }
        try {
            List<QualityTraceTradeBillOutput> list = this.qualityTraceTradeBillService.findQualityTraceTradeBillByTradeBillIdList(tradeBillIdList);

            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure(e.getMessage());
        }


    }

    /**
     * 获得二维码图片
     * @param input
     * @return
     */
//    @RequestMapping("/queryQrBase64Img.api")
//    public BaseOutput<String> queryQrBase64Img(@RequestBody QualityTraceTradeBillInputDto input) {
//        if(input==null||input.getBillId()==null){
//            return BaseOutput.failure("参数错误");
//        }
//


}
