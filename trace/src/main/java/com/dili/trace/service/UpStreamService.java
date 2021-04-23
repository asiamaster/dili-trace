package com.dili.trace.service;

import java.util.*;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.dili.common.exception.TraceBizException;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.ss.dto.IDTO;
import com.dili.trace.dao.UpStreamMapper;
import com.dili.trace.domain.RUserUpstream;
import com.dili.trace.domain.UpStream;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.UpStreamDto;
import com.dili.trace.enums.UserFlagEnum;

import com.dili.trace.rpc.service.CustomerRpcService;
import com.google.common.collect.Lists;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import tk.mybatis.mapper.entity.Example;

/**
 * 上下游业务类
 */
@Service
public class UpStreamService extends BaseServiceImpl<UpStream, Long> {
	/**
	 * 下游企业标志 20
	 */
	public UpStreamMapper getActualDao() {
		return (UpStreamMapper) getDao();
	}

	@Autowired
	RUserUpStreamService rUserUpStreamService;

	@Autowired
	MarketService marketService;
	@Autowired
	CustomerRpcService customerRpcService;

	/**
	 * license 最大长度
	 */
	private static final int MAX_LICENSE_LENGTH = 20;

	/**
	 * 分页查询上游信息
	 */
	public BasePage<UpStream> listPageUpStream(UpStreamDto query) {

		String userIdListStr=StreamEx.ofNullable(query.getUserIds()).flatCollection(Function.identity()).append(query.getUserId()).nonNull().map(String::valueOf).joining(",");
		if(StringUtils.isBlank(userIdListStr)){
			BasePage<UpStream> result = new BasePage<>();

			result.setPage(1);
			result.setRows(0);
			result.setTotalItem(0L);
			result.setTotalPage(1);
			result.setStartIndex(1L);
			return result;
		}
		query.setMetadata(IDTO.AND_CONDITION_EXPR,
				"id in(select upstream_id from r_user_upstream where user_id in(" + userIdListStr + ") )");
		BasePage<UpStream> page = this.listPageByExample(query);
		return page;
	}



	/**
	 * 删除用户上游关系
	 * 
	 * @param userId
	 * @param upstreamId
	 * @return
	 */
	public int deleteUpstream(Long userId, Long upstreamId) {

		if (userId == null || upstreamId == null) {
			throw new TraceBizException("参数错误");
		}
		RUserUpstream rUserUpstream = new RUserUpstream();
		rUserUpstream.setUserId(userId);
		rUserUpstream.setUpstreamId(upstreamId);
		RUserUpstream item = this.rUserUpStreamService.listByExample(rUserUpstream).stream().findFirst().orElse(null);
		if (item != null) {
			int value = this.rUserUpStreamService.delete(item.getId());
			return value;
		}
		return 0;
	}

	/**
	 * 新增上游
	 * 
	 * @param input
	 * @return
	 */
	@Transactional
	public BaseOutput addUpstream(UpStreamDto input, OperatorUser operatorUser,Boolean ... noticeException) {
		// 增加下游企业时创建用户
		this.addUserForDownStream(input).ifPresent(sourceuserId -> {
			input.setSourceUserId(sourceuserId);
		});
		try {

			// 校验数据
			if (StringUtils.isNoneBlank(input.getLicense()) && input.getLicense().length() > MAX_LICENSE_LENGTH) {
				throw new TraceBizException("统一信用代码不超过20位！");
			}
			if (StringUtils.isNoneBlank(input.getName()) && input.getName().length() > MAX_LICENSE_LENGTH) {
				throw new TraceBizException("企业(个人)名称不超过20位！");
			}
			if (StringUtils.isNoneBlank(input.getLegalPerson()) && input.getLegalPerson().length() > MAX_LICENSE_LENGTH) {
				throw new TraceBizException("法人姓名不超过20位！");
			}


			// 校验手机号是否重复
			validateDuplicateTelPhone(input).ifPresent((us)->{
				input.setId(us.getId());
			});

			if (input.getId() == null){
				insertSelective(input);
			}
			addUpstreamUsers(input, operatorUser);
		} catch (DuplicateKeyException e) {
			if (noticeException != null && noticeException[0]){
				throw new TraceBizException("已存在手机号:" + input.getTelphone() + "的企业/个人");
			}
		}

		return BaseOutput.successData(input.getId());
	}

	/**
	 * 验证身份证号是否唯一
	 *
	 * @param marketId
	 * @param idCard
	 */
	private Optional<UpStream> validateDuplicateIdCardNo(Long marketId, String idCard, Integer upOrDown) {
		if(StringUtils.isBlank(idCard)){
			LOGGER.info("无需校验身份证号");
			return Optional.empty();
		}
		UpStreamDto dto = new UpStreamDto();
		dto.setIdCard(idCard);
		dto.setMarketId(marketId);
		dto.setUpORdown(upOrDown);
		List<UpStream> streamList = getActualDao().select(dto);
		return StreamEx.of(streamList).nonNull().findFirst();

	}

	/**
	 * 验证手机号是否唯一
	 *
	 */
	private Optional<UpStream> validateDuplicateTelPhone(UpStream input) {
		if(StringUtils.isBlank(input.getTelphone())){
			LOGGER.info("无需校验手机号");
			return Optional.empty();
		}
		UpStreamDto dto = new UpStreamDto();
		dto.setTelphone(input.getTelphone());
		dto.setMarketId(input.getMarketId());
//		dto.setUpORdown(input.getUpORdown());
		dto.setName(input.getName());
		List<UpStream> streamList = getActualDao().select(dto);
		return StreamEx.of(streamList).nonNull().findFirst();

	}

	/**
	 * 增加下游企业时创建用户
	 * @Date 2020/11/26 18:04
	 * @Param
	 * @return
	 */
	public Optional<Long> addUserForDownStream(UpStreamDto upStreamDto) {

		if (UserFlagEnum.DOWN.equalsToCode(upStreamDto.getUpORdown())) {
//			Long sourceuserId = doAddUser(upStreamDto);
//			return Optional.ofNullable(sourceuserId);
		}

		return Optional.empty();
	}

	/**
	 * 新增用户
	 * @param upStreamDto
	 * @return
	 */
//	private Long doAddUser(UpStreamDto upStreamDto) {
//		List<User> users = userService.getUserByExistsAccount(upStreamDto.getTelphone());
//		boolean existsFlag = !CollUtil.isEmpty(users);
//		if (!existsFlag) {
//			JSONObject object = new JSONObject();
//			object.put("phone", upStreamDto.getTelphone());
//			object.put("name", upStreamDto.getName());
//			if (Objects.nonNull(upStreamDto.getMarketId())) {
//				object.put("marketId", upStreamDto.getMarketId());
//
//				marketService.getMarketById(upStreamDto.getMarketId()).ifPresent(e ->{
//					object.put("marketName", e.getName());
//				});
//			}
//			if (upStreamDto.getUpstreamType() == UpStreamTypeEnum.CORPORATE.getCode()) {// 企业
//				object.put("legal_person", upStreamDto.getLegalPerson());
//				object.put("license", upStreamDto.getLicense());
//			}
//
//			User user = JSONObject.parseObject(object.toJSONString(), User.class);
////			userService.register(user, false);
//			return user.getId();
//		}
//		return users.get(0).getId();
//
//	}

	/**
	 * 添加上游的所有业户信息（绑定）
	 * 
	 * @param upStreamDto
	 */
	public void addUpstreamUsers(UpStreamDto upStreamDto, OperatorUser operatorUser) {
		if (CollectionUtils.isNotEmpty(upStreamDto.getUserIds())) {
			List<RUserUpstream> rUserUpstreams = new ArrayList<>();
			upStreamDto.getUserIds().forEach(userId -> {
				if (upStreamDto.getId() == null){
					return;
				}
				String userName=this.customerRpcService.findCustByIdOrEx(userId).getName();
				RUserUpstream rUserUpstream = new RUserUpstream();
				rUserUpstream.setUpstreamId(upStreamDto.getId());
				rUserUpstream.setUserId(userId);
				rUserUpstream.setUserName(userName);
				rUserUpstream.setCreated(new Date());
				rUserUpstream.setModified(new Date());
				if (CollUtil.isEmpty(rUserUpStreamService.listByExample(rUserUpstream))){
					rUserUpstream.setOperatorId(operatorUser.getId());
					rUserUpstream.setOperatorName(operatorUser.getName());
					rUserUpstreams.add(rUserUpstream);
				}
//				rUserUpstream.setCreated(new Date());
//				rUserUpstream.setModified(new Date());
			});
			if (rUserUpstreams.size() > 0){
				rUserUpStreamService.batchInsert(rUserUpstreams);
			}

		}

	}

	/**
	 * 修改上游
	 * 
	 * @param upStreamDto
	 * @return
	 */
	@Transactional
	public BaseOutput updateUpstream(UpStreamDto upStreamDto, OperatorUser operatorUser) {
		updateSelective(upStreamDto);

		RUserUpstream rUserUpstreamDelCondition = new RUserUpstream();
		rUserUpstreamDelCondition.setUpstreamId(upStreamDto.getId());

		List<Long> changedUserId = this.rUserUpStreamService.list(rUserUpstreamDelCondition).stream()
				.map(RUserUpstream::getUserId).collect(Collectors.toList());

		rUserUpStreamService.deleteByExample(rUserUpstreamDelCondition);
		addUpstreamUsers(upStreamDto, operatorUser);

		return BaseOutput.success();
	}

	/**
	 * 批量查询上游对应的业户集合
	 * 
	 * @param upstreamIds
	 * @return Map｛upstreamId，userNames｝
	 */
	public List<Map<String, Object>> queryUsersByUpstreamIds(List<Long> upstreamIds) {
		return getActualDao().queryUsersByUpstreamIds(upstreamIds);
	}

	/**
	 *
	 * @param userId
	 * @return
	 */
	public List<UpStream> queryUpStreamByUserId(Long userId) {
		if (userId == null) {
			return new ArrayList<>();
		}
		RUserUpstream rUpstreamQuery = new RUserUpstream();
		rUpstreamQuery.setUserId(userId);
		List<Long> upStreamIdList = this.rUserUpStreamService.listByExample(rUpstreamQuery).stream()
				.map(RUserUpstream::getUpstreamId).collect(Collectors.toList());
		if (upStreamIdList.isEmpty()) {
			return new ArrayList<>();
		}
		Example example = new Example(UpStream.class);
		example.and().andIn("id", upStreamIdList);
		return this.getActualDao().selectByExample(example);
	}

	/**
	 * 根据条件查询上游
	 * @param userId
	 * @param userFlagEnum
	 * @param sourceUserId
	 * @return
	 */
	public List<UpStream> queryUpStreamByUserIdAndFlag(Long userId,UserFlagEnum userFlagEnum,Long sourceUserId) {
		if (userId == null) {
			return new ArrayList<>();
		}
		RUserUpstream rUpstreamQuery = new RUserUpstream();
		rUpstreamQuery.setUserId(userId);
		List<Long> upStreamIdList = this.rUserUpStreamService.listByExample(rUpstreamQuery).stream()
				.map(RUserUpstream::getUpstreamId).collect(Collectors.toList());
		if (upStreamIdList.isEmpty()) {
			return new ArrayList<>();
		}
		Example example = new Example(UpStream.class);
		example.and().andIn("id", upStreamIdList).andEqualTo("upORdown", userFlagEnum.getCode()).andEqualTo("sourceUserId", sourceUserId);
		return this.getActualDao().selectByExample(example);

	}

	/**
	 * 根据条件查询上游
	 * @param sourceUserId
	 * @return
	 */
	public UpStream queryUpStreamBySourceUserId(Long sourceUserId) {
		if (sourceUserId == null) {
			return null;
		}
		UpStream query = new UpStream();
		query.setSourceUserId(sourceUserId);
		UpStream upStream = this.listByExample(query).stream().findFirst().orElse(null);
		return upStream;

	}

	/**
	 * 根据关键字查询经营户上游
	 * @param userId 经营户主键
	 * @param keyword 关键字
	 * @return
	 */
	public List<UpStream> queryUpStreamByKeyword(Long userId, String keyword) {
		if (userId == null || StringUtils.isBlank(keyword)) {
			return new ArrayList<>();
		}
		RUserUpstream rUpstreamQuery = new RUserUpstream();
		rUpstreamQuery.setUserId(userId);
		List<Long> upStreamIdList = this.rUserUpStreamService.listByExample(rUpstreamQuery).stream()
				.map(RUserUpstream::getUpstreamId).collect(Collectors.toList());
		if (upStreamIdList.isEmpty()) {
			return new ArrayList<>();
		}
		Example example = new Example(UpStream.class);
		example.and().andIn("id", upStreamIdList);
		if(StringUtils.isNotBlank(keyword)){
			example.and().andLike("name", "%" + keyword.trim() + "%");
		}

		return this.getActualDao().selectByExample(example);

	}

}