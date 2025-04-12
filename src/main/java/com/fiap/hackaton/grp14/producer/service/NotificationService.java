package com.fiap.hackaton.grp14.producer.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notifyUser(String userEmail, String message) {
        // Simulação de envio de e-mail
        System.out.println("Enviando notificação para " + userEmail + ": " + message);
        // Aqui, você poderia integrar com algum serviço de e-mail, SMS, ou outro canal
    }
}