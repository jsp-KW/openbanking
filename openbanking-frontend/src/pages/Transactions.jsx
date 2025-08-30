// src/pages/Transaction.jsx
import { useEffect, useMemo, useState } from "react";
import axios from "../api/axiosInstance";
import { AiOutlineSearch } from "react-icons/ai";
import { FaArrowDown, FaArrowUp } from "react-icons/fa6";
import { useLocation } from "react-router-dom";

export default function Transaction() {
  const location = useLocation();

  // 데이터 상태
  const [accounts, setAccounts] = useState([]);
  const [selectedAccountId, setSelectedAccountId] = useState(""); // ✅ 계좌 ID로 전환
  const [transactions, setTransactions] = useState([]);

  // UI/에러 상태
  const [loading, setLoading] = useState(true);
  const [errMsg, setErrMsg] = useState("");

  // 필터/정렬
  const [q, setQ] = useState("");
  const [typeFilter, setTypeFilter] = useState("ALL"); // ALL | DEPOSIT | WITHDRAWAL
  const [sortBy, setSortBy] = useState("date_desc"); // date_desc | date_asc | amt_desc | amt_asc

  const krw = new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency: "KRW",
    maximumFractionDigits: 0,
  });

  const fmtDate = (ts) => {
    if (!ts) return "-";
    try {
      const d = new Date(ts);
      return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      }).format(d);
    } catch {
      return ts?.slice(0, 10) || "-";
    }
  };

  // 1) 내 계좌 목록 로드 (마운트 1회)
  useEffect(() => {
    (async () => {
      try {
        const res = await axios.get("/accounts/my");
        const list = Array.isArray(res.data) ? res.data : [];
        setAccounts(list);
      } catch (err) {
        console.error("계좌 불러오기 실패:", err);
        setErrMsg("계좌 정보를 불러올 수 없습니다.");
      }
    })();
  }, []);

  // 2) URL 쿼리(?account=) → 선택 계좌 결정 (accounts 로드 후/쿼리 변경 시)
  useEffect(() => {
    if (!accounts.length) return;
    const qs = new URLSearchParams(location.search);
    const raw = qs.get("account"); // 대시보드에서 id로 전달됨

    if (!raw) {
      // 쿼리 없으면 첫 계좌 선택
      setSelectedAccountId(String(accounts[0].id));
      return;
    }

    // 호환: 과거 계좌번호가 넘어온 경우(예: 004-78410036) → id로 매핑
    const byNumber = accounts.find((a) => a.accountNumber === raw);
    if (byNumber) {
      setSelectedAccountId(String(byNumber.id));
    } else {
      // 정상: 이미 id가 넘어온 경우
      setSelectedAccountId(String(raw));
    }
  }, [accounts, location.search]);

  // 3) 선택 계좌의 거래내역 로드 (계좌 ID 사용)
  useEffect(() => {
    if (!selectedAccountId) return;
    setLoading(true);
    setErrMsg("");

    axios
      .get(`/transactions/account/${encodeURIComponent(selectedAccountId)}`)
      .then((res) => {
        setTransactions(Array.isArray(res.data) ? res.data : []);
      })
      .catch((err) => {
        console.error("거래 내역 불러오기 실패:", err);
        const status = err?.response?.status;
        if (status === 401) setErrMsg("로그인이 필요합니다.");
        else if (status === 403) setErrMsg("접근 권한이 없습니다. (계좌 소유 확인)");
        else setErrMsg("거래 내역을 불러올 수 없습니다.");
      })
      .finally(() => setLoading(false));
  }, [selectedAccountId]);

  // 4) 필터 & 정렬
  const viewRows = useMemo(() => {
    let rows = [...transactions];

    const keyword = q.trim();
    if (keyword) {
      const lower = keyword.toLowerCase();
      rows = rows.filter((t) => {
        const memo = String(t.memo ?? "").toLowerCase();
        const other = String(t.targetAccountNumber ?? "").toLowerCase();
        return memo.includes(lower) || other.includes(lower);
      });
    }

    if (typeFilter !== "ALL") {
      rows = rows.filter(
        (t) => t.type === typeFilter || t.type === mapKoreanType(typeFilter)
      );
    }

    rows.sort((a, b) => {
      const aAmt = Number(a.amount || 0);
      const bAmt = Number(b.amount || 0);
      const aDate = new Date(a.timestamp || a.transactionDate || 0).getTime();
      const bDate = new Date(b.timestamp || b.transactionDate || 0).getTime();
      switch (sortBy) {
        case "date_asc":
          return aDate - bDate;
        case "amt_desc":
          return bAmt - aAmt;
        case "amt_asc":
          return aAmt - bAmt;
        case "date_desc":
        default:
          return bDate - aDate;
      }
    });

    return rows;
  }, [transactions, q, typeFilter, sortBy]);

  function mapKoreanType(en) {
    if (en === "DEPOSIT") return "입금";
    if (en === "WITHDRAWAL") return "출금";
    return en;
  }

  const Badge = ({ type }) => {
    const isIn = type === "입금" || type === "DEPOSIT";
    const isOut = type === "출금" || type === "WITHDRAWAL";
    return (
      <span
        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium
          ${isIn ? "bg-emerald-50 text-emerald-700 border border-emerald-100" : ""}
          ${isOut ? "bg-rose-50 text-rose-700 border border-rose-100" : ""}
          ${
            !isIn && !isOut
              ? "bg-slate-50 text-slate-700 border border-slate-200"
              : ""
          }`}
      >
        {isIn ? <FaArrowDown /> : isOut ? <FaArrowUp /> : null}
        {type}
      </span>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-sky-100 via-indigo-100 to-slate-100 p-6">
      <div className="max-w-5xl mx-auto">
        <h2 className="text-2xl md:text-3xl font-extrabold text-slate-800 text-center mb-6">
          📊 거래 내역 조회
        </h2>

        {/* 컨트롤 패널 */}
        <div className="bg-white/90 backdrop-blur-md border border-slate-200 rounded-2xl shadow p-4 md:p-5 mb-4">
          <div className="grid md:grid-cols-2 gap-3">
            {/* 계좌 선택 (ID 기반) */}
            <div>
              <label className="block mb-1 text-sm font-semibold text-slate-800">
                계좌 선택
              </label>
              <select
                value={selectedAccountId}
                onChange={(e) => setSelectedAccountId(e.target.value)}
                className="w-full pl-3 pr-3 py-2.5 rounded-lg bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
              >
                {accounts.map((acc) => (
                  <option key={acc.id} value={acc.id}>
                    {acc.bankName} — {acc.accountNumber}
                  </option>
                ))}
              </select>
            </div>

            {/* 검색 + 필터 + 정렬 */}
            <div className="grid grid-cols-12 gap-2">
              <div className="col-span-6">
                <label className="block mb-1 text-sm font-semibold text-slate-800">
                  검색
                </label>
                <div className="relative">
                  <AiOutlineSearch className="absolute left-3 top-2.5 text-slate-500" />
                  <input
                    value={q}
                    onChange={(e) => setQ(e.target.value)}
                    placeholder="메모/상대 계좌 검색"
                    className="w-full pl-9 pr-3 py-2.5 rounded-lg bg-white border border-slate-200 text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
                  />
                </div>
              </div>
              <div className="col-span-3">
                <label className="block mb-1 text-sm font-semibold text-slate-800">
                  유형
                </label>
                <select
                  value={typeFilter}
                  onChange={(e) => setTypeFilter(e.target.value)}
                  className="w-full py-2.5 px-2 rounded-lg bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
                >
                  <option value="ALL">전체</option>
                  <option value="DEPOSIT">입금</option>
                  <option value="WITHDRAWAL">출금</option>
                </select>
              </div>
              <div className="col-span-3">
                <label className="block mb-1 text-sm font-semibold text-slate-800">
                  정렬
                </label>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="w-full py-2.5 px-2 rounded-lg bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
                >
                  <option value="date_desc">최신 날짜</option>
                  <option value="date_asc">오래된 날짜</option>
                  <option value="amt_desc">금액 높은순</option>
                  <option value="amt_asc">금액 낮은순</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* 에러/로딩/테이블 */}
        {errMsg && (
          <div className="mb-3 rounded-lg border border-rose-200 bg-rose-50 text-rose-700 px-3 py-2 text-sm">
            {errMsg}
          </div>
        )}

        <div className="overflow-x-auto bg-white/95 border border-slate-200 rounded-2xl shadow">
          {loading ? (
            <div className="p-6 animate-pulse space-y-3">
              <div className="h-5 bg-slate-200 rounded w-1/4" />
              <div className="h-5 bg-slate-200 rounded w-2/4" />
              <div className="h-5 bg-slate-200 rounded w-3/4" />
              <div className="h-5 bg-slate-200 rounded w-1/3" />
            </div>
          ) : (
            <table className="w-full table-auto">
              <thead className="bg-slate-50 sticky top-0">
                <tr className="text-slate-600 text-sm">
                  <th className="px-4 py-3 text-left border-b border-slate-200">
                    📅 날짜
                  </th>
                  <th className="px-4 py-3 text-left border-b border-slate-200">
                    유형
                  </th>
                  <th className="px-4 py-3 text-right border-b border-slate-200">
                    금액
                  </th>
                  <th className="px-4 py-3 text-left border-b border-slate-200">
                    상대 계좌
                  </th>
                  <th className="px-4 py-3 text-left border-b border-slate-200">
                    메모
                  </th>
                </tr>
              </thead>
              <tbody>
                {viewRows.length > 0 ? (
                  viewRows.map((tx) => {
                    const isIn =
                      tx.type === "입금" || tx.type === "DEPOSIT";
                    return (
                      <tr
                        key={tx.id}
                        className="text-sm text-slate-800 even:bg-slate-50/40"
                      >
                        <td className="px-4 py-3 align-top">
                          {fmtDate(tx.timestamp || tx.transactionDate)}
                        </td>
                        <td className="px-4 py-3 align-top">
                          <Badge type={tx.type} />
                        </td>
                        <td
                          className={`px-4 py-3 align-top text-right font-semibold ${
                            isIn ? "text-emerald-700" : "text-rose-700"
                          }`}
                        >
                          {isIn ? "+" : "-"}{" "}
                          {krw.format(Number(tx.amount || 0))}
                        </td>
                        <td className="px-4 py-3 align-top">
                          {tx.targetAccountNumber || "-"}
                        </td>
                        <td className="px-4 py-3 align-top">
                          {tx.memo || "-"}
                        </td>
                      </tr>
                    );
                  })
                ) : (
                  <tr>
                    <td
                      colSpan={5}
                      className="px-4 py-8 text-center text-slate-500"
                    >
                      거래 내역이 없습니다.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
