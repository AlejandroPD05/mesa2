import type { ReactNode } from 'react'

export function Cargando({ etiqueta = 'Cargando…' }: { etiqueta?: string }) {
  return (
    <div className="cargando" role="status">
      <span className="girador" aria-hidden="true" />
      <span>{etiqueta}</span>
    </div>
  )
}

export function Aviso({ tono = 'peligro', children }: { tono?: 'peligro' | 'aviso' | 'info'; children: ReactNode }) {
  return (
    <p className={`aviso aviso--${tono}`} role="alert">
      {children}
    </p>
  )
}

export function Vacio({ titulo, children }: { titulo: string; children?: ReactNode }) {
  return (
    <div className="vacio">
      <h3>{titulo}</h3>
      {children ? <p>{children}</p> : null}
    </div>
  )
}
