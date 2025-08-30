// src/pages/Dashboard.jsx
import { useEffect, useMemo, useState } from "react";
import axios from "../api/axiosInstance";
import { BsBank2 } from "react-icons/bs";
import {
  FaClock,         // 세션 남은 시간 아이콘 (fa6)
  FaArrowUp,
  FaArrowDown,
  FaEye,
  FaEyeSlash,
  FaChevronLeft,
  FaChevronRight,
  FaRightFromBracket, // 로그아웃 (fa6)
} from "react-icons/fa6";
import { useNavigate } from "react-router-dom";
import { parseJwt } from "../utils/jwt";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from "recharts";

/**
 * Dashboard — Fintech-Ready Redesign
 * - KPI 카드(수입/지출/순증감), 계좌 캐러셀, 접근성/에러/빈 상태 처리 강화
 * - KRW 포맷/Compact 축 라벨, Tooltip, Grid 등 가독성 강화
 * - useMemo로 파생데이터 계산 비용 관리
 */

// ---------- helpers ---------------------------------------------------------
const krwFmt = new Intl.NumberFormat("ko-KR", {
  style: "currency",
  currency: "KRW",
  maximumFractionDigits: 0,
});
const compactFmt = new Intl.NumberFormat("ko-KR", { notation: "compact" });

function fmtKRW(n) {
  const num = Number(n ?? 0);
  return krwFmt.format(num);
}

function fmtKRWCompact(n) {
  const num = Number(n ?? 0);
  return `${compactFmt.format(num)}원`;
}

function toShortDate(ts) {
  try {
    return new Date(ts).toLocaleDateString("ko-KR", {
      month: "short",
      day: "numeric",
    });
  } catch {
    return "-";
  }
}

// ---------- component ------------------------------------------------------
function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [txByAccount, setTxByAccount] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [remainingTime, setRemainingTime] = useState("");
  const [tokenPayload, setTokenPayload] = useState(null);
  const [visibleBalances, setVisibleBalances] = useState({});
  const [currentAccountIndex, setCurrentAccountIndex] = useState(0);
  const navigate = useNavigate();

  const currentAccount = accounts[currentAccountIndex];

  // ---------- auth controls -----------------------------------------------
  const handleLogout = async () => {
    try {
      const accessToken = localStorage.getItem("jwtToken");
      if (accessToken) {
        await axios.post(
          "/auth/logout",
          {},
          { headers: { Authorization: `Bearer ${accessToken}` } }
        );
      }
    } catch (err) {
      console.warn("서버 로그아웃 실패:", err.response?.data || err.message);
    } finally {
      localStorage.removeItem("jwtToken");
      localStorage.removeItem("refreshToken");
      navigate("/login");
    }
  };

  const handleSessionExtend = async () => {
    try {
      const refreshToken = localStorage.getItem("refreshToken");
      if (!refreshToken) throw new Error("refreshToken 없음");

      const res = await axios.post(
        "/auth/refresh",
        {},
        { headers: { Authorization: `Bearer ${refreshToken}` } }
      );

      const newAccessToken = res.data.accessToken;
      if (!newAccessToken || typeof newAccessToken !== "string")
        throw new Error("accessToken 형식 오류");

      localStorage.setItem("jwtToken", newAccessToken);
      const payload = parseJwt(newAccessToken);
      if (!payload?.exp) throw new Error("accessToken 파싱 실패");
      setTokenPayload(payload);
      console.info("세션 연장 성공");
    } catch (err) {
      console.error("세션 연장 실패:", err);
      handleLogout();
    }
  };

  // ---------- navigation between accounts ---------------------------------
  const nextAccount = () =>
    setCurrentAccountIndex((i) =>
      accounts.length ? (i + 1) % accounts.length : 0
    );
  const prevAccount = () =>
    setCurrentAccountIndex((i) =>
      accounts.length ? (i - 1 + accounts.length) % accounts.length : 0
    );

  const toggleBalanceVisibility = (accountId) =>
    setVisibleBalances((prev) => ({ ...prev, [accountId]: !prev[accountId] }));

  // ---------- derived selectors (memoized) --------------------------------
  const chartData = useMemo(() => {
    if (!currentAccount) return [];
    const list = txByAccount[currentAccount.id] || [];
    return [...list]
      .slice(0, 7)
      .reverse()
      .map((t) => ({
        date: toShortDate(t.transactionDate),
        balance: t.balanceAfter,
        amount:
          t.type === "DEPOSIT" || t.type === "입금" ? t.amount : -t.amount,
        type: t.type,
      }));
  }, [currentAccount, txByAccount]);

  const summary = useMemo(() => {
    if (!currentAccount) return { income: 0, expense: 0, net: 0 };
    const list = txByAccount[currentAccount.id] || [];
    const income = list
      .filter((t) => t.type === "DEPOSIT" || t.type === "입금")
      .reduce((s, t) => s + (t.amount || 0), 0);
    const expense = list
      .filter((t) => t.type === "WITHDRAWAL" || t.type === "출금")
      .reduce((s, t) => s + (t.amount || 0), 0);
    return { income, expense, net: income - expense };
  }, [currentAccount, txByAccount]);

  // ---------- effects ------------------------------------------------------
  useEffect(() => {
    const token = localStorage.getItem("jwtToken");
    if (!token || typeof token !== "string" || !token.includes("."))
      return handleLogout();
    const payload = parseJwt(token);
    if (!payload?.exp) return handleLogout();
    setTokenPayload(payload);
  }, []);

  useEffect(() => {
    if (!tokenPayload?.exp) return;
    const timer = setInterval(() => {
      const now = Math.floor(Date.now() / 1000);
      const left = tokenPayload.exp - now;
      if (left <= 0) {
        setRemainingTime("만료됨");
        clearInterval(timer);
        handleLogout();
        return;
      }
      const m = String(Math.floor(left / 60)).padStart(2, "0");
      const s = String(left % 60).padStart(2, "0");
      setRemainingTime(`${m}:${s}`);
    }, 1000);
    return () => clearInterval(timer);
  }, [tokenPayload]);

  const fetchTransactions = async (ids) => {
    const jobs = ids.map(async (id) => {
      try {
        const res = await axios.get(`/transactions/account/${id}`);
        const recent = Array.isArray(res.data) ? res.data.slice(0, 20) : [];
        return { id, list: recent };
      } catch (err) {
        console.warn(
          `계좌 ${id} 거래내역 실패`,
          err?.response?.status || err?.message
        );
        return { id, list: [] };
      }
    });
    const results = await Promise.all(jobs);
    setTxByAccount(
      results.reduce((acc, { id, list }) => ({ ...acc, [id]: list }), {})
    );
  };

  useEffect(() => {
    (async () => {
      try {
        const res = await axios.get("/accounts/my");
        const data = Array.isArray(res.data) ? res.data : [];
        setAccounts(data);
        setVisibleBalances(data.reduce((m, a) => ({ ...m, [a.id]: false }), {}));
        if (data.length) await fetchTransactions(data.map((a) => a.id));
        setLoading(false);
      } catch (err) {
        console.error("계좌 조회 실패:", err);
        if (err.response?.status === 403) handleLogout();
        else setError("계좌 정보를 불러오지 못했습니다.");
        setLoading(false);
      }
    })();
  }, []);

  // ---------- UI -----------------------------------------------------------
  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="animate-pulse space-y-4 w-[min(680px,90vw)]">
          <div className="h-10 bg-white/70 rounded-xl" />
          <div className="h-36 bg-white/70 rounded-2xl" />
          <div className="h-64 bg-white/70 rounded-2xl" />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Top Bar */}
      <header className="flex items-center justify-between px-6 py-4">
        <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight text-gray-800">
          내 금융 대시보드
        </h1>
        <div
          className="flex items-center gap-3 bg-white border border-blue-200 shadow-sm px-4 py-2 rounded-xl"
          aria-live="polite"
        >
          <FaClock className="text-blue-500" aria-hidden="true" />
          <span className="text-sm text-gray-700">
            남은 시간 <b className="text-blue-600">{remainingTime}</b>
          </span>
          <button
            onClick={handleSessionExtend}
            className="text-sm text-blue-600 hover:text-blue-800 underline"
          >
            연장
          </button>
          <button
            onClick={handleLogout}
            className="flex items-center gap-1 text-sm text-red-500 hover:text-red-600"
          >
            <FaRightFromBracket aria-hidden="true" /> 로그아웃
          </button>
        </div>
      </header>

      {/* Error / Empty States */}
      {error && (
        <div className="px-6">
          <div className="bg-white rounded-2xl p-8 shadow-lg text-center border border-red-100">
            <p className="text-red-600 mb-4">{error}</p>
            <button
              onClick={() => navigate("/create-account")}
              className="bg-red-500 hover:bg-red-600 text-white px-5 py-2 rounded-lg shadow"
            >
              계좌 개설하기
            </button>
          </div>
        </div>
      )}

      {!error && accounts.length === 0 && (
        <div className="px-6">
          <div className="bg-white rounded-2xl p-8 shadow-lg text-center">
            <p className="text-lg">📭 등록된 계좌가 없습니다.</p>
            <button
              onClick={() => navigate("/create-account")}
              className="mt-3 bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-lg shadow"
            >
              계좌 개설하러 가기 →
            </button>
          </div>
        </div>
      )}

      {/* Content */}
      {accounts.length > 0 && currentAccount && (
        <div className="space-y-6">
          {/* Account Switcher + Card */}
          <section className="px-6">
            <div className="flex justify-between items-center mb-3">
              <button
                onClick={prevAccount}
                disabled={accounts.length <= 1}
                className="p-2 rounded-full bg-white shadow disabled:opacity-40"
                aria-label="이전 계좌"
              >
                <FaChevronLeft className="text-gray-600" />
              </button>
              <div className="flex gap-2" role="tablist" aria-label="계좌 선택 점프">
                {accounts.map((_, i) => (
                  <button
                    key={i}
                    onClick={() => setCurrentAccountIndex(i)}
                    className={`w-2.5 h-2.5 rounded-full ${
                      i === currentAccountIndex ? "bg-blue-600" : "bg-gray-300"
                    }`}
                    aria-pressed={i === currentAccountIndex}
                    aria-label={`계좌 ${i + 1}`}
                  />
                ))}
              </div>
              <button
                onClick={nextAccount}
                disabled={accounts.length <= 1}
                className="p-2 rounded-full bg-white shadow disabled:opacity-40"
                aria-label="다음 계좌"
              >
                <FaChevronRight className="text-gray-600" />
              </button>
            </div>

            <div className="bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-800 rounded-3xl p-7 text-white shadow-2xl">
              <div className="flex justify-between items-start mb-5">
                <div className="flex items-center gap-4">
                  <div className="bg-white/20 p-3 rounded-full">
                    <BsBank2 className="text-2xl" aria-hidden="true" />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold">
                      {currentAccount.bankName || "은행명 없음"}
                    </h3>
                    <p className="text-blue-200 text-sm">
                      {currentAccount.accountType || "일반계좌"}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => toggleBalanceVisibility(currentAccount.id)}
                  className="text-white/80 hover:text-white p-2"
                  aria-label="잔액 표시 전환"
                >
                  {visibleBalances[currentAccount.id] ? (
                    <FaEyeSlash size={20} />
                  ) : (
                    <FaEye size={20} />
                  )}
                </button>
              </div>

              <p className="text-blue-200 mb-2 font-mono tracking-wider">
                {currentAccount.accountNumber}
              </p>

              <div className="mb-6">
                <p className="text-blue-200 text-sm mb-1">잔액</p>
                <p className="text-4xl font-extrabold tabular-nums">
                  {visibleBalances[currentAccount.id]
                    ? fmtKRW(currentAccount.balance || 0)
                    : "••••••• 원"}
                </p>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <button
                  onClick={() =>
                    navigate(`/transfer?from=${currentAccount.accountNumber}`)
                  }
                  className="bg-white/20 backdrop-blur-sm px-4 py-3 rounded-xl text-sm font-medium hover:bg-white/30"
                >
                  이체하기
                </button>
                <button
                  onClick={() =>
                    navigate("/transactions", {
                      state: {
                        accountId: currentAccount.id,
                        accountNumber: currentAccount.accountNumber,
                      },
                    })
                  }
                  className="bg-white/20 backdrop-blur-sm px-4 py-3 rounded-xl text-sm font-medium hover:bg-white/30"
                >
                  상세보기
                </button>

                  <button
                      onClick={() =>
                        navigate(`/scheduled-transfer/new?from=${currentAccount.accountNumber}`)
                      }
                      className="bg-white/20 backdrop-blur-sm px-4 py-3 rounded-xl text-sm font-medium hover:bg-white/30"
                    >
                      예약이체
                    </button>

          
              </div>
            </div>
          </section>

            <div className="px-6">
              <button
                onClick={() => navigate("/create-account")}
                className="w-full bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-3 rounded-lg shadow mt-2"
              >
                ➕ 새 계좌 만들기
              </button>
            </div>

          {/* KPI Cards */}
          <section className="px-6">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <KpiCard
                label="이번달 수입"
                value={`+${fmtKRW(summary.income)}원`}
                icon={<FaArrowDown className="text-green-600" />}
                tone="green"
              />
              <KpiCard
                label="이번달 지출"
                value={`-${fmtKRW(summary.expense)}원`}
                icon={<FaArrowUp className="text-red-600" />}
                tone="red"
              />
              <KpiCard
                label="순증감"
                value={`${summary.net >= 0 ? "+" : "-"}${fmtKRW(
                  Math.abs(summary.net)
                )}원`}
                icon={<span className="font-bold">₩</span>}
                tone={summary.net >= 0 ? "blue" : "slate"}
              />
            </div>
          </section>

          {/* Balance Chart */}
          <section className="px-6">
            <div className="bg-white rounded-2xl p-6 shadow-lg">
              <h4 className="text-lg font-bold text-gray-800 mb-4">
                📈 잔액 변화 추이
              </h4>
              <div className="h-64">
                {chartData.length ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={chartData} margin={{ left: 8, right: 8 }}>
                      <defs>
                        <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.35} />
                          <stop offset="95%" stopColor="#3B82F6" stopOpacity={0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis
                        dataKey="date"
                        axisLine={false}
                        tickLine={false}
                        tick={{ fontSize: 12, fill: "#6B7280" }}
                      />
                      <YAxis
                        axisLine={false}
                        tickLine={false}
                        tick={{ fontSize: 12, fill: "#6B7280" }}
                        tickFormatter={(v) => fmtKRWCompact(v)}
                      />
                      <Tooltip
                        formatter={(v, name) => [
                          fmtKRW(v),
                          name === "balance" ? "잔액" : "금액",
                        ]}
                        labelClassName="text-sm"
                      />
                      <Area
                        type="monotone"
                        dataKey="balance"
                        stroke="#3B82F6"
                        strokeWidth={3}
                        fill="url(#colorBalance)"
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                ) : (
                  <EmptyChartState />
                )}
              </div>
            </div>
          </section>

          {/* Recent Transactions */}
          <section className="px-6 pb-8">
            <div className="bg-white rounded-2xl p-6 shadow-lg">
              <h4 className="text-lg font-bold text-gray-800 mb-4">
                💳 최근 거래내역
              </h4>
              <div className="space-y-3">
                {(txByAccount[currentAccount.id] || [])
                  .slice(0, 5)
                  .map((t, idx) => (
                    <TransactionRow key={t.id || idx} t={t} />
                  ))}
                {!(txByAccount[currentAccount.id] || []).length && (
                  <div className="text-center py-8 text-gray-500">
                    거래내역이 없습니다. 첫 거래를 시작해보세요.
                  </div>
                )}
              </div>
              {(txByAccount[currentAccount.id] || []).length > 5 && (
                <button
                  onClick={() =>
                    navigate(`/transactions?account=${currentAccount.accountNumber}`)
                  }
                  className="w-full mt-4 py-3 text-blue-600 font-medium hover:text-blue-800"
                >
                  전체 거래내역 보기 →
                </button>
              )}
            </div>
          </section>

       <section className="px-6 pb-8">
        <div className="bg-white rounded-2xl p-6 shadow-lg">
          <h4 className="text-lg font-bold text-gray-800 mb-4">📅 예약이체</h4>
          <div className="flex gap-3">
            <button
              onClick={() => navigate("/scheduled-transfer/new")}
              className="flex-1 bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition"
            >
              ➕ 예약이체 등록
            </button>
            <button
              onClick={() => navigate("/scheduled-transfers")}
              className="flex-1 border border-indigo-300 text-indigo-600 py-3 rounded-lg font-semibold hover:bg-indigo-50 transition"
            >
              📋 예약이체 내역
            </button>
          </div>
        </div>
      </section>


        </div>
      )}
    </div>
  );
}

// ---------- small reusable UI pieces --------------------------------------
function KpiCard({ label, value, icon, tone = "blue" }) {
  const tones =
    {
      green: { chip: "bg-green-100", text: "text-green-700" },
      red: { chip: "bg-red-100", text: "text-red-700" },
      blue: { chip: "bg-blue-100", text: "text-blue-700" },
      slate: { chip: "bg-slate-100", text: "text-slate-700" },
    }[tone] || { chip: "bg-blue-100", text: "text-blue-700" };

  return (
    <div className="bg-white rounded-2xl p-6 shadow-lg">
      <div className="flex items-center gap-3">
        <div className={`p-3 rounded-full ${tones.chip}`}>{icon}</div>
        <div>
          <p className="text-sm text-gray-500">{label}</p>
          <p className={`text-2xl font-bold ${tones.text} tabular-nums`}>
            {value}
          </p>
        </div>
      </div>
    </div>
  );
}

function TransactionRow({ t }) {
  const isIncome = t.type === "입금" || t.type === "DEPOSIT";
  return (
    <div className="flex justify-between items-center p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition">
      <div className="flex items-center gap-4">
        <div
          className={`p-2 rounded-full ${
            isIncome ? "bg-green-100 text-green-600" : "bg-red-100 text-red-600"
          }`}
        >
          {isIncome ? <FaArrowDown size={16} /> : <FaArrowUp size={16} />}
        </div>
        <div>
          <p className="font-medium text-gray-800">{t.description}</p>
          <p className="text-sm text-gray-500">
            {new Date(t.transactionDate).toLocaleDateString("ko-KR", {
              month: "long",
              day: "numeric",
              hour: "2-digit",
              minute: "2-digit",
            })}
          </p>
        </div>
      </div>
      <div className="text-right">
        <p
          className={`text-lg font-bold ${
            isIncome ? "text-green-600" : "text-red-600"
          }`}
        >
          {isIncome ? "+" : "-"}
          {Number(t.amount || 0).toLocaleString()}원
        </p>
        <p className="text-sm text-gray-400">
          잔액: {Number(t.balanceAfter || 0).toLocaleString()}원
        </p>
      </div>
    </div>
  );
}

function EmptyChartState() {
  return (
    <div className="flex items-center justify-center h-full text-gray-500 text-center">
      <div>
        <p className="text-lg mb-1">📊 그래프 데이터 없음</p>
        <p className="text-sm">거래 내역이 충분하지 않습니다</p>
      </div>
    </div>
  );
}

export default Dashboard;
