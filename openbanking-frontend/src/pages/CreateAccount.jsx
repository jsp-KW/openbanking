// src/pages/CreateAccount.jsx
import { useEffect, useState } from "react";
import axios from "../api/axiosInstance";
import { useNavigate } from "react-router-dom";
import { BsBank2 } from "react-icons/bs";
import { FaFolderOpen } from "react-icons/fa6";

/**
 * CreateAccount — Sky Balanced Theme
 * - 산뜻한 파스텔 배경, 반투명 카드, 부드러운 인터랙션
 * - UX: 로딩/에러 처리, 선택박스 접근성
 */
export default function CreateAccount() {
  const [bankId, setBankId] = useState("");
  const [accountType, setAccountType] = useState("입출금");
  const [banks, setBanks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errMsg, setErrMsg] = useState("");
  const [okMsg, setOkMsg] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        const res = await axios.get("/banks");
        const data = Array.isArray(res.data) ? res.data : [];
        setBanks(data);
        if (data.length > 0) setBankId(data[0].id);
      } catch (err) {
        console.error("은행 목록 불러오기 실패:", err);
        setErrMsg("은행 정보를 불러올 수 없습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!bankId || !accountType) return;
    setErrMsg("");
    setOkMsg("");
    try {
      await axios.post("/accounts", {
        bankId: parseInt(bankId),
        accountType,
        balance: 0,
        password,
      });
      setOkMsg("✅ 계좌가 성공적으로 개설되었습니다!");
      setTimeout(() => navigate("/dashboard"), 1000);
    } catch (err) {
      setErrMsg("❌ 계좌 개설에 실패했습니다.");
      console.error(err);
    }
  };

  return (
    <div className="relative min-h-screen flex justify-center items-center px-4 bg-gradient-to-br from-sky-100 via-indigo-100 to-slate-100">
      {/* Soft blobs */}
      <div className="pointer-events-none absolute inset-0 opacity-40">
        <div className="absolute top-[-60px] left-[-40px] h-64 w-64 rounded-full bg-white/50 blur-3xl" />
        <div className="absolute bottom-[-80px] right-[-40px] h-72 w-72 rounded-full bg-white/40 blur-3xl" />
      </div>

      <form
        onSubmit={handleCreate}
        className="relative w-[min(480px,92vw)] rounded-2xl border border-slate-200 bg-white/90 backdrop-blur-md shadow-xl p-8"
      >
        <h2 className="text-2xl font-extrabold text-slate-800 text-center mb-2">
          🏦 새 계좌 개설
        </h2>
        <p className="text-sm text-slate-600 text-center mb-6">
          원하시는 은행과 계좌 유형을 선택하세요
        </p>

        {/* 은행 선택 */}
        <label className="block text-sm font-semibold text-slate-800 mb-2" htmlFor="bank">
          📌 은행 선택
        </label>
        <div className="relative mb-5">
          <BsBank2 className="absolute left-3 top-2.5 text-slate-500" />
          <select
            id="bank"
            value={bankId}
            onChange={(e) => setBankId(e.target.value)}
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white border border-slate-200 text-slate-900
                       focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
            required
          >
            {banks.map((bank) => (
              <option key={bank.id} value={bank.id}>
                {bank.bankName}
              </option>
            ))}
          </select>
        </div>

        {/* 계좌 유형 */}
        <label className="block text-sm font-semibold text-slate-800 mb-2" htmlFor="accountType">
          📂 계좌 유형
        </label>
        <div className="relative mb-6">
          <FaFolderOpen className="absolute left-3 top-2.5 text-slate-500" />
          <select
            id="accountType"
            value={accountType}
            onChange={(e) => setAccountType(e.target.value)}
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white border border-slate-200 text-slate-900
                       focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
            required
          >
            <option value="입출금">입출금</option>
            <option value="예금">예금</option>
          </select>
        </div>

           {/* 계좌 비밀번호 */}
      <label
        className="block text-sm font-semibold text-slate-800 mb-2"
        htmlFor="password"
        >
          🔐 계좌 비밀번호 (4자리)
        </label>
        <input
          type="password"
          id="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          maxLength={4}
          className="w-full px-3 py-3 mb-6 rounded-lg border border-slate-200 text-slate-900
                    placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
          placeholder="숫자 4자리 입력"
          required
        />

        {/* 버튼 ... */}

        {/* 에러/성공 메시지 */}
        {errMsg && (
          <div className="mb-4 rounded-lg border border-rose-200 bg-rose-50 text-rose-700 px-3 py-2 text-sm">
            {errMsg}
          </div>
        )}
        {okMsg && (
          <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 text-emerald-700 px-3 py-2 text-sm">
            {okMsg}
          </div>
        )}

        {/* 버튼 */}
        <button
          type="submit"
          disabled={loading || !bankId}
          className={`w-full py-3 rounded-lg font-semibold transition
            ${
              loading || !bankId
                ? "bg-slate-200 text-slate-500 cursor-not-allowed"
                : "bg-gradient-to-r from-indigo-600 to-sky-600 text-white hover:brightness-110 shadow"
            }`}
        >
          {loading ? "불러오는 중..." : "🚀 계좌 개설하기"}
        </button>
      </form>
    </div>
  );
}
