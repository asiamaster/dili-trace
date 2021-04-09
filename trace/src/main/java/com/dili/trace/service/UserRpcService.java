package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.trace.util.MarketUtil;
import com.dili.uap.sdk.domain.Department;
import com.dili.uap.sdk.domain.User;
import com.dili.uap.sdk.domain.dto.DepartmentDto;
import com.dili.uap.sdk.domain.dto.UserQuery;
import com.dili.uap.sdk.rpc.DepartmentRpc;
import com.dili.uap.sdk.rpc.UserRpc;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UAP用户接口
 */
@Service
public class UserRpcService {
    private static final Logger logger = LoggerFactory.getLogger(UserRpcService.class);

    @Autowired(required = false)
    UserRpc userRpc;

    @Autowired
    DepartmentRpcService departmentRpcService;

    /**
     * 查询检测部门的用户
     *
     * @return
     */
    public List<User> findDetectDepartmentUsers(String likeUserName, Long marketId) {
        if (marketId == null) {
            return Lists.newArrayList();
        }
        try {
            Department department = departmentRpcService.findDetectDepartment(marketId).orElseThrow(() -> {
                return new TraceBizException("当前登录用户所属市场没有检测部门。");
            });

            UserQuery user = DTOUtils.newDTO(UserQuery.class);
            user.setDepartmentId(department.getId());
            user.setKeyword(likeUserName);
            BaseOutput<List<User>> result = userRpc.listByExample(user);
            if (result.isSuccess()) {
                return result.getData();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Lists.newArrayList();
    }

}
