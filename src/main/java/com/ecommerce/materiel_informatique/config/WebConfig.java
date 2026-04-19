package com.ecommerce.materiel_informatique.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadDirectory = new File(uploadDir);
        String absolutePath = uploadDirectory.getAbsolutePath();
        String resourceLocation = "file:///" + absolutePath.replace("\\", "/") + "/";

        logger.info("========== UPLOAD CONFIG ==========");
        logger.info("uploadDir property: {}", uploadDir);
        logger.info("Absolute path: {}", absolutePath);
        logger.info("Resource location: {}", resourceLocation);
        logger.info("Directory exists: {}", uploadDirectory.exists());
        logger.info("Is directory: {}", uploadDirectory.isDirectory());

        if (uploadDirectory.exists() && uploadDirectory.isDirectory()) {
            File[] files = uploadDirectory.listFiles();
            if (files != null) {
                logger.info("Files in upload directory: {}", files.length);
                for (File file : files) {
                    logger.info("  - {}", file.getName());
                }
            }
        }
        logger.info("====================================");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}



