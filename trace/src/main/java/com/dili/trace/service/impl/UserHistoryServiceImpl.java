package com.dili.trace.service.impl;

import com.dili.common.exception.TraceBizException;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trace.dao.UserHistoryMapper;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.domain.UserHistory;
import com.dili.trace.domain.UserPlate;
import com.dili.trace.domain.UserTallyArea;
import com.dili.trace.dto.UserHistoryListDto;
import com.dili.trace.dto.UserHistoryStaticsDto;
import com.dili.trace.service.UserHistoryService;
import com.dili.trace.service.UserPlateService;
import com.dili.trace.service.UserService;
import com.dili.trace.service.UserTallyAreaService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class UserHistoryServiceImpl extends BaseServiceImpl<UserHistory, Long> implements UserHistoryService {
	@Autowired
	UserService userService;
	@Autowired
	UserPlateService userPlateService;
	@Autowired
	UserTallyAreaService tallyAreaService;

	private UserHistoryMapper getActualDao() {

		return (UserHistoryMapper) this.getDao();
	}

	@Override
	public EasyuiPageOutput listUserHistoryPageByExample(UserHistoryListDto dto) throws Exception {
		
		if(dto.getPage()==null) {
			dto.setPage(1);
		}
		if(dto.getRows()==null) {
			dto.setRows(10);
		}
		dto.setOffSet( ((long)dto.getPage()-1)*dto.getRows());
		long total = this.getActualDao().queryUserHistoryCount(dto);
		List<UserHistory> list = this.getActualDao().queryUserHistory(dto);
		List results = ValueProviderUtils.buildDataByProvider(dto, list);
		return new EasyuiPageOutput(total, results);
	}

	private Optional<String> andCondition(UserHistoryListDto dto) {
		List<String> strList = new ArrayList<>();
		if (dto != null && dto.getHasBusinessLicense() != null) {
			if (dto.getHasBusinessLicense() != null && dto.getHasBusinessLicense()) {
				strList.add(" (business_license_url is not null and business_license_url<>'') ");
			} else {
				strList.add(" (business_license_url is null or business_license_url='') ");
			}
		}

		if (!strList.isEmpty()) {
			return Optional.of(String.join(" and ", strList));
		}
		return Optional.empty();
	}

	@Override
	public UserHistoryStaticsDto queryStatics(UserHistoryListDto dto) throws Exception {

		return ((UserHistoryMapper) super.getDao()).queryUserHistoryStatics(dto);
	}

	@Override
	public int insertUserHistoryForNewUser(Long userId) {
		return this.buildUserHistory(userId).map(uh -> {
			return this.insertSelective(uh);

		}).orElseThrow(() -> {
			return new TraceBizException("查询用户相关数据错误");
		});
	}

	@Override
	public int insertUserHistoryForUpdateUser(Long userId) {
		return  this.buildUserHistory(userId).map(uh -> {
			return this.insertSelective(uh);

		}).orElseThrow(() -> {
			return new TraceBizException("查询用户相关数据错误");
		});
	}

	@Override
	public int insertUserHistoryForDeleteUser(Long userId) {
		return this.buildUserHistory(userId).map(uh -> {
			uh.setPlateAmount(0);
			return this.insertSelective(uh);

		}).orElseThrow(() -> {
			return new TraceBizException("查询用户相关数据错误");
		});
	}

	private Optional<UserHistory> buildUserHistory(Long userId) {
		if (userId == null) {
			return Optional.empty();
		}
		UserInfo item = this.userService.get(userId);
		if (item == null) {
			return Optional.empty();
		}
		UserTallyArea condition = DTOUtils.newDTO(UserTallyArea.class);
		condition.setUserId(userId);

		List<UserTallyArea> tallyAreaList = this.tallyAreaService.listByExample(condition);
		List<UserPlate> userPlateList = Lists.newArrayList();//this.userPlateService.findUserPlateByUserId(userId);
		return Optional.of(this.buildUserHistory(item, userPlateList, tallyAreaList));

	}

	private UserHistory buildUserHistory(UserInfo user, List<UserPlate> userPlateList, List<UserTallyArea> tallyAreaList) {
		UserHistory history = new UserHistory();
		history.setUserId(user.getId());
		history.setAddr(user.getAddr());
//		history.setBusinessLicenseUrl(user.getBusinessLicenseUrl());
		history.setCardNo(user.getCardNo());
//		history.setCardNoBackUrl(user.getCardNoBackUrl());
//		history.setCardNoFrontUrl(user.getCardNoFrontUrl());
		history.setCreated(user.getCreated());
		history.setModified(new Date());
		history.setName(user.getName());
//		history.setPassword(user.getPassword());
		history.setPhone(user.getPhone());
		history.setPlateAmount(userPlateList.size());
		history.setSalesCityId(user.getSalesCityId());
		history.setSalesCityName(user.getSalesCityName());
		history.setState(user.getState());
		history.setTallyAreaNos(user.getTallyAreaNos());
		// history.setUserId(user.getId());
		String userPlates = userPlateList.stream().map(UserPlate::getPlate).filter(StringUtils::isNotBlank)
				.collect(Collectors.joining(","));
		history.setUserPlates(userPlates);
//		history.setVersion(user.getVersion());
		history.setYn(user.getYn());
		return history;
	}
}
