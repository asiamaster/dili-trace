package com.dili.trace.service;

import com.dili.ss.base.BaseService;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.trace.domain.UserHistory;
import com.dili.trace.dto.UserHistoryListDto;
import com.dili.trace.dto.UserHistoryStaticsDto;

/**
 * 用户历史数据
 */
public interface UserHistoryService extends BaseService<UserHistory, Long> {



	/**
	 * 保存用户历史数据
	 * 
	 * @param userId
	 * @return
	 */

	public int insertUserHistoryForNewUser(Long userId);
	/**
	 * 保存用户历史数据
	 * 
	 * @param userId
	 * @return
	 */

	public int insertUserHistoryForUpdateUser(Long userId);
	/**
	 * 保存用户历史数据
	 * 
	 * @param userId
	 * @return
	 */

	public int insertUserHistoryForDeleteUser(Long userId);
	/**
	 * 分页查询
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	public EasyuiPageOutput listUserHistoryPageByExample(UserHistoryListDto dto) throws Exception ;
	/**
	 * 查询统计信息
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	public UserHistoryStaticsDto queryStatics(UserHistoryListDto dto) throws Exception ;
}
