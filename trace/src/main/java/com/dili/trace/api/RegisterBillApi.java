package com.dili.trace.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dili.assets.sdk.dto.CusCategoryDTO;
import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.commons.glossary.YesOrNoEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.trace.api.input.RegisterBillApiInputDto;
import com.dili.trace.api.input.RegisterBillQueryInputDto;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.domain.RegisterTallyAreaNo;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.enums.BillTypeEnum;
import com.dili.trace.service.AssetsRpcService;
import com.dili.trace.service.RegisterBillService;
import com.dili.trace.service.RegisterTallyAreaNoService;
import com.dili.trace.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author
 */
@RestController
@RequestMapping(value = "/api/registerBillApi")
@Api(value = "/api/registerBillApi", description = "登记单相关接口")
@AppAccess(role = Role.ANY)
public class RegisterBillApi {
    private static final Logger logger = LoggerFactory.getLogger(RegisterBillApi.class);

    @Autowired
    private RegisterBillService registerBillService;
    @Autowired
    private LoginSessionContext sessionContext;
    @Autowired
    private UserService userService;
    @Autowired
    RegisterTallyAreaNoService registerTallyAreaNoService;

    /**
     * 通过登记单ID获取登记单详细信息
     *
     * @param inputDto
     * @return
     */
    @ApiOperation(value = "通过登记单ID获取登记单详细信息")
    @RequestMapping(value = "/viewTradeDetailBill.api", method = RequestMethod.POST)
    public BaseOutput<RegisterBillOutputDto> viewTradeDetailBill(@RequestBody RegisterBillApiInputDto inputDto) {
        if (inputDto == null || (inputDto.getBillId() == null && inputDto.getTradeDetailId() == null)) {
            return BaseOutput.failure("参数错误");
        }

        logger.info("获取登记单详细信息->marketId:{},billId:{},tradeDetailId:{}", inputDto.getMarketId(), inputDto.getBillId(), inputDto.getTradeDetailId());
        try {
            SessionData sessionData=this.sessionContext.getSessionData();
            Long userId=sessionData.getUserId();
            if (userId == null) {
                return BaseOutput.failure("你还未登录");
            }
            RegisterBillOutputDto outputdto = this.registerBillService.viewTradeDetailBill(inputDto);
            List<RegisterTallyAreaNo> arrivalTallynos = this.registerTallyAreaNoService.findTallyAreaNoByBillIdAndType(outputdto.getBillId(), BillTypeEnum.REGISTER_BILL);
            outputdto.setArrivalTallynos(StreamEx.of(arrivalTallynos).map(RegisterTallyAreaNo::getTallyareaNo).toList());

            String data = JSON.toJSONString(outputdto, SerializerFeature.DisableCircularReferenceDetect);
            return BaseOutput.success().setData(JSON.parse(data));

        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }

    /**
     * 获取报备单列表
     *
     * @param inputDto
     * @param request
     * @return
     */
    @ApiOperation(value = "获取报备单列表")
    @RequestMapping(value = "/queryBillNo", method = RequestMethod.POST)
//    @InterceptConfiguration(loginRequired = false, signRequired = true, signValue = "cGFzczk4NzYyMDIw")
    public BaseOutput<RegisterBillOutputDto> queryBillNo(@RequestBody RegisterBillQueryInputDto inputDto, HttpServletRequest request) {
        // 校验经营户卡号为必填参数
        boolean isValidate = inputDto == null || inputDto.getSupplierId() == null;
        if (isValidate) {
            String result = "参数错误";
            if (null == inputDto.getSupplierId()) {
                result = "参数：supplierId不能为空";
            }
            return BaseOutput.failure(result);
        }

        logger.info("获取报备单列表->billId:{},SupplierId:{}", inputDto.getBillId(), inputDto.getSupplierId());
        try {
            // 根据经营户卡号查询经营户
            UserInfo user = new UserInfo();
            user.setThirdPartyCode(inputDto.getSupplierId());
            user.setYn(YesOrNoEnum.YES.getCode());
            List<UserInfo> userList = userService.listByExample(user);
            if (CollectionUtils.isEmpty(userList)) {
                return BaseOutput.failure("supplierId没有匹配的经营户");
            }
            Long userId = null;
            Integer row = 10;
            Integer noDelete = 0;
            String billNo = inputDto.getBillId();
            String goodsCode = inputDto.getCategoryCode();
            Long productId = null;
            if (CollectionUtils.isNotEmpty(userList)) {
                userId = userList.get(0).getId();
            }
            if (StringUtils.isNotBlank(goodsCode)) {
//                productId = getCategoryByGoodsCode(goodsCode);
            }

            // 根据经营户和商品id查询报备单
            RegisterBill query = new RegisterBill();
            query.setUserId(userId);
            query.setIsDeleted(noDelete);
            if (null != productId) {
                query.setProductId(productId);
            }
            if (StringUtils.isNotBlank(billNo)) {
                query.setMetadata(IDTO.AND_CONDITION_EXPR, " code like '%" + billNo + "%'");
            } else {
                query.setOrder("desc");
                query.setSort("created");
                query.setRows(row);
            }
            List<RegisterBill> billList = registerBillService.listByExample(query);

            // 返回报备单编号列表
            List<RegisterBillQueryInputDto> billNos = StreamEx.of(billList).nonNull().map(b -> {
                RegisterBillQueryInputDto inp = new RegisterBillQueryInputDto();
                StringBuffer sb = new StringBuffer(b.getCode());
                sb.append(";").append(b.getProductName());
                if (StringUtils.isNotBlank(b.getOriginName())) {
                    sb.append(";").append(b.getOriginName().split(",")[0]);
                }
                inp.setBillId(sb.toString());
                return inp;
            }).collect(Collectors.toList());

            return BaseOutput.success().setData(billNos);

        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return BaseOutput.failure("查询数据出错");
        }

    }

    /* *//**
     * 返回商品码对应的第三级品种ID
     *
     * @param goodsCode 商品码
     * @return
     *//*
    private Long getCategoryByGoodsCode(String goodsCode) {
        Category queCate = new Category();
        queCate.setCode(goodsCode);
        List<Category> categories = categoryService.listByExample(queCate);
        if (CollectionUtils.isNotEmpty(categories)) {
            Category resCategory = categories.get(0);
            return getParentCategoryId(resCategory);
        } else {
            throw new TraceBizException("商品码[" + goodsCode + "]不存在，请联系管理员检查商品主数据！");
        }
    }*/
    /*

     */
/**
 * 查询目标商品的三级商品
 * @param resCategory
 * @return
 *//*

    private Long getParentCategoryId(CusCategoryDTO resCategory) {
        if (null == resCategory) {
            return null;
        }
        final int targetLevel = 3;
        // 商品本身小于等于三级，直接返回
        if (resCategory.getLevel() <= targetLevel) {
            return resCategory.getId();
        }

        String recursionCode = resCategory.getCode();
        while (recursionCode.length() > 1) {
            // 无锡斯坦有些商品主数据级别不完整，直接根据商品码截取形式查询上级
            String parentCode = recursionCode.substring(0, recursionCode.length() - 1);
            CusCategoryDTO condition = new CusCategoryDTO();
            condition.setCode(parentCode);
            List<CusCategoryDTO> parentCategories = categoryService.list(condition);
            if (CollectionUtils.isEmpty(parentCategories)) {
                // 没有找到父级商品，继续截取。处理级别中断问题。
                recursionCode = parentCode;
                continue;
            }
            if (parentCategories.size() > 1) {
                logger.error("商品码["+ parentCode +"]重复!!!");
            }
            CusCategoryDTO parentCategory = parentCategories.get(0);
            if (parentCategory.getLevel() > targetLevel) {
                // 级别大于3，继续找父级
                recursionCode = parentCode;
                continue;
            } else if (parentCategory.getLevel() == targetLevel) {
                // 找到三级商品，循环结束
                return parentCategory.getId();
            }
            recursionCode = parentCode;
        }
        throw new TraceBizException("商品码[" + resCategory.getCode() + "]的三级商品不存在，请联系管理员检查商品主数据！");
    }
*/

}
