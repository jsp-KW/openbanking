import { useEffect, useState } from 'react';
import axios from '../api/axiosInstance';

function Dashboard() {
  const [accounts, setAccounts] = useState([]);

  useEffect(() => {
    axios.get('/accounts/my')
      .then((res) => {
        setAccounts(res.data);
      })
      .catch((err) => {
        console.error('계좌 조회 실패:', err);
      });
  }, []);

  return (
    <div>
      <h2>내 계좌 목록</h2>
      <ul>
        {accounts.map((acc) => (
          <li key={acc.id}>
            계좌번호: {acc.accountNumber} / 잔액: {acc.balance}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Dashboard;
