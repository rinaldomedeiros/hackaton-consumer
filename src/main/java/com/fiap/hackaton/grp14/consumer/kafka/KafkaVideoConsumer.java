package com.fiap.hackaton.grp14.consumer.kafka;

import com.fiap.hackaton.grp14.consumer.service.VideoProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaVideoConsumer {

    private final static String VIDEO_PATH = "%s/%s.mp4";

    @Value("${app.video.input.path}")
    private String videoInputPath;

    private final VideoProcessingService videoProcessingService;

    public KafkaVideoConsumer(VideoProcessingService videoProcessingService) {
        this.videoProcessingService = videoProcessingService;
    }


    @KafkaListener(topics = "video-process-topic", groupId = "video-processing-group")
    public void consumeVideoMessage(String videoId) {
        log.info("Mensagem recebida para processar o vídeo: {}", videoId);

        try {
            String videoPath = String.format(VIDEO_PATH, videoInputPath, videoId);
            videoProcessingService.processVideo(videoPath, videoId);
        } catch (Exception e) {
            log.error("Erro ao processar o vídeo com ID {}: {}", videoId, e.getMessage(), e);
            // Relança a exceção para interromper o consumo da mensagem
            throw e;
        }

    }
}

