package io.github.hejun.neutron.fs.service.impl;

import io.github.hejun.neutron.common.core.dto.Result;
import io.github.hejun.neutron.fs.enums.MinioConstants;
import io.github.hejun.neutron.fs.properties.MinioProperties;
import io.github.hejun.neutron.fs.service.FileService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Filter;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.Status;
import io.minio.messages.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.json.JsonMapper;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 文件 Service Minio 实现
 *
 * @author HeJun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileServiceImpl implements FileService {

    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
    private final JsonMapper jsonMapper;

    private final MinioAsyncClient minioClient;
    private final MinioProperties properties;

    @PostConstruct
    public void initBucket() {
        minioClient.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucketName()).build())
            .thenCompose(exists -> {
                if (!exists) {
                    log.debug("Bucket {} not exists, creating...", properties.getBucketName());

                    List<Integer> expireDays = new ArrayList<>();
                    ReflectionUtils.doWithFields(MinioConstants.ExpireTimes.class, field -> expireDays.add(field.getInt(null)));
                    List<LifecycleConfiguration.Rule> rules = expireDays.stream().map(day -> {
                            Filter filter = new Filter(new Tag(MinioConstants.FILE_ATTR.EXPIRE_DAYS, day.toString()));
                            LifecycleConfiguration.Expiration expiration = new LifecycleConfiguration.Expiration((ZonedDateTime) null, day, null, null);
                            return new LifecycleConfiguration.Rule(Status.ENABLED, null, expiration, filter, null, null, null, null);
                        })
                        .toList();

                    return minioClient
                        .makeBucket(MakeBucketArgs.builder().bucket(properties.getBucketName()).build())
                        .thenCompose(resp ->
                            minioClient.setBucketLifecycle(
                                SetBucketLifecycleArgs.builder()
                                    .bucket(properties.getBucketName())
                                    .config(new LifecycleConfiguration(rules))
                                    .build()
                            )
                        );
                }
                return CompletableFuture.completedFuture(null);
            })
            .exceptionally(ex -> {
                log.error("Failed to process bucket initialization for {}", properties.getBucketName(), ex);
                return null;
            });
    }

    @Override
    public Mono<String> upload(FilePart file, Boolean isPublic, Integer expireDays) {
        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull(SecurityContext::getAuthentication)
            .map(authentication -> Boolean.TRUE.equals(isPublic) || !authentication.isAuthenticated() ? "public" : authentication.getName())
            .flatMap(username -> {
                String ext = FilenameUtils.getExtension(file.filename());
                ext = StringUtils.hasText(ext) ? "." + ext : "";
                String objectName = username + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
                String encodedOriginalName = URLEncoder.encode(file.filename(), StandardCharsets.UTF_8);

                Map<String, String> tags = new HashMap<>();
                if (expireDays != null) {
                    tags.put(MinioConstants.FILE_ATTR.EXPIRE_DAYS, expireDays.toString());
                }

                String contentType = Optional.ofNullable(file.headers().getContentType())
                    .map(MediaType::toString)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

                return Mono.usingWhen(
                    Mono
                        .fromCallable(() -> Files
                            .createTempFile("fs-upload-", ".tmp"))
                        .subscribeOn(Schedulers.boundedElastic()),
                    tempFile -> file
                        .transferTo(tempFile)
                        .then(Mono.fromCallable(() -> UploadObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .filename(tempFile.toAbsolutePath().toString())
                            .contentType(contentType)
                            .tags(tags)
                            .userMetadata(Map.of(MinioConstants.FILE_ATTR.ORIGINAL_NAME, encodedOriginalName))
                            .build()
                        ))
                        .flatMap(args -> Mono.fromFuture(minioClient.uploadObject(args)))
                        .map(ObjectWriteResponse::object)
                        .map(object -> "/fs/" + object),
                    tempFile -> Mono
                        .fromCallable(() -> Files.deleteIfExists(tempFile))
                        .subscribeOn(Schedulers.boundedElastic())
                );
            });
    }

    @Override
    public Mono<ResponseEntity<Flux<DataBuffer>>> download(String file) {
        Mono<Boolean> accessCheck = file.startsWith("/public/") ?
            Mono.just(true) :
            ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .map(auth -> auth.isAuthenticated() && file.startsWith("/" + auth.getName() + "/"))
                .defaultIfEmpty(false);

        return accessCheck
            .filter(Boolean.TRUE::equals)
            .switchIfEmpty(Mono.error(new AccessDeniedException("无权下载")))
            .flatMap(authentication -> Mono.fromFuture(minioClient.statObject(
                StatObjectArgs.builder().bucket(properties.getBucketName()).object(file).build()
            )))
            .flatMap(stat -> {
                MediaType mediaType = Optional.ofNullable(stat.contentType())
                    .map(MediaType::parseMediaType)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

                String originalName = stat.userMetadata().getFirst(MinioConstants.FILE_ATTR.ORIGINAL_NAME);
                String finalOriginalName = StringUtils.hasText(originalName) ? URLDecoder.decode(originalName, StandardCharsets.UTF_8) : null;

                return Mono
                    .fromFuture(minioClient.getObject(GetObjectArgs.builder().bucket(properties.getBucketName()).object(file).build()))
                    .map(objectResp -> {
                        Flux<DataBuffer> bufferFlux = Flux.using(
                            () -> objectResp,
                            resp -> DataBufferUtils.readInputStream(() -> resp, bufferFactory, 8192),
                            resp -> {
                                try {
                                    resp.close();
                                } catch (Exception e) {
                                    log.warn("Failed to close Minio response stream", e);
                                }
                            }
                        ).subscribeOn(Schedulers.boundedElastic());
                        ResponseEntity.BodyBuilder builder = ResponseEntity.ok().contentType(mediaType);
                        if (StringUtils.hasText(finalOriginalName)) {
                            String encodedFileName = URLEncoder.encode(finalOriginalName, StandardCharsets.UTF_8).replace("+", "%20");
                            builder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"");
                        }
                        return builder.body(bufferFlux);
                    });
            })
            .onErrorResume(AccessDeniedException.class, ex -> this.errorResp(file, Result.ERROR(401, ex.getMessage())))
            .onErrorResume(ErrorResponseException.class, ex -> {
                String code = ex.errorResponse().code();
                if ("NoSuchKey".equals(code) || "ResourceNotFound".equals(code) || "NoSuchBucket".equals(code)) {
                    return this.errorResp(file, Result.ERROR(404, "文件不存在"));
                }
                return this.errorResp(file, Result.ERROR(500, "文件服务异常"), ex);
            })
            .onErrorResume(ex -> this.errorResp(file, Result.ERROR(500, "系统内部错误"), ex));
    }

    private Mono<ResponseEntity<Flux<DataBuffer>>> errorResp(String file, Result<?> result) {
        return this.errorResp(file, result, null);
    }

    private Mono<ResponseEntity<Flux<DataBuffer>>> errorResp(String file, @NonNull Result<?> result, @Nullable Throwable ex) {
        if (ex != null) {
            log.error("文件服务异常: {}", file, ex);
        } else {
            log.debug("文件下载失败: {}", file);
        }
        return Mono.just(ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(Flux.just(
                bufferFactory.wrap(
                    jsonMapper.writeValueAsBytes(result)
                )
            )));
    }

}
