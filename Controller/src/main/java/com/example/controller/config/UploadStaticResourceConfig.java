package com.example.controller.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadStaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:${user.home}/.marketplace-uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path products = Path.of(uploadDir).toAbsolutePath().normalize().resolve("products");
        String location = products.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/api/files/products/**").addResourceLocations(location);
    }
}
