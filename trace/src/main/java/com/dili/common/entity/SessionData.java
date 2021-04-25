package com.dili.common.entity;

import com.dili.common.annotation.Role;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.trace.dto.OperatorUser;
import com.dili.uap.sdk.domain.UserTicket;
import org.apache.commons.beanutils.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SessionData {
    private static final Logger logger = LoggerFactory.getLogger(SessionData.class);
    /**
     * 身份类型
     */
    private Integer identityType;
    /**
     * 角色
     */
    private Role role;

    /**
     * 角色
     */
    private List<CustomerEnum.CharacterType> subRoles = new ArrayList<>();
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 市场名称
     */
    private String marketName;
    /**
     * 市场id
     */
    private Long marketId;


    public Optional<OperatorUser> getOptUser() {
        if (this.role == null || (this.role != Role.Manager && this.role != Role.Client)) {
            return Optional.empty();
        }
        OperatorUser operatorUser = new OperatorUser(this.getUserId(), this.getUserName());
        operatorUser.setMarketId(this.getMarketId());
        operatorUser.setMarketName(this.getMarketName());
        return Optional.of(operatorUser);
    }

    public SessionData() {

    }

    public List<CustomerEnum.CharacterType> getSubRoles() {
        return subRoles;
    }

    public void setSubRoles(List<CustomerEnum.CharacterType> subRoles) {
        this.subRoles = subRoles;
    }


    private Map<Object, Object> convertThisToMap() {
        Map<Object, Object> map = new HashMap<>(new BeanMap(this));
        return map;
    }

    public Map<Object, Object> toMap() {
        return this.convertThisToMap();
    }


    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static SessionData fromUserTicket(UserTicket ut) {
        SessionData data = new SessionData();
        data.userId = ut.getId();
        data.userName = ut.getRealName();
        data.marketId = ut.getFirmId();
        data.marketName = ut.getFirmName();
        return data;
    }

    /**
     * @return Integer return the identityType
     */
    public Integer getIdentityType() {
        return identityType;
    }

    /**
     * @param identityType the identityType to set
     */
    public void setIdentityType(Integer identityType) {
        this.identityType = identityType;
    }

    /**
     * @return Long return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return String return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }



    /**
     * @param marketName the marketName to set
     */
    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }


    /**
     * @return the marketName
     */
    public String getMarketName() {
        return marketName;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

}