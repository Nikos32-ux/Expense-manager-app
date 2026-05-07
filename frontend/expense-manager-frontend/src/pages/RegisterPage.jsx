import React, { useState } from 'react';
import { LuUser, LuLock, LuMail, LuImage } from 'react-icons/lu';
import { Link, useNavigate } from 'react-router-dom';
import api from '../axiosClientApi/axios.js';

import { registerFieldsValidation } from '../utils/RegisterPageValidation.js';

const RegisterPage = () => {

    
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [imageProfile, setImageProfile] = useState(null);
    const [registerSuccess, setRegisterSuccess] = useState("");
    const [missingField, setMissingField] = useState(false);
    const [isError, setIsError] = useState(false);
    const [resError, setResError] = useState("");
    const [errorFieldMessage, setErrorFieldMessage] = useState("");
    const [isBeingSent, setIsBeingSent] = useState(false);
    const navigate = useNavigate();

    const handleSubmitForm = async (e) => {
        try {
            e.preventDefault();
            setIsBeingSent(true);
            const validationSuccess = registerFieldsValidation(username, password, email, imageProfile, setMissingField, setIsError, setErrorFieldMessage);

            if (!validationSuccess) {
                console.log(errorFieldMessage);
                return;
            }

            const formData = new FormData();
            formData.append("username", username);
            formData.append("password", password);
            formData.append("email", email);
            formData.append("imageProfile", imageProfile);

            const res = await api.post("auth/register", formData);
            if (res.status === 201) {
                setRegisterSuccess("User successfully registered!")
                setTimeout(() => {
                    setRegisterSuccess("")
                    navigate("/login");
                }, 2000);
            }

        }
        catch (error) {
            console.log("error object", error?.response?.data);
            setResError(error.response?.data?.message);
            setTimeout(() => {
                setResError("");
            }, 3000);
        }finally{
            setIsBeingSent(false)
        }
    }



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

      <form
        onSubmit={(e) => handleSubmitForm(e)}
        className='register-form flex flex-col flex-1 items-center lg:w-[450px]'>

        {missingField && <div className='bg-red-600 text-white font-bold'><h1>All fields are required</h1></div>}
        {isError && <div className='bg-red-600 text-white font-bold'><h1>{errorFieldMessage}</h1></div>}
        {registerSuccess && <div className='bg-green-600 text-white font-bold'><h1>{registerSuccess}</h1></div>}
        {resError && <div className='bg-red-400 text-white font-bold'><h1>{resError}</h1></div>}

        <div className='register-form-input-container flex flex-col items-center px-4 gap-4 w-[90%] mt-5 px-3'>

          <div className='register-input flex relative w-full'>
            <LuUser className='text-blue-400 w-4 h-4 absolute left-1 top-1/2 -translate-y-1/2' />
            <input
              onChange={(e) => setUsername(e.target.value)}
              value={username}
              className='w-full text-gray-900 rounded-lg py-2 border-2 border-white/20 ring-1 ring-blue-400/20 pl-10 shadow-md'
              type="text"
              placeholder='Name'
            />
          </div>

          <div className='register-input flex relative w-full'>
            <LuLock className='text-blue-400 w-4 h-4 absolute left-1 top-1/2 -translate-y-1/2' />
            <input
              onChange={(e) => setPassword(e.target.value)}
              value={password}
              className='w-full rounded-lg py-2 border-2 border-white/20 ring-1 ring-blue-400/20 pl-10 shadow-md text-gray-900'
              type="password"
              placeholder='Password'
            />
          </div>

          <div className='register-input flex relative w-full'>
            <LuMail className='text-blue-400 w-4 h-4 absolute left-1 top-1/2 -translate-y-1/2' />
            <input
              onChange={(e) => setEmail(e.target.value)}
              value={email}
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
                onChange={(e) => setImageProfile(e.target.files[0])}
                type="file"
                className='hidden'
              />
            </label>
          </div>

        </div>

        <div className='register-submit-button w-[60%] mx-auto mt-6'>
          <button
            disabled={isBeingSent}
            className={`w-full ${isBeingSent ? "bg-blue-300" : "bg-blue-500"} text-white p-2 shadow-lg rounded-md font-bold`}
          >
            {isBeingSent ? "Please wait" : "Sign up"}
          </button>
        </div>

      </form>
    </div>
  </div>
)
}

export default RegisterPage