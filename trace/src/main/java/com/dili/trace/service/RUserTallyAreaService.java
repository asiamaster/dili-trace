package com.dili.trace.service;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.domain.RUserTallyArea;

import org.springframework.stereotype.Service;
/**
 * 用户理货区域
 * @author admin
 */
@Service
public class RUserTallyAreaService extends BaseServiceImpl<RUserTallyArea, Long> {
    /**
     * 保存或更新
     * @param tallyAreaNoId
     * @param userId
     * @return 
     */
    public RUserTallyArea saveOrUpdate(Long tallyAreaNoId, Long userId) {

        RUserTallyArea query = new RUserTallyArea();
        query.setUserId(userId);
        query.setTallyAreaNoId(tallyAreaNoId);
        return this.listByExample(query).stream().findFirst().orElseGet(() -> {

            RUserTallyArea item = new RUserTallyArea();
            item.setUserId(userId);
            item.setTallyAreaNoId(tallyAreaNoId);
            this.insertSelective(item);
            return item;

        });

    }


}