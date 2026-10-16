/**
 * Tema visual de la aplicación (claro u oscuro).
 *
 * La primera visita arranca siempre en claro: la preferencia del sistema se
 * ignora a propósito. Al elegir un tema se guarda en localStorage y las
 * visitas siguientes lo recuperan sin parpadeo (lo pinta un script inline en
 * index.html antes del primer render).
 */

export type Tema = 'claro' | 'oscuro'

export const CLAVE_TEMA = 'mesa2.tema'
export const CLASE_TEMA_OSCURO = 'tema-oscuro'

/** El tema guardado por el usuario, o `null` si aún no ha elegido. */
export function temaGuardado(): Tema | null {
  try {
    const valor = localStorage.getItem(CLAVE_TEMA)
    return valor === 'claro' || valor === 'oscuro' ? valor : null
  } catch {
    return null // almacenamiento no disponible (modo privado, etc.)
  }
}

/** Tema con el que debe arrancar la sesión: claro la primera vez. */
export function temaInicial(): Tema {
  return temaGuardado() ?? 'claro'
}

export function guardarTema(tema: Tema): void {
  try {
    localStorage.setItem(CLAVE_TEMA, tema)
  } catch {
    /* sin persistencia: el tema sigue funcionando en esta sesión */
  }
}

/** Aplica el tema al documento y sincroniza el color de la barra del navegador. */
export function aplicarTema(tema: Tema): void {
  document.documentElement.classList.toggle(CLASE_TEMA_OSCURO, tema === 'oscuro')
  const meta = document.querySelector('meta[name="theme-color"]')
  meta?.setAttribute('content', tema === 'oscuro' ? '#04100a' : '#ffffff')
}

export function alternarTema(tema: Tema): Tema {
  return tema === 'oscuro' ? 'claro' : 'oscuro'
}
