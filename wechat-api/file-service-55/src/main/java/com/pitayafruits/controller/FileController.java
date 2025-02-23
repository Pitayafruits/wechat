package com.pitayafruits.controller;


import com.pitayafruits.base.BaseInfoProperties;
import com.pitayafruits.grace.result.GraceJSONResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("file")
public class FileController extends BaseInfoProperties {

    @PostMapping("uploadFace")
    public GraceJSONResult upload(@RequestBody MultipartFile file,
                                  String userId) throws IOException {

        // 声明文件的新名称
        String filename = file.getOriginalFilename();
        String suffixName = filename.substring(filename.lastIndexOf("."));
        String newFileName = userId + suffixName;

        // 设置文件的存储路径
        String rootPath = "/temp" + File.separator;
        String filePath = rootPath + File.separator + "face" + File.separator + newFileName;
        File newFile = new File(filePath);

        // 如果目标文件的目录不存在，则创建父级目录
        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }

        // 写入文件
        file.transferTo(newFile);

        return GraceJSONResult.ok();
    }


}
