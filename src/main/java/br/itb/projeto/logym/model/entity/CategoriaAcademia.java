package br.itb.projeto.logym.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CategoriaAcademia")
public class CategoriaAcademia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(length = 20, nullable = false)
    private String statusCategoriaAcademia;

    public Long getId() {
        return id;
    }

    public Academia getAcademia() {
        return academia;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getStatusCategoriaAcademia() {
        return statusCategoriaAcademia;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void setStatusCategoriaAcademia(String statusCategoriaAcademia) {
        this.statusCategoriaAcademia = statusCategoriaAcademia;
    }
}
