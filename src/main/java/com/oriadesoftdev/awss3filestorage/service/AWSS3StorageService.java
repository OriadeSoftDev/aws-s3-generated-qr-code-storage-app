package com.oriadesoftdev.awss3filestorage.service;

import com.oriadesoftdev.awss3filestorage.data.request.QRCodeRequestBody;
import com.oriadesoftdev.awss3filestorage.qrcode.RoundQRCodeWithLogo;
import com.oriadesoftdev.awss3filestorage.util.QRCodeUtilsKt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import java.util.List;
import java.util.UUID;

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

    @Autowired
    private ImageDownloaderService imageDownloaderService;

    public String uploadFile(QRCodeRequestBody body) {
        System.out.println(body.getData());
        System.out.println(bucketName);
        // Replace with your URL

        try {
            String logoFileName = UUID.randomUUID().toString();
            HttpStatus httpStatus = downloadLogoImage(body.getImageUrl(), logoFileName);
            Path logoPath = staticResourceService.getPathToSlydoLogo();
            if (httpStatus == HttpStatus.OK) {
                logoPath = staticResourceService.getPathToStaticFile(logoFileName);
                System.err.println(logoPath.toString());
            }
            String outputFileName = UUID.randomUUID().toString();
            String slydoFolder = "slydo";
            String qrCodeFileName = String.format("%1$s.png", outputFileName);
            String cwd = System.getProperty("user.dir");
            File outputFile = new File(cwd, Paths.get(slydoFolder, qrCodeFileName).toString());
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            String bucketKey = String.format("%1$s/%2$s", slydoFolder, qrCodeFileName);
            if (!body.getBucketFolderPath().isBlank())
                bucketKey = body.getBucketFolderPath();
            Color foregroundColor = null;
            if (body.getForegroundColor() != null)
                foregroundColor = color(body.getForegroundColor());
            List<Color> backgroundGradientColors = null;
            if (body.getBackgroundGradientColors() != null)
                backgroundGradientColors = body.getBackgroundGradientColors()
                        .stream()
                        .map(QRCodeUtilsKt::color).toList();
            createQRCodeWithLogo(
                    body.getData(),
                    foregroundColor,
                    backgroundGradientColors,
                    logoPath, outputFile.getPath());
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(bucketKey)
                    .build();
            s3Client.putObject(putObjectRequest, outputFile.toPath());
//            if (httpStatus == HttpStatus.OK)
//                if (logoPath.toFile().exists())
//                    logoPath.toFile().delete();
//            outputFile.delete();
            System.out.println("File uploaded successfully to S3 bucket.");
            return String.format("%1$s uploaded successfully", qrCodeFileName);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private HttpStatus downloadLogoImage(String imageUrl, String logoFileName) {
        try {
            HttpStatus httpStatus = imageDownloaderService.downloadImage(imageUrl, "/", logoFileName);
            if (httpStatus == HttpStatus.OK)
                return HttpStatus.OK;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
//            throw new RuntimeException(e);
        }
        return HttpStatus.BAD_REQUEST;
    }

    private void createQRCodeWithLogo(String url, Color foregroundColor, List<Color> backgroundGradientColors,
                                      Path logoPath, String outputPath) {
        RoundQRCodeWithLogo.Builder qrCodeBuilder = new RoundQRCodeWithLogo.Builder()
                .data(url)
                .outputPath(outputPath)
                .logoPath(logoPath);
        if (foregroundColor == null)
            qrCodeBuilder.foregroundColor();
        if (backgroundGradientColors == null)
            qrCodeBuilder.backgroundGradientColors();
        RoundQRCodeWithLogo qrCodeWithLogo = qrCodeBuilder.build();
        qrCodeWithLogo.createQRCodeWithLogo(25);
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
