import { Route, Routes } from 'react-router-dom'
import { Cabecera } from './componentes/Cabecera'
import { RutaProtegida } from './rutas/RutaProtegida'
import { AdminKb } from './paginas/AdminKb'
import { Articulo } from './paginas/Articulo'
import { Bandeja } from './paginas/Bandeja'
import { CentroAyuda } from './paginas/CentroAyuda'
import { Login } from './paginas/Login'
import { MisTickets } from './paginas/MisTickets'
import { NoEncontrada } from './paginas/NoEncontrada'
import { NuevoTicket } from './paginas/NuevoTicket'
import { Panel } from './paginas/Panel'
import { Registro } from './paginas/Registro'
import { Ticket } from './paginas/Ticket'

export function App() {
  return (
    <div className="aplicacion">
      <a className="salto-contenido" href="#contenido">
        Saltar al contenido
      </a>
      <Cabecera />
      <main id="contenido" className="contenido" tabIndex={-1}>
        <div className="contenedor">
          <Routes>
            <Route path="/" element={<CentroAyuda />} />
            <Route path="/ayuda/:slug" element={<Articulo />} />
            <Route path="/login" element={<Login />} />
            <Route path="/registro" element={<Registro />} />

            <Route element={<RutaProtegida />}>
              <Route path="/mis-tickets" element={<MisTickets />} />
              <Route path="/tickets/nuevo" element={<NuevoTicket />} />
              <Route path="/tickets/:id" element={<Ticket />} />
            </Route>

            <Route element={<RutaProtegida roles={['tecnico', 'administrador']} />}>
              <Route path="/bandeja" element={<Bandeja />} />
              <Route path="/panel" element={<Panel />} />
            </Route>

            <Route element={<RutaProtegida roles={['administrador']} />}>
              <Route path="/admin" element={<AdminKb />} />
            </Route>

            <Route path="*" element={<NoEncontrada />} />
          </Routes>
        </div>
      </main>
      <footer className="pie">
        <div className="contenedor">
          Mesa 2.0 · Portal de soporte IT y helpdesk web — RoyalTown · Proyecto de portfolio de
          Alejandro Prieto Díez
        </div>
      </footer>
    </div>
  )
}
