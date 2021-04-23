package com.dili.trace.api;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.KeyTextPair;
import com.dili.trace.enums.*;

import com.dili.trace.glossary.UpStreamTypeEnum;
import com.dili.trace.service.EnumService;
import com.dili.trace.service.FieldConfigDetailService;
import de.cronn.reflection.util.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import one.util.streamex.StreamEx;

/**
 * 枚举变量接口
 */
@RestController
@RequestMapping(value = "/api/enums")
@AppAccess(role = Role.ANY)
public class EnumsApi {
    @Autowired
    FieldConfigDetailService fieldConfigDetailService;
    @Autowired
    LoginSessionContext loginSessionContext;
    @Autowired
    EnumService enumService;

    /**
     * 上游类型枚举查询
     */
    @RequestMapping(value = "/listUpStreamTypeEnum.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listUpStreamTypeEnum() {
        try {
            List<KeyTextPair> list = StreamEx.of(UpStreamTypeEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;
            }).toList();
            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 客户类型枚举查询
     */
    @RequestMapping(value = "/listClientTypeEnum.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listClientTypeEnum() {
        try {
            List<KeyTextPair> list = StreamEx.of(ClientTypeEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getDesc());
                return p;
            }).toList();
            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 检测状态枚举查询
     */
    @RequestMapping(value = "/listDetectStatusEnum.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listDetectStatusEnum() {
        try {
            List<KeyTextPair> list = StreamEx.of(DetectStatusEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;
            }).toList();

            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 检测类型枚举查询
     */
    @RequestMapping(value = "/listDetectTypeEnum.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listDetectTypeEnum() {
        try {
            List<KeyTextPair> list = StreamEx.of(DetectTypeEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;
            }).toList();
            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 证明类型查询
     */
    @RequestMapping(value = "/listImageCertType.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listImageCertType() {
        try {
            SessionData sessionData = loginSessionContext.getSessionData();
            List<KeyTextPair> list = StreamEx.of(this.enumService.listImageCertType(sessionData.getMarketId(), FieldConfigModuleTypeEnum.REGISTER)).map(e -> {

                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;

            }).toList();

            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }


    /**
     * 报备单审核状态查询
     */
    @RequestMapping(value = "/listBillVerifyStatus.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listBillVerifyStatus() {
        try {
            List<KeyTextPair> list = StreamEx.of(BillVerifyStatusEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;
            }).toList();
            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 商品保存类型查询
     */
    @RequestMapping(value = "/listPreserveType.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listPreserveType() {
        try {
            List<KeyTextPair> list = StreamEx.of(PreserveTypeEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;
            }).toList();
            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 登记单类型查询
     */
    @RequestMapping(value = "/listBillType.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listBillType() {
        try {
            List<KeyTextPair> list = StreamEx.of(BillTypeEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;
            }).toList();
            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 登记类型查询
     */
    @RequestMapping(value = "/listRegistType.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listRegistType() {
        try {
            List<KeyTextPair> list = StreamEx.of(RegistTypeEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;
            }).toList();
            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 报备单类型查询
     */
    @RequestMapping(value = "/listTruckType.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listTruckType() {
        try {
            SessionData sessionData = loginSessionContext.getSessionData();
            List<KeyTextPair> list = StreamEx.of(this.enumService.listTruckType(sessionData.getMarketId(), FieldConfigModuleTypeEnum.REGISTER)).map(e -> {

                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;

            }).toList();

            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 重量单位型枚举查询
     */
    @RequestMapping(value = "/listWeightUnitEnum.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listWeightUnitEnum() {
        try {
            List<KeyTextPair> list = StreamEx.of(WeightUnitEnum.values()).map(e -> {
                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;
            }).toList();
            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }
    /**
     * 计重类型查询
     */
    @RequestMapping(value = "/listMeasureType.api", method = RequestMethod.POST)
    public BaseOutput<List<KeyTextPair>> listMeasureType() {
        try {
            SessionData sessionData = loginSessionContext.getSessionData();
            List<KeyTextPair> list = StreamEx.of(this.enumService.listMeasureType(sessionData.getMarketId(), FieldConfigModuleTypeEnum.REGISTER)).map(e -> {

                KeyTextPair p = new KeyTextPair();
                p.setKey(e.getCode());
                p.setText(e.getName());
                return p;

            }).toList();

            return BaseOutput.success().setData(list);
        } catch (Exception e) {
            return BaseOutput.failure(e.getMessage());
        }
    }

}
