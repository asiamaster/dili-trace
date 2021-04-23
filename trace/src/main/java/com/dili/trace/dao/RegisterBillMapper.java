package com.dili.trace.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.dili.trace.api.input.UserQueryDto;
import com.dili.trace.domain.DetectRequest;
import com.dili.trace.dto.GroupByProductReportDto;
import com.dili.trace.dto.MatchDetectParam;
import com.dili.trace.dto.RegisterBillReportQueryDto;
import com.dili.trace.dto.RegisterBillStaticsDto;
import com.dili.ss.base.MyMapper;
import com.dili.trace.api.output.VerifyStatusCountOutputDto;
import com.dili.trace.domain.RegisterBill;
import com.dili.trace.dto.*;
import com.dili.trace.dto.thirdparty.report.RegionCountInfo;
import com.dili.trace.dto.thirdparty.report.ReportCountDto;

import com.dili.trace.dto.thirdparty.report.ReportRegisterBillDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RegisterBillMapper extends MyMapper<RegisterBill> {
    /**
     * 统计不同审核状态报备单数据
     * 
     * @param query
     * @return
     */
    public List<VerifyStatusCountOutputDto> countByVerifyStatus(RegisterBill query);


    /**
     * 通过ID悲观锁定数据
     */
    @Select("select * from register_bill where id = #{id} for update")
    public Optional<RegisterBill> selectByIdForUpdate(Long id);

    //    RegisterBill selectByIdForUpdate(Long id);

    public List<TraceReportDto> selectBillReportData(TraceReportQueryDto query);

    /**
     * 查询品种产地排名统计数据
     * 
     * @param billDto
     * @return
     */
    public List<RegionCountInfo> selectRegionCountData(RegisterBillDto billDto);

    /**
     * 报备检测数据统计
     */
    public List<ReportCountDto> selectReportCountData(RegisterBillDto billDto);

    /**
     * 报备检测数据统计
     */
    public List<ReportRegisterBillDto> selectRegisterBillReport(RegisterBillDto billDto);

    /**
     * 查询数据库当前时间
     */
    public Date selectCurrentTime();

    /**
     * 产地进场重量分布统计
     * @param queryDto
     * @return
     */
    public List<OrigionReportDto> queryOrigionReport(OrigionReportQueryDto queryDto);

    /**
     * 进场商品产地分布统计
     * @param queryDto
     * @return
     */
    public List<ProductOrigionReportDto> queryProductOrigionReport(OrigionReportQueryDto queryDto);


    List<RegisterBill> findUnMatchedRegisterBill(MatchDetectParam matchDetectParam);

    List<DetectRequest> selectDetectingOrWaitDetectBillId(@Param("exeMachineNo") String exeMachineNo
            , @Param("taskCount") int taskCount
            , @Param("marketId") Long marketId);

    RegisterBillStaticsDto groupByState(RegisterBillDto dto);

    int doRemoveReportAndCertifiy(RegisterBill registerBill);

    int doRemoveReportAndCertifiyNew(ReportAndCertifiyRemoveDto removeDto);

    List<GroupByProductReportDto> listPageGroupByProduct(RegisterBillReportQueryDto dto);

    GroupByProductReportDto summaryGroup(RegisterBillReportQueryDto dto);

    Long listPageGroupByProductCount(RegisterBillReportQueryDto dto);

    List<RegisterBillDto> queryListByExample(RegisterBillDto query);

}