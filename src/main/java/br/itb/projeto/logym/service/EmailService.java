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
        mensagem.setText("Voce solicitou a recuperacao de senha do LOGYM.\n\n"
                + "Codigo de recuperacao: " + codigo + "\n"
                + "Este codigo e valido por 15 minutos.\n\n"
                + "Se voce nao solicitou esta recuperacao, ignore este e-mail.");
        javaMailSender.send(mensagem);
    }
}
