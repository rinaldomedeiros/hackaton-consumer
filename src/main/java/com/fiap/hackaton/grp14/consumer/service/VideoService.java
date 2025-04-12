package com.fiap.hackaton.grp14.consumer.service;

import com.fiap.hackaton.grp14.consumer.model.Video;
import com.fiap.hackaton.grp14.consumer.model.VideoStatus;
import com.fiap.hackaton.grp14.consumer.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoService {

    private final static String VIDEO_PATH = "%s/%s.mp4";

    @Value("${app.video.input.path}")
    private String videoInputPath;
    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    /**
     * Atualiza o status do vídeo no banco de dados.
     */
    public void updateVideoStatus(String videoId, VideoStatus status, String zipFilePath) {
        Optional<Video> opt = videoRepository.findByFilename(videoId);
        if (opt.isPresent()) {
            Video video = opt.get();
            video.setStatus(status);
            // Se desejar, você pode armazenar o caminho do ZIP em outro campo da entidade.
            videoRepository.save(video);
        } else {
            System.err.println("Vídeo com ID " + videoId + " não encontrado para atualizar status.");
        }
    }

    /**
     * Retorna o status do vídeo para o endpoint GET.
     */
    public String getVideoStatus(String videoId) {
        Optional<Video> videoOptional = videoRepository.findByFilename(videoId);
        return videoOptional.map(video -> video.getStatus().name())
                            .orElseThrow(() -> new RuntimeException("Vídeo não encontrado: " + videoId));
    }

    /**
     * Retorna todos videos registrados.
     */
    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }
}

