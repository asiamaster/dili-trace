package com.dili.trace.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.ProcessConfig;
import com.dili.trace.service.ProcessConfigService;
import com.dili.trace.service.UapRpcService;
import com.dili.uap.sdk.domain.Firm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 业务配置
 */
@Controller
@RequestMapping("/processConfig")
public class ProcessConfigController {
    @Autowired
    ProcessConfigService processConfigService;
    @Autowired
    UapRpcService uapRpcService;

    /**
     * 进入页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping("/index.html")
    public String index(ModelMap modelMap) {
        Firm firm = this.uapRpcService.getCurrentFirm().orElse(null);
        ProcessConfig processConfig = this.processConfigService.findByMarketId(firm.getId());
        modelMap.put("processConfig", processConfig);
        modelMap.put("currentFirm", firm);
        return "processConfig/index";
    }

    /**
     * 更新
     *
     * @param processConfig
     * @return
     */
    @RequestMapping("/doUpdate.action")
    @ResponseBody
    public BaseOutput doUpdate(@RequestBody ProcessConfig processConfig) {
        if (processConfig == null) {
            return BaseOutput.failure("参数错误");
        }
        Firm firm = this.uapRpcService.getCurrentFirm().orElse(null);
        processConfig.setMarketId(firm.getId());
        this.processConfigService.saveOrUpdateSelective(processConfig);
        return BaseOutput.success();
    }


}
