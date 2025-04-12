package com.fiap.hackaton.grp14.producer.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fiap.hackaton.grp14.producer.kafka.KafkaVideoProducer;
import com.fiap.hackaton.grp14.producer.model.Video;
import com.fiap.hackaton.grp14.producer.model.VideoStatus;
import com.fiap.hackaton.grp14.producer.repository.VideoRepository;

@Service
public class VideoService {

    private final static String VIDEO_PATH = "%s/%s.mp4";

    @Value("${app.video.input.path}")
    private String videoInputPath;
    private final VideoRepository videoRepository;
    private final KafkaVideoProducer kafkaVideoProducer;

    public VideoService(VideoRepository videoRepository, KafkaVideoProducer kafkaVideoProducer) {
        this.videoRepository = videoRepository;
        this.kafkaVideoProducer = kafkaVideoProducer;
    }

    /**
     * Faz o upload do vídeo:
     * - Salva o arquivo localmente.
     * - Cria um registro no banco de dados com status PENDING.
     * - Envia uma mensagem ao Kafka para disparar o processamento.
     * Retorna o videoId gerado.
     */
    public String uploadVideo(MultipartFile file, String username) throws Exception {
        // Gera um ID único para o vídeo
        String videoId = UUID.randomUUID().toString();

        // Define o caminho do arquivo (usaremos o videoId + ".mp4" para salvar o arquivo)
        String videoPath = String.format(VIDEO_PATH, this.videoInputPath, videoId);

        // Cria o diretório se não existir
        File videoDirectory = new File(this.videoInputPath);
        if (!videoDirectory.exists() && !videoDirectory.mkdirs()) {
            throw new IOException("Não foi possível criar o diretório " + VIDEO_PATH);
        }

        // Salva o arquivo de vídeo no sistema de arquivos
        file.transferTo(new File(videoPath));

        // Cria e salva o registro no banco de dados com status PENDING.
        Video video = new Video();
        video.setFilename(videoId); // Armazenamos o videoId sem extensão para facilitar a busca
        video.setStatus(VideoStatus.PENDING);
        video.setUploadTime(LocalDateTime.now());
        video.setUsername(username);
        videoRepository.save(video);

        // Envia a mensagem para o Kafka para processar o vídeo
        kafkaVideoProducer.sendVideoMessage(videoId);

        return videoId;
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

