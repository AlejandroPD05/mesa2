import { describe, expect, it } from 'vitest'
import {
  destinoPorRol,
  nombreEstado,
  nombreRol,
  tonoEstado,
  tonoPrioridad,
  valorLegible,
} from '../utils/presentacion'

describe('etiquetas de presentación', () => {
  it('traduce los nombres técnicos de estado', () => {
    expect(nombreEstado('En_Progreso')).toBe('En progreso')
    expect(nombreEstado('Pendiente_Usuario')).toBe('Pendiente de ti')
  })

  it('asigna tonos coherentes por estado y prioridad', () => {
    expect(tonoEstado('Resuelto')).toBe('exito')
    expect(tonoEstado('En_Progreso')).toBe('progreso')
    expect(tonoPrioridad('Critica')).toBe('peligro')
    expect(tonoPrioridad('Baja')).toBe('neutro')
  })

  it('traduce el rol del usuario', () => {
    expect(nombreRol('administrador')).toBe('Administrador')
    expect(nombreRol('tecnico')).toBe('Técnico')
  })

  it('en el historial, los estados se muestran legibles y los nulos como guion', () => {
    expect(valorLegible('estado', 'En_Progreso')).toBe('En progreso')
    expect(valorLegible('tecnico_asignado', null)).toBe('—')
  })

  it('lleva a cada perfil a su pantalla tras entrar', () => {
    expect(destinoPorRol('empleado')).toBe('/mis-tickets')
    expect(destinoPorRol('tecnico')).toBe('/bandeja')
    expect(destinoPorRol('administrador')).toBe('/bandeja')
  })
})
