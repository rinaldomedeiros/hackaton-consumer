package com.fiap.hackaton.grp14.consumer.controller;

import com.fiap.hackaton.grp14.consumer.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/zip")
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ConsumerController {

    @Autowired
    private ServletContext servletContext;

    private final RedisService redisService;

    @GetMapping()
    @Operation(summary = "Este endpoint é responsável por listar todos os videos processados.")
    public ResponseEntity<Map<String, String>> getAllVideoEntries() {
        return ResponseEntity.ok(redisService.getAllVideoEntries());
    }

    @GetMapping("/paths")
    @Operation(summary = "Este endpoint é responsável por listar os caminhos dos arquivos disponíveis para download.")
    public ResponseEntity<List<String>> getAllVideoPaths() {
        return ResponseEntity.ok(redisService.getAllPaths());
    }

    @GetMapping("/keys")
    @Operation(summary = "Este endpoint é responsável por listar as chaves de todos os arquivos disponíveis para download")
    public ResponseEntity<Set<String>> getAllVideoKeys() {
        return ResponseEntity.ok(redisService.getSafeVideoKeys());
    }

    @GetMapping("/file")
    @Operation(summary = "Este endpoint é responsável por listar o caminho de um vídeo específico")
    public ResponseEntity<String> getFilePath(@RequestParam("video-id") String videoId) {
        return ResponseEntity.ok(redisService.getValue(videoId));
    }


    @GetMapping("/download")
    @Operation(summary = "Este endpoint é responsável por realizar o download de um arquivo")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("video-id") String videoId) {
        try {
            String filePath = redisService.getValue(videoId);
            if (filePath == null) {
                return ResponseEntity.notFound().build();
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(file.getAbsolutePath());
            MediaType mediaType = MediaType.parseMediaType(Files.probeContentType(path));

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(mediaType)
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            log.error("Error during file download: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
