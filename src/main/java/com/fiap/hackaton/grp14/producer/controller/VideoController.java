package com.fiap.hackaton.grp14.producer.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fiap.hackaton.grp14.producer.model.Video;
import com.fiap.hackaton.grp14.producer.service.VideoService;

@Slf4j
@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Endpoint para upload de vídeo.
     * Recebe o arquivo e o nome do usuário, salva o arquivo localmente,
     * registra o vídeo no banco de dados com status PENDING e envia uma mensagem para o Kafka.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("video") MultipartFile file,
                                              @RequestParam("username") String username) {
        try {
            String videoId = videoService.uploadVideo(file, username);
            return ResponseEntity.ok("Vídeo recebido e processamento iniciado: " + videoId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erro ao fazer upload do vídeo: " + e.getMessage());
        }
    }

    /**
     * Endpoint para consulta do status do processamento do vídeo.
     * Busca no banco de dados o status associado ao vídeo (identificado pelo videoId).
     */
    @GetMapping("/status/{videoId}")
    public ResponseEntity<String> getVideoStatus(@PathVariable String videoId) {
        try {
            String status = videoService.getVideoStatus(videoId);
            return ResponseEntity.ok("Status do vídeo " + videoId + ": " + status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Erro ao buscar status do vídeo: " + e.getMessage());
        }
    }
    /**
     * Endpoint para buscar todos os vídeos
     */
    @GetMapping()
    public ResponseEntity<List<Video>> getAllVideos() {
        List<Video> videos = videoService.getAllVideos();
        return ResponseEntity.ok(videos);
    }
}

