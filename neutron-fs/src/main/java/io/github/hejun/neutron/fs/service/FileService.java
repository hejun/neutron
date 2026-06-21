package io.github.hejun.neutron.fs.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 文件 Service
 *
 * @author HeJun
 */
public interface FileService {

    Mono<String> upload(FilePart file, Boolean isPublic, Integer expireAfterDays);

    Mono<ResponseEntity<Flux<DataBuffer>>> download(String file);

}
