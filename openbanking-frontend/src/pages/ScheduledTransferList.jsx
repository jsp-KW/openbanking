// src/pages/ScheduledTransferList.jsx
import { useEffect, useState } from "react";
import axios from "../api/axiosInstance";
import { useNavigate } from "react-router-dom";
import { FaClock, FaCheckCircle, FaTimesCircle } from "react-icons/fa";

export default function ScheduledTransferList() {
  const [scheduledTransfers, setScheduledTransfers] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    axios
      .get("/scheduled-transfers/my")
      .then((res) => setScheduledTransfers(res.data))
      .catch((err) => {
        console.error("예약이체 내역 조회 실패:", err);
        alert("예약이체 목록을 불러오는 데 실패했습니다.");
        navigate("/dashboard");
      });
  }, []);

  return (
    <div className="relative min-h-screen flex items-center justify-center px-4 bg-gradient-to-br from-sky-100 via-indigo-100 to-slate-100">
      {/* soft blobs */}
      <div className="absolute inset-0 pointer-events-none opacity-40">
        <div className="absolute -top-16 -left-20 h-72 w-72 bg-white/50 rounded-full blur-3xl" />
        <div className="absolute bottom-[-80px] right-[-60px] h-80 w-80 bg-white/40 rounded-full blur-3xl" />
      </div>

      <div className="relative w-[min(800px,95vw)] bg-white/90 backdrop-blur-md border border-slate-200 rounded-2xl shadow-xl p-8">
        <h2 className="text-2xl font-extrabold text-slate-800 text-center mb-6">
          📋 예약이체 내역
        </h2>

        {scheduledTransfers.length === 0 ? (
          <p className="text-center text-slate-600">
            아직 등록된 예약이체가 없습니다.
          </p>
        ) : (
          <div className="space-y-4">
            {scheduledTransfers.map((item) => (
              <div
                key={item.id}
                className="p-5 bg-white/80 border border-slate-200 rounded-xl shadow hover:shadow-lg transition"
              >
                <div className="flex justify-between items-center">
                  <div>
                    <p className="font-semibold text-slate-800">
                      {item.fromAccountNumber} → {item.toAccountNumber}
                    </p>
                    <p className="text-sm text-slate-500">
                      {new Date(item.scheduledAt).toLocaleString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold text-sky-700">
                      {item.amount.toLocaleString()} 원
                    </p>
                    {item.status === "완료" ? (
                      <span className="flex items-center gap-1 text-green-600 text-sm font-medium">
                        <FaCheckCircle /> 완료
                      </span>
                    ) : item.status === "실패" ? (
                      <span className="flex items-center gap-1 text-red-600 text-sm font-medium">
                        <FaTimesCircle /> 실패
                      </span>
                    ) : (
                      <span className="flex items-center gap-1 text-yellow-600 text-sm font-medium">
                        <FaClock /> 대기중
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 대시보드로 돌아가기 버튼 */}
        <button
          onClick={() => navigate("/dashboard")}
          className="w-full mt-6 py-3 rounded-lg border border-slate-300 text-slate-700 font-medium hover:bg-slate-100 transition"
        >
          ← 대시보드로 돌아가기
        </button>
      </div>
    </div>
  );
}
