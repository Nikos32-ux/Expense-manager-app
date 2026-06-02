import { useMutation, useQuery } from '@tanstack/react-query';
import React, { useEffect, useReducer, useRef, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { LuCar, LuPencilLine, LuCheck, LuSquareX, LuX, LuLayoutGrid, LuCalendarDays, LuCreditCard, LuTrash2, LuSquarePen, LuHash } from 'react-icons/lu';
import { Link, useParams } from 'react-router-dom';
import useExpense from '../../hooks/useExpense';
import  {categories}  from '../../categories/categories.js';


const ExpenseDetail = () => {
  const { id } = useParams();
  const [isEditMode, setIsEditMode] = useState(false);
  const [updateExpensePending, setUpdateExpensePending] = useState(null);
  const [updateExpenseSuccess, setUpdateExpenseSuccess] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const expenseDetailRef = useRef(null);
  const navigate = useNavigate();

  const [newData, setNewData] = useState({
    amount: "",
    desc: "",
    category: {
      id: "",
      category: ""
    },
    date: "",
    payment: "",
  });

  const {
    expense,
    expenseError,
    expenseLoading,
    updateExpense,
    updatePending,
    updateSuccess,
    expenseDelete,
    expenseDeletePending,
    expenseDeleteSuccess } = useExpense(id);


  useEffect(() => {
    if (!expense) return;
    setNewData(prev => ({
      ...prev,
      amount: expense.amount,
      desc: expense.description,
      category: {
        ...prev.category,
        id: expense.categoryId,
        category: expense.categoryName
      },
      date: expense.date,
      payment: expense.payment
    }))
  }, [expense])

  useEffect(() => {
    let pendingTimer;

    if (updatePending) {
      setUpdateExpensePending(true);
    }

    if (updateSuccess) {
      setUpdateExpensePending(false);
      setUpdateExpenseSuccess(true);
      pendingTimer = setTimeout(() => {
        setUpdateExpenseSuccess(false);
        setIsEditMode(false);
      }, 2000);
    }

    return () => {
      clearTimeout(pendingTimer);
    }

  }, [updatePending, updateSuccess])


  useEffect(() => {
    const handleClickOutside = (e) => {
      if (expenseDetailRef && !expenseDetailRef.current.contains(e.target)) {
        navigate("/dashboard");
      }
    }

    window.addEventListener("mousedown", handleClickOutside);

    return () => {
      window.removeEventListener("mousedown", handleClickOutside)
    }
  }, [navigate])

  if (expenseLoading && !expense) return <div><h1>Loading...</h1></div>;
  const category = categories.filter(category => category.id === expense.categoryId)[0];

  

  return (
    <div className='container w-full h-[100dvh] fixed inset-0 z-50 bg-white/20 backdrop-blur-sm fadeInParent'>
      <div ref={expenseDetailRef} className='fixed bottom-0 left-0 right-0 h-[80vh] flex flex-col openExpenseDetail'>
        <div className="header relative">
          <div className='flex items-center justify-center bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 h-[25vh] rounded-t-2xl '>
            <Link to={"/dashboard"} className='absolute font-bold text-xl top-5 right-5 active:scale-125 bg-gray-100/20 px-2 py-1 rounded-md' >X</Link>
            <div className='flex flex-col items-center'>
              <div className={`${category.bg} w-16 h-16 rounded-[100%] flex items-center justify-center rounded- shadow-md`}>
                <category.icon className={`${category.text}`} size={32} />
              </div>
              <input
                onChange={(event) => setNewData(prev => ({ ...prev, amount: event.target.value }))}
                readOnly={isEditMode ? false : true}
                value={isEditMode ? newData.amount : expense.amount}
                className='input-amount text-3xl text-white font-extrabold bg-gradient-to-r from-yellow-400 via-orange-400 to-red-500 text-center bg-clip-text text-transparent mt-1 mb-2 outline-none border-none '
              />
              <p className='text-md text-gray-100 leading-tightest'>{new Date(expense.date).toLocaleDateString()}</p>
              <div className='w-[40%] mx-auto bg-white border border-3 border-gray-200 mt-4'></div>
            </div>
          </div>
        </div>
        <div className='card-info-container relative flex flex-col justify-around flex-1 overflow-y-auto min-h-0 bg-white p-2 '>
          {updateExpenseSuccess && (
            <div className="fadeInSuccess absolute  -top-10 self-center bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg animate-fadeInOut z-50">
              <h1 className='text-center'> Updated successfully!</h1>
            </div>
          )}
          {updateExpensePending
            ? (
              <div className="flex items-center justify-center h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
              </div>)
            : (
              <div className='grid  grid-cols-1 p-4 gap-1'>
                <div className='bg-white p-3 shadow-md text-black rounded-md flex flex-col h-full min-h-[70px]'>
                  <div className="transactionId flex gap-2 items-center mb-1">
                    <div className='bg-gray-200/40 p-1 rounded'>
                      <LuHash size={14} />
                    </div>
                    <span className='text-gray-500 text-[11px] font-bold uppercase'>Transaction ID:</span>
                  </div>
                  <p className='font-bold text-sm text-gray-800 mt-auto'>{expense.id}</p>
                </div>
                <div className='description-card bg-white p-3 shadow-md text-black rounded-md flex flex-col h-full min-h-[70px]'>
                  <div className="title flex gap-2 items-center">
                    <div className='bg-gray-200/20 p-1'>
                      <LuPencilLine />
                    </div>
                    <span className='text-gray-800 text-[11px] font-semibold uppercase'>Description</span>
                  </div>

                  <input
                    onChange={(event) => setNewData(prev => ({ ...prev, desc: event.target.value }))}
                    readOnly={isEditMode ? false : true}
                    value={isEditMode ? newData.desc : expense.description}
                    className='font-bold text-sm mt-1 text-gray-800 w-full border-none outline-none bg-transparent' />
                </div>
                <div className='bg-white p-3 shadow-md text-black rounded-md flex flex-col h-full min-h-[70px]'>
                  <div className="title flex gap-2 items-center mb-1">
                    <div className='bg-gray-200/20 p-1'>
                      <LuLayoutGrid size={14} />
                    </div>
                    <span className='text-gray-500 text-[11px] font-semibold uppercase'>Category</span>
                  </div>

                  <select
                    onChange={(event) => {
                      setNewData(prev => ({ ...prev, category: { ...prev.category, id: event.target.value } }))
                    }}
                    disabled={isEditMode ? false : true}
                    value={isEditMode ? newData.category.id : expense.categoryId}
                    name='categoryId'
                    type="categoryId"
                    id="categoryId"
                    className='font-bold text-sm text-gray-800 mt-auto  w-full border-none outline-none bg-transparent'
                  >
                    { categories.map((category) => {
                      return <option
                        key={category.id}
                        value={category.id}
                        className=''
                      >
                        {category.name}
                      </option>
                    })}

                  </select>
                </div>
                <div className='bg-white p-3 shadow-md active:scale-110 active:shadow-2xl text-black rounded-md flex flex-col h-full min-h-[70px]'>
                  <div className="title flex gap-2 items-center mb-1">
                    <div className='bg-gray-200/40 p-1 rounded'>
                      <LuCalendarDays size={14} />
                    </div>
                    <span className='text-gray-500 text-[11px] font-bold uppercase'>Date and Time</span>
                  </div>
                  {isEditMode
                    ? (
                      <input
                        value={newData.date}
                        onChange={(event) => setNewData(prev => ({ ...prev, date: event.target.value }))}
                        className='datetime-local'
                        type="datetime-local" />
                    )
                    : (
                      <p className='font-bold text-sm text-gray-800 mt-auto leading-tight'>
                        {new Date(expense.date).toLocaleString('en-US', {
                          weekday: 'long',
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </p>
                    )
                  }

                </div>
                <div className='bg-blue/20 p-3 shadow-3xl text-black rounded-md flex flex-col h-full min-h-[70px]'>
                  <div className="title flex gap-2 items-center mb-1">
                    <div className='bg-gray-200/40 p-1 rounded'>
                      <LuCreditCard size={14} />
                    </div>
                    <span className='text-gray-500 text-[11px] font-bold uppercase'>Payment Method</span>
                  </div>
                  <input
                    onChange={(event) => setNewData(prev => ({ ...prev, payment: event.target.value }))}
                    readOnly={isEditMode ? false : true}
                    value={isEditMode ? newData.payment : expense.payment}
                    className='font-bold text-sm text-gray-800 mt-auto w-full border-none outline-none bg-transparent'
                  />
                </div>
              </div>)
          }
          {deleteConfirm &&
            <div class="fixed inset-0 flex items-center justify-center bg-black/50 z-50">
              <div class="bg-white rounded-xl shadow-lg p-6 w-[90%] text-center">
                {expenseDeletePending
                  ? (
                    <div className="flex items-center justify-center h-screen">
                      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
                    </div>
                  )
                  : (
                    <div>
                      <h2 class="text-lg font-semibold mb-8">Are you sure you want to delete this expense?</h2>
                      <div class="flex justify-around">
                        <button onClick={() => expenseDelete(expense.id)} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600">Delete</button>
                        <button onClick={() => setDeleteConfirm(false)} class="bg-gray-200 px-4 py-2 rounded hover:bg-gray-300">Cancel</button>
                      </div>
                    </div>
                  )
                };

              </div>
            </div>
          }
          {isEditMode
            ? (
              <div className="isEditModeBtns flex gap-2 justify-around p-2">
                <button onClick={() => {
                  updateExpense({ id: expense.id, data: newData })
                }} className='relative z-10 bg-green-400 flex justify-center p-2 items-center flex-1 rounded-md' >
                  <LuCheck className='text-white-600 mr-2 font-bold' />
                </button>
                <button onClick={() => setIsEditMode(false)} className='bg-white border border-red-600 text-red-600 font-bold flex justify-center p-2 items-center flex-1 rounded-md'>
                  <LuX className='text-red-600 mr-2 font-bold' />
                </button>
              </div>
            )
            : (
              <div className="sticky bottom-0 edit-delete-btns flex gap-2 justify-around p-2">
                <button className='bg-gradient-to-b from-teal-300 to-teal-500 flex justify-center p-2 items-center flex-1 rounded-md'>
                  <LuSquarePen className='text-white-600 mr-2 font-bold' />
                  <span onClick={() => setIsEditMode(true)} className='text-white text-md font-semibold '>
                    Edit
                  </span>
                </button>
                <button onClick={() => setDeleteConfirm(true)} className='bg-white border border-red-600 text-red-600 font-bold flex justify-center p-2 items-center flex-1 rounded-md'>
                  <LuTrash2 className='text-red-600 mr-2 font-bold' />
                  Delete
                </button>
              </div>
            )
          }

        </div>
      </div >
    </div >
  )
}

export default ExpenseDetail