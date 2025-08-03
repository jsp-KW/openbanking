import { useState } from "react";
import axios from "../api/axiosInstance";
import { useNavigate, Link } from "react-router-dom";
import { AiOutlineLock, AiOutlineUser } from "react-icons/ai";

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.post('/auth/login', { email, password });
      const {accessToken, refreshToken } = response.data;
      if (!accessToken || typeof accessToken !== 'string') {
        throw new Error('로그인 응답에 토큰 없음 또는 잘못됨');
      }

      localStorage.setItem('jwtToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      navigate('/dashboard');
    } catch (error) {
      console.error('로그인 실패:', error);
      alert('이메일 또는 비밀번호가 틀렸습니다!');
    }
  };

  return (
    <div className="min-h-screen bg-yellow-100 flex items-center justify-center">
      <form onSubmit={handleLogin} className="bg-white p-8 rounded-xl shadow-md w-full max-w-sm">
        <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">로그인</h2>

        <div className="mb-4">
          <label className="block mb-1 text-sm font-medium text-gray-700">이메일</label>
          <div className="relative">
            <AiOutlineUser className="absolute left-3 top-2.5 text-gray-400" />
            <input
              type="email"
              placeholder="example@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full pl-10 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-300"
              required
            />
          </div>
        </div>

        <div className="mb-6">
          <label className="block mb-1 text-sm font-medium text-gray-700">비밀번호</label>
          <div className="relative">
            <AiOutlineLock className="absolute left-3 top-2.5 text-gray-400" />
            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full pl-10 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-300"
              required
            />
          </div>
        </div>

        <button
          type="submit"
          className="w-full bg-yellow-400 text-black font-semibold py-2 rounded-md hover:bg-yellow-300 transition"
        >
          로그인
        </button>

        <p className="mt-4 text-sm text-center text-gray-600">
          계정이 없으신가요?{' '}
          <Link to="/signup" className="text-yellow-600 font-semibold hover:underline">
            회원가입
          </Link>
        </p>
      </form>
    </div>
  );
}
