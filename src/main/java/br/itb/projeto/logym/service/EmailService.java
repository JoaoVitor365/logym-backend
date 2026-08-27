package br.itb.projeto.logym.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void enviarCodigoRecuperacao(String destino, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destino);
        mensagem.setSubject("Recuperacao de senha - LOGYM");
        mensagem.setText("Você solicitou a recuperação de senha do LOGYM.\n\n"
                + "Código de recuperação: " + codigo + "\n"
                + "Este código é válido por 15 minutos.\n\n"
                + "Se você não solicitou esta recuperação, ignore este e-mail.");
        javaMailSender.send(mensagem);
    }
}
