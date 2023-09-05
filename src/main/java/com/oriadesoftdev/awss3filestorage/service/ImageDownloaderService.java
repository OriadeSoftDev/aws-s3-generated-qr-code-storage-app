package com.oriadesoftdev.awss3filestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;

@Service
public class ImageDownloaderService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private StaticResourceService staticResourceService;

    public HttpStatus downloadImage(String imageUrl, String directory, String filename) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(imageUrl, HttpMethod.GET, entity, byte[].class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try (InputStream inputStream = new ByteArrayInputStream(Objects.requireNonNull(response.getBody()))) {
                staticResourceService.saveFile(inputStream, directory, filename);
                return HttpStatus.OK;
            }
        } else {
            throw new IOException("Failed to download image: " + imageUrl);
        }
    }
}
