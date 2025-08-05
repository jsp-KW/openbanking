
import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';
import { BsBank2 } from 'react-icons/bs';
import { FaClockRotateLeft } from 'react-icons/fa6';
import { useNavigate } from 'react-router-dom';
import { parseJwt } from '../utils/jwt';

function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [remainingTime, setRemainingTime] = useState('');
  const [tokenPayload, setTokenPayload] = useState(null);
  const navigate = useNavigate();

  //  로그아웃 처리
const handleLogout = async () => {
  try {
    const accessToken = localStorage.getItem('jwtToken');
    if (accessToken) {
      await axios.post('/auth/logout', {}, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
    }
  } catch (err) {
    console.warn('서버 로그아웃 실패:', err.response?.data || err.message);
  } finally {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('refreshToken');
    navigate('/login');
  }
};



  // 세션 연장 처리
  const handleSessionExtend = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) throw new Error('refreshToken 없음');

      const res = await axios.post('/auth/refresh', {}, {
        headers: {
          Authorization: `Bearer ${refreshToken}`,
        },
      });

      const newAccessToken = res.data.accessToken;
      if (!newAccessToken || typeof newAccessToken !== 'string') {
        throw new Error('accessToken 없음 또는 형식 오류');
      }

      localStorage.setItem('jwtToken', newAccessToken);
      const newPayload = parseJwt(newAccessToken);

      if (!newPayload?.exp) {
        throw new Error('파싱 실패한 accessToken');
      }

      setTokenPayload(newPayload);
      alert(' 세션이 연장되었습니다!');
    } catch (err) {
      console.error('세션 연장 실패:', err);
      alert(' 세션 연장 실패. 다시 로그인해주세요.');
      handleLogout();
    }
  };

  //  토큰 파싱 및 초기화
  useEffect(() => {
    const token = localStorage.getItem('jwtToken');

    if (!token || typeof token !== 'string' || !token.includes('.')) {
      console.warn('토큰 없음 또는 형식 오류 - 로그아웃 처리');
      handleLogout();
      return;
    }

    const payload = parseJwt(token);
    if (payload?.exp) {
      setTokenPayload(payload);
    } else {
      console.warn(' 토큰 파싱 실패 - 로그아웃');
      handleLogout();
    }
  }, []);

  //  세션 타이머 관리
  useEffect(() => {
    if (!tokenPayload?.exp) return;

    const interval = setInterval(() => {
      const now = Math.floor(Date.now() / 1000);
      const secondsLeft = tokenPayload.exp - now;

      if (secondsLeft <= 0) {
        setRemainingTime('만료됨');
        clearInterval(interval);
        alert('세션이 만료되었습니다. 다시 로그인 해주세요.');
        handleLogout();
        return;
      }

      const minutes = String(Math.floor(secondsLeft / 60)).padStart(2, '0');
      const seconds = String(secondsLeft % 60).padStart(2, '0');
      setRemainingTime(`${minutes}:${seconds}`);
    }, 1000);

    return () => clearInterval(interval);
  }, [tokenPayload]);

  //  내 계좌 목록 불러오기
  useEffect(() => {
    axios.get('/accounts/my')
      .then((res) => {
        setAccounts(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('계좌 조회 실패:', err);
        if (err.response?.status === 403) {
          alert('세션이 만료되었습니다. 다시 로그인해주세요.');
          handleLogout();
        } else {
          setError('계좌 정보를 불러오지 못했습니다.');
        }
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <p className="text-center mt-10 text-gray-600">🔄 계좌 정보를 불러오는 중입니다...</p>;
  }
  return (
    <div className="min-h-screen bg-yellow-100 p-6">
      {/* 상단 헤더 */}
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">🏦 내 계좌 목록</h2>

        {/* 세션 정보 박스 */}
        <div className="flex items-center gap-4 bg-white shadow px-4 py-2 rounded-xl border border-yellow-300">
          <FaClockRotateLeft className="text-yellow-500 text-lg" />
          <span className="text-sm text-gray-700">세션 남은 시간: <b>{remainingTime}</b></span>
          <button
            className="text-sm text-blue-600 underline hover:text-blue-800"
            onClick={handleSessionExtend}
          >
            🔁 연장
          </button>
          <button
            className="text-sm text-red-500 underline hover:text-red-700"
            onClick={handleLogout}
          >
            로그아웃
          </button>
        </div>
      </div>

      {/* 계좌 없음 */}
      {error && (
        <div className="text-center text-red-500 mb-6">
          <p>{error}</p>
          <button
            onClick={() => navigate('/create-account')}
            className="mt-4 inline-block bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600 transition"
          >
            계좌 개설하러 가기 →
          </button>
        </div>
      )}

      {!error && accounts.length === 0 && (
        <div className="text-center text-gray-700 space-y-3">
          <p>📭 등록된 계좌가 없습니다.</p>
          <button
            onClick={() => navigate('/create-account')}
            className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600 transition"
          >
            계좌 개설하러 가기 →
          </button>
        </div>
      )}

      {/* 계좌 목록 */}
     {accounts.length > 0 && (
  <div className="space-y-4">
    {accounts.map((acc) => (
      <div
        key={acc.id}
        className="bg-white rounded-xl shadow-md p-4 flex justify-between items-center"
      >
        <div>
          <p className="text-lg font-semibold text-gray-800 flex items-center gap-2">
            <BsBank2 className="text-yellow-500" />
            {acc.bankName || '은행명 없음'}
          </p>
          <p className="text-sm text-gray-600">계좌번호: {acc.accountNumber}</p>
        </div>
        <div className="text-right">
          <p className="text-lg font-bold text-gray-900">
            {(Number(acc.balance) || 0).toLocaleString()} 원
          </p>
          <button
            onClick={() => navigate(`/transfer?from=${acc.accountNumber}`)}
            className="mt-2 text-sm text-blue-600 underline hover:text-blue-800"
          >
            이체하기 →
          </button>
        </div>
      </div>
    ))}

    {/* ✅ 계좌 추가 버튼 */}
    <div className="text-center mt-6">
      <button
        onClick={() => navigate('/create-account')}
        className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600 transition"
      >
        ➕ 계좌 추가 개설하기
      </button>
    </div>
  </div>
)}
</div>
  );
}

export default Dashboard;
