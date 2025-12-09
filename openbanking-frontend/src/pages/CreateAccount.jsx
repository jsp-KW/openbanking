// src/pages/CreateAccount.jsx
import { useEffect, useState } from "react";
import axios from "../api/axiosInstance";
import { useNavigate } from "react-router-dom";
import { BsBank2 } from "react-icons/bs";
import { FaFolderOpen } from "react-icons/fa6";

export default function CreateAccount() {
  const [bankId, setBankId] = useState("");
  const [accountType, setAccountType] = useState("");
  const [banks, setBanks] = useState([]);
  const [accountTypes, setAccountTypes] = useState([]);

  const [loading, setLoading] = useState(true);
  const [errMsg, setErrMsg] = useState("");
  const [okMsg, setOkMsg] = useState("");
  const [password, setPassword] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        const bankRes = await axios.get("/banks");
        const bankData = Array.isArray(bankRes.data) ? bankRes.data : [];
        setBanks(bankData);
        if (bankData.length > 0) setBankId(bankData[0].id);

        const typeRes = await axios.get("/account-types");
        setAccountTypes(typeRes.data);
        if (typeRes.data.length > 0) setAccountType(typeRes.data[0]);

      } catch (err) {
        console.error("초기 데이터 로딩 실패:", err);
        setErrMsg("초기 정보를 불러오는 중 문제가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
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
      console.error(err);
      setErrMsg("❌ 계좌 개설에 실패했습니다.");
    }
  };

  return (
    <div className="relative min-h-screen flex justify-center items-center px-4
                    bg-gradient-to-br from-sky-100 via-indigo-100 to-slate-100">

      {/* Soft floating blobs */}
      <div className="pointer-events-none absolute inset-0 opacity-40">
        <div className="absolute top-[-60px] left-[-40px] h-64 w-64 rounded-full bg-white/50 blur-3xl" />
        <div className="absolute bottom-[-80px] right-[-40px] h-72 w-72 rounded-full bg-white/40 blur-3xl" />
      </div>

      <form
        onSubmit={handleCreate}
        className="relative w-[min(480px,92vw)] rounded-2xl border border-slate-200
                   bg-white/90 backdrop-blur-md shadow-xl p-8"
      >
        <h2 className="text-2xl font-extrabold text-slate-800 text-center mb-2">
          🏦 새 계좌 개설
        </h2>

        <p className="text-sm text-slate-600 text-center mb-6">
          원하시는 은행과 계좌 유형을 선택하세요
        </p>

        {/* 은행 선택 */}
        <label className="block text-sm font-semibold text-slate-800 mb-2">
          📌 은행 선택
        </label>
        <div className="relative mb-5">
          <BsBank2 className="absolute left-3 top-2.5 text-slate-500" />
          <select
            value={bankId}
            onChange={(e) => setBankId(e.target.value)}
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white border border-slate-200
                       text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-300"
          >
            {banks.map((bank) => (
              <option key={bank.id} value={bank.id}>
                {bank.bankName}
              </option>
            ))}
          </select>
        </div>

        {/* 계좌 유형 선택 */}
        <label className="block text-sm font-semibold text-slate-800 mb-2">
          📂 계좌 유형
        </label>
        <div className="relative mb-6">
          <FaFolderOpen className="absolute left-3 top-2.5 text-slate-500" />
          <select
            value={accountType}
            onChange={(e) => setAccountType(e.target.value)}
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white border border-slate-200
                       text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-300"
          >
            {accountTypes.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>

        {/* 비밀번호 */}
        <label className="block text-sm font-semibold text-slate-800 mb-2">
          🔐 계좌 비밀번호 (4자리)
        </label>
        <input
          type="password"
          maxLength={4}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full px-3 py-3 mb-6 rounded-lg border border-slate-200 text-slate-900
                     focus:outline-none focus:ring-2 focus:ring-sky-300"
          placeholder="숫자 4자리 입력"
          required
        />

        {/* 메시지 */}
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
          disabled={loading}
          className={`w-full py-3 rounded-lg font-semibold transition 
            ${
              loading
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
