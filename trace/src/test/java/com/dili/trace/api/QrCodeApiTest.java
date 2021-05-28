package com.dili.trace.api;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.api.output.QrOutputDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Annotation;

@EnableDiscoveryClient
public class QrCodeApiTest extends AutoWiredBaseTest {
    @Autowired
    QrCodeApi qrCodeApi;

    @Test
    public void getMyQrCode() {
        LoginSessionContext sessionContext=new LoginSessionContext();
        SessionData sessionData=new SessionData();
        sessionData.setMarketId(8L);
        sessionData.setUserId(131193L);
        sessionContext.setSessionData(sessionData, new AppAccess(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return AppAccess.class;
            }

            @Override
            public Role role() {
                return Role.Client;
            }

            @Override
            public CustomerEnum.CharacterType[] subRoles() {
                return new CustomerEnum.CharacterType[]{CustomerEnum.CharacterType.经营户};
            }

            @Override
            public String method() {
                return "POST";
            }

            @Override
            public String url() {
                return "";
            }
        });
        ReflectionTestUtils.setField(this.qrCodeApi,"sessionContext",sessionContext);
        BaseOutput<QrOutputDto> out = this.qrCodeApi.getMyQrCode();
        Assertions.assertNotNull(out);
    }
}
