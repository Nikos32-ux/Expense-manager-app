import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import './utils/i18n.js'
import './index.css';
import App from './App.jsx'
import { ExpenseDetailProvider } from './context/ExpenseDetailContext.jsx';
import WelcomePage from './pages/WelcomePage.jsx';
import DashBoard from './pages/Dashboard.jsx';
import AddExpense from './components/expenses/AddExpense.jsx';
import AddIncome from './components/expenses/AddIncome.jsx';
import ExpenseDetail from './components/expenses/ExpenseDetail.jsx';
import LoginPage, { loginAction } from './pages/LoginPage.jsx';
import RegisterPage, { registerAction } from './pages/RegisterPage.jsx';
import Profile from './pages/Profile.jsx';
import AccountInfo from './components/profile-modals/AccountInfo.jsx';
import Security from './components/profile-modals/Security.jsx';
import Categories from './pages/Categories.jsx';
import Transactions from './pages/Transactions.jsx';
import LoadSpinner from './components/ui/LoadSpinner.jsx';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { customPersistOptions, queryClient } from './context/queryClient.js';
import { addIncomeAction } from './components/expenses/AddIncome.jsx';
import { addExpenseAction } from './components/expenses/AddExpense.jsx';
import { WebSocketProvider } from './context/WebsocketContext.jsx';
import toast, { Toaster } from 'react-hot-toast';
import RootErrorBoundary from './components/ui/RootErrorBoundary.jsx';
import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client';
import PrivateRoutes from './layouts/PrivateRoutes.jsx';
import PublicRoutes from './layouts/PublicRoutes.jsx';


const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    id: "root",
    errorElement: <RootErrorBoundary />,
    children: [
      {
        element: <PublicRoutes />,
        children: [
          { index: true, element: <WelcomePage /> },
          { path: "login", element: <LoginPage />, action: loginAction },
          { path: "register", element: <RegisterPage />, action: registerAction }
        ]
      },

      {
        element: <PrivateRoutes />,
        children: [
          {
            path:"dashboard",
            element: <DashBoard />,
            children: [
              { path: "add-expense", action: addExpenseAction, element: <AddExpense /> },
              { path: "add-income", action: addIncomeAction, element: <AddIncome /> },
              { path: ":id", element: <ExpenseDetail /> }
            ]
          },
          {
            path:"profile",
            element: <Profile />,
            children: [
              { path: "account-info", element: <AccountInfo /> },
              { path: "security", element: <Security /> }
            ]
          },
          {
            path: "categories", 
            element: <Categories />
          },
          {
            path: "transactions",
            element: <Transactions />,
            children: [
              { path: ":id", element: <ExpenseDetail /> }
            ]
          }
        ]
      },
    ]
  }
])

window.queryClient = queryClient;


createRoot(document.getElementById('root')).render(
  <StrictMode>
    <PersistQueryClientProvider client={queryClient} persistOptions={customPersistOptions}>
      <ExpenseDetailProvider>
        <Toaster />
        <RouterProvider router={router} />
      </ExpenseDetailProvider>
    </PersistQueryClientProvider>
  </StrictMode>
)
