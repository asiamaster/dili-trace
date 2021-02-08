package com.dili.trace.api.client;

import java.util.List;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.trace.api.input.ProductStockInput;
import com.dili.trace.api.input.ProductStockQueryDto;
import com.dili.trace.domain.ProductStock;
import com.dili.trace.domain.TradeDetail;
import com.dili.trace.enums.SaleStatusEnum;
import com.dili.trace.service.ProductStockService;
import com.dili.trace.service.BrandService;
import com.dili.trace.service.TradeDetailService;
import com.dili.trace.service.TradeRequestService;
import com.dili.trace.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

/**
 * 客户可售商品接口
 */
@SuppressWarnings("deprecation")
@Api(value = "/api/client/clientProductStockApi")
@RestController
@AppAccess(role = Role.Client,url = "",subRoles = {CustomerEnum.CharacterType.经营户, CustomerEnum.CharacterType.买家})
@RequestMapping(value = "/api/client/clientProductStockApi")
public class ClientProductStockApi {
	private static final Logger logger = LoggerFactory.getLogger(ClientTradeDetailApi.class);
	@Autowired
	private LoginSessionContext sessionContext;
	@Autowired
	ProductStockService batchStockService;
	@Autowired
	TradeDetailService tradeDetailService;
	@Autowired
	TradeRequestService tradeRequestService;
	@Autowired
	BrandService brandService;

	/**
	 * 查询用户自己的商品库存信息
	 * @param condition
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/listMyProductStock.api", method = { RequestMethod.POST })
	public BaseOutput<BasePage<ProductStock>> listMyProductStock(@RequestBody ProductStockQueryDto condition) {
		try {
			SessionData sessionData=this.sessionContext.getSessionData();
			Long userId = sessionData.getUserId();
			condition.setUserId(userId);
			condition.setSort("created");
			condition.setOrder("desc");
			condition.setMinTradeDetailNum(1);
			BasePage<ProductStock> page = this.batchStockService.listPageByExample(condition);
			return BaseOutput.success().setData(page);
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("查询出错");
		}

	}

	/**
	 * 查询卖家的库存信息
	 */
	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/listSellersProductStock.api", method = { RequestMethod.POST })
	public BaseOutput<BasePage<ProductStock>> listSellersProductStock(@RequestBody ProductStockQueryDto condition) {
		if (condition == null || condition.getUserId() == null) {
			return BaseOutput.failure("参数错误");
		}
		try {
			SessionData sessionData=this.sessionContext.getSessionData();
			Long userId = sessionData.getUserId();

			condition.setSort("created");
			condition.setOrder("desc");
			condition.setMinTradeDetailNum(1);
			BasePage<ProductStock> page = this.batchStockService.listPageByExample(condition);
			return BaseOutput.success().setData(page);
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("查询出错");
		}
	}

	/**
	 * 通过库存ID获得批次列表
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/listTradeDetailForSaleByProductStoreId.api", method = { RequestMethod.POST })
	public BaseOutput<List<TradeDetail>> listTradeDetailForSaleByProductStoreId(@RequestBody ProductStockInput inputDto) {

		if (inputDto == null || inputDto.getProductStockId() == null) {
			return BaseOutput.failure("参数错误");
		}
		try {
			SessionData sessionData=this.sessionContext.getSessionData();
			Long userId = sessionData.getUserId();
			ProductStock batchStockItem = this.batchStockService.get(inputDto.getProductStockId());
			if (batchStockItem == null) {
				return BaseOutput.failure("数据不存在");
			}
			TradeDetail tradeDetailQuery = new TradeDetail();
			tradeDetailQuery.setSaleStatus(SaleStatusEnum.FOR_SALE.getCode());
			tradeDetailQuery.setProductStockId(inputDto.getProductStockId());
			tradeDetailQuery.setBuyerId(userId);
			List<TradeDetail> tradeDetailList = this.tradeDetailService.listByExample(tradeDetailQuery);
			return BaseOutput.success().setData(tradeDetailList);
		} catch (TraceBizException e) {
			return BaseOutput.failure(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return BaseOutput.failure("服务端出错");
		}

	}

	

}