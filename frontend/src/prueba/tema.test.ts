import { beforeEach, describe, expect, it } from 'vitest'
import {
  CLASE_TEMA_OSCURO,
  CLAVE_TEMA,
  alternarTema,
  aplicarTema,
  guardarTema,
  temaInicial,
} from '../utils/tema'

describe('tema claro/oscuro', () => {
  beforeEach(() => {
    localStorage.clear()
    document.documentElement.className = ''
    document.head.innerHTML = '<meta name="theme-color" content="#ffffff" />'
  })

  it('la primera visita empieza en claro', () => {
    expect(temaInicial()).toBe('claro')
  })

  it('recuerda la elección guardada del usuario', () => {
    guardarTema('oscuro')
    expect(temaInicial()).toBe('oscuro')
  })

  it('ignora valores desconocidos en el almacenamiento', () => {
    localStorage.setItem(CLAVE_TEMA, 'turquesa')
    expect(temaInicial()).toBe('claro')
  })

  it('aplicarTema pone y quita la clase del documento', () => {
    aplicarTema('oscuro')
    expect(document.documentElement).toHaveClass(CLASE_TEMA_OSCURO)
    aplicarTema('claro')
    expect(document.documentElement).not.toHaveClass(CLASE_TEMA_OSCURO)
  })

  it('sincroniza el color de la barra del navegador', () => {
    const meta = () => document.querySelector('meta[name="theme-color"]')?.getAttribute('content')
    aplicarTema('oscuro')
    expect(meta()).toBe('#04100a')
    aplicarTema('claro')
    expect(meta()).toBe('#ffffff')
  })

  it('alternarTema cambia de claro a oscuro y vuelta', () => {
    expect(alternarTema('claro')).toBe('oscuro')
    expect(alternarTema('oscuro')).toBe('claro')
  })
})
