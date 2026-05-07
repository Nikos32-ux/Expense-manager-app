/** @type {import('tailwindcss').Config} */
import tailwindAnimate from "tailwindcss-animate";
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [tailwindAnimate],
  safelist: [
    'bg-blue-100', 'text-blue-700',
    'bg-yellow-100', 'text-yellow-700',
    'bg-green-100', 'text-green-700',
    'bg-slate-100', 'text-slate-700',
    'bg-red-100', 'text-red-700',
    'bg-orange-100', 'text-orange-700',
    'bg-purple-100', 'text-purple-700',
    'bg-pink-100', 'text-pink-700',
    'bg-indigo-100', 'text-indigo-700',
    'bg-teal-100', 'text-teal-700',
    'bg-cyan-100', 'text-cyan-700',
    'bg-rose-100', 'text-rose-700',
    'bg-gray-100', 'text-gray-700',
  ]
}