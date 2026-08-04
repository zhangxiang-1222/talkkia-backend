package com.talkkia.api.services;

import com.talkkia.api.dto.upload.CreateUploadRequest;
import com.talkkia.api.dto.upload.CreateUploadResponse;
import com.talkkia.api.dto.reply.CreateReplyUploadRequest;
import com.talkkia.api.dto.reply.CreateReplyUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class R2UploadService {

    private static final long MAX_AUDIO_SIZE = 50L * 1024 * 1024;
    private static final long MAX_REPLY_AUDIO_SIZE = 10L * 1024 * 1024;
    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(10);

    private static final Set<String> ALLOWED_TYPES = Set.of(
//            "audio/mpeg",
//            "audio/mp4",
//            "audio/aac",
//            "audio/wav",
//            "audio/x-wav",
//            "audio/ogg",
            "audio/m4a"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("m4a");

    private final S3Presigner presigner;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-base-url}")
    private String publicBaseUrl;

    public R2UploadService(S3Presigner presigner) {
        this.presigner = presigner;
    }

    public CreateUploadResponse createUploadUrl(
            String userId,
            CreateUploadRequest request,
            Long cityId
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Upload request is required");
        }

        String contentType = request.getContentType();
        Long fileSize = request.getFileSize();
        String extension = validateAudioUpload(
                request.getExtension(),
                contentType,
                fileSize,
                MAX_AUDIO_SIZE
        );

        String objectKey = String.format(
                "%s/%s.%s",
                userId,
                UUID.randomUUID(),
                extension
        );

        PresignedPutObjectRequest signedRequest = presignUpload(objectKey, contentType);

        return new CreateUploadResponse(
                objectKey,
                signedRequest.url().toString(),
                contentType,
                Instant.now().plus(PRESIGNED_URL_DURATION),
                cityId,
                null
        );
    }

    public CreateReplyUploadResponse createReplyUploadUrl(
            String userId,
            CreateReplyUploadRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Reply upload request is required");
        }

        String contentType = request.getContentType();
        String extension = validateAudioUpload(
                request.getExtension(),
                contentType,
                request.getFileSize(),
                MAX_REPLY_AUDIO_SIZE
        );
        String objectKey = String.format(
                "replies/%s/%s.%s",
                userId,
                UUID.randomUUID(),
                extension
        );
        PresignedPutObjectRequest signedRequest = presignUpload(objectKey, contentType);

        return new CreateReplyUploadResponse(
                null,
                objectKey,
                signedRequest.url().toString(),
                contentType,
                Instant.now().plus(PRESIGNED_URL_DURATION)
        );
    }

    public String createDownloadUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("R2 object key is required");
        }

        String baseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
        String normalizedObjectKey = objectKey.startsWith("/")
                ? objectKey.substring(1)
                : objectKey;
        return baseUrl + "/" + normalizedObjectKey;
    }

    private String validateAudioUpload(
            String extension,
            String contentType,
            Long fileSize,
            long maxFileSize
    ) {
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported audio type");
        }
        if (fileSize == null || fileSize <= 0 || fileSize > maxFileSize) {
            throw new IllegalArgumentException("Invalid audio file size");
        }

        String normalizedExtension = extension == null
                ? ""
                : extension.trim().toLowerCase(Locale.ROOT);
        if (normalizedExtension.startsWith(".")) {
            normalizedExtension = normalizedExtension.substring(1);
        }
        if (!ALLOWED_EXTENSIONS.contains(normalizedExtension)) {
            throw new IllegalArgumentException("Unsupported audio extension");
        }
        return normalizedExtension;
    }

    private PresignedPutObjectRequest presignUpload(
            String objectKey,
            String contentType
    ) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(PRESIGNED_URL_DURATION)
                        .putObjectRequest(putObjectRequest)
                        .build();
        return presigner.presignPutObject(presignRequest);
    }

}
