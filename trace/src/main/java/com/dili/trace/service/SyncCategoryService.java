package com.dili.trace.service;

import com.dili.assets.sdk.dto.CusCategoryQuery;
import com.dili.trace.async.AsyncService;
import com.dili.trace.domain.hangguo.HangGuoCategory;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 同步商品信息
 */
@Service
public class SyncCategoryService {
    @Autowired
    CategoryService categoryService;
    @Autowired
    AsyncService asyncService;

    /**
     * 保存并同步商品信息(提交报备信息时触发)
     *
     * @param categoryId
     * @param marketId
     */
    public void saveAndSyncGoodInfo(Long categoryId, Long marketId) {
        if (categoryId == null || marketId == null) {
            return;
        }
        HangGuoCategory category = this.categoryService.saveCategory(categoryId, marketId);
        this.asyncService.syncCategoryInfo(category, cusCategoryDTO -> {
            this.categoryService.updateCategory(category.getId(), cusCategoryDTO);
        });
    }

}
