import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';
import { useNavigate } from 'react-router-dom';

export default function CreateAccount() {
  const [bankId, setBankId] = useState('');
  const [accountType, setAccountType] = useState('입출금');
  const [banks, setBanks] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    axios.get('/banks')
      .then((res) => {
        setBanks(res.data);
        if (res.data.length > 0) setBankId(res.data[0].id);
      })
      .catch((err) => {
        console.error('은행 목록 불러오기 실패:', err);
        alert('은행 정보를 불러올 수 없습니다.');
      });
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await axios.post('/accounts', {
        bankId: parseInt(bankId),
        accountType,
        balance : 0
      });
      alert('✅ 계좌가 성공적으로 개설되었습니다!');
      navigate('/dashboard');
    } catch (err) {
      alert('❌ 계좌 개설에 실패했습니다.');
      console.error(err);
    }
  };

  return (
    <div className="min-h-screen bg-yellow-100 flex justify-center items-center px-4">
      <form onSubmit={handleCreate} className="bg-white p-8 rounded-xl shadow-lg w-full max-w-md border border-yellow-300">
        <h2 className="text-2xl font-bold mb-6 text-gray-800 text-center">🏦 새 계좌 개설</h2>

        <div className="mb-4">
          <label className="block text-sm font-semibold text-gray-700 mb-1">📌 은행 선택</label>
          <select
            value={bankId}
            onChange={(e) => setBankId(e.target.value)}
            className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-yellow-400"
          >
            {banks.map((bank) => (
              <option key={bank.id} value={bank.id}>
                {bank.bankName}
              </option>
            ))}
          </select>
        </div>

        <div className="mb-6">
          <label className="block text-sm font-semibold text-gray-700 mb-1">📂 계좌 유형</label>
          <select
            value={accountType}
            onChange={(e) => setAccountType(e.target.value)}
            className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-yellow-400"
          >
            <option value="입출금">입출금</option>
            <option value="예금">예금</option>
          </select>
        </div>

        <button
          type="submit"
          className="w-full bg-yellow-500 hover:bg-yellow-600 text-white py-2 px-4 rounded transition duration-300"
        >
          🚀 계좌 개설하기
        </button>
      </form>
    </div>
  );
}
