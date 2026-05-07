import React, { useRef, useEffect, useState } from 'react'
import { LuPencilLine } from 'react-icons/lu'
import { data, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import api from '../../axiosClientApi/axios';
import { useTranslation } from 'react-i18next';

const Security = () => {
  const navigate = useNavigate();
  const securityRef = useRef(null);
  const {t} = useTranslation();
  const inputRef = useRef(null);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("")
  const [isEditing, setIsEditing] = useState(false);
  const [disableSendBtn, setDisableSendBtn] = useState(false);

  const passwordRef = useRef(null);


  useEffect(() => {
    const handleClickOutside = (e) => {
      if (securityRef && securityRef.current && !securityRef.current.contains(e.target)) {
        navigate("/profile");
      }
    }
    window.addEventListener("mousedown", handleClickOutside)

    return () => {
      window.removeEventListener("mousedown", handleClickOutside)
    }
  }, [])

  useEffect(() => {
    if (isEditing) passwordRef.current.focus();
  }, [isEditing]);

  const passwordMatch = (newPassword, confirmPassword) => {
    return newPassword === confirmPassword;
  }


  const handleUpdatePassword = async ({ newPassword, confirmPassword }) => {
    setDisableSendBtn(true);
    const isPasswordMatch = passwordMatch(newPassword, confirmPassword);
    
    if (!isPasswordMatch) {
      throw new Error(t("password-must-match"));
    }
    
    const res = await api.put("auth/update-password", { password: newPassword });

    return res.data;
  }

  const { mutate: applyPasswordUpdate, isPending: updatePasswordisPending } = useMutation({
    mutationFn: handleUpdatePassword,
    onSuccess: (data) => {
      toast.success(data.message)
    },
    onError: (error) => {
      setDisableSendBtn(false);
      toast.error(t("something-went-wrong"));
      console.error(error);
    }
  })



  return (
    <div className='w-full h-[100%] z-10 backdrop-blur-[2px] fixed top-0 left-0 right-0 rounded-t-xl flex flex-col items-center justify-center fadeInParent'>
      <div ref={securityRef} className="account-info-container bg-white backdrop-blur-2xl w-full h-[60vh] fixed bottom-0 rounded-t-2xl openExpenseDetail">
        <h1 className='text-center uppercase font-bold text-xl text-blue-500 mt-3'>{t("security")}</h1>
        <div className="personal-info flex flex-col gap-3 h-full w-full px-3 bg-white">
          <div className=" password-input-one flex items-center justify-between mx-auto shadow-md p-1 bg-white rounded-lg">
            <div className='flex flex-col '>
              <label htmlFor="name" className='text-gray-800/30'>{t("password")}</label>
              <input
                onChange={((e) => setNewPassword(e.target.value))}
                value={newPassword}
                type='text'
                ref={passwordRef}
                readOnly={isEditing ? false : true}
                className='bg-white text-gray-800 font-bold text-lg border-none focus:outline-none  border-white'
              />
            </div>
            <div className=' mt-5 mr-2'>
              <LuPencilLine
                onClick={() => setIsEditing(true)}
                size={20}
                className='text-gray-900 active:scale-125' />
            </div>
          </div>
          <div className="password-input-two flex items-center justify-between mx-auto shadow-md p-1 bg-white rounded-lg">
            <div className='flex flex-col'>
              <label htmlFor="name" className='text-gray-800/30'>{t("confirmPassword")}</label>
              <input
                onChange={((e) => setConfirmPassword(e.target.value))}
                value={confirmPassword}
                type="text"
                className='bg-white text-gray-800 font-bold text-lg border-none focus:outline-none bg-white border-white'
              />
            </div>

          </div>
          <div className=' w-full flex justify-center p-1'>
            <button
              onClick={() => applyPasswordUpdate({ newPassword, confirmPassword })}
              disabled={updatePasswordisPending}
              className={`${updatePasswordisPending ? "bg-blue-200" : "bg-blue-400"} text-white font-bold w-[90%] rounded-xl text-bold 
              tracking-widest text-lg transition-all duration-200 
              py-2 mt-5`}

            >
              {(updatePasswordisPending || disableSendBtn) ? t("updating") : t("update")}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Security