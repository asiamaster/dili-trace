package com.dili.trace.service;

import com.dili.common.exception.TraceBizException;
import com.dili.trace.domain.RegisterTallyAreaNo;
import com.dili.trace.dto.query.TallyAreaNoQueryDto;
import com.dili.trace.enums.BillTypeEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 到货摊位号
 */
@Service
public class RegisterTallyAreaNoService extends TraceBaseService<RegisterTallyAreaNo, Long> {
    /**
     * 插入绑定的到货摊位号
     *
     * @param tallyAreaNoList
     * @param billId
     * @param billTypeEnum
     * @return
     */
    public int insertTallyAreaNoList(List<String> tallyAreaNoList, Long billId, BillTypeEnum billTypeEnum) {
        if (billId == null || billTypeEnum == null) {
            throw new TraceBizException("参数错误");
        }
        List<RegisterTallyAreaNo> noList = StreamEx.ofNullable(tallyAreaNoList).flatCollection(Function.identity())
                .filter(StringUtils::isNotBlank)
                .map(noStr -> {
                    RegisterTallyAreaNo areaNo = new RegisterTallyAreaNo();
                    areaNo.setBillType(billTypeEnum.getCode());
                    areaNo.setTallyareaNo(noStr.trim());
                    areaNo.setCreated(LocalDateTime.now());
                    areaNo.setBillId(billId);
                    return areaNo;
                })
                .toList();
        if (noList.isEmpty()) {
            return 0;
        }

        Integer maxLength=StreamEx.of(noList).map(RegisterTallyAreaNo::getTallyareaNo).mapToInt(tallyAreaNo->tallyAreaNo.length()).max().orElse(0);
        if(maxLength>50){
            throw new TraceBizException("摊位号超长(50字符)");
        }

        RegisterTallyAreaNo dq = new RegisterTallyAreaNo();
        dq.setBillType(billTypeEnum.getCode());
        dq.setBillId(billId);
        this.deleteByExample(dq);
        return this.batchInsert(noList);
    }

    /**
     * 根据billId和billType查询到货摊位号
     *
     * @param billId
     * @param billTypeEnum
     * @return
     */
    public List<RegisterTallyAreaNo> findTallyAreaNoByBillIdAndType(Long billId, BillTypeEnum billTypeEnum) {
        if (billId == null || billTypeEnum == null) {
            return Lists.newArrayList();
        }
        RegisterTallyAreaNo dq = new RegisterTallyAreaNo();
        dq.setBillType(billTypeEnum.getCode());
        dq.setBillId(billId);
        return this.listByExample(dq);

    }
    /**
     * 根据id查询摊位号
     * @param billIdList
     * @return
     */
    public Map<Long, List<String>> findTallyAreaNoByRegisterHeadIdList(List<Long> billIdList) {
        return this.findTallyAreaNoByIdListAndType(billIdList,BillTypeEnum.MASTER_BILL);
    }
    /**
     * 根据id查询摊位号
     * @param billIdList
     * @return
     */
    public Map<Long, List<String>> findTallyAreaNoByRegisterBillIdList(List<Long> billIdList) {

        return this.findTallyAreaNoByIdListAndType(billIdList,BillTypeEnum.REGISTER_BILL);
    }
    /**
     * 根据id查询摊位号
     * @param billIdList
     * @return
     */
    private Map<Long, List<String>> findTallyAreaNoByIdListAndType(List<Long> billIdList,BillTypeEnum billTypeEnum) {
        if (CollectionUtils.isEmpty(billIdList)) {
            return Maps.newHashMap();
        }
        TallyAreaNoQueryDto q=new TallyAreaNoQueryDto();
        q.setBillIdList(billIdList);
        q.setBillType(billTypeEnum.getCode());
        return StreamEx.of(this.listByExample(q)).mapToEntry(RegisterTallyAreaNo::getBillId,RegisterTallyAreaNo::getTallyareaNo).grouping();
    }

}
