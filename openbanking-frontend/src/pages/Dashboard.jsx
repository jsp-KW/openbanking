import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';
import { BsBank2 } from 'react-icons/bs'; // 아이콘 (optional)

function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    axios.get('/accounts/my')
      .then((res) => {
        setAccounts(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('계좌 조회 실패:', err);
        setError('계좌 정보를 불러오지 못했습니다.');
        setLoading(false);
      });
  }, []);

  if (loading) return <p className="text-center mt-10">불러오는 중...</p>;
  if (error) return <p className="text-center text-red-500">{error}</p>;

  return (
    <div className="min-h-screen bg-yellow-100 p-6">
      <h2 className="text-2xl font-bold mb-6 text-gray-800">내 계좌 목록</h2>

      {accounts.length === 0 ? (
        <p className="text-gray-600">등록된 계좌가 없습니다.</p>
      ) : (
        <div className="space-y-4">
          {accounts.map((acc) => (
            <div key={acc.id} className="bg-white rounded-xl shadow-md p-4 flex justify-between items-center">
              <div>
                <p className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                  <BsBank2 className="text-yellow-500" />
                  {acc.bankName || '카카오뱅크'}
                </p>
                <p className="text-sm text-gray-600">계좌번호: {acc.accountNumber}</p>
              </div>
              <div className="text-right">
                <p className="text-lg font-bold text-gray-900">
                  {acc.balance.toLocaleString()} 원
                </p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Dashboard;
