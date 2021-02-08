package com.dili.trace.api.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.dili.common.entity.LoginSessionContext;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.enums.VerifyTypeEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class ManagerRegisterBillApiTest extends AutoWiredBaseTest {
    @Autowired
    ManagerRegisterBillApi managerRegisterBillApi;

    @MockBean
    LoginSessionContext sessionContext;
    // private MockMvc mockMvc;
    // @Injectable
    private UserInfo userItem;

    @BeforeEach
    public void before() {
        MockitoAnnotations.initMocks(this);
        userItem = super.findUser();
        assertNotNull(userItem);
//        Mockito.doReturn(userItem.getId()).when(sessionContext).getAccountId();
//        Mockito.doReturn(userItem.getName()).when(sessionContext).getUserName();
    }

    // @Test
    // public void countByVerifyStatus() {
    //     RegisterBillDto input = new RegisterBillDto();
    //     input.setCreatedStart("2010-01-01 00:00:00");
    //     input.setCreatedEnd("2020-12-31 23:59:59");
    //     input.setVerifyType(VerifyTypeEnum.PASSED_AFTER_CHECKIN.getCode());
    //     BaseOutput<List<VerifyStatusCountOutputDto>> out=this.managerRegisterBillApi.countByVerifyStatus(input);
    //     assertNotNull(out);
    //     List<VerifyStatusCountOutputDto>list=out.getData();
    //     assertNotNull(list);
    //     assertTrue(list.size()==4);
    // }

}