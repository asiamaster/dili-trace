package com.dili.trace.api.client;

import java.util.List;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.trace.domain.UserPlate;
import com.dili.trace.domain.UserQrHistory;
import com.dili.trace.dto.query.UserQrHistoryQueryDto;
import com.dili.trace.glossary.TFEnum;
import com.dili.trace.service.UserQrHistoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 查询用户二维码历史信息
 */
@RestController
@AppAccess(role = Role.Client, url = "", subRoles = {CustomerEnum.CharacterType.经营户, CustomerEnum.CharacterType.买家})
@RequestMapping(value = "/api/client/clientUserQrHistoryApi")
public class ClientUserQrHistoryApi {
    private static final Logger logger = LoggerFactory.getLogger(ClientUserQrHistoryApi.class);
    @Autowired
    private LoginSessionContext sessionContext;

    @Autowired
    UserQrHistoryService userQrHistoryService;

    /**
     * 查询用户最新的二维码变更记录
     *
     * @param condition
     * @return
     */
    @SuppressWarnings({"unchecked"})
    @RequestMapping(value = "/listPage.api", method = {RequestMethod.POST})
    public BaseOutput<List<UserPlate>> listPage(@RequestBody UserQrHistoryQueryDto condition) {
        if (condition == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            SessionData sessionData = this.sessionContext.getSessionData();

            Long userId = sessionData.getUserId();
            logger.info("当前登录用户:{}", userId);
            condition.setUserId(userId);
            condition.setMarketId(sessionData.getMarketId());
            condition.setSort("created");
            condition.setOrder("desc");
            condition.setIsValid(YesOrNoEnum.YES.getCode());
            BasePage<UserQrHistory> data = this.userQrHistoryService.listPageByUserQrHistoryQuery(condition);
            return BaseOutput.success().setData(data);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询出错");
        }

    }
}