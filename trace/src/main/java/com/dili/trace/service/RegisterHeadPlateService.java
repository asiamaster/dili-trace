package com.dili.trace.service;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.trace.domain.RegisterHeadPlate;
import com.dili.trace.dto.query.RegisterHeadPlateQueryDto;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 台账车牌
 */
@Service
public class RegisterHeadPlateService extends BaseServiceImpl<RegisterHeadPlate, Long> {
    /**
     * 删除并新增车牌信息
     *
     * @param registerHeadId
     * @param plateList
     * @return
     */
    public int deleteAndInsertPlateList(Long registerHeadId, List<String> plateList) {

        if (registerHeadId == null || plateList == null || plateList.isEmpty()) {
            return 0;
        }
        RegisterHeadPlate q = new RegisterHeadPlate();
        q.setRegisterHeadId(registerHeadId);
        this.deleteByExample(q);
        List<RegisterHeadPlate> headPlateList = StreamEx.of(plateList).filter(StringUtils::isNotBlank).map(plate -> {
            RegisterHeadPlate headPlate = new RegisterHeadPlate();
            headPlate.setPlate(plate);
            headPlate.setRegisterHeadId(registerHeadId);
            headPlate.setCreated(LocalDateTime.now());
            headPlate.setModified(LocalDateTime.now());
            return headPlate;
        }).toList();
        if (headPlateList.isEmpty()) {
            return 0;
        }
        return this.batchInsert(headPlateList);
    }

    /**
     * 根据台账id查询相关车牌信息
     *
     * @param registerHeadId
     * @return
     */
    public List<RegisterHeadPlate> findHeadPlateByHeadId(Long registerHeadId) {

        if (registerHeadId == null) {
            return Lists.newArrayList();
        }
        RegisterHeadPlate q = new RegisterHeadPlate();
        q.setRegisterHeadId(registerHeadId);
        return this.listByExample(q);
    }

    /**
     * 根据id查询车牌号
     * @param registerHeadIdList
     * @return
     */
    public Map<Long, List<String>> findPlateByRegisterHeadIdList(List<Long> registerHeadIdList) {
        if (CollectionUtils.isEmpty(registerHeadIdList)) {
            return Maps.newHashMap();
        }
        RegisterHeadPlateQueryDto q = new RegisterHeadPlateQueryDto();
        q.setRegisterHeadIdList(registerHeadIdList);
        return StreamEx.of(this.listByExample(q)).mapToEntry(RegisterHeadPlate::getRegisterHeadId,RegisterHeadPlate::getPlate).grouping();
    }
}
