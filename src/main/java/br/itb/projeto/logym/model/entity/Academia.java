package br.itb.projeto.logym.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "Academia")
public class Academia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 14, nullable = false)
    private String cnpj;

    @Column(length = 400, nullable = false)
    private String descricao;

    @Column(length = 8, nullable = false)
    private String cep;

    @Column(length = 150)
    private String endereco;

    @Column(nullable = false)
    private BigDecimal numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 80)
    private String bairro;

    @Column(length = 80)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 25, nullable = false)
    private String telefone;

    @Column(length = 25)
    private String celular;

    @Column(length = 100)
    private String email;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 300)
    private String categorias;

    @Transient
    private List<Long> categoriaIds;

    @Transient
    private List<Categoria> categoriasVinculadas;

    @Column(length = 300)
    private String facilidades;

    @Transient
    private List<Long> facilidadeIds;

    @Transient
    private List<Facilidade> facilidadesVinculadas;

    private BigDecimal nota;

    @ManyToOne
    @JoinColumn(name = "gerente_id", nullable = false)
    private Gerente gerente;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    @Column(length = 20, nullable = false)
    private String statusAcademia;

    @Column(length = 20)
    private String statusAnteriorBloqueioGerente;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCep() {
        return cep;
    }

    public String getEndereco() {
        return endereco;
    }

    public BigDecimal getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getEstado() {
        return estado;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCelular() {
        return celular;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getCategorias() {
        return categorias;
    }

    public List<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public List<Categoria> getCategoriasVinculadas() {
        return categoriasVinculadas;
    }

    public String getFacilidades() {
        return facilidades;
    }

    public List<Long> getFacilidadeIds() {
        return facilidadeIds;
    }

    public List<Facilidade> getFacilidadesVinculadas() {
        return facilidadesVinculadas;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public Gerente getGerente() {
        return gerente;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public String getStatusAcademia() {
        return statusAcademia;
    }

    public String getStatusAnteriorBloqueioGerente() {
        return statusAnteriorBloqueioGerente;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public void setNumero(BigDecimal numero) {
        this.numero = numero;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public void setCategorias(String categorias) {
        this.categorias = categorias;
    }

    public void setCategoriaIds(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }

    public void setCategoriasVinculadas(List<Categoria> categoriasVinculadas) {
        this.categoriasVinculadas = categoriasVinculadas;
    }

    public void setFacilidades(String facilidades) {
        this.facilidades = facilidades;
    }

    public void setFacilidadeIds(List<Long> facilidadeIds) {
        this.facilidadeIds = facilidadeIds;
    }

    public void setFacilidadesVinculadas(List<Facilidade> facilidadesVinculadas) {
        this.facilidadesVinculadas = facilidadesVinculadas;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public void setGerente(Gerente gerente) {
        this.gerente = gerente;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public void setStatusAcademia(String statusAcademia) {
        this.statusAcademia = statusAcademia;
    }

    public void setStatusAnteriorBloqueioGerente(String statusAnteriorBloqueioGerente) {
        this.statusAnteriorBloqueioGerente = statusAnteriorBloqueioGerente;
    }
}
