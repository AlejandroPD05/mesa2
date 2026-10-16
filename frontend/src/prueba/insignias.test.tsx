import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { InsigniaEstado, InsigniaPrioridad, InsigniaSla } from '../componentes/Etiquetas'

describe('insignias de ticket', () => {
  it('muestra el estado en lenguaje natural', () => {
    render(<InsigniaEstado estado="En_Progreso" />)
    expect(screen.getByText('En progreso')).toBeInTheDocument()
  })

  it('muestra la prioridad con su tono visual', () => {
    render(<InsigniaPrioridad prioridad="Critica" />)
    expect(screen.getByText('Critica')).toHaveClass('insignia--peligro')
  })

  it('avisa cuando el SLA está incumplido', () => {
    render(<InsigniaSla sla={{ relevante: true, incumplido: true, minutosRestantes: -180 }} />)
    expect(screen.getByText(/SLA incumplido/)).toBeInTheDocument()
  })

  it('pinta cuánto queda cuando el plazo sigue vivo', () => {
    render(<InsigniaSla sla={{ relevante: true, incumplido: false, minutosRestantes: 300 }} />)
    expect(screen.getByText(/SLA: queda 5 h/)).toBeInTheDocument()
  })

  it('no pinta nada si el ticket ya cerró', () => {
    const { container } = render(
      <InsigniaSla sla={{ relevante: false, incumplido: false, minutosRestantes: null }} />,
    )
    expect(container).toBeEmptyDOMElement()
  })
})
