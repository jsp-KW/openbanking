import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';

export default function Transaction() {
  const [transactions, setTransactions] = useState([]);
  const [accountNumber, setAccountNumber] = useState('');
  const [accountOptions, setAccountOptions] = useState([]);

  // 1. 사용자의 계좌 목록을 먼저 불러오기
  useEffect(() => {
    axios.get('/accounts/my')
      .then((res) => {
        setAccountOptions(res.data);
        if (res.data.length > 0) {
          setAccountNumber(res.data[0].accountNumber);
        }
      })
      .catch((err) => {
        console.error('계좌 불러오기 실패:', err);
        alert('계좌 정보를 불러올 수 없습니다.');
      });
  }, []);

  // 2. 선택된 계좌의 거래 내역 조회
  useEffect(() => {
    if (!accountNumber) return;
    axios.get(`/transactions/account/${accountNumber}`)
      .then((res) => {
        setTransactions(res.data);
      })
      .catch((err) => {
        console.error('거래 내역 불러오기 실패:', err);
        alert('거래 내역을 불러올 수 없습니다.');
      });
  }, [accountNumber]);

  return (
    <div className="min-h-screen bg-yellow-50 p-6">
      <h2 className="text-2xl font-bold mb-6 text-center">📊 거래 내역 조회</h2>

      <div className="mb-6">
        <label className="block mb-2 text-sm font-medium text-gray-700">계좌 선택</label>
        <select
          value={accountNumber}
          onChange={(e) => setAccountNumber(e.target.value)}
          className="w-full border border-gray-300 rounded p-2"
        >
          {accountOptions.map((acc) => (
            <option key={acc.id} value={acc.accountNumber}>
              {acc.accountNumber} ({acc.bankName})
            </option>
          ))}
        </select>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full table-auto border-collapse bg-white shadow-md">
          <thead className="bg-yellow-200">
            <tr>
              <th className="border px-4 py-2">📅 날짜</th>
              <th className="border px-4 py-2">유형</th>
              <th className="border px-4 py-2">금액</th>
              <th className="border px-4 py-2">상대 계좌</th>
              <th className="border px-4 py-2">메모</th>
            </tr>
          </thead>
          <tbody>
            {transactions.length > 0 ? (
              transactions.map((tx) => (
                <tr key={tx.id} className="text-center">
                  <td className="border px-4 py-2">{tx.timestamp?.slice(0, 10)}</td>
                  <td className="border px-4 py-2">{tx.type}</td>
                  <td className="border px-4 py-2">{tx.amount.toLocaleString()}원</td>
                  <td className="border px-4 py-2">{tx.targetAccountNumber}</td>
                  <td className="border px-4 py-2">{tx.memo}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="text-center py-4 text-gray-500">거래 내역이 없습니다.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
