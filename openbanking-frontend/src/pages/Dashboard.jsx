import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';
import { BsBank2 } from 'react-icons/bs';
import { FaClockRotateLeft, FaArrowUp, FaArrowDown, FaEye, FaEyeSlash, FaChevronLeft, FaChevronRight } from 'react-icons/fa6';
import { useNavigate } from 'react-router-dom';
import { parseJwt } from '../utils/jwt';
import { LineChart, Line, XAxis, YAxis, ResponsiveContainer, AreaChart, Area } from 'recharts';

function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [remainingTime, setRemainingTime] = useState('');
  const [tokenPayload, setTokenPayload] = useState(null);
  const [visibleBalances, setVisibleBalances] = useState({});
  const [currentAccountIndex, setCurrentAccountIndex] = useState(0);
  const navigate = useNavigate();

  // 현재 선택된 계좌
  const currentAccount = accounts[currentAccountIndex];

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

  // 계좌 전환 함수
  const nextAccount = () => {
    setCurrentAccountIndex((prev) => (prev + 1) % accounts.length);
  };

  const prevAccount = () => {
    setCurrentAccountIndex((prev) => (prev - 1 + accounts.length) % accounts.length);
  };

  // 잔액 표시/숨김 토글
  const toggleBalanceVisibility = (accountId) => {
    setVisibleBalances(prev => ({
      ...prev,
      [accountId]: !prev[accountId]
    }));
  };

  // 그래프 데이터 생성
  const getChartData = () => {
    if (!currentAccount || !transactions[currentAccount.id]) return [];
    
    const accountTransactions = transactions[currentAccount.id];
    return accountTransactions
      .slice(0, 7)
      .reverse()
      .map((t, index) => ({
        date: new Date(t.transactionDate).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }),
        balance: t.balanceAfter,
        amount: t.type === 'DEPOSIT' || t.type === '입금' ? t.amount : -t.amount,
        type: t.type
      }));
  };

  // 최근 수입/지출 요약
  const getSummary = () => {
    if (!currentAccount || !transactions[currentAccount.id]) return { income: 0, expense: 0 };
    
    const accountTransactions = transactions[currentAccount.id];
    const income = accountTransactions
      .filter(t => t.type === 'DEPOSIT' || t.type === '입금')
      .reduce((sum, t) => sum + t.amount, 0);
    const expense = accountTransactions
      .filter(t => t.type === 'WITHDRAWAL' || t.type === '출금')
      .reduce((sum, t) => sum + t.amount, 0);
    return { income, expense };
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
        const res = await axios.get(`/transactions/account/${accountId}`);
        const recentTransactions = Array.isArray(res.data) ? res.data.slice(0, 10) : [];
        return { accountId, transactions: recentTransactions };
      } catch (err) {
        if (err.response?.status === 403) {
          console.warn(`계좌 ID ${accountId}에 대한 거래내역 조회 권한이 없습니다.`);
        } else {
          console.warn(`계좌 ${accountId} 거래내역 조회 실패:`, err);
        }
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
    return <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
      <p className="text-center text-gray-600 text-xl">🔄 계좌 정보를 불러오는 중입니다...</p>
    </div>;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* 상단 헤더 */}
      <div className="flex justify-between items-center p-6 pb-4">
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
        <div className="text-center text-red-500 mb-6 px-6">
          <div className="bg-white rounded-2xl p-8 shadow-lg">
            <p className="text-lg mb-4">{error}</p>
            <button
              onClick={() => navigate('/create-account')}
              className="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-3 rounded-xl hover:from-blue-600 hover:to-blue-700 transition shadow-lg"
            >
              계좌 개설하러 가기 →
            </button>
          </div>
        </div>
      )}

      {!error && accounts.length === 0 && (
        <div className="text-center text-gray-700 space-y-4 bg-white rounded-2xl p-8 shadow-lg mx-6">
          <p className="text-xl">📭 등록된 계좌가 없습니다.</p>
          <button
            onClick={() => navigate('/create-account')}
            className="bg-gradient-to-r from-blue-500 to-blue-600 text-white px-6 py-3 rounded-xl hover:from-blue-600 hover:to-blue-700 transition shadow-lg"
          >
            계좌 개설하러 가기 →
          </button>
        </div>
      )}

      {/* 메인 계좌 카드 및 그래프 */}
      {accounts.length > 0 && currentAccount && (
        <div className="space-y-6">
          {/* 계좌 네비게이션 */}
          <div className="px-6">
            <div className="flex justify-between items-center mb-4">
              <button
                onClick={prevAccount}
                disabled={accounts.length <= 1}
                className="p-3 rounded-full bg-white shadow-md hover:shadow-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <FaChevronLeft className="text-gray-600 text-lg" />
              </button>
              
              <div className="flex gap-2">
                {accounts.map((_, index) => (
                  <button
                    key={index}
                    onClick={() => setCurrentAccountIndex(index)}
                    className={`w-3 h-3 rounded-full transition ${
                      index === currentAccountIndex ? 'bg-blue-500' : 'bg-gray-300'
                    }`}
                  />
                ))}
              </div>
              
              <button
                onClick={nextAccount}
                disabled={accounts.length <= 1}
                className="p-3 rounded-full bg-white shadow-md hover:shadow-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <FaChevronRight className="text-gray-600 text-lg" />
              </button>
            </div>

            {/* 메인 계좌 카드 */}
            <div className="bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-800 rounded-3xl p-8 text-white shadow-2xl transform transition-all duration-300 hover:scale-[1.02]">
              {/* 카드 헤더 */}
              <div className="flex justify-between items-start mb-6">
                <div className="flex items-center gap-4">
                  <div className="bg-white/20 p-3 rounded-full">
                    <BsBank2 className="text-2xl" />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold">{currentAccount.bankName || '은행명 없음'}</h3>
                    <p className="text-blue-200 text-sm">{currentAccount.accountType || '일반계좌'}</p>
                  </div>
                </div>
                <button
                  onClick={() => toggleBalanceVisibility(currentAccount.id)}
                  className="text-white/80 hover:text-white transition p-2"
                >
                  {visibleBalances[currentAccount.id] ? <FaEyeSlash size={20} /> : <FaEye size={20} />}
                </button>
              </div>

              {/* 계좌번호 */}
              <p className="text-blue-200 mb-4 font-mono tracking-wider">
                {currentAccount.accountNumber}
              </p>

              {/* 잔액 */}
              <div className="mb-6">
                <p className="text-blue-200 text-sm mb-1">잔액</p>
                <p className="text-4xl font-bold">
                  {visibleBalances[currentAccount.id] 
                    ? `${(Number(currentAccount.balance) || 0).toLocaleString()} 원`
                    : '••••••• 원'
                  }
                </p>
              </div>

              {/* 빠른 액션 버튼 */}
              <div className="flex gap-3">
                <button
                  onClick={() => navigate(`/transfer?from=${currentAccount.accountNumber}`)}
                  className="flex-1 bg-white/20 backdrop-blur-sm px-4 py-3 rounded-xl text-sm font-medium hover:bg-white/30 transition"
                >
                  이체하기
                </button>
                <button
                  onClick={() => navigate(`/account-details/${currentAccount.id}`)}
                  className="flex-1 bg-white/20 backdrop-blur-sm px-4 py-3 rounded-xl text-sm font-medium hover:bg-white/30 transition"
                >
                  상세보기
                </button>
                <button className="flex-1 bg-white/20 backdrop-blur-sm px-4 py-3 rounded-xl text-sm font-medium hover:bg-white/30 transition">
                  QR코드
                </button>
              </div>
            </div>
          </div>

          {/* 수입/지출 요약 */}
          <div className="px-6">
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center gap-3">
                  <div className="bg-green-100 p-3 rounded-full">
                    <FaArrowDown className="text-green-600" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">이번달 수입</p>
                    <p className="text-2xl font-bold text-green-600">+{getSummary().income.toLocaleString()}원</p>
                  </div>
                </div>
              </div>
              
              <div className="bg-white rounded-2xl p-6 shadow-lg">
                <div className="flex items-center gap-3">
                  <div className="bg-red-100 p-3 rounded-full">
                    <FaArrowUp className="text-red-600" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">이번달 지출</p>
                    <p className="text-2xl font-bold text-red-600">-{getSummary().expense.toLocaleString()}원</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* 잔액 변화 그래프 */}
          <div className="px-6">
            <div className="bg-white rounded-2xl p-6 shadow-lg">
              <h4 className="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
                📈 잔액 변화 추이
              </h4>
              <div className="h-64">
                {getChartData().length > 0 ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={getChartData()}>
                      <defs>
                        <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.3}/>
                          <stop offset="95%" stopColor="#3B82F6" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <XAxis 
                        dataKey="date" 
                        axisLine={false}
                        tickLine={false}
                        tick={{ fontSize: 12, fill: '#6B7280' }}
                      />
                      <YAxis 
                        axisLine={false}
                        tickLine={false}
                        tick={{ fontSize: 12, fill: '#6B7280' }}
                        tickFormatter={(value) => `${(value / 10000).toFixed(0)}만`}
                      />
                      <Area
                        type="monotone"
                        dataKey="balance"
                        stroke="#3B82F6"
                        strokeWidth={3}
                        fill="url(#colorBalance)"
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="flex items-center justify-center h-full text-gray-500">
                    <div className="text-center">
                      <p className="text-lg mb-2">📊 그래프 데이터 없음</p>
                      <p className="text-sm">거래 내역이 충분하지 않습니다</p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* 최근 거래내역 */}
          <div className="px-6">
            <div className="bg-white rounded-2xl p-6 shadow-lg">
              <h4 className="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
                💳 최근 거래내역
              </h4>
              <div className="space-y-3">
                {transactions[currentAccount.id]?.length > 0 ? (
                  transactions[currentAccount.id].slice(0, 5).map((transaction, index) => (
                    <div key={transaction.id || index} className="flex justify-between items-center p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition">
                      <div className="flex items-center gap-4">
                        <div className={`p-2 rounded-full ${
                          transaction.type === '입금' || transaction.type === 'DEPOSIT'
                            ? 'bg-green-100 text-green-600' 
                            : 'bg-red-100 text-red-600'
                        }`}>
                          {(transaction.type === '입금' || transaction.type === 'DEPOSIT') ? 
                            <FaArrowDown size={16} /> : <FaArrowUp size={16} />}
                        </div>
                        <div>
                          <p className="font-medium text-gray-800">{transaction.description}</p>
                          <p className="text-sm text-gray-500">
                            {new Date(transaction.transactionDate).toLocaleDateString('ko-KR', {
                              month: 'long',
                              day: 'numeric',
                              hour: '2-digit',
                              minute: '2-digit'
                            })}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className={`text-lg font-bold ${
                          (transaction.type === '입금' || transaction.type === 'DEPOSIT') ? 'text-green-600' : 'text-red-600'
                        }`}>
                          {(transaction.type === '입금' || transaction.type === 'DEPOSIT') ? '+' : '-'}{transaction.amount?.toLocaleString()}원
                        </p>
                        <p className="text-sm text-gray-400">
                          잔액: {transaction.balanceAfter?.toLocaleString()}원
                        </p>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8">
                    <p className="text-lg text-gray-500 mb-2">💳 거래 내역을 불러올 수 없습니다</p>
                    <p className="text-sm text-gray-400 mb-4">계좌 권한을 확인하거나 거래를 시작해보세요</p>
                    <button
                      onClick={() => navigate(`/transfer?from=${currentAccount.accountNumber}`)}
                      className="text-sm text-blue-600 underline hover:text-blue-800"
                    >
                      첫 거래 시작하기 →
                    </button>
                  </div>
                )}
              </div>
              
              {transactions[currentAccount.id]?.length > 5 && (
                <button 
                  onClick={() => navigate(`/account-details/${currentAccount.id}`)}
                  className="w-full mt-4 py-3 text-blue-600 font-medium hover:text-blue-800 transition"
                >
                  전체 거래내역 보기 →
                </button>
              )}
            </div>
          </div>

          {/* 하단 액션 버튼들 */}
          <div className="px-6 pb-8">
            <div className="grid grid-cols-3 gap-4">
              <button
                onClick={() => navigate('/scheduled-transfer/new')}
                className="bg-gradient-to-r from-purple-500 to-purple-600 text-white p-4 rounded-xl hover:from-purple-600 hover:to-purple-700 transition shadow-lg flex flex-col items-center gap-2"
              >
                <span className="text-2xl">📅</span>
                <span className="text-sm font-medium">예약이체</span>
              </button>
              <button
                onClick={() => navigate('/scheduled-transfers')}
                className="bg-gradient-to-r from-green-500 to-green-600 text-white p-4 rounded-xl hover:from-green-600 hover:to-green-700 transition shadow-lg flex flex-col items-center gap-2"
              >
                <span className="text-2xl">📋</span>
                <span className="text-sm font-medium">이체목록</span>
              </button>
              <button
                onClick={() => navigate('/create-account')}
                className="bg-gradient-to-r from-blue-500 to-blue-600 text-white p-4 rounded-xl hover:from-blue-600 hover:to-blue-700 transition shadow-lg flex flex-col items-center gap-2"
              >
                <span className="text-2xl">➕</span>
                <span className="text-sm font-medium">계좌개설</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;