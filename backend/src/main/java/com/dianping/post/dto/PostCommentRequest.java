package com.dianping.post.dto;

import javax.validation.constraints.NotBlank;

public class PostCommentRequest {
    @NotBlank(message = "comment content is required")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
