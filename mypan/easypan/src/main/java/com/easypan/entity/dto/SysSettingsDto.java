package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Data
public class SysSettingsDto {
    private String registerEmailTitle = "邮箱验证码";
    private String registerEmailContent = "您好，您的邮箱验证码是：%s,15分钟有效";
    private Integer userInitUseSpace = 1000;

}
