package com.dili.trace.service;

import com.dili.ss.base.BaseService;
import com.dili.ss.domain.BasePage;
import com.dili.trace.api.input.CreateRegisterHeadInputDto;
import com.dili.trace.domain.ImageCert;
import com.dili.trace.domain.RegisterHead;
import com.dili.trace.dto.OperatorUser;
import com.dili.trace.dto.RegisterHeadDto;

import java.util.List;
import java.util.Optional;

/**
 * 进门主台账单接口
 *
 * @author Lily
 */
public interface RegisterHeadService extends BaseService<RegisterHead, Long> {

	/**
	 * 主台账列表查询
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	public String listPage(RegisterHeadDto dto) throws Exception;

	/**
	 * 创建多个进门主台账单
	 *
	 * @param registerHeads
	 * @param userId
	 * @param operatorUser
	 * @return
	 */
	public List<Long> createRegisterHeadList(List<CreateRegisterHeadInputDto> registerHeads,
											 Optional<OperatorUser> operatorUser,Long marketId);

	/**
	 * 创建单个进门主台账单
	 *
	 * @param registerHead
	 * @param imageCertList
	 * @param operatorUser
	 * @return
	 */
	Long createRegisterHead(RegisterHead registerHead, List<ImageCert> imageCertList, Optional<OperatorUser> operatorUser);

	/**
	 * 修改单个进门主台账单
	 *
	 * @param registerHead
	 * @param imageCertList
	 * @param operatorUser
	 * @return
	 */
	public Long doEdit(RegisterHead registerHead, List<ImageCert> imageCertList, Optional<OperatorUser> operatorUser);

	/**
	 * 查询并检查报备单子是否删除
	 *
	 * @param id
	 */
	public Optional<RegisterHead> getAndCheckById(Long id);

	/**
	 * 作废进门主台账单
	 * @param dto
	 * @param userId
	 * @param operatorUser
	 * @return
	 */
	public Long doDelete(CreateRegisterHeadInputDto dto, Long userId, Optional<OperatorUser> operatorUser);

	/**
	 * 启用/关闭进门主台账单
	 * @param dto
	 * @param userId
	 * @param operatorUser
	 * @return
	 */
	public Long doUpdateActive(CreateRegisterHeadInputDto dto, Long userId, Optional<OperatorUser> operatorUser);



	/**
	 *
	 * @Author guzman.liu
	 * @Description
	 * 给小程序提供查询 主台账的接口
	 * @Date 2020/10/20 16:04
	 * @return
	 */
	public BasePage<RegisterHead> listPageApi(RegisterHeadDto input);


	/**
	 * 根据code查询
	 * @param registerHeadCode
	 * @return
	 */
	public Optional<RegisterHead>findByCode(String registerHeadCode);

}