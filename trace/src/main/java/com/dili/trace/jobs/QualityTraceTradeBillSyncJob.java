package com.dili.trace.jobs;

import com.dili.common.exception.TraceBizException;
import com.dili.ss.dto.DTOUtils;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.domain.QualityTraceTradeBillSyncPoint;
import com.dili.trace.service.QualityTraceTradeBillService;
import com.dili.trace.service.QualityTraceTradeBillSyncPointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 同步订单数据定时任务
 */
@EnableAsync
@Component
public class QualityTraceTradeBillSyncJob {
    private static final Logger logger = LoggerFactory.getLogger(QualityTraceTradeBillSyncJob.class);
    @Autowired
    QualityTraceTradeBillService qualityTraceTradeBillService;
    @Autowired
    QualityTraceTradeBillSyncPointService qualityTraceTradeBillSyncPointService;

    @Value("${schedule.enable:true}")
    private boolean scheduleEnable;

    @Autowired
    com.dili.trace.service.OrderService orderService;

    /**
     * 间隔两分钟同步数据
     */
    // 间隔两分钟同步数据
    @Scheduled(fixedDelay = 1000L * 60L * 2L)
    public void execute() {
        if (!scheduleEnable) {
            return;
        }
        logger.info("===sync data===");
        while (true) {
            // 查询同步点
//			VTradeBill vTradeBill = this.vTradeBillService.selectRemoteLatestData();
//			if (vTradeBill == null || vTradeBill.getBillID() == null) {
//				logger.info("电子结算无数据或数据出错");
//				break;
//			}
//			Long remoteMaxBillId = vTradeBill.getBillID();

            QualityTraceTradeBillSyncPoint pointItem = this.getSyncPoint();
            if (pointItem == null) {
                pointItem = new QualityTraceTradeBillSyncPoint();

            }
            if(pointItem.getBillId()==null){
                pointItem.setBillId(0L);
            }

            List<QualityTraceTradeBill> list = this.orderService.fetchOrderData(pointItem.getBillId(), 100);
            if (list.isEmpty()) {
                logger.info("数据无需同步 localMaxBillId: {}", pointItem.getBillId());
                break;
            }
            this.syncVTradeBillList(pointItem, list);

        }

    }

    /**
     * 同步数据到mysql数据库
     *
     * @param
     * @return
     */
    private boolean syncVTradeBillList(QualityTraceTradeBillSyncPoint pointItem, List<QualityTraceTradeBill> list) {
        try {
            this.qualityTraceTradeBillSyncPointService.syncData(pointItem, list);
            return true;
        } catch (TraceBizException e) {
            logger.warn(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }

    /**
     * 查询同步点
     * @return
     */
    private QualityTraceTradeBillSyncPoint getSyncPoint() {

        QualityTraceTradeBillSyncPoint domain = new QualityTraceTradeBillSyncPoint();

        List<QualityTraceTradeBillSyncPoint> list = this.qualityTraceTradeBillSyncPointService.listPageByExample(domain)
                .getDatas();
        if (list.size() > 1) {
            throw new RuntimeException("同步点数据出错");
        } else if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


}
