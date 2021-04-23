package com.dili.trace.excel;

import com.alibaba.fastjson.JSONObject;
import com.dili.common.util.MD5Util;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.ExcelUtils;
import com.dili.trace.AutoWiredBaseTest;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.service.UserService;
import com.dili.trace.util.BeanMapUtil;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.beetl.ext.fn.StringUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

@Rollback(false)
public class ExcelTest extends AutoWiredBaseTest {
    @Autowired
    private UserService userService;
    @Test
    public void testReadExcel() {
        String fileName = "D:\\前台信息组\\项目工作\\杭州溯源\\农溯安市场经营户预安装台账-登录与导入情况-import.xls";
        // // 这里默认读取第一个sheet
        try {
            List<List<Map<String, Object>>> datas = ExcelUtils.getSheetsDatas(new FileInputStream(fileName),0);
//            int len =0;
//            User user1= null;
            for (Map<String,Object> data : datas.get(0) ) {
                UserInfo user = JSONObject.parseObject(JSONObject.toJSONString(data),UserInfo.class);
                user.setValidateState(40);
//                user.setPassword("123456");
//                userService.register(user,false);
//
//                int relen = StringUtils.length(user.getTallyAreaNos());
//                if (relen > len){
//                    len = relen;
//                    user1 = user;
//                }



            }
//            System.out.println("------------------------>"+len);
//            System.out.println(user1.getPhone());

        } catch (Exception e) {

        }
    }

}