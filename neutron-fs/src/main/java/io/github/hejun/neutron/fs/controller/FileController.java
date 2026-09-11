package io.github.hejun.neutron.fs.controller;

import io.github.hejun.neutron.common.core.dto.Result;
import io.github.hejun.neutron.fs.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 文件 Controller
 *
 * @author HeJun
 */
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("{*file}")
    @PreAuthorize("hasAuthority('file:read')")
    public Mono<ResponseEntity<Flux<DataBuffer>>> download(@PathVariable String file) {
        return fileService.download(file);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('file:write')")
    public Mono<Result<String>> upload(FilePart file,
                                       @RequestPart(required = false) FormFieldPart isPublic,
                                       @RequestPart(required = false) FormFieldPart expireDays) {
        Boolean extractedIsPublic = Optional.ofNullable(isPublic)
            .map(FormFieldPart::value)
            .filter(StringUtils::hasText)
            .map(Boolean::valueOf)
            .orElse(false);
        Integer extractedExpireDays = Optional.ofNullable(expireDays)
            .map(FormFieldPart::value)
            .filter(StringUtils::hasText)
            .map(Integer::valueOf)
            .orElse(null);
        return fileService.upload(file, extractedIsPublic, extractedExpireDays).map(Result::SUCCESS);
    }

}
