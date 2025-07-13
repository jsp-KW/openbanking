import { useState } from 'react';
import axios from '../api/axiosInstance';
import { useNavigate, Link } from 'react-router-dom';
import { AiOutlineUser, AiOutlineMail, AiOutlineLock, AiOutlinePhone } from 'react-icons/ai';

function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    phone: '',
    role: 'USER',
  });

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    axios.post('/auth/signup', form)
      .then(() => {
        alert('회원가입 성공!');
        navigate('/login');
      })
      .catch(() => {
        alert('회원가입 실패!');
      });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-yellow-100">
      <form onSubmit={handleSubmit} className="bg-white p-8 rounded-xl shadow-md w-full max-w-sm">
        <h2 className="text-2xl font-bold mb-6 text-center text-gray-800"> 회원가입</h2>

        {/* 이름 */}
        <div className="mb-4">
          <label className="block mb-1 text-sm text-gray-700">이름</label>
          <div className="relative">
            <AiOutlineUser className="absolute left-3 top-2.5 text-gray-400" />
            <input
              name="name"
              type="text"
              onChange={handleChange}
              required
              className="w-full pl-10 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-300"
            />
          </div>
        </div>

        {/* 이메일 */}
        <div className="mb-4">
          <label className="block mb-1 text-sm text-gray-700">이메일</label>
          <div className="relative">
            <AiOutlineMail className="absolute left-3 top-2.5 text-gray-400" />
            <input
              name="email"
              type="email"
              onChange={handleChange}
              required
              className="w-full pl-10 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-300"
            />
          </div>
        </div>

        {/* 비밀번호 */}
        <div className="mb-4">
          <label className="block mb-1 text-sm text-gray-700">비밀번호</label>
          <div className="relative">
            <AiOutlineLock className="absolute left-3 top-2.5 text-gray-400" />
            <input
              name="password"
              type="password"
              onChange={handleChange}
              required
              className="w-full pl-10 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-300"
            />
          </div>
        </div>

        {/* 전화번호 */}
        <div className="mb-6">
          <label className="block mb-1 text-sm text-gray-700">전화번호</label>
          <div className="relative">
            <AiOutlinePhone className="absolute left-3 top-2.5 text-gray-400" />
            <input
              name="phone"
              type="text"
              onChange={handleChange}
              required
              className="w-full pl-10 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-300"
            />
          </div>
        </div>

        {/* 버튼 */}
        <button
          type="submit"
          className="w-full bg-yellow-400 text-black font-semibold py-2 rounded-md hover:bg-yellow-300 transition"
        >
          회원가입
        </button>

        <p className="mt-4 text-sm text-center text-gray-600">
          이미 계정이 있으신가요?{' '}
          <Link to="/login" className="text-yellow-600 font-semibold hover:underline">
            로그인
          </Link>
        </p>
      </form>
    </div>
  );
}

export default Signup;
