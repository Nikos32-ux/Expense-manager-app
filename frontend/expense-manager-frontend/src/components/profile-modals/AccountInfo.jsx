import React, { useEffect, useRef, useState } from 'react'
import { LuPencilLine } from 'react-icons/lu';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { verifyUser } from '../../queries/authQuery';
import api from '../../axiosClientApi/axios';
import toast from 'react-hot-toast';
import { useMutation } from '@tanstack/react-query';
import { useRouteLoaderData } from 'react-router-dom';

const AccountInfo = () => {
    const { t, i18n } = useTranslation();
    const user = useRouteLoaderData("root")
    const modalRef = useRef(null);
    const usernameRef = useRef(null);
    const emailRef = useRef(null);
    const navigate = useNavigate();
    const [isUsernameEdit, setIsUsernameEdit] = useState(false);
    const [isEmailEdit, setIsEmailEdit] = useState(false);
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [draftContent, setDraftContent] = useState({
        username: "",
        email: ""
    });
    const [langChoice, setLangChoice] = useState(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const handleLangChange = (langChoice) => {
        i18n.changeLanguage(langChoice);
        localStorage.setItem("lang", langChoice);
        navigate("/profile");
    }

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (modalRef && modalRef.current && !modalRef.current.contains(e.target)) {
                navigate("/profile");
            }
        }
        window.addEventListener("mousedown", handleClickOutside)

        return () => {
            window.removeEventListener("mousedown", handleClickOutside)
        }
    }, [])

    useEffect(() => {
        if (isUsernameEdit) {
            usernameRef.current.focus();
        } else if (isEmailEdit) {
            emailRef.current.focus();
        }
    }, [isUsernameEdit, isEmailEdit]);



    const handleUpdateAccount = async (data) => {
        const res = await api.put("auth/update-account-info", data);
        console.log("updateAccount result", res.data);
        return res.data;
    }

    const { mutate: applyAccountUpdatedInfo, isPending: updateAccountisPending } = useMutation({
        mutationFn: handleUpdateAccount,
        onSuccess: (data) => {
            toast.success("Account successfully updated")
        },
        onError: () => {
            toast.error("Failed to update account");
        }
    })



    return (
        <div className='w-full h-[100%] z-10 backdrop-blur-[2px] fixed top-0 left-0 right-0 rounded-t-xl flex flex-col items-center justify-center overflow-hidden fadeInParent
lg:left-72 lg:w-[calc(100%-18rem)]'>
            <div ref={modalRef} className="account-info-container bg-white w-full max-w-auto h-[60vh] fixed bottom-0 rounded-t-2xl openExpenseDetail">
                <h1 className='text-center uppercase font-bold text-xl text-blue-500 mt-3'>{t("profile")}</h1>
                <div className="language flex flex-col p-4 mb-4 bg-gray-50 rounded-xl border border-gray-100">
                    <label htmlFor="language" className='text-gray-500 text-xs uppercase tracking-wider font-bold mb-3'>
                        {t('language')}
                    </label>

                    <div className="flex items-center justify-between gap-4">

                        <div className="flex bg-gray-200 p-1 rounded-xl w-full max-w-[240px]">
                            <button
                                type="button"
                                onClick={() => setLangChoice("en")}
                                className={`flex-1 px-4 py-2 text-sm font-semibold rounded-lg transition-all duration-200 ${langChoice === "en"
                                    ? "bg-white text-blue-600 shadow-sm"
                                    : "text-gray-500 hover:text-gray-700"
                                    }`}
                            >
                                English
                            </button>
                            <button
                                type="button"
                                onClick={() => setLangChoice("el")}
                                className={`flex-1 px-4 py-2 text-sm font-semibold rounded-lg transition-all duration-200 ${langChoice === "el"
                                    ? "bg-white text-blue-600 shadow-sm"
                                    : "text-gray-500 hover:text-gray-700"
                                    }`}
                            >
                                Ελληνικά
                            </button>
                        </div>

                        <button
                            onClick={() => handleLangChange(langChoice)}
                            className='text-blue-500 font-bold text-sm hover:bg-blue-50 px-4 py-2 rounded-lg transition-colors'
                        >
                            {t('apply')}
                        </button>
                    </div>
                </div>
                <div className="personal-info h-full w-full px-3 p-4 gap-6">
                    <div className="name mb-3 mt-4 flex items-center justify-between mx-auto shadow-md p-1 bg-white rounded-lg">
                        <div className='flex flex-col w-full mb-4'>
                            <label htmlFor="name" className='text-gray-500 text-sm mb-1 font-medium'>{t('username')}</label>
                            <input
                                onChange={(e) => setDraftContent(prev => ({ ...prev, username: e.target.value }))}
                                type="text"
                                readOnly={isUsernameEdit ? false : true}
                                ref={usernameRef}
                                value={isUsernameEdit ? draftContent.username : user.username}
                                className="bg-white/80 backdrop-blur-sm shadow-sm text-gray-800 font-semibold text-lg rounded-md px-3 py-2 border-none outline-none "
                            />
                        </div>
                        <div className=' mt-5 mr-2'>
                            <LuPencilLine size={20} className='text-gray-900 active:scale-125' onClick={() => setIsUsernameEdit(true)} />
                        </div>
                    </div>
                    <div className="email flex items-center justify-between mx-auto shadow-md p-1 bg-white rounded-lg mb-3">
                        <div className='flex flex-col'>
                            <label htmlFor="name" className='text-gray-500 text-sm mb-1 font-medium'>{t('email')}</label>
                            <input
                                onChange={(e) => setDraftContent(prev => ({ ...prev, email: e.target.value }))}
                                type="text"
                                readOnly={isEmailEdit ? false : true}
                                ref={emailRef}
                                value={isEmailEdit ? draftContent.email : user.email}
                                className="bg-white/80 backdrop-blur-sm shadow-sm text-gray-800 font-semibold text-lg rounded-md px-3 py-2 border-none outline-none"
                            />
                        </div>
                        <div className=' mt-5 mr-2'>
                            <LuPencilLine size={20} className='text-gray-900 active:scale-125' onClick={() => setIsEmailEdit(true)} />
                        </div>
                    </div>
                    <div className=' w-full flex justify-center'>
                        <button
                            onClick={() => applyAccountUpdatedInfo(draftContent)}
                            disabled={updateAccountisPending}
                            className={`${updateAccountisPending ? "hover:bg-blue-200" : "bg-blue-400 "} text-white font-bold w-full rounded-xl lg:max-w-[200px] text-bold tracking-widest text-lg transition-all duration-200 p-3 mt-5`}>
                            {!updateAccountisPending ? t('update') : "Updating..."}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default AccountInfo