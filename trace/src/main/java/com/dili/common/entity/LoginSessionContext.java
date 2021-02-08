package com.dili.common.entity;

import java.io.Serializable;

import com.dili.common.annotation.AppAccess;
import com.dili.common.entity.SessionData;
import com.dili.trace.api.enums.LoginIdentityTypeEnum;
import com.dili.common.exception.TraceBizException;
import com.dili.trace.dto.OperatorUser;

public class LoginSessionContext implements Serializable {
    private static final long serialVersionUID = 1L;
    private SessionData sessionData;

//    public String getUserName() {
//
//        return this.sessionData == null ? null : this.sessionData.getUserName();
//    }

//    public Long getAccountId() {
//
//        return this.sessionData == null ? null : this.getSessionData().getUserId();
//    }
    /**
     * @return SessionData return the sessionData
     */
    public SessionData getSessionData() {

        if (this.sessionData == null) {
            throw new TraceBizException("你还未登录");
        }
        return sessionData;
    }

    /**
     * @param sessionData the sessionData to set
     */
    public void setSessionData(SessionData sessionData, AppAccess access) {
        if(sessionData!=null){
            sessionData.setRole(access.role());
        }
        this.sessionData = sessionData;
    }
}
