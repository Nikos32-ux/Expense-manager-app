import React, { useEffect, useMemo, useState } from 'react';
import { LuUser, LuLock, LuMail, LuImage } from 'react-icons/lu';
import { Link, useNavigate, useNavigation, useActionData, Form } from 'react-router-dom';
import api from '../axiosClientApi/axios.js';
import { checkPassRules } from '../utils/validatePassword.js'


const RegisterPage = () => {
  const actionLoginData = useActionData();
  const navigation = useNavigation();
  const [imageProfile, setImageProfile] = useState(null);
  const [newPassword, setNewPassword] = useState("");
  const [username, setUsername] = useState("");
  const navigate = useNavigate();
  const [fieldErrors, setFieldErrors] = useState(null);
  const [serverErrors, setServerErrors] = useState(null);
  const [generalErrors, setgeneraErrors] = useState(null);
  const [showUsernameRules, setShowUsernameRules] = useState(false);
  const [showPasswordRules, setShowPasswordRules] = useState(false);

  useEffect(() => {
    if (!actionLoginData || navigation.state === "submitting") return;
    let errorTimer;

    if (actionLoginData?.data) navigate("/login", { state: { success: actionLoginData.data.message } });

    else if (actionLoginData?.generalErrors) {
      console.log("general errors", actionLoginData.generalErrors);
      setgeneraErrors(actionLoginData?.generalErrors);

      errorTimer = setTimeout(() => {
        setgeneraErrors(false);
      }, 3000);

    } else if (actionLoginData?.fieldErrors) {
      console.log("field errors", actionLoginData.fieldErrors);
      setFieldErrors(actionLoginData?.fieldErrors);

      errorTimer = setTimeout(() => {
        setFieldErrors(false);
      }, 3000);
    } else if (actionLoginData?.serverErrors) {
      console.log("server errors", actionLoginData.serverErrors);
      setServerErrors(actionLoginData?.serverErrors);

      errorTimer = setTimeout(() => {
        setServerErrors(false);
      }, 3000);
    }

    return () => {
      clearTimeout(errorTimer);
    }
  }, [actionLoginData, navigation])


  const usernameRule = username.length > 6 && username.length < 20;
  const passwordRules = useMemo(() => checkPassRules(newPassword), [newPassword]);

  return (
    <div className='container flex flex-col min-h-[100dvh] bg-[#0b1220] overflow-hidden w-full
                  lg:flex-row lg:h-screen lg:items-center lg:justify-center lg:bg-gray-900'>

      <div className='register-top w-full  h-[200px] lg:min-h-[400px] relative 
                    bg-[linear-gradient(to_bottom,rgba(0,0,0,0.4),rgba(0,0,0,0.4)),url("/login-page.jpg")] 
                    bg-cover bg-center
                    lg:h-[100%] lg:w-1/2 rounded-left-30'>

        <p className='text-[24px] text-white text-3xl font-bold tracking-widest absolute top-10 left-1/2 -translate-x-1/2 lg:text-4xl '>
          Expensifier
        </p>
      </div>

      <div className='register-bottom flex flex-col flex-1 relative rounded-t-[40px] 
                  bg-black/40 backdrop-blur-xl border-t border-white/10 text-white -mt-10 pb-10 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden
                    lg:mt-0 lg:h-[100%] lg:rounded-none lg:border-t-0 lg:w-1/2 lg:flex lg:justify-center lg:items-center lg:pt-10'>

        <div className='register-heading text-center mt-2'>
          <h1 className='text-blue-500 text-2xl font-medium mt-1'>Sign up</h1>
        </div>

        <Form method='post' encType="multipart/form-data"
          className={`register-form flex flex-col flex-1 items-center lg:w-[450px] ${navigation.state === "submitting" ? "opacity-50 pointer-events-none" : "opacity-100"}`}>

          {(serverErrors || generalErrors) && (
            <div className='w-[90%] mx-auto'>
              <p className='text-red-400 text-sm font-medium text-center'>{serverErrors || generalErrors}</p>
            </div>
          )}
          <div className='register-form-input-container flex flex-col items-center px-4 gap-2 w-[90%] mt-5 px-3'>

            <div className='w-full bg-gray-800/50 border border-white/20 rounded-xl backdrop-blur-md p-3 flex items-center gap-3 focus-within:ring-2 focus-within:ring-blue-500 transition-all'>
              <LuUser className='text-blue-400 w-5 h-5 flex-shrink-0' />
              <input
                name='username'
                autoComplete="nickname"
                onChange={(e) => setUsername(e.target.value)}
                onFocus={() => setShowUsernameRules(true)}
                className='w-full bg-transparent text-white placeholder:text-gray-500 outline-none'
                type="text"
                placeholder='Name'
              />
            </div>
            {showUsernameRules &&
              <div className='flex items-center self-start gap-2 text-sm'>
                <div className={`rounded-full  h-4 w-4 flex items-center justify-center transition-colors ${usernameRule ? "bg-emerald-500" : "bg-gray-300"} `}>
                  {usernameRule && <span className='self-start className="text-[10px] text-white"'>✓</span>}
                </div>
                <p className={usernameRule ? "text-emerald-500" : "text-gray-400 "}>3 to 25 characters</p>
              </div>
            }
            {fieldErrors?.username && (
              <div className='w-[90%] mx-auto'>
                <p className='text-red-400 text-sm font-medium text-center'>{fieldErrors?.username}</p>
              </div>
            )}

            <div className='w-full bg-gray-800/50 border border-white/20 rounded-xl backdrop-blur-md p-3 flex items-center gap-3 focus-within:ring-2 focus-within:ring-blue-500 transition-all'>
              <LuLock className='text-blue-400 w-5 h-5 flex-shrink-0' />
              <input
                name='password'
                autoComplete="pass"
                onFocus={() => setShowPasswordRules(true)}
                className='w-full bg-transparent text-white placeholder:text-gray-500 outline-none'
                onChange={(e) => { setNewPassword(e.target.value) }}
                type="text"
                placeholder='Password'
              />
            </div>
            {fieldErrors?.password && (
              <div className='w-[90%] mx-auto'>
                <p className='text-red-400 text-sm font-medium text-center'>{fieldErrors?.password}</p>
              </div>
            )}

            <div className="self-start p-2 update-password-rules bg-gray-950/50 border border-white/5 rounded-xl">
              <h1 className="font-semibold mb-2 text-gray-400 text-sm">
                Password must contain:
              </h1>
              <div className="rules-container flex flex-col gap-2 mt-2">
                {showPasswordRules && passwordRules.map((rule) => (
                  <div key={rule.id} className='flex items-center gap-2 text-sm'>
                    <div className={`rounded-full h-4 w-4 flex items-center justify-center transition-colors ${rule.valid ? "bg-emerald-500" : "bg-gray-300"} `}>
                      {rule.valid && <span className='className="text-[10px] text-white"'>✓</span>}
                    </div>
                    <p className={rule.valid ? "text-emerald-500" : "text-gray-400"}>
                      {rule.rule}
                    </p>
                  </div>
                ))}
              </div>
            </div>

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
            {fieldErrors?.email && (
              <div className='w-[90%] mx-auto'>
                <p className='text-red-400 text-sm font-medium text-center'>{fieldErrors?.email}</p>
              </div>
            )}
            <div className='w-full bg-gray-800/50 border border-white/20 rounded-xl backdrop-blur-md p-3 flex items-center gap-3 focus-within:ring-2 focus-within:ring-blue-500 transition-all'>
              <LuImage className='text-blue-400 w-5 h-5 flex-shrink-0' />

              <label className='w-full cursor-pointer min-w-0'>
                <div className='w-full truncate text-gray-500 rounded-lg'>
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
            {fieldErrors?.imageProfile && (
              <div className='w-[90%] mx-auto'>
                <p className='text-red-400 text-sm font-medium text-center'>{fieldErrors?.imageProfile}</p>
              </div>
            )}
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
    if (!formData.get("username") ||
      !formData.get("password") ||
      !formData.get("email") ||
      !formData.get("imageProfile")) {
      return { generalErrors: "All fields are required" };
    }

    const passRules = checkPassRules(formData.get("password"));
    const isInvalidPass = passRules.some((rule) => !rule.valid);
    if (isInvalidPass) return { preValidationError: "Password must match all rules" }

    const res = await api.post("/auth/register", formData);
    return { data: res.data };
  }
  catch (error) {
    if (import.meta.env.MODE !== "production") {
      console.error("Error", error);
    }

    if (error.response) {
      if (error.response.status === 400 || error.response.status === 409 || error.response.status === 413) {
        return { fieldErrors: error.response?.data?.message };
      }

      if (error.response.status >= 500) {
        return { serverErrors: "Server is having trouble, try again in a minute" }
      }
    }

    if (error.request) {
      return { serverErrors: "We can't connect to the server. Please check your internet."} 
    }


    return { generalErrors: "Registration failed" }
  }
};


export default RegisterPage