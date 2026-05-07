import { useEffect } from 'react'
import './App.css'
import { Outlet, useNavigate, useRouteLoaderData } from 'react-router-dom'
import { WebSocketProvider } from './context/WebsocketContext'

function App() {

  return (
    <WebSocketProvider>
      <Outlet />
    </WebSocketProvider>
  )
}

export default App
