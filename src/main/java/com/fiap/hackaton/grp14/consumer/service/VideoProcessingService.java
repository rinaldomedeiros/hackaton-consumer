package com.fiap.hackaton.grp14.consumer.service;

import com.fiap.hackaton.grp14.consumer.model.VideoStatus;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class VideoProcessingService {

    private final static String VIDEO_ZIP_PATH = "%s/%s.zip";
    private final static String VIDEO_ZIP_OUTPUT_PATH = "%s/%s";

    @Value("${app.video.output.path}")
    private String videoOutputPath;

    private final VideoService videoService;

    public VideoProcessingService(VideoService videoService) {
        this.videoService = videoService;
    }

    public void processVideo(String videoPath, String videoId) {

        try {
            // Atualiza para PROCESSING antes de iniciar
            videoService.updateVideoStatus(videoId, VideoStatus.PROCESSING, null);

            // Configuração do FFmpeg e FFprobe com os caminhos instalados no container
            FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
            FFprobe ffprobe = new FFprobe("/usr/bin/ffprobe");

            // Define o diretório de saída para os frames
            String outputFolder = String.format(VIDEO_ZIP_OUTPUT_PATH, videoOutputPath, videoId);
            File dir = new File(outputFolder);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Não foi possível criar o diretório de saída: " + outputFolder);
            }
            
            // Configuração para extração dos frames
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(videoPath)
                    .addOutput(outputFolder + "/frame_%04d.jpg")
                    .setFrames(10)
                    .setVideoFilter("fps=1")
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            // Compacta as imagens em um arquivo ZIP
            String zipFilePath = String.format(VIDEO_ZIP_PATH, outputFolder, videoId);
            zipDirectory(outputFolder, zipFilePath);

            // Atualiza o status para COMPLETED após processamento bem-sucedido
            videoService.updateVideoStatus(videoId, VideoStatus.COMPLETED, zipFilePath);

            System.out.println("Processamento concluído e arquivo zip gerado: " + zipFilePath);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            // Em caso de erro, atualiza o status para FAILED
            videoService.updateVideoStatus(videoId, VideoStatus.FAILED, null);
        }
    }

    private void zipDirectory(String folderPath, String zipPath) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            File folder = new File(folderPath);
            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                throw new IOException("Nenhum arquivo encontrado para compactar no diretório: " + folderPath);
            }
            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                }
            }
        }
    }
}

