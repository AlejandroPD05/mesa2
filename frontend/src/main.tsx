import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { App } from './App.tsx'
import { SesionProveedor } from './contexto/SesionProveedor.tsx'
import './estilos/tokens.css'
import './estilos/app.css'

const cliente = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={cliente}>
      <BrowserRouter>
        <SesionProveedor>
          <App />
        </SesionProveedor>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
