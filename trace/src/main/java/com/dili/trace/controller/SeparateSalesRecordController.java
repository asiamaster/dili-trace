package com.dili.trace.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.trace.domain.SeparateSalesRecord;
import com.dili.trace.service.SeparateSalesRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2019-07-26 09:20:35.
 */
@Api("/separateSalesRecord")
@Controller
@RequestMapping("/separateSalesRecord")
public class SeparateSalesRecordController {
    @Autowired
    SeparateSalesRecordService separateSalesRecordService;

    /**
     * 跳转到SeparateSalesRecord页面
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到SeparateSalesRecord页面")
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "separateSalesRecord/index";
    }

    /**
     * 查询SeparateSalesRecord
     * @param separateSalesRecord
     * @return
     */
    @ApiOperation(value="查询SeparateSalesRecord", notes = "查询SeparateSalesRecord，返回列表信息")
    @ApiImplicitParams({
		@ApiImplicitParam(name="SeparateSalesRecord", paramType="form", value = "SeparateSalesRecord的form信息", required = false, dataType = "string")
	})
    /**
     * 查询 数据
     */
    @RequestMapping(value="/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<SeparateSalesRecord> list(SeparateSalesRecord separateSalesRecord) {
        return separateSalesRecordService.list(separateSalesRecord);
    }

    /**
     * 分页查询SeparateSalesRecord
     * @param separateSalesRecord
     * @return
     * @throws Exception
     */
    @ApiOperation(value="分页查询SeparateSalesRecord", notes = "分页查询SeparateSalesRecord，返回easyui分页信息")
    @ApiImplicitParams({
		@ApiImplicitParam(name="SeparateSalesRecord", paramType="form", value = "SeparateSalesRecord的form信息", required = false, dataType = "string")
	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(SeparateSalesRecord separateSalesRecord) throws Exception {
        return separateSalesRecordService.listEasyuiPageByExample(separateSalesRecord, true).toString();
    }

    /**
     * 新增SeparateSalesRecord
     * @param separateSalesRecord
     * @return
     */
    @ApiOperation("新增SeparateSalesRecord")
    @ApiImplicitParams({
		@ApiImplicitParam(name="SeparateSalesRecord", paramType="form", value = "SeparateSalesRecord的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput insert(SeparateSalesRecord separateSalesRecord) {
        separateSalesRecordService.insertSelective(separateSalesRecord);
        return BaseOutput.success("新增成功");
    }

    /**
     * 修改SeparateSalesRecord
     * @param separateSalesRecord
     * @return
     */
    @ApiOperation("修改SeparateSalesRecord")
    @ApiImplicitParams({
		@ApiImplicitParam(name="SeparateSalesRecord", paramType="form", value = "SeparateSalesRecord的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput update(SeparateSalesRecord separateSalesRecord) {
        separateSalesRecordService.updateSelective(separateSalesRecord);
        return BaseOutput.success("修改成功");
    }

    /**
     * 删除SeparateSalesRecord
     * @param id
     * @return
     */
    @ApiOperation("删除SeparateSalesRecord")
    @ApiImplicitParams({
		@ApiImplicitParam(name="id", paramType="form", value = "SeparateSalesRecord的主键", required = true, dataType = "long")
	})
    @RequestMapping(value="/delete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput delete(Long id) {
        separateSalesRecordService.delete(id);
        return BaseOutput.success("删除成功");
    }
}