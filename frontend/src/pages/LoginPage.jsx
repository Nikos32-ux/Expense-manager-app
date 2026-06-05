import React, { useContext, useEffect, useState, useRef } from 'react';
import { LuUser, LuLock, LuMail } from 'react-icons/lu';
import { Form, Link, redirect, useActionData, useLocation, useNavigate, useNavigation } from 'react-router-dom';
import api from '../axiosClientApi/axios.js';
import { queryClient } from '../context/queryClient.js';
import toast from 'react-hot-toast';


const LoginPage = () => {
  const actionLoginData = useActionData();
  const navigation = useNavigation();
  const [displayError, setDisplayError] = useState(null);
  const [newPassword, setNewPassword] = useState("");
  const navigate = useNavigate();
  const formRef = useRef(null);
  const location = useLocation();
  const toastRef = useRef(false);

  useEffect(() => {
    if(!location.state?.success) return;
    if(location.state.success && !toastRef.current){
       toastRef.current = true;
       toast.success(location.state.success);
      window.history.replaceState({}, document.title);
    }
  },[location.state?.success]);


  useEffect(() => {
    if (!actionLoginData || navigation.state === "submitting") return;
    let errorTimer;

    if (actionLoginData.success) {
      if (formRef.current) {
        formRef.current.reset();
      }
      navigate("/dashboard")
    }
    else if (actionLoginData.error) {
      setDisplayError(actionLoginData?.error);

      errorTimer = setTimeout(() => {
        setDisplayError(false);
      }, 3000);

    }

    return () => {
      clearTimeout(errorTimer);
    }
  }, [actionLoginData, navigation])




  return (
    <div className='container flex flex-col h-[100dvh] bg-[#0b1220] lg:flex-row lg:items-center lg:justify-center lg:bg-gray-900 rounded-lg'>
      <div className='login-heading-top w-full h-[30vh] min-h-[300px] login-top flex-shrink-0 relative
                    bg-[linear-gradient(to_top,rgba(0,0,0,0.6),rgba(0,0,0,0.6)),url("/login-page.jpg")]
                    bg-cover bg-center
                    lg:h-auto lg:w-1/2'>

        <p className='text-[28px] text-white text-[2rem] font-bold tracking-widest absolute top-10 left-1/2 -translate-x-1/2 mt-8'>
          Expensifier
        </p>
      </div>
      <div className='sign-in-container flex flex-col flex-1 login-bottom relative rounded-t-[40px]
                    bg-black/30 backdrop-blur-lg border-t border-white/10 text-white -mt-10
                    lg:mt-0 lg:rounded-none lg:border-t-0 lg:w-1/2 lg:flex lg:justify-center lg:items-center'>

        <div className='sign-in-heading text-center mt-5'>
          <h1 className='text-blue-500 text-2xl italic mb-6'>Sign in</h1>
        </div>

        <Form method='post' ref={formRef} className={`sign-in-form flex flex-col items-center justify-center pb-6 lg:w-[400px] ${navigation.state === "submitting" ? "opacity-50 pointer-events-none" : "opacity-100"}`}>
          {displayError && (
            <div className='w-[90%] mx-auto mb-2'>
              <p className='text-red-400 text-sm font-medium text-center'>{displayError}</p>
            </div>
          )}

          <div className='input-container flex items-center justify-center flex-col px-4 gap-5 w-[90%] mx-auto px-3'>
            <div className='w-full bg-gray-800/50 border border-white/20 rounded-xl backdrop-blur-md p-3 flex items-center gap-3 focus-within:ring-2 focus-within:ring-blue-500 transition-all'>
              <LuMail className='text-blue-400 w-5 h-5 flex-shrink-0' />
              <input
                name='email'
                autoComplete="mail"
                className='w-full bg-transparent text-white placeholder:text-gray-500 outline-none'
                type="text"
                placeholder='Email'
              />
            </div>

            <div className='w-full bg-gray-800/50 border border-white/20 rounded-xl backdrop-blur-md p-3 flex items-center gap-3 focus-within:ring-2 focus-within:ring-blue-500 transition-all'>
              <LuLock className='text-blue-400 w-5 h-5 flex-shrink-0' />
              <input
                name='password'
                autoComplete="pass"
                className='w-full bg-transparent text-white placeholder:text-gray-500 outline-none'
                onChange={(e) => { setNewPassword(e.target.value) }}
                type="text"
                placeholder='Password'
              />
            </div>

          </div>

          <div className='login-button w-[60%] mx-auto mt-5'>
            <button
              disabled={navigation.state === "submitting"}
              type='submit'
              className={`w-full ${navigation.state === "submitting" ? "bg-blue-400/20" : "bg-blue-600"} text-white p-2 shadow-md rounded-md font-bold`}>
              {navigation.state === "submitting"
                ? (
                  <span className='flex items-center justify-center gap-2'>
                    <span className='animate-spin h-4 w-4 border-2 border-white border-b-transparent rounded-full'></span>
                    Signing in....
                  </span>
                )
                : "Sign in"}
            </button>
          </div>

          <p className='mt-3 text-white/60 text-sm'>
            I'm new user.
            <Link to={"/register"} className={`text-blue-400 text-[15px] font-semibold ml-2 hover:text-blue-300 transition-colors ${navigation.state === "submitting" ? "opacity-50 pointer-events-none" : ""} `}>
              SIGN UP
            </Link>
          </p>

        </Form>
      </div>
    </div>
  )
}

export const loginAction = async ({ request }) => {
  const formData = await request.formData();
  const email = formData.get("email");
  const password = formData.get("password");

  try {
    if (!email.trim() || !password.trim()) return { error: "All fields are required" };

    let response = await api.post("/auth/login", { email, password });
    queryClient.setQueryData(["verification"], response.data);
    return redirect("/dashboard");
  } catch (error) {
    if (error.response) {
      console.error("SERVER_ERROR:", error.response.data);
    } else if (error.request) {
      console.error("NETWORK_ERROR:", error.request);
      return { error: "Server is unreachable. Try again later." };
    }
    console.error("UNKNOWN_ERROR:", error.message);
    return { error: "Something went wrong." };
  }
};


export default LoginPage