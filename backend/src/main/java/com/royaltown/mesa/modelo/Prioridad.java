package com.royaltown.mesa.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prioridades")
public class Prioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prioridad")
    private Integer id;

    @Column(nullable = false, unique = true, length = 15)
    private String nombre;

    @Column(nullable = false, unique = true)
    private Integer orden;

    @Column(name = "sla_horas", nullable = false)
    private Integer slaHoras;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getSlaHoras() {
        return slaHoras;
    }

    public void setSlaHoras(Integer slaHoras) {
        this.slaHoras = slaHoras;
    }
}
