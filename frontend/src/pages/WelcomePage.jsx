import React from 'react'
import { Link } from 'react-router-dom';

const WelcomePage = () => {
  return (
    <div className='flex flex-col min-h-[100dvh] lg:flex-row lg:items-center lg:justify-center lg:bg-gray-900'>
      <div className='lg:w-[900px] lg:flex lg:rounded-2xl lg:overflow-hidden lg:shadow-2xl'>
        
        <div className='w-full h-[50vh] flex justify-center items-top relative bg-[linear-gradient(to_bottom,rgba(0,0,0,0.4),rgba(0,0,0,0.4)),url("/login-page.jpg")] bg-cover bg-center lg:h-auto lg:w-1/2'>
          <p className='text-[40px] text-white mt-10 font-bold tracking-widest '>
            Expensifier
          </p>
        </div>
        <div className='w-full wp-signup flex-1 rounded-t-[40px] bg-black/30 backdrop-blur-lg border-t border-white/10 text-white -mt-10 relative z-10
                        lg:mt-0 lg:rounded-none lg:border-t-0 lg:flex lg:flex-col lg:justify-center'>

          <div className='wp-heading text-center text-2xl mt-5 text-blue-500 p-3 w-[300px] mx-auto'>
            <h1 className='text-2xl italic'>Best way to keep track of your expenses</h1>
          </div>

          <div className='wp-buttons pt-4 w-[60%] mx-auto flex flex-col text-center gap-4 pb-5'>
            <Link className='button bg-blue-500 hover:bg-blue-700 transition-all duration-300 text-white font-bold text-md tracking-widest p-2 w-[90%] rounded-md  mx-auto'
              to={"/login"}>
              Sign in
            </Link>

            <Link className='button bg-blue-500 hover:bg-blue-700 text-white transition-all duration-300 font-bold text-md tracking-widest p-2 w-[90%] rounded-md mx-auto'
              to={"/register"}>
              Sign up
            </Link>
          </div>

        </div>
      </div>
    </div>
  )
}

export default WelcomePage