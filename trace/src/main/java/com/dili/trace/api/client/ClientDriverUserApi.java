package com.dili.trace.api.client;

import com.dili.common.annotation.AppAccess;
import com.dili.common.annotation.Role;
import com.dili.common.entity.LoginSessionContext;
import com.dili.common.entity.SessionData;
import com.dili.common.exception.TraceBizException;
import com.dili.customer.sdk.enums.CustomerEnum;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.BasePage;
import com.dili.trace.domain.EventMessage;
import com.dili.trace.domain.TruckEnterRecord;
import com.dili.trace.domain.UserDriverRef;
import com.dili.trace.domain.UserInfo;
import com.dili.trace.dto.query.TruckEnterRecordQueryDto;
import com.dili.trace.dto.query.UserDriverRefQueryDto;
import com.dili.trace.enums.MessageReceiverEnum;
import com.dili.trace.enums.MessageStateEnum;
import com.dili.trace.service.DriverUserService;
import com.dili.trace.service.EventMessageService;
import com.dili.trace.service.TruckEnterRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.function.Function;

/**
 * 司机接口
 *
 * @version 1.0
 * @ClassName DriverUserApi
 * @Description todo
 * @createTime 2020年12月01日 15:56:00
 */
@Api(value = "/api/driverUserApi")
@RestController
@RequestMapping(value = "/api/driverUserApi")
@AppAccess(role = Role.Client, url = "", subRoles = {CustomerEnum.CharacterType.经营户, CustomerEnum.CharacterType.其他类型})
public class ClientDriverUserApi {

    private static final Logger logger = LoggerFactory.getLogger(ClientDriverUserApi.class);

    @Autowired
    DriverUserService driverUserService;
    @Autowired
    TruckEnterRecordService truckEnterRecordService;
    @Autowired
    EventMessageService eventMessageService;
    @Autowired
    LoginSessionContext sessionContext;

    /**
     * 查询司机进门报备数据列表
     *
     * @param queryDto
     * @return
     */
    @ApiOperation(value = "查询司机进门报备数据列表", notes = "查询司机进门报备数据列表")
    @RequestMapping(value = "/listPagedEnterRecord.api", method = RequestMethod.POST)
    public BaseOutput listPagedEnterRecord(@RequestBody TruckEnterRecordQueryDto queryDto) {
        try {
            SessionData sessionData = this.sessionContext.getSessionData();
            queryDto.setDriverId(sessionData.getUserId());
            BasePage<TruckEnterRecord> page = this.truckEnterRecordService.listPageByExample(queryDto);
            return BaseOutput.successData(page);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
    }

    /**
     * 查询司机消息数据列表
     *
     * @param queryDto
     * @return
     */
    @ApiOperation(value = "查询司机消息数据列表", notes = "查询司机消息数据列表")
    @RequestMapping(value = "/listPagedEventMessage.api", method = RequestMethod.POST)
    public BaseOutput listPagedEventMessage(@RequestBody EventMessage queryDto) {
        try {
            queryDto.setReceiverId(this.sessionContext.getSessionData().getUserId());
            queryDto.setReceiverType(MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_NORMAL.getCode());
            BasePage<EventMessage> page = this.eventMessageService.listPageByExample(queryDto);
            return BaseOutput.successData(page);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }


    }

    /**
     * 查看消息详情
     *
     * @param queryDto
     * @return
     */
    @ApiOperation(value = "查看消息详情", notes = "查看消息详情")
    @RequestMapping(value = "/eventMessageDetail.api", method = RequestMethod.POST)
    public BaseOutput<EventMessage> eventMessageDetail(@RequestBody EventMessage queryDto) {
        if (queryDto.getId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            queryDto.setReceiverId(this.sessionContext.getSessionData().getUserId());
            queryDto.setReceiverType(MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_NORMAL.getCode());

            return StreamEx.ofNullable(this.eventMessageService.listByExample(queryDto)).flatCollection(Function.identity()).nonNull()
                    .findFirst().map(data -> BaseOutput.successData(data)).orElseGet(() -> BaseOutput.failure("数据不存在"));
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
    }

    /**
     * 消息标记为已阅读
     *
     * @param queryDto
     * @return
     */
    @ApiOperation(value = "消息标记为已阅读", notes = "消息标记为已阅读")
    @RequestMapping(value = "/doRead.api", method = RequestMethod.POST)
    public BaseOutput<EventMessage> doRead(@RequestBody EventMessage queryDto) {
        if (queryDto.getId() == null) {
            return BaseOutput.failure("参数错误");
        }
        try {
            queryDto.setReceiverId(this.sessionContext.getSessionData().getUserId());
            queryDto.setReceiverType(MessageReceiverEnum.MESSAGE_RECEIVER_TYPE_NORMAL.getCode());

            return StreamEx.ofNullable(this.eventMessageService.listByExample(queryDto)).flatCollection(Function.identity()).nonNull()
                    .findFirst().map(data -> {
                        EventMessage msg = new EventMessage();
                        msg.setId(data.getId());
                        msg.setReadFlag(MessageStateEnum.READ.getCode());
                        this.eventMessageService.updateSelective(msg);
                        return BaseOutput.successData(this.eventMessageService.get(data.getId()));
                    }).orElseGet(() -> {
                        return BaseOutput.failure("数据不存在");
                    });
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
    }

    /**
     * 查询司机未读取消息数量
     *
     * @param queryDto
     * @return
     */
    @ApiOperation(value = "查询司机未读取消息数量", notes = "查询司机未读取消息数量")
    @RequestMapping(value = "/countReadableEventMessage.api", method = RequestMethod.POST)
    public BaseOutput<Integer> countReadableEventMessage(@RequestBody EventMessage queryDto) {
        try {
            Integer cnt = this.eventMessageService.countReadableEventMessage(queryDto.getReceiverId(), this.sessionContext.getSessionData().getMarketId());
            return BaseOutput.successData(cnt);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }
    }

    /**
     * 是否需要注册
     *
     * @param user 业户
     * @return
     */
    @ApiOperation(value = "是否需要注册", notes = "是否需要注册")
    @RequestMapping(value = "/needRegister.api", method = RequestMethod.POST)
    @Deprecated
    public BaseOutput needRegister(@RequestBody UserInfo user) {

        return BaseOutput.success().setData("need");
    }

    /**
     * 获取司机用户列表
     *
     * @param user 查询条件
     * @return 司机用户列表
     */
    @ApiOperation(value = "获取司机用户列表", notes = "获取司机用户列表")
    @RequestMapping(value = "/getDriverUserList.api", method = RequestMethod.POST)
    public BaseOutput<BasePage<UserDriverRef>> getDriverUserList(@RequestBody UserDriverRefQueryDto user) {
        try {
            BasePage<UserDriverRef> userList = driverUserService.getDriverUserList(user);
            return BaseOutput.success().setData(userList);
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("getDriverUserList", e);
            return BaseOutput.failure(e.getMessage());
        }
    }

    /**
     * 获取司机列表
     *
     * @return 司机列表
     */
    @ApiOperation(value = "获取司机列表", notes = "获取司机列表")
    @RequestMapping(value = "/getDriverList.api", method = RequestMethod.POST)
    @Deprecated
    public BaseOutput getDriverList() {
        try {
            UserInfo user = new UserInfo();
            user.setName("测试");
            user.setId(1L);

            return BaseOutput.success().setData(Collections.singletonList(user));
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            return BaseOutput.failure().setData(e.getMessage());
        }

    }

    /**
     * 新增司机与卖家关联关系
     *
     * @param userRef 司机与卖家关联关系信息
     * @return 新增结果
     */
    @ApiOperation(value = "新增司机与卖家关联关系", notes = "新增司机与卖家关联关系")
    @RequestMapping(value = "/updateDriverUserRef.api", method = RequestMethod.POST)
    public BaseOutput updateDriverUserRef(@RequestBody UserDriverRef userRef) {
        try {
            driverUserService.updateDriverUserRef(userRef);
            return BaseOutput.success();
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("register", e);
            return BaseOutput.failure("服务端出错");
        }
    }

}
