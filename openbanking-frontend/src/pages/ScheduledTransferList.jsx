import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';
import { useNavigate } from 'react-router-dom';

export default function ScheduledTransferList() {
  const [scheduledTransfers, setScheduledTransfers] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    axios.get('/scheduled-transfers/my')
      .then((res) => setScheduledTransfers(res.data))
      .catch((err) => {
        console.error('예약이체 내역 조회 실패:', err);
        alert('예약이체 목록을 불러오는 데 실패했습니다.');
        navigate('/dashboard');
      });
  }, []);

  return (
    <div className="min-h-screen bg-yellow-50 p-6">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">📋 예약이체 목록</h2>

      {scheduledTransfers.length === 0 ? (
        <p className="text-gray-600">등록된 예약이체가 없습니다.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white shadow-md rounded-xl overflow-hidden">
            <thead className="bg-yellow-200 text-gray-700">
              <tr>
                <th className="px-4 py-2 text-left">출금 계좌</th>
                <th className="px-4 py-2 text-left">입금 계좌</th>
                <th className="px-4 py-2 text-left">금액</th>
                <th className="px-4 py-2 text-left">예약 시간</th>
                <th className="px-4 py-2 text-left">상태</th>
              </tr>
            </thead>
            <tbody>
              {scheduledTransfers.map((item) => (
                <tr key={item.id} className="border-t hover:bg-yellow-50">
                  <td className="px-4 py-2">{item.fromAccountNumber}</td>
                  <td className="px-4 py-2">{item.toAccountNumber}</td>
                  <td className="px-4 py-2">{item.amount.toLocaleString()} 원</td>
                  <td className="px-4 py-2">{new Date(item.scheduledAt).toLocaleString()}</td>
                  <td className="px-4 py-2">
                  {item.executed ? (
                    <span className="text-green-600 font-bold">✅ 완료</span>
                  ) : (
                    <span className="text-yellow-600 font-semibold">⌛ 대기중</span>
                  )}  
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {/* 대시보드로 돌아가기 버튼 */}
          <div className="mt-6">
            <button
              onClick={() => navigate('/dashboard')}
              className="w-full text-yellow-700 border border-yellow-400 py-2 rounded-lg font-semibold hover:bg-yellow-100 transition"
            >
              ← 대시보드로 돌아가기
            </button>
          </div>

        </div>

        
      )}
    </div>
  );
}
