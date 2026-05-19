import React, { useContext, useEffect, useState } from 'react';
import { LuUser, LuLock } from 'react-icons/lu';
import { Form, Link, useActionData, useNavigate, useNavigation } from 'react-router-dom';
import api from '../axiosClientApi/axios.js';
import { queryClient } from '../context/queryClient.js';


const LoginPage = () => {
    const actionLoginData = useActionData();
    const navigation = useNavigation();
    const [displayError, setDisplayError] = useState(null);
    const [success, setSuccess] = useState(null);
    const navigate = useNavigate();


    useEffect(() => {
        if (!actionLoginData || navigation.state === "submitting") return;
        let successTimer;
        let errorTimer;
        let navigationTimer;

        if (actionLoginData.success) {
            setSuccess(actionLoginData.success);

            successTimer = setTimeout(() => {
                setSuccess(false);
            }, 4000);

            navigationTimer = setTimeout(() => {
                navigate("/dashboard")
            }, 2000);

        } else if (actionLoginData.error) {
            setDisplayError(actionLoginData?.error);

            errorTimer = setTimeout(() => {
                setDisplayError(false);
            }, 4000);

        }

        return () => {
            clearTimeout(successTimer);
            clearTimeout(errorTimer);
            clearTimeout(navigationTimer);
        }
    }, [actionLoginData, navigation])


   return (
  <div className='container flex flex-col min-h-[100dvh] overflow-hidden lg:flex-row lg:items-center lg:justify-center lg:bg-gray-900 rounded-lg'>
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

      <Form method='post' className={`sign-in-form flex flex-col items-center justify-center pb-6 lg:w-[400px] ${navigation.state === "submitting" ? "opacity-50 pointer-events-none" : "opacity-100" }`}>
        {displayError && (
          <div className='w-[80%] mx-auto rounded-md py-2 px-2 bg-red-600/20 backdrop-blur-xl text-center'>
            <p className='text-white text-[18px] font-semibold'>{displayError}</p>
          </div>
        )}

        {success && (
          <div className='w-[80%] mx-auto rounded-md py-2 px-2 bg-green-600/20 backdrop-blur-xl text-center'>
            <p className='text-white text-[18px] font-semibold'>{success}</p>
          </div>
        )}

        <div className='input-container flex items-center justify-center flex-col px-4 gap-5 w-[90%] mx-auto px-3'>
          
          <div className='input flex relative w-[90%]'>
            <LuUser className='icon  text-blue-400 w-4 h-4 absolute left-1 z-10 top-1/2 -translate-y-1/2' />
            <input
              disabled={navigation.state === "submitting"}
              name='email'
              className='w-full text-gray-900 rounded-lg py-2 border-2 border-white/20 ring-1 ring-blue-400/20 pl-10 shadow-md'
              type="email"
              placeholder='Email'
            />
          </div>

          <div className='input flex relative w-[90%]'>
            <LuLock className='icon text-blue-400 w-4 h-4 absolute left-1 z-10 top-1/2 -translate-y-1/2' />
            <input
              disabled={navigation.state === "submitting"}
              name='password'
              className='w-full text-gray-900 rounded-lg py-2 border-2 border-white/20 ring-1 ring-blue-400/20 pl-10 shadow-md'
              type="password"
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

        <p className='mt-3 text-white/60'>
          I'm new user.
          <Link to={"/register"} className={`text-blue-400 font-semibold ml-2 hover:text-blue-300 transition-colors ${navigation.state === "submitting" ? "opacity-50 pointer-events-none" : "" } `}>
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
        if (!email.trim() || !password.trim()) {
            return { error: "All fields are required" };
        }
        await api.post("/auth/login", { email, password });
        return { success: "Login was successful" };
    } catch (error) {
        return { error: "Login failed" };
    }
};


export default LoginPage