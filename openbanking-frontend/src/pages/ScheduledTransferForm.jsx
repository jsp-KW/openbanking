import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../api/axiosInstance';
import { makeFingerprint, getOrCreateKey, clearKey } from '../utils/idempotency';

function ScheduledTransferForm() {
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState([]);
  const [fromAccount, setFromAccount] = useState('');
  const [toAccount, setToAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [scheduledTime, setScheduledTime] = useState('');
  const [loading, setLoading] = useState(false);

  const userId = 'me'; // TODO: JWT payload에서 추출하여 대체

  useEffect(() => {
    axios.get('/accounts/my')
      .then((res) => setAccounts(res.data))
      .catch(() => {
        alert('계좌 정보를 불러오지 못했습니다.');
        navigate('/dashboard');
      });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!fromAccount || !toAccount || !amount || !scheduledTime) {
      alert('모든 항목을 입력해주세요.');
      return;
    }

    if (fromAccount === toAccount) {
      alert('출금 계좌와 입금 계좌가 같을 수 없습니다.');
      return;
    }

    const payload = {
      fromAccountNumber: fromAccount,
      toAccountNumber: toAccount,
      amount: Number(amount),
      scheduledAt: scheduledTime,
    };

    // 멱등키 생성
    const fingerprint = makeFingerprint(payload);
    const { key: idempotencyKey, storageKey } = getOrCreateKey(userId, fingerprint);

    setLoading(true);
    try {
      await axios.post('/scheduled-transfers', payload, {
        headers: {
          'Idempotency-Key': idempotencyKey,
        },
      });

      alert('예약이체가 등록되었습니다.');
      clearKey(storageKey);
      navigate('/dashboard');
    } catch (err) {
      console.error('예약이체 등록 실패:', err);
      const status = err?.response?.status;

      if (status && [400, 401, 403, 404, 409, 422].includes(status)) {
        clearKey(storageKey);
      }

      alert(err?.response?.data?.message || '예약이체 등록 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-yellow-50 p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">📅 예약이체 등록</h2>

      <form onSubmit={handleSubmit} className="space-y-5 bg-white p-6 rounded-xl shadow-md max-w-lg mx-auto">
        {/* 출금 계좌 선택 */}
        <div>
          <label className="block text-gray-700 font-medium">출금 계좌</label>
          <select
            value={fromAccount}
            onChange={(e) => setFromAccount(e.target.value)}
            className="w-full mt-1 border rounded p-2"
            required
          >
            <option value="">선택하세요</option>
            {accounts.map((acc) => (
              <option key={acc.id} value={acc.accountNumber}>
                {acc.bankName} - {acc.accountNumber}
              </option>
            ))}
          </select>
        </div>

        {/* 입금 계좌 */}
        <div>
          <label className="block text-gray-700 font-medium">입금 계좌번호</label>
          <input
            type="text"
            value={toAccount}
            onChange={(e) => setToAccount(e.target.value)}
            className="w-full mt-1 border rounded p-2"
            placeholder="입금 받을 계좌번호"
            required
          />
        </div>

        {/* 금액 */}
        <div>
          <label className="block text-gray-700 font-medium">이체 금액 (원)</label>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="w-full mt-1 border rounded p-2"
            min="1"
            required
          />
        </div>

        {/* 예약 시간 */}
        <div>
          <label className="block text-gray-700 font-medium">예약 시간</label>
          <input
            type="datetime-local"
            value={scheduledTime}
            onChange={(e) => setScheduledTime(e.target.value)}
            className="w-full mt-1 border rounded p-2"
            required
          />
        </div>

        {/* 버튼 */}
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-yellow-500 text-white py-2 px-4 rounded hover:bg-yellow-600 transition"
        >
          {loading ? '등록 중...' : '예약이체 등록하기'}
        </button>
      </form>
    </div>
  );
}

export default ScheduledTransferForm;
