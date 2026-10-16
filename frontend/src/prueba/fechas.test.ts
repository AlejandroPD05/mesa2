import { describe, expect, it } from 'vitest'
import { comoTiempoRestante, formatearDuracion } from '../utils/fechas'

describe('formatearDuracion', () => {
  it('formatea minutos sueltos', () => {
    expect(formatearDuracion(45)).toBe('45 min')
  })

  it('formatea horas y minutos', () => {
    expect(formatearDuracion(135)).toBe('2 h 15 min')
  })

  it('omite los minutos cuando son cero', () => {
    expect(formatearDuracion(120)).toBe('2 h')
  })

  it('sin datos devuelve un guion', () => {
    expect(formatearDuracion(null)).toBe('—')
  })
})

describe('comoTiempoRestante', () => {
  it('usa días cuando quedan más de 48 h', () => {
    expect(comoTiempoRestante(72 * 60)).toBe('3 días')
  })

  it('combina días y horas sueltas', () => {
    expect(comoTiempoRestante(50 * 60)).toBe('2 días 2 h')
  })

  it('bajo 48 h usa horas y minutos', () => {
    expect(comoTiempoRestante(90)).toBe('1 h 30 min')
  })

  it('acepta valores negativos (retraso del SLA)', () => {
    expect(comoTiempoRestante(-180)).toBe('3 h')
  })
})
