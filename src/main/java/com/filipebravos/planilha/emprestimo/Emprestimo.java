package com.filipebravos.planilha.emprestimo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Credor credor;

    /** Só preenchido quando o credor é TERCEIROS. */
    @Column(length = 120)
    private String nomeTerceiro;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    /** Em quantas vezes (parcelas) o empréstimo foi dividido. */
    @Column(nullable = false)
    private int parcelas;

    /** Quanto já foi pago até agora. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    public Long getId() { return id; }

    public Credor getCredor() { return credor; }
    public void setCredor(Credor credor) { this.credor = credor; }

    public String getNomeTerceiro() { return nomeTerceiro; }
    public void setNomeTerceiro(String nomeTerceiro) { this.nomeTerceiro = nomeTerceiro; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public int getParcelas() { return parcelas; }
    public void setParcelas(int parcelas) { this.parcelas = parcelas; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
}
