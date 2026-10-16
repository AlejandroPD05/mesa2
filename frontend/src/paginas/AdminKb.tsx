import { useDeferredValue, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  useArticulo,
  useArticulos,
  useCatalogo,
  useEliminarArticulo,
  useGuardarArticulo,
} from '../api/consultas'
import { ErrorApi, mensajeDe } from '../api/cliente'
import { Aviso, Cargando, Vacio } from '../componentes/Avisos'
import { Insignia } from '../componentes/Etiquetas'
import type { ArticuloDetalle } from '../api/tipos'

export function AdminKb() {
  const [texto, setTexto] = useState('')
  const textoDiferido = useDeferredValue(texto)
  const [editor, setEditor] = useState<{ slug?: string } | null>(null)

  const { data: articulos, isPending, isError, error } = useArticulos({
    texto: textoDiferido,
    todos: true,
  })
  const eliminar = useEliminarArticulo()
  const [errorAccion, setErrorAccion] = useState<string | null>(null)

  async function borrar(articulo: { id: number; titulo: string }) {
    setErrorAccion(null)
    // MVP: confirmación nativa; sin ella no se borra nada.
    if (!window.confirm(`¿Eliminar el artículo «${articulo.titulo}»? Esta acción no se puede deshacer.`)) {
      return
    }
    try {
      await eliminar.mutateAsync(articulo.id)
    } catch (fallo) {
      setErrorAccion(mensajeDe(fallo))
    }
  }

  return (
    <div className="pila">
      <div className="fila fila--separada">
        <div>
          <h1>Administración del centro de ayuda</h1>
          <p className="sutil">
            Crea, edita y publica los artículos que ve el portal. Los borradores no son visibles para
            nadie sin perfil de soporte.
          </p>
        </div>
        <button type="button" className="boton" onClick={() => setEditor({})}>
          Nuevo artículo
        </button>
      </div>

      {editor ? (
        <EditorArticulo
          key={editor.slug ?? 'nuevo'}
          slug={editor.slug}
          alCerrar={() => setEditor(null)}
        />
      ) : null}

      {errorAccion ? <Aviso>{errorAccion}</Aviso> : null}

      <div className="campo">
        <label htmlFor="kb-admin-texto">Buscar artículo</label>
        <input
          id="kb-admin-texto"
          type="search"
          placeholder="Título, resumen o contenido…"
          value={texto}
          onChange={(evento) => setTexto(evento.target.value)}
        />
      </div>

      {isError ? <Aviso>{mensajeDe(error)}</Aviso> : null}
      {isPending ? <Cargando etiqueta="Cargando artículos…" /> : null}

      {articulos && articulos.length === 0 ? (
        <Vacio titulo="Sin artículos">Crea el primero con el botón «Nuevo artículo».</Vacio>
      ) : null}

      {articulos && articulos.length > 0 ? (
        <div className="tabla-envoltura">
          <table className="tabla">
            <caption className="oculto-visual">Artículos del centro de ayuda</caption>
            <thead>
              <tr>
                <th scope="col">Título</th>
                <th scope="col">Slug</th>
                <th scope="col">Categoría</th>
                <th scope="col">Estado</th>
                <th scope="col">Visitas</th>
                <th scope="col">Votos</th>
                <th scope="col">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {articulos.map((articulo) => (
                <tr key={articulo.id}>
                  <td>
                    <a href={`/ayuda/${articulo.slug}`} target="_blank" rel="noopener noreferrer">
                      {articulo.titulo}
                    </a>
                  </td>
                  <td>
                    <code>{articulo.slug}</code>
                  </td>
                  <td>{articulo.categoria}</td>
                  <td>
                    {articulo.publicado ? (
                      <Insignia tono="exito">Publicado</Insignia>
                    ) : (
                      <Insignia tono="aviso">Borrador</Insignia>
                    )}
                  </td>
                  <td>{articulo.visitas}</td>
                  <td>
                    {articulo.votosUtil} útiles · {articulo.votosNoUtil} no
                  </td>
                  <td>
                    <div className="fila">
                      <button
                        type="button"
                        className="boton boton--secundario boton--pequeno"
                        onClick={() => setEditor({ slug: articulo.slug })}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className="boton boton--peligro boton--pequeno"
                        disabled={eliminar.isPending}
                        onClick={() => void borrar(articulo)}
                      >
                        Eliminar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  )
}

// ---------------------------------------------------------------------------
// Editor
// ---------------------------------------------------------------------------

function EditorArticulo({ slug, alCerrar }: { slug?: string; alCerrar: () => void }) {
  if (slug === undefined) {
    return <FormularioArticulo alCerrar={alCerrar} />
  }
  return <EditorExistente slug={slug} alCerrar={alCerrar} />
}

function EditorExistente({ slug, alCerrar }: { slug: string; alCerrar: () => void }) {
  const { data, isPending, isError, error } = useArticulo(slug)
  if (isPending) return <Cargando etiqueta="Abriendo el artículo…" />
  if (isError || !data) return <Aviso>{mensajeDe(error)}</Aviso>
  return <FormularioArticulo articulo={data} alCerrar={alCerrar} />
}

function FormularioArticulo({
  articulo,
  alCerrar,
}: {
  articulo?: ArticuloDetalle
  alCerrar: () => void
}) {
  const { data: catalogo } = useCatalogo()
  const guardar = useGuardarArticulo(articulo?.id)

  const [titulo, setTitulo] = useState(articulo?.titulo ?? '')
  const [slug, setSlug] = useState(articulo?.slug ?? '')
  const [resumen, setResumen] = useState(articulo?.resumen ?? '')
  const [contenido, setContenido] = useState(articulo?.contenido ?? '')
  const [idCategoria, setIdCategoria] = useState(articulo?.idCategoria ?? 0)
  const [publicado, setPublicado] = useState(articulo?.publicado ?? false)
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setError(null)
    setErrores({})
    try {
      await guardar.mutateAsync({
        titulo: titulo.trim(),
        slug: slug.trim(),
        resumen: resumen.trim(),
        contenido: contenido.trim(),
        idCategoria,
        publicado,
      })
      alCerrar()
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.problema.errores) {
        setErrores(fallo.problema.errores)
      } else if (fallo instanceof ErrorApi && fallo.problema.campo === 'slug') {
        setErrores({ slug: fallo.problema.detail ?? 'Slug no válido' })
      } else {
        setError(mensajeDe(fallo))
      }
    }
  }

  return (
    <section className="tarjeta" aria-labelledby="titulo-editor">
      <h2 id="titulo-editor">{articulo ? `Editar «${articulo.titulo}»` : 'Nuevo artículo'}</h2>

      {error ? <Aviso>{error}</Aviso> : null}

      <form className="formulario" onSubmit={enviar} noValidate>
        <div className="campo">
          <label htmlFor="kb-titulo">Título</label>
          <input
            id="kb-titulo"
            type="text"
            required
            maxLength={150}
            value={titulo}
            onChange={(evento) => setTitulo(evento.target.value)}
            aria-invalid={errores.titulo ? true : undefined}
          />
          {errores.titulo ? <p className="error-campo">{errores.titulo}</p> : null}
        </div>

        <div className="campos-dobles">
          <div className="campo">
            <label htmlFor="kb-slug">Slug (URL)</label>
            <input
              id="kb-slug"
              type="text"
              value={slug}
              placeholder="se genera del título"
              onChange={(evento) => setSlug(evento.target.value)}
              aria-invalid={errores.slug ? true : undefined}
            />
            <p className="ayuda-campo">Minúsculas, números y guiones. /ayuda/&lt;slug&gt;</p>
            {errores.slug ? <p className="error-campo">{errores.slug}</p> : null}
          </div>
          <div className="campo">
            <label htmlFor="kb-categoria">Categoría</label>
            <select
              id="kb-categoria"
              required
              value={idCategoria}
              onChange={(evento) => setIdCategoria(Number(evento.target.value))}
            >
              <option value={0} disabled>
                Elige una categoría…
              </option>
              {catalogo?.categorias.map((categoria) => (
                <option key={categoria.id} value={categoria.id}>
                  {categoria.nombre}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="campo">
          <label htmlFor="kb-resumen">Resumen</label>
          <textarea
            id="kb-resumen"
            required
            rows={2}
            maxLength={300}
            value={resumen}
            onChange={(evento) => setResumen(evento.target.value)}
            aria-invalid={errores.resumen ? true : undefined}
          />
          {errores.resumen ? <p className="error-campo">{errores.resumen}</p> : null}
        </div>

        <div className="campo">
          <label htmlFor="kb-contenido">Contenido (Markdown)</label>
          <textarea
            id="kb-contenido"
            required
            rows={14}
            value={contenido}
            onChange={(evento) => setContenido(evento.target.value)}
            aria-invalid={errores.contenido ? true : undefined}
          />
          {errores.contenido ? <p className="error-campo">{errores.contenido}</p> : null}
        </div>

        <label className="casilla">
          <input
            type="checkbox"
            checked={publicado}
            onChange={(evento) => setPublicado(evento.target.checked)}
          />
          Publicado (visible en el portal)
        </label>

        <div className="fila">
          <button type="submit" className="boton" disabled={guardar.isPending}>
            {guardar.isPending ? 'Guardando…' : 'Guardar artículo'}
          </button>
          <button type="button" className="boton boton--secundario" onClick={alCerrar}>
            Cancelar
          </button>
          {articulo ? (
            <Link to={`/ayuda/${articulo.slug}`} target="_blank" rel="noopener noreferrer" className="enlace">
              Ver en el portal
            </Link>
          ) : null}
        </div>
      </form>
    </section>
  )
}
