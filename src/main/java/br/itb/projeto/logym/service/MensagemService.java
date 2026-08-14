package br.itb.projeto.logym.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.itb.projeto.logym.model.entity.Mensagem;
import br.itb.projeto.logym.repository.MensagemRepository;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;

    public MensagemService(MensagemRepository mensagemRepository) {
        this.mensagemRepository = mensagemRepository;
    }

    public Mensagem enviarMensagem(Mensagem mensagem) {

        mensagem.setDataMensagem(LocalDateTime.now());
        mensagem.setStatusMensagem("ATIVO");

        return mensagemRepository.save(mensagem);
    }

    public List<Mensagem> findAll() {
        return mensagemRepository.findAll();
    }

    public Mensagem findById(Long id) {
        return mensagemRepository.findById(id).orElse(null);
    }

    public Mensagem abrirMensagem(Long id) {
        Mensagem _mensagem = mensagemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));

        _mensagem.setStatusMensagem("LIDA");
        _mensagem.setDataAtualizacao(LocalDateTime.now());

        return mensagemRepository.save(_mensagem);
    }

    public Mensagem inativar(Long id) {
        Mensagem _mensagem = mensagemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));

        _mensagem.setStatusMensagem("INATIVA");
        _mensagem.setDataAtualizacao(LocalDateTime.now());

        return mensagemRepository.save(_mensagem);
    }

}
