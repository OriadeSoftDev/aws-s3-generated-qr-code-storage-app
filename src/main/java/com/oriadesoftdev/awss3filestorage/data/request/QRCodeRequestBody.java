package com.oriadesoftdev.awss3filestorage.data.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.*;
import java.util.List;

public class QRCodeRequestBody {
    @NotBlank(message = "Data cannot be blank")
    @NotNull
    private String data;

    @JsonProperty(value = "image_url")
    @Null(message = "Image URL cannot be null. It must be a valid HTTP/HTTPS URL")
    @Pattern(regexp = "^(http|https)://.*$", message = "Image URL must be a valid HTTP/HTTPS URL")
    private String imageUrl;

    @NotBlank(message = "S3 folder path cannot be blank")
    @NotNull(message = "S3 folder path is a non nullable string")
    @JsonProperty(value = "bucket_folder_path")
    private String bucketFolderPath;

    @Null(message = "Color must be null or in a valid format")
    @JsonProperty(value = "foreground_color")
    @Pattern(
            regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$",
            message = "Invalid hexadecimal color format"
    )
    private String foregroundColor;

    @Null(message = "Color must be null or in a valid format")
    @JsonProperty(value = "background_grad_colors")
    @Pattern(
            regexp = "^\\[\"#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})\"(, ?\"#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})\")*]$",
            message = "Invalid hexadecimal color array format"
    )
    private List<String> backgroundGradientColors;

    public QRCodeRequestBody() {

    }

    public QRCodeRequestBody(String data, String s3FolderPath) {
        this.data = data;
        this.bucketFolderPath = s3FolderPath;
    }


    public QRCodeRequestBody(String data, String imageUrl, String bucketFolderPath, String foregroundColor,
                             List<String> backgroundGradientColors) {
        this.data = data;
        this.imageUrl = imageUrl;
        this.bucketFolderPath = bucketFolderPath;
        this.foregroundColor = foregroundColor;
        this.backgroundGradientColors = backgroundGradientColors;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBucketFolderPath() {
        return bucketFolderPath;
    }

    public void setBucketFolderPath(String bucketFolderPath) {
        this.bucketFolderPath = bucketFolderPath;
    }

    public String getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(String foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public List<String> getBackgroundGradientColors() {
        return backgroundGradientColors;
    }

    public void setBackgroundGradientColors(List<String> backgroundGradientColors) {
        this.backgroundGradientColors = backgroundGradientColors;
    }
}
