export function Paginacion({
  pagina,
  total,
  tamano,
  onCambiar,
}: {
  pagina: number
  total: number
  tamano: number
  onCambiar: (pagina: number) => void
}) {
  const paginas = Math.max(1, Math.ceil(total / tamano))
  if (total === 0) return null
  return (
    <nav className="paginacion" aria-label="Paginación">
      <button
        type="button"
        className="boton boton--secundario boton--pequeno"
        disabled={pagina <= 1}
        onClick={() => onCambiar(pagina - 1)}
      >
        Anterior
      </button>
      <span className="paginacion__estado">
        Página {pagina} de {paginas} · {total} {total === 1 ? 'ticket' : 'tickets'}
      </span>
      <button
        type="button"
        className="boton boton--secundario boton--pequeno"
        disabled={pagina >= paginas}
        onClick={() => onCambiar(pagina + 1)}
      >
        Siguiente
      </button>
    </nav>
  )
}
