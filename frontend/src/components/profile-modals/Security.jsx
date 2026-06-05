import React, { useRef, useEffect, useState, useMemo } from 'react'
import { LuPencilLine } from 'react-icons/lu'
import { data, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import api from '../../axiosClientApi/axios';
import { useTranslation } from 'react-i18next';
import { z } from "zod";

const Security = () => {
  const navigate = useNavigate();
  const securityRef = useRef(null);
  const { t } = useTranslation();
  const inputRef = useRef(null);
  const [newPassword, setNewPassword] = useState("");
  const [confirmNewPassword, setConfirmNewPassword] = useState("")
  const [isEditing, setIsEditing] = useState(false);
  const [errors, setErrors] = useState({});
  const passwordRef = useRef(null);


  const { mutate: applyPasswordUpdate, isPending: updatePasswordIsPending } = useMutation({
    mutationFn: async (data) => {

      const res = await api.put("auth/update-password", { password: data });
      return res.data;
    },
    onSuccess: async (response) => {
      setNewPassword("");
      setConfirmNewPassword("");
      toast.success(response.message);
      try {
        await api.post("/auth/logout");
        queryClient.removeQueries();
        localStorage.removeItem("REACT_QUERY_OFFLINE_CACHE");
        window.location.replace("/login")
      }
      catch (err) {
        console.error("Logout failed:", err);
        window.location.replace("/login")
      }
    },
    onError: (error) => {
      const errors = error.response?.data?.data;
      if (errors) {
        setErrors(errors);
      }
      toast.error(t("something-went-wrong"));
      console.error("Mutation failed:", error);
    }
  })

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (updatePasswordIsPending) return;

      if (securityRef && securityRef.current && !securityRef.current.contains(e.target)) {
        navigate("/profile");
      }
    }
    window.addEventListener("mousedown", handleClickOutside)

    return () => {
      window.removeEventListener("mousedown", handleClickOutside)
    }
  }, [updatePasswordIsPending])

  useEffect(() => {
    if (isEditing) passwordRef.current.focus();
  }, [isEditing]);

  const passwordSchema = z.object({
    password: z.string()
      .min(8, "Password must be at least 8 characters")
      .max(25, "Password cannot exceed 25 characters")
      .regex(/[A-Z]/, "Must contain at least one uppercase letter")
      .regex(/[a-z]/, "Must contain at least one lowercase letter")
      .regex(/[!@#$%^&*(),.?":{}|<>]/, "Must contain at least one special character"),
    confirmPassword: z.string()
  }).refine((data) => data.password === data.confirmPassword, {
    message: "Passwords must match",
    path: ["confirmPassword"]
  })

  const handleSubmitPassword = (e) => {
    e.preventDefault();
    const validation = passwordSchema.safeParse({ password: newPassword, confirmPassword: confirmNewPassword })
    if (!validation.success) {
      setErrors(validation.error.flatten().fieldErrors);
      return;
    }

    applyPasswordUpdate(validation.data.password)
  }

  const passwordRules = useMemo(() => [
      { id: 1, type: "length", rule: "8 to 25 characters", valid: newPassword.length >= 8 && newPassword.length <= 25 },
      { id: 2, type: "uppercase", rule: "An uppercase letter", valid: /[A-Z]/.test(newPassword) },
      { id: 3, type: "lowercase", rule: "A lowercase letter", valid: /[a-z]/.test(newPassword) },
      { id: 4, type: "special", rule: "A special character", valid: /[!@#$%^&*(),.?":{}|<>]/.test(newPassword) }
    ]
  , [newPassword]);

  return (
    <>
      {updatePasswordIsPending && (
        <div className="fixed inset-0 z-[9999] cursor-wait bg-black/10" />
      )}
      <div className={`w-full h-[100%] z-10 backdrop-blur-[2px] fixed top-0 left-0 right-0 rounded-t-xl flex flex-col items-center justify-center fadeInParent `}>
        <div
          ref={securityRef}
          className={`account-info-container flex flex-col bg-white backdrop-blur-2xl w-full h-[60vh] min-h-0 fixed bottom-0 rounded-t-2xl  openExpenseDetail `}>
          <h1 className='text-center uppercase font-bold text-xl text-blue-500 mt-3'>{t("security")}</h1>
          <form onSubmit={handleSubmitPassword} className="personal-info py-4 flex flex-col gap-4 w-full h-full px-4 bg-white pb-5 min-h-0">
            <div className='w-full flex-1 overflow-y-auto px-4 pb-12 flex flex-col gap-4 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden'>
              <div className={`password-input-one w-full flex items-center justify-between mx-auto shadow-md p-1 rounded-lg transition-all`}>
                <div className='flex flex-col '>
                  <label htmlFor="password" className='text-gray-800/30'>{t("password")}</label>
                  <input
                    id="password"
                    onChange={((e) => { setNewPassword(e.target.value) })}
                    disabled={updatePasswordIsPending}
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
              <div className="update-password-rules text-gray-800">
                <h1 className="font-semibold mb-2">
                  Password must contain:
                </h1>
                <div className="rules-container flex flex-col gap-2">
                  {passwordRules.map((password) => {
                    return <div key={password.id} className='flex items-center gap-2'>
                      <span className={`rounded-full h-4 w-4  ${password.valid ? "bg-emerald-500" : "bg-gray-300"}`}>
                      </span>
                      <span className={`${password.valid ? "block" : "hidden"}`}>✓</span>
                      <p>{password.rule}</p>
                    </div>
                  })}
                </div>
              </div>
              {errors.password && (
                <span className="text-red-500 text-sm px-1 font-semibold">
                  {errors.password[0]}
                </span>
              )}
              <div className={`password-input-two w-full flex items-center justify-between mx-auto shadow-md p-1 rounded-lg transition-all ${updatePasswordIsPending ? "opacity-50 pointer-events-none" : "opacity-100"}`}>
                <div className='flex flex-col'>
                  <label htmlFor="confirm-password" className='text-gray-800/30'>{t("confirmPassword")}</label>
                  <input
                    id="confirm-password"
                    onChange={((e) => setConfirmNewPassword(e.target.value))}
                    disabled={updatePasswordIsPending}
                    value={confirmNewPassword}
                    type="text"
                    className='bg-white text-gray-800 font-bold text-lg border-none focus:outline-none bg-white border-white'
                  />
                </div>

              </div>
              {errors.confirmPassword && (
                <span className="text-red-500 text-sm px-1 font-semibold">
                  {errors.confirmPassword[0]}
                </span>
              )}
              <div className='update-button-container w-full flex justify-center p-1'>
                <button
                  disabled={updatePasswordIsPending}
                  className={`${updatePasswordIsPending ? "bg-blue-200" : "bg-blue-600"} text-white font-bold w-[90%] rounded-xl text-bold
              tracking-widest text-lg transition-all duration-200 
              py-2 mt-5`}

                >
                  {updatePasswordIsPending ? (
                    <span className="flex items-center justify-center gap-2">
                      <span className="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
                      {t("updating")}
                    </span>
                  ) : t("update")}
                </button>
              </div>
            </div>

          </form>
        </div>
      </div>
    </>
  )
}

export default Security