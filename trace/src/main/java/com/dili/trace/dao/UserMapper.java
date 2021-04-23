package com.dili.trace.dao;

import com.dili.ss.base.MyMapper;
import com.dili.trace.api.input.UserInput;
import com.dili.trace.api.output.UserOutput;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.query.UserQrHistoryQueryDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserMapper extends MyMapper<UserInfo> {
    public List<UserInfo> selectUserInfoByQrHistory(UserQrHistoryQueryDto historyQueryDto);

    public List<UserOutput> countGroupByValidateState(UserInfo user);

    public List<UserOutput> listUserByQuery(UserInput user);


    public List<UserOutput> groupByQrStatus(@Param("qrStatusList") List<Integer> qrStatusList, @Param("marketId") Long marketId);

    public List<UserOutput> listUserByStoreName(@Param("userId") Long userId, @Param("queryCondition") String queryCondition, @Param("marketId") Long marketId);

    UserOutput getUserByUserId(Long id);

    /**
     * 更新是否已上报标志位
     *
     * @param isPush
     * @param userIdList
     */
    void updateUserIsPushFlag(@Param("isPush") Integer isPush, @Param("idList") List<Long> userIdList);

    /**
     * 根据报备单的新增修改情况修改用户活跃标志位
     *
     * @param map is_active active
     */
    void updateUserActiveByBill(Map<String, Object> map);

    /**
     * 根据交易单（购买）的新增修改情况修改用户活跃标志位
     *
     * @param map
     */
    void updateUserActiveByBuyer(Map<String, Object> map);

    /**
     * 根据交易单（销售）的新增修改情况修改用户活跃标志位
     *
     * @param map
     */
    void updateUserActiveBySeller(Map<String, Object> map);

    /**
     * 查询时间段内没有报备单的用户列表
     *
     * @param map
     * @return
     */
    List<UserInfo> getActiveUserListByBill(Map<String, Object> map);

    /**
     * 查询时间段内没有交易单（购买）的用户
     *
     * @param map
     * @return
     */
    List<UserInfo> getActiveUserListByBuyer(Map<String, Object> map);

    /**
     * 查询时间段内没有交易单（销售）的用户
     *
     * @param map
     * @return
     */
    List<UserInfo> getActiveUserListBySeller(Map<String, Object> map);

    /**
     * 根据用户id更新活跃
     *
     * @param idList
     */
    void updateUserUnActiveFlag(@Param("idList") List<Long> idList);

    /**
     * 获取无照片的经营户
     *
     * @param user
     * @return
     */
    List<UserInfo> getUserByCredentialUrl(UserInfo user);

    /**
     * 根据用户ids获取用户列表
     *
     * @param idList
     * @return
     */
    List<UserInfo> getUserListByUserIds(@Param("idList") List<Long> idList);

    /**
     * 插入或者忽略用户信息
     *
     * @param userInfo
     * @return
     */

    public int insertIgnoreUserInfo(UserInfo userInfo);
}