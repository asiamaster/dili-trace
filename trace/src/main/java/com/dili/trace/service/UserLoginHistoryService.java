package com.dili.trace.service;

import java.util.Date;
import java.util.List;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BasePage;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trace.dao.UserLoginHistoryMapper;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.domain.UserLoginHistory;
import com.dili.trace.dto.BillReportDto;
import com.dili.trace.dto.UserLoginHistoryQueryDto;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户登录信息
 * @author admin
 */
@Service
@Transactional
public class UserLoginHistoryService extends BaseServiceImpl<UserLoginHistory, Long> {
    /**
     * createLoginHistory
     * @param user
     * @return 
     */
    public UserLoginHistory createLoginHistory(UserInfo user) {
        UserLoginHistory loginHistory = new UserLoginHistory();

        loginHistory.setUserId(user.getId());
        loginHistory.setUserName(user.getName());
        loginHistory.setCreated(new Date());
        loginHistory.setModified(new Date());
        this.insertSelective(loginHistory);
        return loginHistory;
    }

    /**
     * listEasyuiPageByExample
     * @param domain
     * @return
     * @throws Exception 
     */
    public EasyuiPageOutput listEasyuiPageByExample(UserLoginHistoryQueryDto domain) throws Exception {
        UserLoginHistoryMapper dao = (UserLoginHistoryMapper) this.getDao();

        PageHelper.startPage(domain.getPage(), domain.getRows());
        List<BillReportDto> list = dao.queryUserLoginHistory(domain);
        ;
        Page<BillReportDto> page = (Page) list;
        BasePage<BillReportDto> result = new BasePage<BillReportDto>();
        result.setDatas(list);
        result.setPage(page.getPageNum());
        result.setRows(page.getPageSize());
        result.setTotalItem(page.getTotal());
        result.setTotalPage(page.getPages());
        result.setStartIndex(page.getStartRow());

        long total = list instanceof Page ? ((Page) list).getTotal() : list.size();
        List results = ValueProviderUtils.buildDataByProvider(domain, list);
        return new EasyuiPageOutput(total, results);
    }
}