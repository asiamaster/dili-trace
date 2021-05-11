package com.dili.trace.api.client;

import java.util.List;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.api.input.BrandInputDto;
import com.dili.trace.api.output.BrandOutputDto;
import com.dili.trace.domain.Brand;
import com.dili.trace.service.BrandService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;

/**
 * (经营户)品牌接口
 */
@RestController
@RequestMapping(value = "/api/client/clientBrand")
@Api(value = "/api/client/clientBrand", description = "品牌相关接口")
@AppAccess(role = Role.Client,url = "",subRoles = {CustomerEnum.CharacterType.经营户, CustomerEnum.CharacterType.买家})
public class ClientBrandApi {
	private static final Logger logger = LoggerFactory.getLogger(ClientBrandApi.class);
	@Autowired
	private LoginSessionContext sessionContext;
	@Autowired
	BrandService brandService;

	/**
	 * 获取品牌列表
	 * @param inputDto
	 * @return
	 */
	@ApiOperation(value = "获取品牌列表")
	@ApiImplicitParam(paramType = "body", name = "RegisterBill", dataType = "RegisterBill", value = "获取登记单列表")
	@RequestMapping(value = "/listPage.api", method = RequestMethod.POST)
	public BaseOutput<List<BrandOutputDto>> listPage(@RequestBody BrandInputDto inputDto) {
		try {
			SessionData sessionData=this.sessionContext.getSessionData();
			Long userId = sessionData.getUserId();
			logger.info("获取品牌列表 操作用户:{}", userId);
			// inputDto.setUserId(userId);
			if (StringUtils.isBlank(inputDto.getOrder())) {
				inputDto.setOrder("desc");
				inputDto.setSort("created");
			}
			if (inputDto.getMarketId() == null) {
				inputDto.setMarketId(sessionData.getMarketId());
			}
			List<BrandOutputDto>list=StreamEx.of(brandService.listByExample(inputDto)).map(BrandOutputDto::build).toList();
			return BaseOutput.success().setData(list);
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("查询数据出错");
		}

	}

	/**
	 * 创建品牌列表
	 * @param inputDto
	 * @return
	 */

	@ApiOperation(value = "创建品牌列表")
	@ApiImplicitParam(paramType = "body", name = "RegisterBill", dataType = "RegisterBill", value = "获取登记单列表")
	@RequestMapping(value = "/createBrand.api", method = RequestMethod.POST)
	public BaseOutput createBrand(@RequestBody BrandInputDto inputDto) {
		if(inputDto==null){
			return BaseOutput.failure("参数错误");
		}
		if(StringUtils.isBlank(inputDto.getBrandName())){
			return BaseOutput.failure("品牌名称不能为空");
		}
		try {
			SessionData sessionData=this.sessionContext.getSessionData();
			Long userId = sessionData.getUserId();

			if (inputDto.getUserId() != null) {
				userId = inputDto.getUserId();
			}
			Brand query=new Brand();
			// query.setUserId(userId);
			query.setBrandName(StringUtils.trimToNull(inputDto.getBrandName()));
			if (inputDto.getMarketId() == null) {
				query.setMarketId(sessionData.getMarketId());
			} else {
				query.setMarketId(inputDto.getMarketId());
			}
			boolean exists=this.brandService.listByExample(query).size()>0;
			if(exists){
				return BaseOutput.failure("已经存在");
			}
			this.brandService.createOrUpdateBrand(StringUtils.trimToNull(inputDto.getBrandName()), userId, query.getMarketId());
			return BaseOutput.success();
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("查询数据出错");
		}

	}

}
