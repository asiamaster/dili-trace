package com.dili.trace.dto.query;

import com.dili.ss.domain.annotation.Operator;
import com.dili.trace.domain.RegisterHeadPlate;

import javax.persistence.Column;
import java.util.List;

public class RegisterHeadPlateQueryDto extends RegisterHeadPlate {

    @Column(name = "`id`")
    @Operator(Operator.IN)
    private List<Long> idList;


    @Column(name = "`register_head_id`")
    @Operator(Operator.IN)
    private List<Long> registerHeadIdList;

    public List<Long> getRegisterHeadIdList() {
        return registerHeadIdList;
    }

    public void setRegisterHeadIdList(List<Long> registerHeadIdList) {
        this.registerHeadIdList = registerHeadIdList;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }
}
