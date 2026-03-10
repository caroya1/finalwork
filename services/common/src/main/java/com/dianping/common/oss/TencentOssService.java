package com.dianping.common.oss;

import com.dianping.common.exception.BusinessException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 腾讯云OSS服务
 */
@Service
public class TencentOssService {

    private static final Logger log = LoggerFactory.getLogger(TencentOssService.class);

    private final TencentOssProperties properties;
    private COSClient cosClient;

    public TencentOssService(TencentOssProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        if (properties.getSecretId() == null || properties.getSecretId().trim().isEmpty()) {
            log.warn("Tencent OSS SecretId not configured, OSS service disabled");
            return;
        }
        if (properties.getSecretKey() == null || properties.getSecretKey().trim().isEmpty()) {
            log.warn("Tencent OSS SecretKey not configured, OSS service disabled");
            return;
        }
        if (properties.getBucketName() == null || properties.getBucketName().trim().isEmpty()) {
            log.warn("Tencent OSS bucketName not configured, OSS service disabled");
            return;
        }

        COSCredentials cred = new BasicCOSCredentials(properties.getSecretId(), properties.getSecretKey());
        Region region = new Region(properties.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        this.cosClient = new COSClient(cred, clientConfig);
        log.info("Tencent OSS client initialized, bucket: {}, region: {}", properties.getBucketName(), properties.getRegion());
    }

    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
            log.info("Tencent OSS client shutdown");
        }
    }

    /**
     * 上传图片文件
     *
     * @param file 文件
     * @param dir  目录，如 posts, shops, users
     * @return 文件访问URL
     */
    public String uploadImage(MultipartFile file, String dir) {
        if (cosClient == null) {
            throw new BusinessException("OSS服务未配置，请联系管理员");
        }
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String key = generateKey(dir, extension);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    properties.getBucketName(),
                    key,
                    inputStream,
                    metadata
            );

            PutObjectResult result = cosClient.putObject(putObjectRequest);
            log.info("File uploaded to OSS, key: {}, etag: {}", key, result.getETag());

            return buildUrl(key);
        } catch (IOException e) {
            log.error("Failed to read file input stream", e);
            throw new BusinessException("文件读取失败");
        } catch (Exception e) {
            log.error("Failed to upload file to OSS", e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 上传文件（通用）
     *
     * @param file        文件
     * @param dir         目录
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String dir, String contentType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > properties.getMaxSize()) {
            throw new BusinessException("文件大小超过限制");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String key = generateKey(dir, extension);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (contentType != null) {
                metadata.setContentType(contentType);
            }

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    properties.getBucketName(),
                    key,
                    inputStream,
                    metadata
            );

            PutObjectResult result = cosClient.putObject(putObjectRequest);
            log.info("File uploaded to OSS, key: {}, etag: {}", key, result.getETag());

            return buildUrl(key);
        } catch (IOException e) {
            log.error("Failed to read file input stream", e);
            throw new BusinessException("文件读取失败");
        } catch (Exception e) {
            log.error("Failed to upload file to OSS", e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }
        try {
            String key = extractKeyFromUrl(fileUrl);
            if (key != null) {
                cosClient.deleteObject(properties.getBucketName(), key);
                log.info("File deleted from OSS, key: {}", key);
            }
        } catch (Exception e) {
            log.error("Failed to delete file from OSS: {}", fileUrl, e);
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        if (file.getSize() > properties.getMaxSize()) {
            throw new BusinessException("文件大小超过限制，最大支持 " + (properties.getMaxSize() / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException("无法识别文件类型");
        }

        Set<String> allowedTypes = new HashSet<>(Arrays.asList(
                properties.getAllowedTypes().split(",")
        ));
        if (!allowedTypes.contains(contentType)) {
            throw new BusinessException("不支持的文件类型: " + contentType);
        }
    }

    /**
     * 生成文件Key
     */
    private String generateKey(String dir, String extension) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return dir + "/" + dateStr + "/" + uuid + "." + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 构建访问URL
     */
    private String buildUrl(String key) {
        if (properties.getDomain() != null && !properties.getDomain().trim().isEmpty()) {
            return properties.getDomain() + "/" + key;
        }
        return "https://" + properties.getBucketName() + ".cos." + properties.getRegion() + ".myqcloud.com/" + key;
    }

    /**
     * 从URL提取Key
     */
    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        // 自定义域名格式
        if (properties.getDomain() != null && !properties.getDomain().trim().isEmpty()
                && fileUrl.startsWith(properties.getDomain())) {
            return fileUrl.substring(properties.getDomain().length() + 1);
        }
        // COS默认域名格式
        String prefix = "https://" + properties.getBucketName() + ".cos." + properties.getRegion() + ".myqcloud.com/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        // 尝试提取路径
        int index = fileUrl.indexOf(".myqcloud.com/");
        if (index > 0) {
            return fileUrl.substring(index + ".myqcloud.com/".length());
        }
        return null;
    }
}
