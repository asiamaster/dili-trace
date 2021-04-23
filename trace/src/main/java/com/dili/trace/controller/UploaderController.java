package com.dili.trace.controller;

import com.dili.common.exception.TraceBizException;
import com.dili.ss.domain.BaseOutput;
import com.dili.trace.rpc.service.DfsRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传
 */
@RestController
@RequestMapping(value = "/uploader")
public class UploaderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploaderController.class);

    @Autowired
    DfsRpcService dfsRpcService;


    /**
     * 上传图片
     *
     * @param file
     * @param type
     * @param compress
     * @return
     */
    @RequestMapping(value = "/uploadImage.action", method = RequestMethod.POST)
    public BaseOutput<String> uploadImage(@RequestParam MultipartFile file, @RequestParam(required = false) Integer type, @RequestParam(required = false) Boolean compress) {
        try {
            return BaseOutput.successData(this.dfsRpcService.uploadImage(file));
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("upload", e);
            return BaseOutput.failure();
        }
    }
    /**
     * 上传文件
     *
     * @param file
     * @param type
     * @param compress
     * @return
     */
    @RequestMapping(value = "/uploadFile.action", method = RequestMethod.POST)
    public BaseOutput<String> uploadFile(@RequestParam MultipartFile file, @RequestParam(required = false) Integer type, @RequestParam(required = false) Boolean compress) {
        try {
            return BaseOutput.successData(this.dfsRpcService.uploadFile(file));
        } catch (TraceBizException e) {
            return BaseOutput.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("upload", e);
            return BaseOutput.failure();
        }
    }

}
