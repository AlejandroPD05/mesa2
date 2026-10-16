import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { BotonTema } from '../componentes/BotonTema'
import { CLASE_TEMA_OSCURO } from '../utils/tema'

describe('botón de tema', () => {
  beforeEach(() => {
    localStorage.clear()
    document.documentElement.className = ''
    document.head.innerHTML = '<meta name="theme-color" content="#ffffff" />'
  })

  it('en claro etiqueta la acción de pasar a oscuro', () => {
    render(<BotonTema />)
    expect(screen.getByRole('button', { name: 'Cambiar a modo oscuro' })).toBeInTheDocument()
  })

  it('al pulsarlo activa el modo oscuro, lo guarda y permite volver', async () => {
    const usuario = userEvent.setup()
    render(<BotonTema />)

    await usuario.click(screen.getByRole('button', { name: 'Cambiar a modo oscuro' }))
    expect(document.documentElement).toHaveClass(CLASE_TEMA_OSCURO)
    expect(localStorage.getItem('mesa2.tema')).toBe('oscuro')
    expect(screen.getByRole('button', { name: 'Cambiar a modo claro' })).toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: 'Cambiar a modo claro' }))
    expect(document.documentElement).not.toHaveClass(CLASE_TEMA_OSCURO)
    expect(localStorage.getItem('mesa2.tema')).toBe('claro')
  })

  it('arranca en oscuro si el usuario lo dejó elegido', () => {
    localStorage.setItem('mesa2.tema', 'oscuro')
    render(<BotonTema />)
    expect(document.documentElement).toHaveClass(CLASE_TEMA_OSCURO)
    expect(screen.getByRole('button', { name: 'Cambiar a modo claro' })).toBeInTheDocument()
  })
})
