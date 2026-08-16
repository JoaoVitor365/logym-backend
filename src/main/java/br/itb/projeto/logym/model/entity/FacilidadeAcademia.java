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
@Table(name = "FacilidadeAcademia")
public class FacilidadeAcademia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "facilidade_id", nullable = false)
    private Facilidade facilidade;

    @Column(length = 20, nullable = false)
    private String statusFacilidadeAcademia;

    public Long getId() {
        return id;
    }

    public Academia getAcademia() {
        return academia;
    }

    public Facilidade getFacilidade() {
        return facilidade;
    }

    public String getStatusFacilidadeAcademia() {
        return statusFacilidadeAcademia;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }

    public void setFacilidade(Facilidade facilidade) {
        this.facilidade = facilidade;
    }

    public void setStatusFacilidadeAcademia(String statusFacilidadeAcademia) {
        this.statusFacilidadeAcademia = statusFacilidadeAcademia;
    }
}
