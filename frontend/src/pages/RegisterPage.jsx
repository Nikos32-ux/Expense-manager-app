import React, { useEffect, useState } from 'react';
import { LuUser, LuLock, LuMail, LuImage } from 'react-icons/lu';
import { Link, useNavigate, useNavigation, useActionData, Form } from 'react-router-dom';
import api from '../axiosClientApi/axios.js';



const RegisterPage = () => {

  const actionLoginData = useActionData();
  const navigation = useNavigation();
  const [displayError, setDisplayError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [imageProfile, setImageProfile] = useState(null);
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
        navigate("/login")
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
    <div className='container flex flex-col min-h-[100dvh] overflow-hidden
                  lg:flex-row lg:items-center lg:justify-center lg:bg-gray-900 p-2'>

      <div className='w-full register-top min-h-[250px] lg:min-h-[400px] relative 
                    bg-[linear-gradient(to_bottom,rgba(0,0,0,0.4),rgba(0,0,0,0.4)),url("/login-page.jpg")] 
                    bg-cover bg-center
                    lg:h-auto lg:w-1/2 rounded-left-30'>

        <p className='text-[24px] text-white text-3xl font-bold tracking-widest absolute top-10 left-1/2 -translate-x-1/2
                  lg:text-4xl '>
          Expensifier
        </p>
      </div>

      <div className='register-bottom flex flex-col flex-1 relative rounded-t-[40px] 
                    bg-black/30 backdrop-blur-lg border-t border-white/10 text-white -mt-10 pb-10
                    lg:mt-0 lg:rounded-none lg:border-t-0 lg:w-1/2 lg:flex lg:justify-center lg:items-center'>

        <div className='register-heading text-center mt-5'>
          <h1 className='text-blue-500 text-xl mt-2'>Sign up</h1>
        </div>

        <Form method='post' encType="multipart/form-data"
          className={`register-form flex flex-col flex-1 items-center lg:w-[450px] ${navigation.state === "submitting" ? "opacity-50 pointer-events-none" : "opacity-100"}`}>
          {success && (
            <div className='w-[80%] mx-auto rounded-md py-2 px-2 bg-green-600/20 backdrop-blur-xl text-center'>
              <p className='text-white text-[18px] font-semibold'>{success}</p>
            </div>
          )}
          {displayError && (
            <div className='w-[80%] mx-auto rounded-md py-2 px-2 bg-red-600/20 backdrop-blur-xl text-center'>
              <p className='text-white text-[18px] font-semibold'>{displayError}</p>
            </div>
          )}
          <div className='register-form-input-container flex flex-col items-center px-4 gap-4 w-[90%] mt-5 px-3'>

            <div className='register-input flex relative w-full'>
              <LuUser className='text-blue-400 w-4 h-4 absolute left-1 top-1/2 -translate-y-1/2' />
              <input
                disabled={navigation.state === "submitting"}
                name='username'
                className='w-full text-gray-900 rounded-lg py-2 border-2 border-white/20 ring-1 ring-blue-400/20 pl-10 shadow-md'
                type="text"
                placeholder='Name'
              />
            </div>

            <div className='register-input flex relative w-full'>
              <LuLock className='text-blue-400 w-4 h-4 absolute left-1 top-1/2 -translate-y-1/2' />
              <input
                disabled={navigation.state === "submitting"}
                name='password'
                className='w-full rounded-lg py-2 border-2 border-white/20 ring-1 ring-blue-400/20 pl-10 shadow-md text-gray-900'
                type="password"
                placeholder='Password'
              />
            </div>

            <div className='register-input flex relative w-full'>
              <LuMail className='text-blue-400 w-4 h-4 absolute left-1 top-1/2 -translate-y-1/2' />
              <input
                disabled={navigation.state === "submitting"}
                name='email'
                className='w-full text-gray-900 rounded-lg py-2 border-2 border-white/20 ring-1 ring-blue-400/20 pl-10 shadow-md'
                type="email"
                placeholder='Email'
              />
            </div>

            <div className='register-input flex relative w-full'>
              <LuImage className='text-blue-400 w-4 h-4 absolute left-1 top-1/2 -translate-y-1/2' />

              <label className='w-full cursor-pointer'>
                <div className='w-full truncate text-white rounded-lg py-2 border-2 border-white/20 ring-1 ring-blue-400/20 pl-10 shadow-md bg-black/30 backdrop-blur-lg'>
                  {imageProfile ? imageProfile.name : "Choose profile image"}
                </div>
                <input
                  disabled={navigation.state === "submitting"}
                  name='imageProfile'
                  onChange={(e) => setImageProfile(e.target.files[0])}
                  type="file"
                  className='hidden'
                />
              </label>
            </div>

          </div>

         <div className='register-button w-[60%] mx-auto mt-5'>
          <button
            disabled={navigation.state === "submitting"}
            type='submit'
            className={`w-full ${navigation.state === "submitting" ? "bg-blue-400/20" : "bg-blue-600"} text-white p-2 shadow-md rounded-md font-bold`}>
            {navigation.state === "submitting" 
              ? (
                <span className='flex items-center justify-center gap-2'>
                  <span className='animate-spin h-4 w-4 border-2 border-white border-b-transparent rounded-full'></span>
                  Signing up....
                </span>
              ) 
              : "Sign up"}
          </button>
        </div>

        </Form>
      </div>
    </div>
  )
}


export const registerAction = async ({ request }) => {
  const formData = await request.formData();
  try {
    if (!formData.get("username") || !formData.get("password") || !formData.get("email") || !formData.get("imageProfile")) {
      return { error: "All fields are required" };
    }
    
    await api.post("/auth/register", formData);
    return { success: "You registered successfully" };
  }
  catch (error) {
    return { error: error.response?.data?.message || "Registration failed"};
  }
};


export default RegisterPage