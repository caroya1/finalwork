package com.dianping.common.oss;

/**
 * 文件上传响应
 */
public class OssUploadResponse {

    /**
     * 文件URL
     */
    private String url;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件大小
     */
    private long size;

    /**
     * 文件类型
     */
    private String contentType;

    public OssUploadResponse() {
    }

    public OssUploadResponse(String url, String filename, long size, String contentType) {
        this.url = url;
        this.filename = filename;
        this.size = size;
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
