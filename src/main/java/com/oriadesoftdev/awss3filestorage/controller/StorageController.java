package com.oriadesoftdev.awss3filestorage.controller;

import com.oriadesoftdev.awss3filestorage.data.request.QRCodeRequestBody;
import com.oriadesoftdev.awss3filestorage.service.AWSS3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/")
@Validated
public class StorageController {

    @Autowired
    private AWSS3StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @Valid @RequestBody QRCodeRequestBody requestBody
            ) {
        return new ResponseEntity<>(
                storageService.uploadFile(requestBody),
                HttpStatus.OK
        );
    }

    @PostMapping("/upload/default")
    public ResponseEntity<String> uploadFile() {
        String result = storageService.uploadFile(new QRCodeRequestBody("https://www.slydo.co/users/oriade" ,""));
        return new ResponseEntity<>(
                    result,
                    HttpStatus.OK
            );
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @PathVariable String filename
    ) {
        byte[] data = storageService.downloadFile("", "");
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header(
                        "Content-disposition",
                        String.format("attachment; filename=\"%1$s\"", filename))
                .body(resource);
    }

    @GetMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        return new ResponseEntity<>(storageService.deleteFile(filename), HttpStatus.OK);
    }
}
