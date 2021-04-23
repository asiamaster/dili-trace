package com.dili.trace.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.dto.query.BrandQueryDto;
import com.dili.trace.service.BrandService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 品牌
 */
@Controller
@RequestMapping("/brand")
public class BrandController {
    @Autowired
    BrandService brandService;

    /**
     * 查询品牌
     *
     * @param queryDto
     * @return
     */
    @RequestMapping("/listBrands.action")
    @ResponseBody
    public BaseOutput listBrands(@RequestBody BrandQueryDto queryDto) {
        if (queryDto == null || StringUtils.isBlank(queryDto.getLikeBrandName())) {
            return BaseOutput.successData(Lists.newArrayList());
        }
        return BaseOutput.successData(this.brandService.listByExample(queryDto));

    }
}
