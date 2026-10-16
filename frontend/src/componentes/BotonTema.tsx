import { useEffect, useState } from 'react'
import { alternarTema, aplicarTema, guardarTema, temaInicial, type Tema } from '../utils/tema'

/**
 * Interruptor claro/oscuro de la cabecera. Recuerda la elección entre visitas
 * y, si el sistema no pide movimiento reducido, la paleta gira con una
 * transición fluida (la clase `tema-animando` activa el fundido global).
 */
export function BotonTema() {
  const [tema, setTema] = useState<Tema>(temaInicial)
  const esOscuro = tema === 'oscuro'

  useEffect(() => {
    aplicarTema(tema)
  }, [tema])

  function cambiar() {
    const sinMovimiento =
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (!sinMovimiento) {
      const raiz = document.documentElement
      raiz.classList.add('tema-animando')
      window.setTimeout(() => raiz.classList.remove('tema-animando'), 500)
    }

    const siguiente = alternarTema(tema)
    guardarTema(siguiente)
    setTema(siguiente)
  }

  const etiqueta = esOscuro ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'

  return (
    <button
      type="button"
      className="boton boton--secundario boton--pequeno boton-tema"
      onClick={cambiar}
      aria-label={etiqueta}
      title={etiqueta}
    >
      {esOscuro ? (
        <svg
          aria-hidden="true"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
        >
          <circle cx="12" cy="12" r="4" />
          <path d="M12 2v2.5M12 19.5V22M4.9 4.9l1.8 1.8M17.3 17.3l1.8 1.8M2 12h2.5M19.5 12H22M4.9 19.1l1.8-1.8M17.3 6.7l1.8-1.8" />
        </svg>
      ) : (
        <svg
          aria-hidden="true"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" />
        </svg>
      )}
    </button>
  )
}
