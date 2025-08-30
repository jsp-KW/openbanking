import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "../api/axiosInstance";
import {
  makeFingerprint,
  getOrCreateKey,
  clearKey,
} from "../utils/idempotency";
import { FaCalendarCheck } from "react-icons/fa6";

/**
 * ScheduledTransferForm — 예약이체 등록
 * - Transfer.jsx 스타일 기반
 * - 출금 계좌 선택 + 입금 은행 선택 + 계좌번호 + 금액 + 비밀번호 + 예약 시간
 */
export default function ScheduledTransferForm() {
  const navigate = useNavigate();
  const location = useLocation();
  const params = new URLSearchParams(location.search);

  const [accounts, setAccounts] = useState([]);
  const [banks, setBanks] = useState([]);
  const [fromAccount, setFromAccount] = useState("");
  const [toBankId, setToBankId] = useState("");
  const [toAccount, setToAccount] = useState("");
  const [amount, setAmount] = useState("");
  const [scheduledTime, setScheduledTime] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState("");
  const [okMsg, setOkMsg] = useState("");

  const userId = "me"; // TODO: JWT payload에서 userId 가져오기

  // 내 계좌 / 은행 목록 불러오기
  useEffect(() => {
    axios
      .get("/accounts/my")
      .then((res) => setAccounts(res.data))
      .catch(() => {
        alert("계좌 목록 조회 실패");
        navigate("/dashboard");
      });

    axios
      .get("/banks")
      .then((res) => setBanks(res.data))
      .catch(() => {
        alert("은행 목록 조회 실패");
      });

    const initialFrom = params.get("from");
    if (initialFrom) setFromAccount(initialFrom);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    if (!fromAccount || !toBankId || !toAccount || !amount || !scheduledTime || !password) {
      setErrMsg("모든 항목을 입력해주세요.");
      return;
    }

    const payload = {
      fromAccountNumber: fromAccount,
      toAccountNumber: toAccount,
      toBankId: Number(toBankId),
      amount: Number(amount),
      scheduledAt: scheduledTime,
      password,
    };

    // 멱등키 생성
    const fp = makeFingerprint(payload);
    const { key: idemKey, storageKey } = getOrCreateKey(userId, fp);

    setLoading(true);
    setErrMsg("");
    setOkMsg("");

    try {
      await axios.post("/scheduled-transfers", payload, {
        headers: { "Idempotency-Key": idemKey },
      });
      setOkMsg("✅ 예약이체가 등록되었습니다.");
      clearKey(storageKey);
      setTimeout(() => navigate("/scheduled-transfers"), 1200);
    } catch (err) {
      console.error("예약이체 등록 실패:", err);
      const status = err?.response?.status;
      const message = err?.response?.data?.message;
      if (status && [400, 401, 403, 404, 409, 422].includes(status)) {
        clearKey(storageKey);
      }
      setErrMsg(message || "❌ 예약이체 등록 중 오류 발생");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen flex items-center justify-center px-4 bg-gradient-to-br from-sky-100 via-indigo-100 to-slate-100">
      {/* soft blobs */}
      <div className="absolute inset-0 pointer-events-none opacity-40">
        <div className="absolute -top-16 -left-20 h-72 w-72 bg-white/50 rounded-full blur-3xl" />
        <div className="absolute bottom-[-80px] right-[-60px] h-80 w-80 bg-white/40 rounded-full blur-3xl" />
      </div>

      <div className="relative w-[min(500px,95vw)] bg-white/90 backdrop-blur-md border border-slate-200 rounded-2xl shadow-xl p-8">
        <h2 className="text-2xl font-extrabold text-slate-800 text-center mb-2 flex items-center justify-center gap-2">
          <FaCalendarCheck className="text-sky-600" /> 예약이체 등록
        </h2>
        <p className="text-sm text-slate-600 text-center mb-6">
          원하는 시간에 자동으로 이체합니다
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* 출금 계좌 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-slate-800">
              출금 계좌
            </label>
            <select
              value={fromAccount}
              onChange={(e) => setFromAccount(e.target.value)}
              className="w-full px-3 py-3 rounded-lg border border-slate-200 bg-white text-slate-900
                         focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
              required
            >
              <option value="">출금 계좌 선택</option>
              {accounts.map((acc) => (
                <option key={acc.id} value={acc.accountNumber}>
                  {acc.bankName} - {acc.accountNumber} (잔액:{" "}
                  {(acc.balance ?? 0).toLocaleString()}원)
                </option>
              ))}
            </select>
          </div>

          {/* 입금 은행 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-slate-800">
              입금 은행
            </label>
            <select
              value={toBankId}
              onChange={(e) => setToBankId(e.target.value)}
              className="w-full px-3 py-3 rounded-lg border border-slate-200 bg-white text-slate-900
                         focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
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
            <label className="block mb-1 text-sm font-semibold text-slate-800">
              입금 계좌번호
            </label>
            <input
              type="text"
              value={toAccount}
              onChange={(e) => setToAccount(e.target.value)}
              className="w-full px-3 py-3 rounded-lg border border-slate-200 bg-white text-slate-900
                         placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
              placeholder="상대방 계좌번호"
              required
            />
          </div>

          {/* 이체 금액 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-slate-800">
              이체 금액 (원)
            </label>
            <input
              type="number"
              min={1}
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full px-3 py-3 rounded-lg border border-slate-200 bg-white text-slate-900
                         placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
              placeholder="예: 10000"
              required
            />
          </div>

          {/* 계좌 비밀번호 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-slate-800">
              계좌 비밀번호 (4자리)
            </label>
            <input
              type="password"
              maxLength={4}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-3 rounded-lg border border-slate-200 bg-white text-slate-900
                        placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
              placeholder="출금 계좌 비밀번호"
              required
            />
          </div>

          {/* 예약 시간 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-slate-800">
              예약 실행 시간
            </label>
            <input
              type="datetime-local"
              value={scheduledTime}
              onChange={(e) => setScheduledTime(e.target.value)}
              className="w-full px-3 py-3 rounded-lg border border-slate-200 bg-white text-slate-900
                        focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
              required
            />
          </div>

          {/* 에러/성공 메시지 */}
          {errMsg && (
            <div className="rounded-lg border border-rose-200 bg-rose-50 text-rose-700 px-3 py-2 text-sm">
              {errMsg}
            </div>
          )}
          {okMsg && (
            <div className="rounded-lg border border-emerald-200 bg-emerald-50 text-emerald-700 px-3 py-2 text-sm">
              {okMsg}
            </div>
          )}

          {/* 등록 버튼 */}
          <button
            type="submit"
            disabled={loading}
            className={`w-full py-3 rounded-lg font-semibold transition
              ${
                loading
                  ? "bg-slate-200 text-slate-500 cursor-not-allowed"
                  : "bg-gradient-to-r from-indigo-600 to-sky-600 text-white hover:brightness-110 shadow"
              }`}
          >
            {loading ? "등록 중..." : "📅 예약이체 등록"}
          </button>
        </form>

        {/* 대시보드로 돌아가기 */}
        <button
          type="button"
          onClick={() => navigate("/dashboard")}
          className="w-full mt-3 py-3 rounded-lg border border-slate-300 text-slate-700 font-medium hover:bg-slate-100 transition"
        >
          마이계좌 홈으로 돌아가기
        </button>
      </div>
    </div>
  );
}
