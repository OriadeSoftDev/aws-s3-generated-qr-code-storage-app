package com.oriadesoftdev.awss3filestorage.service;

import com.oriadesoftdev.awss3filestorage.qrcode.RoundQRCodeWithLogo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.oriadesoftdev.awss3filestorage.util.QRCodeUtilsKt.color;

@Service
@Slf4j
public class AWSS3StorageService {

    @Value("${s3.bucket}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private StaticResourceService staticResourceService;

    public String uploadFile(String url) {
        System.out.println(url);
        System.out.println(bucketName);
        // Replace with your URL
        String[] urlArray = url.split("/");
        String username = urlArray[urlArray.length - 1];
        String qrCodeFileName = String.format("%1$d_%2$s_qr.png", System.currentTimeMillis(), username);
        String slydoFolder = "slydo";
        try {
            Path logoPath = staticResourceService.getPathToSlydoLogo();
            String cwd = System.getProperty("user.dir");
            File outputFile = new File(cwd, Paths.get(slydoFolder, qrCodeFileName).toString());
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            createQRCodeWithLogo(url, logoPath, outputFile.getPath());
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(String.format("%1$s/%2$s", slydoFolder, qrCodeFileName))
                    .build();
            s3Client.putObject(putObjectRequest, outputFile.toPath());
            System.out.println("File uploaded successfully to S3 bucket.");
            return String.format("%1$s uploaded successfully", qrCodeFileName);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void createQRCodeWithLogo(String url, Path logoPath, String outputPath) {
        new RoundQRCodeWithLogo().createQRCodeWithLogo(
                url,
                logoPath,
                outputPath,
                new Color[]{
                        color("#cce6ff"),
                        Color.LIGHT_GRAY,
                        Color.LIGHT_GRAY,
                        color("#cce6ff")
                },
                25
        );
    }

    public byte[] downloadFile(String objectKey, String destinationPath) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        ResponseInputStream<GetObjectResponse> getObjectResponse = s3Client.getObject(getObjectRequest);

        File downloadedFile = new File(destinationPath);
        try (FileOutputStream outputStream = new FileOutputStream(downloadedFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = getObjectResponse.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteFile(String objectKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        System.out.println("File deleted successfully from S3.");
        return String.format("%1$s deleted successfully", objectKey);
    }
}
