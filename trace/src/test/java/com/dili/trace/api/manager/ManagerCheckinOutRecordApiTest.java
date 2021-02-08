package com.dili.trace.api.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import com.dili.common.entity.LoginSessionContext;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.enums.TruckTypeEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import one.util.streamex.StreamEx;

@TestInstance(Lifecycle.PER_CLASS)
public class ManagerCheckinOutRecordApiTest extends AutoWiredBaseTest {
    @Autowired
    ManagerCheckinOutRecordApi managerCheckinOutRecordApi;

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

    @Test
    public void listPageCheckInData() {
        RegisterBillDto inputDto = new RegisterBillDto();
        inputDto.setUserId(2L);
        BaseOutput<Map<Integer, Map<String,List<RegisterBill>>>> out = this.managerCheckinOutRecordApi.listPageCheckInData(inputDto);
        assertNotNull(out);
        assertTrue(out.isSuccess());
        Map<Integer, Map<String,List<RegisterBill>>> map = out.getData();
        assertNotNull(map);
        System.out.println(map);
        if (map.containsKey(TruckTypeEnum.FULL.getCode())) {
            assertTrue(map.get(TruckTypeEnum.FULL.getCode()) instanceof List);
            List<Object> list = (List) map.get(TruckTypeEnum.FULL.getCode());
            assertNotNull(list);
            assertTrue(list.size() > 0);
            assertTrue(list.get(0) instanceof RegisterBill);

        }

        if (map.containsKey(TruckTypeEnum.POOL.getCode())) {
            assertTrue(map.get(TruckTypeEnum.POOL.getCode()) instanceof Map);
            Map<String, List<Object>> plateMap = (Map) map.get(TruckTypeEnum.POOL.getCode());

            assertNotNull(plateMap);
            assertTrue(plateMap.size() > 0);
            String plate=StreamEx.ofKeys(plateMap).findFirst().orElse("");
            List<Object> list =plateMap.get(plate);
            assertNotNull(list);
            assertTrue(list.size() > 0);
            assertTrue(list.get(0) instanceof RegisterBill);
        }

    }

}