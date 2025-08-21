import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';
import { BsBank2 } from 'react-icons/bs';
import { FaClockRotateLeft, FaArrowUp, FaArrowDown, FaEye, FaEyeSlash } from 'react-icons/fa6';
import { useNavigate } from 'react-router-dom';
import { parseJwt } from '../utils/jwt';

function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [remainingTime, setRemainingTime] = useState('');
  const [tokenPayload, setTokenPayload] = useState(null);
  const [visibleBalances, setVisibleBalances] = useState({});
  const navigate = useNavigate();

  // 로그아웃 처리
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

  // 잔액 표시/숨김 토글
  const toggleBalanceVisibility = (accountId) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }));
  };

  // 토큰 파싱 및 초기화
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

  // 세션 타이머 관리
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

  // 거래 내역 가져오기 (모든 계좌)
  const fetchTransactions = async (accountIds) => {
    const transactionPromises = accountIds.map(async (accountId) => {
      try {
        // axiosInstance가 자동으로 Authorization 헤더를 추가하므로 별도 헤더 설정 불필요
        const res = await axios.get(`/transactions/account/${accountId}`);
        // 최근 5건만 표시하도록 제한
        const recentTransactions = Array.isArray(res.data) ? res.data.slice(0, 5) : [];
        return { accountId, transactions: recentTransactions };
      } catch (err) {
        // 403 오류인 경우 해당 계좌의 거래내역 조회 권한이 없음을 의미
        if (error.response?.status === 401 && !originalRequest._retry) {
          console.warn(`계좌 ID ${accountId}에 대한 거래내역 조회 권한이 없습니다.`);
        } else {
          console.warn(`계좌 ${accountId} 거래내역 조회 실패:`, err);
        }
        // API 실패시 빈 배열 반환 (UI에서는 "거래내역이 없습니다"로 표시됨)
        return { accountId, transactions: [] };
      }
    });

    const results = await Promise.all(transactionPromises);
    const transactionMap = {};
    results.forEach(({ accountId, transactions }) => {
      transactionMap[accountId] = transactions;
    });
    setTransactions(transactionMap);
  };

  // 내 계좌 목록 불러오기
  useEffect(() => {
    axios.get('/accounts/my')
      .then(async (res) => {
        const accountsData = res.data;
        setAccounts(accountsData);
        
        // 각 계좌의 잔액을 기본적으로 숨김 상태로 설정
        const balanceVisibility = {};
        accountsData.forEach(acc => {
          balanceVisibility[acc.id] = false;
        });
        setVisibleBalances(balanceVisibility);

        // 거래 내역 가져오기
        if (accountsData.length > 0) {
          await fetchTransactions(accountsData.map(acc => acc.id));
        }
        
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
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-6">
      {/* 상단 헤더 */}
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-800">💳 내 금융 대시보드</h2>

        {/* 세션 정보 박스 */}
        <div className="flex items-center gap-4 bg-white shadow-lg px-6 py-3 rounded-2xl border border-blue-200">
          <FaClockRotateLeft className="text-blue-500 text-lg" />
          <span className="text-sm text-gray-700">세션 남은 시간: <b className="text-blue-600">{remainingTime}</b></span>
          <button
            className="text-sm text-blue-600 underline hover:text-blue-800 transition"
            onClick={handleSessionExtend}
          >
            🔁 연장
          </button>
          <button
            className="text-sm text-red-500 underline hover:text-red-700 transition"
            onClick={handleLogout}
          >
            로그아웃
          </button>
        </div>
      </div>

      {/* 계좌 없음 또는 에러 */}
      {error && (
        <div className="text-center text-red-500 mb-6">
          <p className="text-lg mb-4">{error}</p>
          <button
            onClick={() => navigate('/create-account')}
            className="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-3 rounded-xl hover:from-blue-600 hover:to-blue-700 transition shadow-lg"
          >
            계좌 개설하러 가기 →
          </button>
        </div>
      )}

      {!error && accounts.length === 0 && (
        <div className="text-center text-gray-700 space-y-4 bg-white rounded-2xl p-8 shadow-lg">
          <p className="text-xl">📭 등록된 계좌가 없습니다.</p>
          <button
            onClick={() => navigate('/create-account')}
            className="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-3 rounded-xl hover:from-blue-600 hover:to-blue-700 transition shadow-lg"
          >
            계좌 개설하러 가기 →
          </button>
        </div>
      )}

      {/* 가로 스크롤 계좌 목록 */}
      {accounts.length > 0 && (
        <div className="space-y-8">
          <div className="overflow-x-auto pb-4">
            <div className="flex space-x-6 min-w-max">
              {accounts.map((acc) => (
                <div
                  key={acc.id}
                  className="bg-white rounded-2xl shadow-xl p-6 min-w-[400px] max-w-[400px] border border-gray-100 hover:shadow-2xl transition-all duration-300"
                >
                  {/* 계좌 헤더 */}
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center gap-3">
                      <div className="bg-gradient-to-br from-blue-500 to-blue-600 p-3 rounded-full">
                        <BsBank2 className="text-white text-xl" />
                      </div>
                      <div>
                        <h3 className="text-lg font-bold text-gray-800">{acc.bankName || '은행명 없음'}</h3>
                        <p className="text-sm text-gray-500">{acc.accountNumber}</p>
                      </div>
                    </div>
                    <button
                      onClick={() => toggleBalanceVisibility(acc.id)}
                      className="text-gray-400 hover:text-gray-600 transition"
                    >
                      {visibleBalances[acc.id] ? <FaEyeSlash /> : <FaEye />}
                    </button>
                  </div>

                  {/* 잔액 표시 */}
                  <div className="mb-6">
                    <p className="text-sm text-gray-500 mb-1">계좌 잔액</p>
                    <p className="text-2xl font-bold text-gray-900">
                      {visibleBalances[acc.id] 
                        ? `${(Number(acc.balance) || 0).toLocaleString()} 원`
                        : '••••••• 원'
                      }
                    </p>
                  </div>

                  {/* 빠른 액션 버튼 */}
                  <div className="flex gap-2 mb-6">
                    <button
                      onClick={() => navigate(`/transfer?from=${acc.accountNumber}`)}
                      className="flex-1 bg-gradient-to-r from-blue-500 to-blue-600 text-white px-3 py-2 rounded-lg text-sm hover:from-blue-600 hover:to-blue-700 transition"
                    >
                      이체하기
                    </button>
                    <button
                      onClick={() => navigate(`/account-details/${acc.id}`)}
                      className="flex-1 bg-gray-100 text-gray-700 px-3 py-2 rounded-lg text-sm hover:bg-gray-200 transition"
                    >
                      상세보기
                    </button>
                  </div>

                  {/* 최근 거래 내역 */}
                  <div>
                    <h4 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
                      📈 최근 거래내역
                    </h4>
                    <div className="space-y-2 max-h-48 overflow-y-auto">
                      {transactions[acc.id]?.length > 0 ? (
                        transactions[acc.id].slice(0, 5).map((transaction, index) => (
                          <div key={transaction.id || index} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                            <div className="flex items-center gap-3">
                              <div className={`p-1.5 rounded-full ${
                                transaction.type === '입금' || transaction.type === 'DEPOSIT'
                                  ? 'bg-green-100 text-green-600' 
                                  : 'bg-red-100 text-red-600'
                              }`}>
                                {(transaction.type === '입금' || transaction.type === 'DEPOSIT') ? <FaArrowDown size={12} /> : <FaArrowUp size={12} />}
                              </div>
                              <div>
                                <p className="text-sm font-medium text-gray-800">{transaction.description}</p>
                                <p className="text-xs text-gray-500">
                                  {new Date(transaction.transactionDate).toLocaleDateString('ko-KR', {
                                    month: 'short',
                                    day: 'numeric',
                                    hour: '2-digit',
                                    minute: '2-digit'
                                  })}
                                </p>
                              </div>
                            </div>
                            <div className="text-right">
                              <p className={`text-sm font-semibold ${
                                (transaction.type === '입금' || transaction.type === 'DEPOSIT') ? 'text-green-600' : 'text-red-600'
                              }`}>
                                {(transaction.type === '입금' || transaction.type === 'DEPOSIT') ? '+' : '-'}{transaction.amount?.toLocaleString()}원
                              </p>
                              <p className="text-xs text-gray-400">
                                잔액: {transaction.balanceAfter?.toLocaleString()}원
                              </p>
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="text-center py-6">
                          <p className="text-sm text-gray-500">💳 거래 내역을 불러올 수 없습니다</p>
                          <p className="text-xs text-gray-400 mt-1">계좌 권한을 확인하거나 거래를 시작해보세요</p>
                          <button
                            onClick={() => navigate(`/transfer?from=${acc.accountNumber}`)}
                            className="mt-2 text-xs text-blue-600 underline hover:text-blue-800"
                          >
                            첫 거래 시작하기 →
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 하단 액션 버튼들 */}
          <div className="flex justify-center gap-4 mt-10">
            <button
              onClick={() => navigate('/scheduled-transfer/new')}
              className="bg-gradient-to-r from-purple-500 to-purple-600 text-white px-6 py-3 rounded-xl hover:from-purple-600 hover:to-purple-700 transition shadow-lg flex items-center gap-2"
            >
              📅 예약이체 등록하기
            </button>
            <button
              onClick={() => navigate('/scheduled-transfers')}
              className="bg-gradient-to-r from-green-500 to-green-600 text-white px-6 py-3 rounded-xl hover:from-green-600 hover:to-green-700 transition shadow-lg flex items-center gap-2"
            >
              📋 예약이체 목록 보기
            </button>
            <button
              onClick={() => navigate('/create-account')}
              className="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-3 rounded-xl hover:from-blue-600 hover:to-blue-700 transition shadow-lg flex items-center gap-2"
            >
              ➕ 새 계좌 개설
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;