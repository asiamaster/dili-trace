package com.dili.trace.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.domain.CheckSheetDetail;
/**
 * SB
 * @author admin
 */
@Service
public class CheckSheetDetailService extends BaseServiceImpl<CheckSheetDetail, Long> {

	/**
	 * 通过checksheetid查询对应的detail子表信息
	 * 
	 * @param checkSheetId
	 * @return
	 */
	public List<CheckSheetDetail> findCheckSheetDetailByCheckSheetId(Long checkSheetId) {
		if (checkSheetId == null) {
			return Collections.emptyList();
		}
		CheckSheetDetail checkSheetDetailQuery = new CheckSheetDetail();
		checkSheetDetailQuery.setCheckSheetId(checkSheetId);
		List<CheckSheetDetail> checkSheetDetailList = this.listByExample(checkSheetDetailQuery);
		return checkSheetDetailList;
	}

}
