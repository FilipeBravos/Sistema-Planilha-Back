package com.filipebravos.planilha.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Linha da planilha: um dia de trabalho. Os campos calculados (dia da semana,
 * totais e faturamento líquido) não são persistidos, ver {@code CalculoRegistro}.
 */
@Entity
@Table(name = "registro_diario")
public class RegistroDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    // Horário de cada motorista; nulo quando a pessoa não trabalhou no dia.
    private LocalTime horaInicialVagner;
    private LocalTime horaFinalVagner;
    private LocalTime horaInicialFilipe;
    private LocalTime horaFinalFilipe;

    @Column(nullable = false)
    private int kmInicial;

    @Column(nullable = false)
    private int kmFinal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cargaPosto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorVagner = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorFilipe = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicialVagner() { return horaInicialVagner; }
    public void setHoraInicialVagner(LocalTime v) { this.horaInicialVagner = v; }

    public LocalTime getHoraFinalVagner() { return horaFinalVagner; }
    public void setHoraFinalVagner(LocalTime v) { this.horaFinalVagner = v; }

    public LocalTime getHoraInicialFilipe() { return horaInicialFilipe; }
    public void setHoraInicialFilipe(LocalTime v) { this.horaInicialFilipe = v; }

    public LocalTime getHoraFinalFilipe() { return horaFinalFilipe; }
    public void setHoraFinalFilipe(LocalTime v) { this.horaFinalFilipe = v; }

    public int getKmInicial() { return kmInicial; }
    public void setKmInicial(int kmInicial) { this.kmInicial = kmInicial; }

    public int getKmFinal() { return kmFinal; }
    public void setKmFinal(int kmFinal) { this.kmFinal = kmFinal; }

    public BigDecimal getCargaPosto() { return cargaPosto; }
    public void setCargaPosto(BigDecimal cargaPosto) { this.cargaPosto = cargaPosto; }

    public BigDecimal getValorVagner() { return valorVagner; }
    public void setValorVagner(BigDecimal valorVagner) { this.valorVagner = valorVagner; }

    public BigDecimal getValorFilipe() { return valorFilipe; }
    public void setValorFilipe(BigDecimal valorFilipe) { this.valorFilipe = valorFilipe; }
}
