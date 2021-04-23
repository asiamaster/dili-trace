package com.dili.trace.api;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.FieldConfig;
import com.dili.trace.dto.ret.FieldConfigDetailRetDto;
import com.dili.trace.enums.FieldConfigModuleTypeEnum;
import com.dili.trace.service.FieldConfigDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 查询字段配置信息
 */
@RestController
@RequestMapping(value = "/api/fieldConfigDetailApi")
@AppAccess(role = Role.ANY)
public class FieldConfigDetailApi {
    @Autowired
    LoginSessionContext loginSessionContext;
    @Autowired
    FieldConfigDetailService fieldConfigDetailService;

    /**
     * 查询字段配置信息
     *
     * @param input
     * @return
     */
    @RequestMapping("/findFieldConfigDetail.api")
    public BaseOutput<List<FieldConfigDetailRetDto>> findFieldConfigDetail(@RequestBody FieldConfig input) {
        if (input == null || input.getModuleType() == null) {
            return BaseOutput.failure("参数错误");
        }
        FieldConfigModuleTypeEnum moduleTypeEnum = FieldConfigModuleTypeEnum.fromCode(input.getModuleType()).orElse(null);
        if (moduleTypeEnum == null) {
            return BaseOutput.failure("参数错误");
        }
        SessionData sessionData = this.loginSessionContext.getSessionData();
        try {
            List<FieldConfigDetailRetDto> data = this.fieldConfigDetailService.findByMarketIdAndModuleType(sessionData.getMarketId(), moduleTypeEnum);
            return BaseOutput.successData(data);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure("服务端出错");
        }
    }
}
