package com.oriadesoftdev.awss3filestorage.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    public void saveFile(InputStream inputStream, String directory, String fileName) throws IOException {
        // Get the static resource directory as a resource
        Resource resource = resourceLoader.getResource(String.format("classpath:static/%1$s", directory));

        // Create the directory if it doesn't exist
        if (!resource.exists()) {
            Files.createDirectories(Path.of(resource.getURI()));
        }

        // Define the file path where the file will be saved
        Path filePath = Path.of(resource.getURI()).resolve(fileName);

        // Copy the input stream to the file
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }
}
