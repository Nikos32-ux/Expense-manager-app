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
import RegisterPage from './pages/RegisterPage.jsx';
import Profile from './pages/Profile.jsx';
import AccountInfo from './components/profile-modals/AccountInfo.jsx';
import Security from './components/profile-modals/Security.jsx';
import Categories from './pages/Categories.jsx';
import Transactions from './pages/Transactions.jsx';
import { verificationLoader } from './loaders/verificationLoader.js';
import LoadSpinner from './components/ui/LoadSpinner.jsx';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { queryClient } from './context/queryClient.js';
import { addIncomeAction } from './components/expenses/AddIncome.jsx';
import { addExpenseAction } from './components/expenses/AddExpense.jsx';
import { WebSocketProvider } from './context/WebsocketContext.jsx';
import toast, { Toaster } from 'react-hot-toast';

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    id: "root",
    loader: verificationLoader,
    hydrateFallbackElement: <LoadSpinner />,
    children: [
      {
        index: true,
        element: <WelcomePage />
      },
      {
        path: "dashboard",
        element: <DashBoard />,
        children: [
          { path: "add-expense", action: addExpenseAction, element: <AddExpense /> },
          { path: "add-income", action: addIncomeAction, element: <AddIncome /> },
          { path: ":id", element: <ExpenseDetail /> }
        ]
      },
      {
        path: "login",
        element: <LoginPage />,
        action: loginAction
      },
      {
        path: "register",
        element: <RegisterPage />
      },
      {
        path: "profile",
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
  }
])

window.queryClient = queryClient;


createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
        <ExpenseDetailProvider>
          <Toaster/>
          <RouterProvider router={router} />
        </ExpenseDetailProvider>
    </QueryClientProvider>
  </StrictMode>
)
