package com.fiap.hackaton.grp14.consumer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "videos")
public class Video {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VideoStatus status;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    @Column(name = "username", nullable = false)  // Renomeado de 'user' para 'username'
    private String username;

    // Construtores
    public Video() {
    }

    public Video(String filename, VideoStatus status, LocalDateTime uploadTime, String username) {
        this.filename = filename;
        this.status = status;
        this.uploadTime = uploadTime;
        this.username = username;
    }

}

