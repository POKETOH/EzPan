package com.easypan.entity.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@Configuration
@AllArgsConstructor
@Data
@Getter
@NoArgsConstructor
public class AppConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //  单个数据大小
        factory.setMaxFileSize(DataSize.ofMegabytes(1024L));
        /// 总上传数据大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(1024L));
        return factory.createMultipartConfig();
    }
    @Value("${spring.mail.userName:}")
    private String fromEmail;
    @Value("${admin.emails:}")
    private String adminEmail;
    @Value("${project.folder:}")
    private String projectFolder;
    public String getFromEmail() {
        return fromEmail;
    }
}
