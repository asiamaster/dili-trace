package com.dili.trace.dao;

import com.dili.ss.base.MyMapper;
import com.dili.trace.domain.UserQrHistory;
import com.dili.trace.dto.query.UserQrHistoryQueryDto;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface UserQrHistoryMapper extends MyMapper<UserQrHistory> {


    public List<Long> selectUserInfoIdWithoutHistory(@Param("createdStart") LocalDateTime createdStart
            ,@Param("createdEnd")LocalDateTime createdEnd
            ,@Param("isValid")Integer isValid);

    public List<UserQrHistory> listPageByUserQrHistoryQuery(UserQrHistoryQueryDto historyQueryDto);

}
