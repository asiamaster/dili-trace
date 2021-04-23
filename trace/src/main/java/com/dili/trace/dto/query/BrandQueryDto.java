package com.dili.trace.dto.query;

import com.dili.ss.domain.annotation.Like;
import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.Brand;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;

public class BrandQueryDto extends Brand {
    @ApiModelProperty(value = "品牌名称")
    @Column(name = "`brand_name`")
    @Like(Like.BOTH)
    private String likeBrandName;

    public String getLikeBrandName() {
        return likeBrandName;
    }

    public void setLikeBrandName(String likeBrandName) {
        this.likeBrandName = likeBrandName;
    }
}
