package com.royaltown.mesa.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Una transición legal del ciclo de vida, validada en base de datos. */
@Entity
@Table(name = "transiciones_estado")
public class TransicionEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transicion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado_origen", nullable = false)
    private Estado origen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado_destino", nullable = false)
    private Estado destino;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Estado getOrigen() {
        return origen;
    }

    public void setOrigen(Estado origen) {
        this.origen = origen;
    }

    public Estado getDestino() {
        return destino;
    }

    public void setDestino(Estado destino) {
        this.destino = destino;
    }
}
