package com.dili.trace.service.impl;

import com.dili.common.exception.TraceBizException;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.dto.DTOUtils;
import com.dili.trace.dao.QualityTraceTradeBillSyncPointMapper;
import com.dili.trace.domain.QualityTraceTradeBill;
import com.dili.trace.domain.QualityTraceTradeBillSyncPoint;
import com.dili.trace.dto.QualityTraceTradeBillSyncPointDto;
import com.dili.trace.service.QualityTraceTradeBillService;
import com.dili.trace.service.QualityTraceTradeBillSyncPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@EnableAsync
@Service
public class QualityTraceTradeBillSyncPointServiceImpl extends BaseServiceImpl<QualityTraceTradeBillSyncPoint, Long>
		implements QualityTraceTradeBillSyncPointService, CommandLineRunner {

	@Autowired
	QualityTraceTradeBillService qualityTraceTradeBillService;

	public QualityTraceTradeBillSyncPointMapper getActualDao() {
		return (QualityTraceTradeBillSyncPointMapper) getDao();
	}

	@Transactional
	@Override
	public List<QualityTraceTradeBill> syncData(QualityTraceTradeBillSyncPoint pointItem,
												List<QualityTraceTradeBill> billList) {
		if (pointItem == null || pointItem.getId() == null || billList.isEmpty()) {
			throw new TraceBizException("参数为空");

		}
		QualityTraceTradeBillSyncPoint item = this.selectByIdForUpdate(pointItem.getId());
		if (item == null) {
			throw new TraceBizException("同步点不存在");
		}

//		if (!Objects.equals(pointItem.getBillId(), item.getBillId())) {
//			throw new TraceBizException("同步已经发生变化");
//
//		}
		for (QualityTraceTradeBill bill : billList) {
			if(item.getBillId()!=null){
				if(item.getBillId().compareTo(bill.getBillId())>=0){
					continue;
				}
			}
			QualityTraceTradeBillSyncPoint point = new QualityTraceTradeBillSyncPoint();
			point.setId(item.getId());
			point.setBillId(bill.getBillId());
			point.setOrderId(bill.getOrderId());
			this.updateSelective(point);
//			bill.setOrderVersion(item.getOrderVersion());
			this.qualityTraceTradeBillService.insertSelective(bill);
		}

		return billList;
	}

	@Override
	public void run(String... args) throws Exception {
		QualityTraceTradeBillSyncPointDto example = new QualityTraceTradeBillSyncPointDto();
		int count = 	this.listByExample(example).size();
		if (count == 0) {
			QualityTraceTradeBillSyncPoint item = new QualityTraceTradeBillSyncPoint();
//			item.setOrderVersion(OrderVersionEnum.VERSION.getCode());
			this.insertSelective(item);

		}

	}

	@Transactional
	@Override
	public QualityTraceTradeBillSyncPoint selectByIdForUpdate(Long id) {
		return this.getActualDao().selectByIdForUpdate(id);
	}

}
