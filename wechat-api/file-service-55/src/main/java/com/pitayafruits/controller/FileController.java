package com.pitayafruits.controller;


import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.config.MinIOConfig;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.grace.result.ResponseStatusEnum;
import com.pitayafruits.utils.MinIOUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("file")
public class FileController extends BaseInfoProperties {

    @Resource
    private MinIOConfig minIOConfig;

    @PostMapping("uploadFace")
    public GraceJSONResult upload(@RequestParam MultipartFile file,
                                 String userId) throws Exception {
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();

        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        filename = "face" +  "/" + userId + "/" + filename;

        MinIOUtils.uploadFile(minIOConfig.getBucketName(), filename, file.getInputStream());

        String faceUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + filename;

        return GraceJSONResult.ok(faceUrl);
    }

}
