package com.dili.trace.rpc.service;

import com.dili.assets.sdk.dto.CarTypeDTO;
import com.dili.assets.sdk.rpc.CarTypeRpc;
import com.dili.ss.domain.BaseOutput;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * SB
 * @author admin
 */
@Service
public class CarTypeRpcService {
    private static final Logger logger = LoggerFactory.getLogger(CarTypeRpcService.class);
    @Autowired
    CarTypeRpc carTypeRpc;

    /**
     * 查询所有车型
     * @return 
     */
    public List<CarTypeDTO> listCarType() {
        try {
            BaseOutput<List<CarTypeDTO>> out = this.carTypeRpc.listCarType();
            if (out != null && out.isSuccess() && out.getData() != null) {
                return out.getData();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Lists.newArrayList();

    }
}
