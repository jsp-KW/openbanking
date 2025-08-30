// src/pages/Transfer.jsx
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "../api/axiosInstance";
import {
  makeFingerprint,
  getOrCreateKey,
  clearKey,
} from "../utils/idempotency";
import { BsBank2 } from "react-icons/bs";
import { FaMoneyBillTransfer } from "react-icons/fa6";

/**
 * Transfer — Sky Balanced Theme
 * - 일관된 파스텔 블루 톤, 카드 글래스 디자인
 * - 멱등키 처리 + 로딩/에러/성공 UX 강화
 */
export default function Transfer() {
  const navigate = useNavigate();
  const location = useLocation();
  const params = new URLSearchParams(location.search);

  const [accounts, setAccounts] = useState([]);
  const [banks, setBanks] = useState([]);
  const [fromAccount, setFromAccount] = useState("");
  const [fromBankId, setFromBankId] = useState(null);
  const [toAccount, setToAccount] = useState("");
  const [toBankId, setToBankId] = useState("");
  const [amount, setAmount] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [errMsg, setErrMsg] = useState("");
  const [okMsg, setOkMsg] = useState("");
  const [password, setPassword] = useState("");

  const userId = "me"; // TODO: JWT payload에서 userId 가져오기

  useEffect(() => {
    axios
      .get("/accounts/my")
      .then((res) => {
        const normalized = res.data.map((a) => ({
          ...a,
          balance: a.balance ?? 0,
        }));
        setAccounts(normalized);

        const initialFrom = params.get("from");
        if (initialFrom) {
          setFromAccount(initialFrom);
          const matched = normalized.find(
            (acc) => acc.accountNumber === initialFrom
          );
          if (matched) setFromBankId(matched.bankId);
        }
      })
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
  }, []);

  const handleFromAccountChange = (value) => {
    setFromAccount(value);
    const matched = accounts.find((acc) => acc.accountNumber === value);
    if (matched) setFromBankId(matched.bankId);
  };

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
      toBankId === ""
    ) {
      setErrMsg("모든 항목을 올바르게 입력해주세요.");
      return;
    }

    const payload = {
      fromAccountNumber: fromAccount,
      toAccountNumber: toAccount,
      amount: parsedAmount,
      fromBankId: Number(fromBankId),
      toBankId: Number(toBankId),
      password,
    };

    const fp = makeFingerprint(payload);
    const { key: idemKey, storageKey } = getOrCreateKey(userId, fp);

    setSubmitting(true);
    setErrMsg("");
    setOkMsg("");
    try {
      await axios.post("/accounts/transfer", payload, {
        headers: {
          "Idempotency-Key": idemKey,
          "Content-Type": "application/json",
        },
      });

      setOkMsg("✅ 이체 성공!");
      clearKey(storageKey);
      setTimeout(() => navigate("/transactions"), 1200);
    } catch (err) {
      console.error("이체 실패:", err);
      const status = err?.response?.status;
      const message = err?.response?.data?.message;

      if (status && [400, 401, 403, 404, 409, 422].includes(status)) {
        clearKey(storageKey);
      }
      setErrMsg(message || "❌ 이체 실패");
    } finally {
      setSubmitting(false);
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
          <FaMoneyBillTransfer className="text-sky-600" /> 이체하기
        </h2>
        <p className="text-sm text-slate-600 text-center mb-6">
          안전하게 송금하세요
        </p>

        <form onSubmit={handleTransfer} className="space-y-5">
          {/* 출금 계좌 */}
          <div>
            <label className="block mb-1 text-sm font-semibold text-slate-800">
              출금 계좌
            </label>
            <select
              value={fromAccount}
              onChange={(e) => handleFromAccountChange(e.target.value)}
              className="w-full pl-3 pr-3 py-3 rounded-lg border border-slate-200 text-slate-900 bg-white
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
              className="w-full pl-3 pr-3 py-3 rounded-lg border border-slate-200 text-slate-900 bg-white
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
              className="w-full px-3 py-3 rounded-lg border border-slate-200 text-slate-900 bg-white
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
              className="w-full px-3 py-3 rounded-lg border border-slate-200 text-slate-900 bg-white
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
              className="w-full px-3 py-3 rounded-lg border border-slate-200 text-slate-900 bg-white
                        placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
              placeholder="계좌 비밀번호 입력"
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

          {/* 전송 버튼 */}
          <button
            type="submit"
            disabled={submitting}
            className={`w-full py-3 rounded-lg font-semibold transition
              ${
                submitting
                  ? "bg-slate-200 text-slate-500 cursor-not-allowed"
                  : "bg-gradient-to-r from-indigo-600 to-sky-600 text-white hover:brightness-110 shadow"
              }`}
          >
            {submitting ? "전송 중..." : "🚀 이체하기"}
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
