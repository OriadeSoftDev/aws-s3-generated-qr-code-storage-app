package com.oriadesoftdev.awss3filestorage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

@Service
public class StaticResourceService {
    private final ResourceLoader resourceLoader;
    private final static String ROUNDED_SLYDO_LOGO = "rounded-slydo-logo.png";

//    @Value("${app.file.directory}") // Configure this in your application.properties or application.yml
//    private String baseDirectory;

    public StaticResourceService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Path getPathToStaticFile(String filename) throws IOException {
        Resource resource = resourceLoader.getResource(MessageFormat.format("classpath:static/{0}", filename));
        return resource.getFile().toPath();
    }

    public Path getPathToSlydoLogo() throws IOException {
        return getPathToStaticFile(ROUNDED_SLYDO_LOGO);
    }
}
