import { useState } from 'react';
import axios from '../api/axiosInstance';
import { useNavigate } from 'react-router-dom';


//프론트 : 이메일 +비밀번호 입력
//백엔드에 POST API 방식 /api/auth/login 으로 전송

// 백엔드 : 해당 email이 db에 존재하는지 확인
// 비밀번호가 맞는경우 -> JWT 토큰 발급
// 프론트에 토큰 반환

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
 const navigate = useNavigate();

  const handleLogin =async (e) => {
    e.preventDefault();
    console.log('로그인 시도:', email, password);
    // TODO: axios로 로그인 요청

    
    try {
      const response = await axios.post('/auth/login', {
        email,
        password,
      });

      const { token } = response.data;
      console.log('받은 토큰:', token); // 

      localStorage.setItem('jwtToken', token);
      navigate('/dashboard');
    } catch (error) {
      console.error('로그인 실패:', error);
      alert('이메일 또는 비밀번호가 틀렸습니다!');
    }

  };

  return (
    <div>
      <h2>로그인</h2>
      <form onSubmit={handleLogin}>
        <input
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        /><br />
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        /><br />
        <button type="submit">로그인</button>
      </form>
    </div>
  );
}

export default Login;  
