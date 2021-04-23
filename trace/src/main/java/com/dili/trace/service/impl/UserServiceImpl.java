package com.dili.trace.service.impl;

import com.alibaba.fastjson.JSON;
import com.dili.common.config.DefaultConfiguration;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.customer.sdk.domain.TallyingArea;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.ss.util.DateUtils;
import com.dili.trace.api.input.UserQueryDto;
import com.dili.trace.dao.UserMapper;
import com.dili.trace.domain.SysConfig;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.domain.UserTallyArea;
import com.dili.trace.enums.SysConfigTypeEnum;
import com.dili.trace.glossary.EnabledStateEnum;
import com.dili.trace.rpc.service.CustomerRpcService;
import com.dili.trace.rpc.service.TallyingAreaRpcService;
import com.dili.trace.service.*;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.stream.Collectors.toList;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:35.
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserInfo, Long> implements UserService {

    public UserMapper getActualDao() {
        return (UserMapper) getDao();
    }

    @Autowired
    UserPlateService userPlateService;
    @Autowired
    UsualAddressService usualAddressService;
    @Autowired
    EventMessageService eventMessageService;
    @Autowired
    RegisterBillService registerBillService;
    @Autowired
    DefaultConfiguration defaultConfiguration;
    @Autowired
    QrCodeService qrCodeService;
    @Autowired
    UserQrHistoryService userQrHistoryService;
    @Autowired
    TallyAreaNoService tallyAreaNoService;
    @Autowired
    IWxAppService wxAppService;

    @Autowired
    MessageService messageService;

    @Autowired
    UserStoreService userStoreService;

    @Autowired
    SysConfigService sysConfigService;

    @Autowired
    CustomerRpcService customerRpcService;

    @Autowired
    TallyingAreaRpcService tallyingAreaRpcService;

    @Override
    public UserInfo login(String phone, String encryptedPassword) {
        return null;
    }

    @Override
    public List<UserInfo> getUserByExistsAccount(String phone) {
        UserInfo query = new UserInfo();
        query.setPhone(phone);
        query.setYn(YesOrNoEnum.YES.getCode());
        return listByExample(query);
    }

    private Optional<String> andCondition(UserQueryDto dto) {
        List<String> strList = new ArrayList<String>();
        if (dto != null && dto.getHasBusinessLicense() != null) {
            if (dto.getHasBusinessLicense()) {
                strList.add(" (business_license_url is not null and business_license_url<>'') ");
            } else {
                strList.add(" (business_license_url is null or business_license_url='') ");
            }
        }

        if (dto != null && StringUtils.isNotBlank(dto.getPlates())) {
            strList.add(" id in (select user_id from user_plate where plate like '"
                    + dto.getPlates().trim().toUpperCase() + "%')");
        }
        if (!strList.isEmpty()) {
            return Optional.of(String.join(" and ", strList));
        }
        return Optional.empty();
    }

    @Override
    public EasyuiPageOutput listEasyuiPageByExample(UserQueryDto dto) throws Exception {
        // 经营户卡号查询，不足6位，左边补0
        if (!StringUtils.isEmpty(dto.getThirdPartyCode())) {
            dto.setThirdPartyCode(StringUtils.leftPad(dto.getThirdPartyCode(), 6, "0"));
        }
        this.andCondition(dto).ifPresent(str -> {
            dto.mset(IDTO.AND_CONDITION_EXPR, str);
        });
        dto.setYn(1);
        EasyuiPageOutput out = this.listEasyuiPageByExample(dto, true);
        List<UserInfo> users = out.getRows();
        List<Long> userIdList = users.stream().map(o -> {
            return o.getId();
        }).collect(toList());
        List<UserInfo> userList = users.stream().map(u -> {
            Long userId = u.getId();
            u.setPlates("");
            return u;
        }).collect(toList());
        out.setRows(userList);

        return out;
    }


    @Override
    public List<UserInfo> findUserByNameOrPhoneOrTallyNo(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return Lists.newArrayList();
        }
        Example e = new Example(UserInfo.class);
        e.or().orLike("tallyAreaNos", "%" + keyword.trim() + "%").orLike("name", "%" + keyword.trim() + "%")
                .orLike("phone", "%" + keyword.trim() + "%");
        return this.getDao().selectByExample(e);
    }

    @Override
    public Integer countUser(UserInfo user) {
        return this.getActualDao().selectCount(user);
    }

    @Override
    public UserInfo wxLogin(String openid) {
        UserInfo query = new UserInfo();
        query.setOpenId(openid);
        query.setYn(YesOrNoEnum.YES.getCode());
        UserInfo po = listByExample(query).stream().findFirst().orElse(null);
        if (po == null) {
            throw new TraceBizException("用户未注册");
        }
        if (EnabledStateEnum.DISABLED.getCode().equals(po.getState())) {
            throw new TraceBizException("手机号已禁用");
        }
        return po;
    }


    @Override
    public void updateUserIsPushFlag(Integer isPush, List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            LOGGER.error("updateUserIsPushFlag :userIdList IS NULL");
            return;
        }
        if (null == isPush) {
            LOGGER.error("updateUserIsPushFlag :isPush IS NULL");
            return;
        }
        getActualDao().updateUserIsPushFlag(isPush, userIdList);
    }

    @Override
    public void updateUserActiveByTime() {
        String optType = SysConfigTypeEnum.OPERATION_LIMIT_DAY.getCode();
        String optCategory = SysConfigTypeEnum.OPERATION_LIMIT_DAY.getCode();
        SysConfig sysConfig = new SysConfig();
        sysConfig.setOptType(optType);
        sysConfig.setOptCategory(optCategory);
        List<SysConfig> sysConfigList = sysConfigService.listByExample(sysConfig);
        //未配置活跃限定天数，则不计算活跃
        if (CollectionUtils.isEmpty(sysConfigList)) {
            return;
        }
        //活跃限定天数为0，则不计算活跃
        String val = sysConfigList.get(0).getOptValue();
        int limitDay = Integer.valueOf(val);
        if (limitDay <= 0) {
            return;
        }
        limitDay = 0 - limitDay;
        Map<String, Object> activeMap = processActiveMap(limitDay);
        //patch活跃
        getActualDao().updateUserActiveByBill(activeMap);
        getActualDao().updateUserActiveByBuyer(activeMap);
        getActualDao().updateUserActiveBySeller(activeMap);
        //patch不活跃
        updateUserUnActive(activeMap);
    }

    /**
     * 更新用户去活跃
     *
     * @param map
     */
    private void updateUserUnActive(Map<String, Object> map) {
        List<UserInfo> unActiveListBill = getActualDao().getActiveUserListByBill(map);
        List<UserInfo> unActiveListSeller = getActualDao().getActiveUserListBySeller(map);
        CopyOnWriteArrayList<Long> billList = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Long> sellList = new CopyOnWriteArrayList<>();
        //去活跃 未激活、禁用用户
        Integer activeFlag = 1;
        UserInfo user = new UserInfo();
        user.setIsActive(activeFlag);
        user.mset(IDTO.AND_CONDITION_EXPR, " ( state=0 or yn=-1 )");
        List<UserInfo> userList = listByExample(user);
        List<Long> sUserList = StreamEx.of(userList).nonNull().map(UserInfo::getId).collect(toList());
        if (CollectionUtils.isNotEmpty(sUserList)) {
            LOGGER.info("---用户状态去活跃集合 relustList---" + JSON.toJSONString(sUserList));
            getActualDao().updateUserUnActiveFlag(sUserList);
        }
        //无报备单用户
        if (CollectionUtils.isNotEmpty(unActiveListBill)) {
            StreamEx.of(unActiveListBill).nonNull().forEach(b -> {
                billList.add(b.getId());
            });
        }
        //无销售交易用户
        if (CollectionUtils.isNotEmpty(unActiveListSeller)) {
            StreamEx.of(unActiveListSeller).nonNull().forEach(b -> {
                sellList.add(b.getId());
            });
        }
        //用户已存在报备单
        if (CollectionUtils.isEmpty(billList)) {
            return;
        }
        //用户已销售交易
        if (CollectionUtils.isEmpty(unActiveListSeller)) {
            return;
        }

        List<Long> resultList = billList.stream().filter(r -> sellList.contains(r)).collect(toList());
        LOGGER.info("---去活跃集合 relustList---" + JSON.toJSONString(resultList));
        if (CollectionUtils.isNotEmpty(resultList)) {
            getActualDao().updateUserUnActiveFlag(resultList);
        }
    }

    private Map<String, Object> processActiveMap(int limitDay) {
        Map<String, Object> activeMap = new HashMap<>(16);
        Date limitDate = new Date();
        limitDate = DateUtils.addDays(limitDate, limitDay);
        String limitDateStr = DateUtils.format(limitDate, "yyyy-MM-dd HH:mm:ss");
        activeMap.put("limitTime", limitDateStr);
        return activeMap;
    }


    @Override
    public UserInfo findByTallyAreaNo(String tallyAreaNo, Long marketId) {
        UserTallyArea query = DTOUtils.newDTO(UserTallyArea.class);
        query.setTallyAreaNo(tallyAreaNo);
        TallyingArea tallyingArea = tallyingAreaRpcService.findCustomerByIdOrEx(tallyAreaNo, marketId);

        if (null == tallyingArea) {
            return null;
        }
        CustomerExtendDto customer = customerRpcService.findCustomerByIdOrEx(tallyingArea.getCustomerId(), marketId);
        UserInfo user = new UserInfo();
        user.setId(customer.getId());
        user.setName(customer.getName());
        user.setCardNo(customer.getCertificateNumber());
        user.setPhone(customer.getContactsPhone());
        user.setAddr(customer.getCertificateAddr());
        return user;
    }


    /**
     * 获取无照片的经营户
     *
     * @return
     */
    @Override
    public List<UserInfo> getUserByCredentialUrl(UserInfo user) {
        return getActualDao().getUserByCredentialUrl(user);
    }

    @Override
    public List<UserInfo> getUserListByUserIds(List<Long> userIdList) {
        return getActualDao().getUserListByUserIds(userIdList);
    }

}