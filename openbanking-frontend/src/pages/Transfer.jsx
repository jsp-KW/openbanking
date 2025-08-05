// pages/Transfer.jsx
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from '../api/axiosInstance';

export default function Transfer() {
  const navigate = useNavigate();
  const location = useLocation();
  const params = new URLSearchParams(location.search);

  const [accounts, setAccounts] = useState([]);
  const [banks, setBanks] = useState([]);

  const [fromAccount, setFromAccount] = useState('');
  const [fromBankId, setFromBankId] = useState(null);

  const [toAccount, setToAccount] = useState('');
  const [toBankId, setToBankId] = useState('');
  const [amount, setAmount] = useState('');

  // 계좌 및 은행 목록 불러오기
  useEffect(() => {
    // 🔹 계좌 목록
    axios.get('/accounts/my')
      .then((res) => {
        console.log('📄 내 계좌 목록:', res.data);
        setAccounts(res.data);
        const initialFrom = params.get('from');
        if (initialFrom) {
          setFromAccount(initialFrom);
          const matched = res.data.find(acc => acc.accountNumber === initialFrom);
          if (matched) setFromBankId(matched.bankId);
        }
      })
      .catch(() => {
        alert('계좌 목록 조회 실패');
        navigate('/dashboard');
      });

    // 🔹 은행 목록
    axios.get('/banks')
      .then((res) => setBanks(res.data))
      .catch(() => {
        alert('은행 목록 조회 실패');
      });
  }, []);

  // 출금 계좌 선택 시 은행 ID 설정
  const handleFromAccountChange = (value) => {
    setFromAccount(value);
    const matched = accounts.find(acc => acc.accountNumber === value);
    if (matched) setFromBankId(matched.bankId);
  };

  // 이체 처리
const handleTransfer = async (e) => {
  e.preventDefault();

  const parsedAmount = parseFloat(amount);

  if (
    !fromAccount ||
    !toAccount ||
    Number.isNaN(parsedAmount) ||
    parsedAmount <= 0 ||
    fromBankId === null ||
    toBankId === ''
  ) {
    alert('모든 항목을 올바르게 입력해주세요.');
    return;
  }
    console.log('💬 이체 요청', {
    fromAccountNumber: fromAccount,
    toAccountNumber: toAccount,
    amount: parsedAmount,
    fromBankId,
    toBankId,
  });
  try {
    await axios.post('/accounts/transfer', {
      fromAccountNumber: fromAccount,
      toAccountNumber: toAccount,
      amount: parsedAmount,
      fromBankId: parseInt(fromBankId),
      toBankId: parseInt(toBankId),
    });

    alert('이체 성공!');
    navigate('/transactions');
  } catch (err) {
    console.error('이체 실패:', err);
    alert(err.response?.data?.message || '이체 실패');
  }
};

  return (
    <div className="min-h-screen bg-yellow-50 py-12 px-6">
      <div className="max-w-md mx-auto bg-white p-8 rounded-2xl shadow-lg">
        <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">💸 이체하기</h2>
        <form onSubmit={handleTransfer} className="space-y-5">

          {/* 출금 계좌 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-gray-600">출금 계좌</label>
            <select
              value={fromAccount}
              onChange={(e) => handleFromAccountChange(e.target.value)}
              className="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-yellow-500"
              required
            >
              <option value="">출금 계좌 선택</option>
              {accounts.map((acc) => (
                <option key={acc.id} value={acc.accountNumber}>
                  {acc.bankName} - {acc.accountNumber} (잔액: {acc.balance.toLocaleString()}원)
                </option>
              ))}
            </select>
          </div>

          {/* 입금 은행 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-gray-600">입금 은행</label>
            <select
              value={toBankId}
              onChange={(e) => setToBankId(e.target.value)}
              className="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-yellow-500"
              required
            >
              <option value="">입금 은행 선택</option>
              {banks.map((bank) => (
                <option key={bank.id} value={bank.id}>
                  {bank.bankName}
                </option>
              ))}
            </select>
          </div>

          {/* 입금 계좌번호 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-gray-600">입금 계좌번호</label>
            <input
              type="text"
              value={toAccount}
              onChange={(e) => setToAccount(e.target.value)}
              className="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-yellow-500"
              placeholder="상대방 계좌번호"
              required
            />
          </div>

          {/* 이체 금액 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-gray-600">이체 금액 (원)</label>
            <input
              type="number"
              min={1}
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-yellow-500"
              placeholder="예: 10000"
              required
            />
          </div>

          {/* 전송 버튼 */}
          <button
            type="submit"
            className="w-full bg-yellow-500 hover:bg-yellow-600 text-white py-2 rounded-lg font-semibold transition-colors duration-300"
          >
            이체하기
          </button>
        </form>
      </div>
    </div>
  );
}
