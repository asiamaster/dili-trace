package com.dili.trace.service;

import com.dili.trace.domain.UserInfo;
import com.dili.trace.domain.UserQrHistory;
import com.dili.trace.dto.thirdparty.report.ReportQrCodeDetailDto;
import com.dili.trace.dto.thirdparty.report.ReportQrCodeDto;
import com.dili.trace.dto.thirdparty.report.ReportUserDto;
import com.dili.trace.dto.thirdparty.report.ReportUserImgDto;
import com.dili.trace.enums.ReportInterfacePicEnum;
import com.dili.trace.glossary.ColorEnum;
import com.dili.trace.glossary.UserTypeEnum;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author asa.lee
 */
@Service
public class ThirdDataReportService {

    @Value("${current.baseWebPath}")
    private String baseWebPath;

    private Integer userStatusNormal = 1;
    private Integer userStatusDelete = -1;

    /**
     *
     * @param info
     * @param platformMarketId
     * @return
     */
    public ReportUserDto reprocessUser(UserInfo info, Long platformMarketId) {
        ReportUserDto reportUser = new ReportUserDto();
        reportUser.setAccountName(info.getName());
        //个人10 企业20 转换 1企业2个人
        Integer oldAccountType = 10;
        Integer accountType = 1;
        if (oldAccountType.equals(info.getUserType())) {
            accountType = 2;
        }
        reportUser.setAccountType(accountType);
        if (UserTypeEnum.CORPORATE.equalsCode(info.getUserType())) {
            reportUser.setComeName(info.getName());
        }
        reportUser.setAddress(info.getAddr());
        reportUser.setBoothNo(info.getTallyAreaNos());
        reportUser.setBlAccountImgList(getBlAccountImgList(info));
        reportUser.setCardNo(info.getCardNo());
        reportUser.setClassName(info.getBusinessCategories());
        //二维码信息
        String codeContent = this.baseWebPath + "/user?userId=" + info.getId();
        reportUser.setCode(codeContent);
        reportUser.setTelphone(info.getPhone());
        Integer userType = 1;
        reportUser.setCustType(userType);
        reportUser.setLegal(info.getLegalPerson());
        reportUser.setLicense(info.getLicense());
        reportUser.setMarketId(platformMarketId);
        Integer userStatus = 0;
        //目标系统 0：正常，1：冻结：2:作废
        //现有 yn为1为正常/-1为删除 state为0禁用/1启用，
        if (userStatusDelete.equals(info.getYn())) {
            userStatus = 2;
        } else if (!userStatusNormal.equals(info.getState())) {
            userStatus = 1;
        }
        reportUser.setStatus(userStatus);
        reportUser.setTelphone(info.getPhone());
        reportUser.setThirdAccId(String.valueOf(info.getId()));
        return reportUser;
    }

    /**
     * SB
     * @param info
     * @return 
     */
    private List<ReportUserImgDto> getBlAccountImgList(UserInfo info) {
        List<ReportUserImgDto> userImgList = new ArrayList<>();
        //生产许可证
        if (StringUtils.isNotBlank(info.getManufacturingLicenseUrl())) {
            String url = info.getManufacturingLicenseUrl();
            ReportUserImgDto imgDto = new ReportUserImgDto();
            imgDto.setCredentialName(ReportInterfacePicEnum.PRODUCTION_LICENSE.getName());
            if (StringUtils.isNotBlank(url)) {
                imgDto.setPicUrl(baseWebPath + url);
            }
            userImgList.add(imgDto);
        }
        //经营许可证
        if (StringUtils.isNotBlank(info.getOperationLicenseUrl())) {
            String url = info.getOperationLicenseUrl();
            ReportUserImgDto imgDto = new ReportUserImgDto();
            imgDto.setCredentialName(ReportInterfacePicEnum.OPERATING_LICENSE.getName());
            if (StringUtils.isNotBlank(url)) {
                imgDto.setPicUrl(baseWebPath + url);
            }
            userImgList.add(imgDto);
        }
        //营业执照
        if (StringUtils.isNotBlank(info.getBusinessLicenseUrl())) {
            String url = info.getBusinessLicenseUrl();
            ReportUserImgDto imgDto = new ReportUserImgDto();
            imgDto.setCredentialName(ReportInterfacePicEnum.BUSINESS_LICENSE.getName());
            if (StringUtils.isNotBlank(url)) {
                imgDto.setPicUrl(baseWebPath + url);
            }
            userImgList.add(imgDto);
        }
        //身份证正面
        if (StringUtils.isNotBlank(info.getCardNoFrontUrl())) {
            String url = info.getCardNoFrontUrl();
            ReportUserImgDto imgDto = new ReportUserImgDto();
            imgDto.setCredentialName(ReportInterfacePicEnum.ID_CARD_FRONT.getName());
            if (StringUtils.isNotBlank(url)) {
                imgDto.setPicUrl(baseWebPath + url);
            }
            userImgList.add(imgDto);
        }
        //身份证反面
        if (StringUtils.isNotBlank(info.getCardNoBackUrl())) {
            String url = info.getCardNoBackUrl();
            ReportUserImgDto imgDto = new ReportUserImgDto();
            imgDto.setCredentialName(ReportInterfacePicEnum.ID_CARD_REVERSE.getName());
            if (StringUtils.isNotBlank(url)) {
                imgDto.setPicUrl(baseWebPath + url);
            }
            userImgList.add(imgDto);
        }
        //证件照
//        if (StringUtils.isNotBlank(info.getCredentialUrl())) {
//            String url = info.getCredentialUrl();
//            ReportUserImgDto imgDto = new ReportUserImgDto();
//            imgDto.setCredentialName(ReportInterfacePicEnum.ID_CARD_REVERSE.getName());
//            if (StringUtils.isNotBlank(url)) {
//                imgDto.setPicUrl(baseWebPath + url);
//            }
//            userImgList.add(imgDto);
//        }
        return userImgList;
    }

    /**
     * reprocessUserQrCode
     */
    public List<ReportQrCodeDto> reprocessUserQrCode(List<UserQrHistory> list, Long platformMarketId) {
        Map<Long, List<UserQrHistory>> groupByPriceMap = StreamEx.of(list).nonNull().collect(Collectors.groupingBy(UserQrHistory::getUserId));
        List<ReportQrCodeDto> qrCodeDtos = new ArrayList<>();
        for (Map.Entry<Long, List<UserQrHistory>> map : groupByPriceMap.entrySet()) {
            Long userId = map.getKey();
            //二维码信息
            String codeContent = this.baseWebPath + "/user?userId=" + String.valueOf(userId);

            ReportQrCodeDto reportQrCodeDto = new ReportQrCodeDto();
            reportQrCodeDto.setThirdAccId(String.valueOf(userId));
            reportQrCodeDto.setCode(codeContent);
            reportQrCodeDto.setMarketId(String.valueOf(platformMarketId));

            List<UserQrHistory> sortQrList = map.getValue();
            //按修改时间排序
            sortQrList.sort((q1, q2) -> q2.getModified().compareTo(q1.getModified()));
            UserQrHistory qrHistory = sortQrList.get(0);
            //0-黑 1-绿 2-黄 3-红
            Integer colorStatus = 0;
            if (ColorEnum.GREEN.equalsCode(qrHistory.getQrStatus())) {
                colorStatus = 1;
            } else if (ColorEnum.YELLOW.equalsCode(qrHistory.getQrStatus())) {
                colorStatus = 2;
            } else if (ColorEnum.RED.equalsCode(qrHistory.getQrStatus())) {
                colorStatus = 3;
            }
            reportQrCodeDto.setColor(colorStatus);
            List<ReportQrCodeDetailDto> codeDetail = new ArrayList<>();
            map.getValue().stream().forEach(c -> {
                ReportQrCodeDetailDto r = new ReportQrCodeDetailDto();
                r.setCodeDetail(c.getContent());
                codeDetail.add(r);
            });
            reportQrCodeDto.setCodeDetailList(codeDetail);
            qrCodeDtos.add(reportQrCodeDto);
        }
        return qrCodeDtos;
    }
}
