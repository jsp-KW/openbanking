// pages/Transfer.jsx
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from '../api/axiosInstance';
import { makeFingerprint, getOrCreateKey, clearKey } from '../utils/idempotency';

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

  const [submitting, setSubmitting] = useState(false);

  // TODO: JWT payload에서 userId 가져오기
  const userId = 'me'; // 임시 값

  // 계좌 및 은행 목록 불러오기
  useEffect(() => {
    axios.get('/accounts/my')
      .then((res) => {
        // balance null → 0으로 정규화
        const normalized = res.data.map(a => ({ ...a, balance: a.balance ?? 0 }));
        setAccounts(normalized);

        const initialFrom = params.get('from');
        if (initialFrom) {
          setFromAccount(initialFrom);
          const matched = normalized.find(acc => acc.accountNumber === initialFrom);
          if (matched) setFromBankId(matched.bankId);
        }
      })
      .catch(() => {
        alert('계좌 목록 조회 실패');
        navigate('/dashboard');
      });

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
    if (submitting) return;

    const parsedAmount = Number(amount);

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

    const payload = {
      fromAccountNumber: fromAccount,
      toAccountNumber: toAccount,
      amount: parsedAmount,
      fromBankId: Number(fromBankId),
      toBankId: Number(toBankId),
    };

    // fingerprint 기반 멱등키 생성/재사용
    const fp = makeFingerprint(payload);
    const { key: idemKey, storageKey } = getOrCreateKey(userId, fp);

    setSubmitting(true);
    try {
      await axios.post('/accounts/transfer', payload, {
        headers: {
          'Idempotency-Key': idemKey,
          'Content-Type': 'application/json',
        },
      });

      alert('이체 성공!');
      clearKey(storageKey);
      navigate('/transactions');
    } catch (err) {
      console.error('이체 실패:', err);
      const status = err?.response?.status;
      const message = err?.response?.data?.message;

      // 확정 실패 시 키 삭제
      if (status && [400, 401, 403, 404, 409, 422].includes(status)) {
        clearKey(storageKey);
      }
      alert(message || '이체 실패');
    
    } finally {
      setSubmitting(false);
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
                  {acc.bankName} - {acc.accountNumber} (잔액: {(acc.balance ?? 0).toLocaleString()}원)
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
            disabled={submitting}
            className={`w-full text-white py-2 rounded-lg font-semibold transition-colors duration-300
              ${submitting ? 'bg-gray-400 cursor-not-allowed' : 'bg-yellow-500 hover:bg-yellow-600'}`}
          >
            {submitting ? '전송 중...' : '이체하기'}
          </button>
        </form>

        {/* 대시보드로 돌아가기 버튼 */}
        <button
          type="button"
          onClick={() => navigate('/dashboard')}
          className="w-full mt-2 text-yellow-700 border border-yellow-400 py-2 rounded-lg font-semibold hover:bg-yellow-100 transition"
        >
          마이계좌 홈으로 돌아가기
        </button>
      </div>
    </div>
  );
}
