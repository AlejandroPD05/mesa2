import { Link } from 'react-router-dom'

export function NoEncontrada() {
  return (
    <div className="pila">
      <section className="heroe">
        <p className="heroe__codigo">404</p>
        <h1>Aquí no hay nada que soportar</h1>
        <p className="heroe__entradilla">
          La página que buscas no existe o ha cambiado de sitio. Vuelve al centro de ayuda o abre un
          ticket si crees que es un error.
        </p>
        <div className="fila">
          <Link to="/" className="boton">
            Ir al centro de ayuda
          </Link>
          <Link to="/mis-tickets" className="boton boton--secundario">
            Ver mis tickets
          </Link>
        </div>
      </section>
    </div>
  )
}
