package com.dianping.common.oss;

import com.dianping.common.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/api/oss")
public class OssController {

    private static final Logger log = LoggerFactory.getLogger(OssController.class);

    private final TencentOssService ossService;

    public OssController(TencentOssService ossService) {
        this.ossService = ossService;
    }

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @param dir  目录：posts(帖子), shops(店铺), users(用户头像), dishes(菜品)
     * @return 图片URL
     */
    @PostMapping("/upload/image")
    public ApiResponse<OssUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dir", defaultValue = "common") String dir) {

        // 校验目录
        if (!isValidDir(dir)) {
            dir = "common";
        }

        String url = ossService.uploadImage(file, dir);

        OssUploadResponse response = new OssUploadResponse(
                url,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );

        return ApiResponse.ok(response);
    }

    /**
     * 批量上传图片
     *
     * @param files 图片文件数组
     * @param dir   目录
     * @return 图片URL列表
     */
    @PostMapping("/upload/images")
    public ApiResponse<java.util.List<OssUploadResponse>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "dir", defaultValue = "common") String dir) {

        if (!isValidDir(dir)) {
            dir = "common";
        }

        java.util.List<OssUploadResponse> responses = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            String url = ossService.uploadImage(file, dir);
            responses.add(new OssUploadResponse(
                    url,
                    file.getOriginalFilename(),
                    file.getSize(),
                    file.getContentType()
            ));
        }

        return ApiResponse.ok(responses);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 操作结果
     */
    @PostMapping("/delete")
    public ApiResponse<Void> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        ossService.deleteFile(fileUrl);
        return ApiResponse.ok(null);
    }

    /**
     * 校验目录是否合法
     */
    private boolean isValidDir(String dir) {
        return dir != null && (
                dir.equals("posts") ||
                dir.equals("shops") ||
                dir.equals("users") ||
                dir.equals("dishes") ||
                dir.equals("common")
        );
    }
}
