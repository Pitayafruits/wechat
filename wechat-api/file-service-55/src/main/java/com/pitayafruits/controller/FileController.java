package com.pitayafruits.controller;


import com.pitayafruits.api.feign.UserInfoMicroServiceFeign;
import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.config.MinIOConfig;
import com.pitayafruits.grace.result.GraceJSONResult;
import com.pitayafruits.grace.result.ResponseStatusEnum;
import com.pitayafruits.utils.JsonUtils;
import com.pitayafruits.utils.MinIOUtils;
import com.pitayafruits.utils.QrCodeUtils;
import com.pitayafruits.pojo.vo.UsersVo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("file")
public class FileController extends BaseInfoProperties {

    @Resource
    private MinIOConfig minIOConfig;

    @Resource
    private UserInfoMicroServiceFeign userInfoMicroServiceFeign;

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

        // 远程调用修改头像
        GraceJSONResult jsonResult = userInfoMicroServiceFeign.updateFace(userId, faceUrl);

        Object data = jsonResult.getData();

        String json = JsonUtils.objectToJson(data);
        UsersVo usersVo = JsonUtils.jsonToPojo(json, UsersVo.class);

        return GraceJSONResult.ok(usersVo);
    }

    @PostMapping("generatorQrCode")
    public String generatorQrCode(String wechatNumber,
                                           String userId) throws Exception {
        // 构建二维码
        Map<String, String> map = new HashMap<>();
        map.put("wechatNumber", wechatNumber);
        map.put("userId", userId);
        // 把对象转成json字符串，用于存储到二维码中
        String data = JsonUtils.objectToJson(map);

        // 生成二维码
        String qrCodePath = QrCodeUtils.generateQRCode(data);
        // 把二维码上传到minIO中
        if (StringUtils.isNotBlank(qrCodePath)) {
            String uuid = UUID.randomUUID().toString();
            String objectName = "wechatNumber" + "/" + userId + "/" + uuid + ".png";
            String imageQrCodeUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(), objectName, qrCodePath, true);
            return imageQrCodeUrl;
        }

        return null;
    }

}
