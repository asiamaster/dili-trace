package com.dili.trace.service;

import com.dili.ss.base.BaseService;
import com.dili.ss.domain.BasePage;
import com.dili.trace.api.input.CreateRegisterBillInputDto;
import com.dili.trace.api.input.RegisterBillApiInputDto;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterBillDto;
import com.dili.trace.dto.RegisterBillOutputDto;
import com.dili.trace.enums.CreatorRoleEnum;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 由MyBatis Generator工具自动生成 This file was generated on 2019-07-26 09:20:34.
 */
public interface RegisterBillService extends BaseService<RegisterBill, Long> {
    /**
     * 通过ID悲观锁定数据
     */
    public Optional<RegisterBill> selectByIdForUpdate(Long id);

    /**
     * 查询并检查报备单子是否删除
     */
    public Optional<RegisterBill> getAndCheckById(Long billId);

    /**
     * 查找第一条由当前用户创建的待审核报备单
     *
     * @param dto
     * @return
     * @throws Exception
     */
    public RegisterBill findFirstWaitAuditRegisterBillCreateByCurrentUser(RegisterBillDto dto) throws Exception;

    /**
     * 分页查询
     *
     * @param dto
     * @return
     * @throws Exception
     */
    public String listPage(RegisterBillDto dto) throws Exception;

    /**
     * 通过登记单查找
     *
     * @param code
     * @return
     */
    RegisterBill findByCode(String code);

    /**
     * 创建单个报备单
     *
     * @param registerBill
     * @return
     */
    Long createRegisterBill(RegisterBill registerBill, Optional<OperatorUser> operatorUser);

    /**
     * 修改单个报备单
     *
     * @param
     * @return
     */
    public Long doEdit(RegisterBill registerBill, List<ImageCert> imageCertList, Optional<OperatorUser> operatorUser);

    /**
     * 进门前审核
     *
     * @param input
     * @param operatorUser
     * @return
     */
    public Long doVerifyBeforeCheckIn(RegisterBill input, Optional<OperatorUser> operatorUser);

    /**
     * 进门后审核
     *
     * @param billId
     * @param operatorUser
     * @return
     */
    public Long doVerifyAfterCheckIn(Long billId, Integer verifyStatus, String reason, Optional<OperatorUser> operatorUser);

    /**
     * 创建多个报备单
     *
     * @param registerBills
     * @param customerId
     * @param operatorUser
     * @return
     */
    public List<Long> createBillList(List<CreateRegisterBillInputDto> registerBills, Long customerId,
                                     Optional<OperatorUser> operatorUser,  CreatorRoleEnum creatorRoleEnum);

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    public BasePage<RegisterBill> listPageBeforeCheckinVerifyBill(RegisterBillDto query);

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    public BasePage<RegisterBill> listPageAfterCheckinVerifyBill(RegisterBillDto query);

    /**
     * 统计不同审核状态报备单数据
     *
     * @param query
     * @return
     */
    public List<VerifyStatusCountOutputDto> countByVerifyStatuseBeforeCheckin(RegisterBillDto query);

    /**
     * 统计不同审核状态报备单数据
     *
     * @param query
     * @return
     */
    public List<VerifyStatusCountOutputDto> countByVerifyStatuseAfterCheckin(RegisterBillDto query);

    /**
     * 根据用户最新报备单审核状态更新颜色码
     *
     * @param userId
     */
    public void updateUserQrStatusByUserId(Long billId, Long userId);

    /**
     * 根据报备单数量更新用户状态到黑码
     *
     * @param createdStart
     * @param createdEnd
     */
    public void updateAllUserQrStatusByRegisterBillNum(Date createdStart, Date createdEnd);

    /**
     * 查询详情
     * @param inputDto
     * @return
     */

    public RegisterBillOutputDto viewTradeDetailBill(RegisterBillApiInputDto inputDto);

    /**
     * 删除报备单
     *
     * @param userId
     * @param operatorUser
     * @return
     */
    public Long doDelete(Long billId, Long userId, Optional<OperatorUser> operatorUser);

    /**
     * 查询进门数据
     * @param query
     * @return
     */
    public Map<Integer, Map<String, List<RegisterBill>>> listPageCheckInData(RegisterBillDto query);

	/**
	 * 创建多个进门登记单
	 *
	 * @param registerBills
	 * @param customerId
	 * @param operatorUser
	 * @param marketId
	 * @return
	 */
	public List<RegisterBill> createRegisterFormBillList(List<CreateRegisterBillInputDto> registerBills, Long customerId,
											 Optional<OperatorUser> operatorUser, Long marketId);

	/**
	 * 创建单个报备单
	 *
	 * @param registerBill
	 * @param imageCertList
	 * @param operatorUser
	 * @return
	 */
	RegisterBill createRegisterFormBill(RegisterBill registerBill, List<ImageCert> imageCertList, Optional<OperatorUser> operatorUser);

    /**
     * 修改单个进门登记单
     *
     * @param registerBill
     * @param imageCertList
     * @param operatorUser
     * @return
     */
    public Long doEditFormBill(RegisterBill registerBill, List<ImageCert> imageCertList, Optional<OperatorUser> operatorUser);

    /**
     * 进门登记单审核(通过/进门/不通过/退回/进门待检)
     *
     * @param input
     * @param operatorUser
     * @return
     */
    public Long doVerifyFormCheckIn(RegisterBill input, Optional<OperatorUser> operatorUser);

    /**
     * @return
     * @Description 给小程序提供查询接口
     * * @Date 2020/10/21 16:04
     */
    public BasePage<RegisterBill> listPageApi(RegisterBillDto input);

    /**
     * 删除进门登记单
     *
     * @param dto
     * @param userId
     * @param operatorUser
     * @return
     */
    public Long doDelete(CreateRegisterBillInputDto dto, Long userId, Optional<OperatorUser> operatorUser);

    /**
     * 统计不同审核状态进门登记单数据
     *
     * @param query
     * @return
     */
    public List<VerifyStatusCountOutputDto> countBillsByVerifyStatus(RegisterBillDto query);

    /**
     * 查询用户最近操作数据
     *
     * @param input
     * @return
     * @throws Exception
     */
    public RegisterBill findHighLightBill(RegisterBillDto input) throws Exception;
}