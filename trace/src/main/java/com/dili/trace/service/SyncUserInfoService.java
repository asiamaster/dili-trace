package com.dili.trace.service;

import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.trace.async.AsyncService;
import com.dili.trace.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 同步用户信息服务
 */
@Service
public class SyncUserInfoService {
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    AsyncService asyncService;


    /**
     * 保存并同步客户信息(客户登录或者管理员帮客户报备时调用此方法)
     *
     * @param userId
     * @param marketId
     * @return
     */
    public Optional<UserInfo> saveAndSyncUserInfo(Long userId, Long marketId) {
        return this.userInfoService.saveUserInfo(userId, marketId).map(userInfo -> {
            if(YesOrNoEnum.YES.getCode().equals(userInfo.getLastSyncSuccess())){
                return userInfo;
            }
            this.asyncService.syncUserInfo(userInfo, customerExtendDto -> {
                this.userInfoService.updateUserInfoByCustomerExtendDto(userInfo.getId(), customerExtendDto);
            });
            return userInfo;
        });

    }
}
