// src/pages/Login.jsx
import { useState } from "react";
import axios from "../api/axiosInstance";
import { useNavigate, Link } from "react-router-dom";
import { AiOutlineLock, AiOutlineUser } from "react-icons/ai";
import { FaEye, FaEyeSlash } from "react-icons/fa6";

/**
 * Login — Sky Balanced Theme
 * - 산뜻한 파스텔 배경 + 톤다운 가독성(슬레이트 텍스트/더 불투명한 카드)
 * - 실무 포인트: 실패 원인별 메시지, 중복 전송 방지, 간단 유효성, 접근성, 비밀번호 토글
 * - NOTE: 데모에서는 localStorage 저장. 운영은 httpOnly 쿠키 + 토큰 회전 권장.
 */
export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState("");
  const navigate = useNavigate();

  const isEmailValid = /\S+@\S+\.\S+/.test(email);
  const isPwValid = password.length >= 8;
  const formValid = isEmailValid && isPwValid;

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!formValid || loading) return;

    setErrMsg("");
    setLoading(true);
    try {
      const res = await axios.post("/auth/login", { email, password });
      const { accessToken, refreshToken } = res.data || {};
      if (!accessToken || typeof accessToken !== "string") throw new Error("토큰 누락");

      // 데모/포폴: localStorage 사용. 실서비스: httpOnly 쿠키 권장.
      localStorage.setItem("jwtToken", accessToken);
      if (refreshToken) localStorage.setItem("refreshToken", refreshToken);

      navigate("/dashboard");
    } catch (err) {
      if (err.response) {
        const { status, data } = err.response;
        if (status === 401) setErrMsg("이메일 또는 비밀번호가 올바르지 않습니다.");
        else if (status === 429) setErrMsg("요청이 많습니다. 잠시 후 다시 시도해주세요.");
        else setErrMsg(data?.message || "로그인 중 오류가 발생했습니다.");
      } else if (err.request) {
        setErrMsg("서버에 연결할 수 없습니다. 네트워크를 확인해주세요.");
      } else {
        setErrMsg("예상치 못한 오류입니다. 다시 시도해주세요.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen flex items-center justify-center overflow-hidden">
      {/* Softer airy gradient (가독성 위해 톤다운) */}
      <div className="absolute inset-0 bg-gradient-to-br from-sky-100 via-indigo-100 to-slate-100" />

      {/* 아주 은은한 블롭 (밝기 낮춤) */}
      <div className="pointer-events-none absolute inset-0 opacity-50">
        <div className="absolute -top-24 -left-24 h-64 w-64 rounded-full bg-white/50 blur-3xl" />
        <div className="absolute bottom-[-60px] right-[-40px] h-72 w-72 rounded-full bg-white/40 blur-3xl" />
      </div>

      {/* Card: 더 불투명 + 대비 높은 텍스트 */}
      <form
        onSubmit={handleLogin}
        aria-busy={loading}
        className="relative w-[min(420px,92vw)] rounded-2xl border border-slate-200 bg-white/90 backdrop-blur-md shadow-xl p-8"
      >
        {/* Brand */}
        <div className="mb-6 text-center">
          <h1 className="text-2xl font-extrabold tracking-wide text-slate-800">OpenBank</h1>
          <p className="mt-1 text-sm text-slate-600">안전하고 산뜻한 디지털 뱅킹</p>
        </div>

        {/* 이메일 */}
        <label className="block text-sm font-medium text-slate-800 mb-2" htmlFor="email">
          이메일
        </label>
        <div className="relative mb-4">
          <AiOutlineUser className="absolute left-3 top-2.5 text-slate-500" aria-hidden="true" />
          <input
            id="email"
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white text-slate-900 placeholder-slate-400 border border-slate-200
                       focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
            autoComplete="email"
            required
            aria-invalid={!isEmailValid && email !== ""}
          />
          {!isEmailValid && email !== "" && (
            <p className="mt-1 text-xs text-rose-600">올바른 이메일 형식을 입력하세요.</p>
          )}
        </div>

        {/* 비밀번호 */}
        <label className="block text-sm font-medium text-slate-800 mb-2" htmlFor="password">
          비밀번호
        </label>
        <div className="relative mb-2">
          <AiOutlineLock className="absolute left-3 top-2.5 text-slate-500" aria-hidden="true" />
          <input
            id="password"
            type={showPw ? "text" : "password"}
            placeholder="비밀번호 (8자 이상)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full pl-10 pr-12 py-3 rounded-lg bg-white text-slate-900 placeholder-slate-400 border border-slate-200
                       focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
            autoComplete="current-password"
            required
            aria-invalid={!isPwValid && password !== ""}
          />
          <button
            type="button"
            onClick={() => setShowPw((v) => !v)}
            className="absolute right-3 top-2.5 text-slate-500 hover:text-slate-700 transition"
            aria-label={showPw ? "비밀번호 숨기기" : "비밀번호 표시"}
          >
            {showPw ? <FaEyeSlash /> : <FaEye />}
          </button>
        </div>
        {!isPwValid && password !== "" && (
          <p className="mb-3 text-xs text-rose-600">비밀번호는 8자 이상이어야 합니다.</p>
        )}

        {/* 에러 */}
        {errMsg && (
          <div className="mb-4 rounded-lg border border-rose-200 bg-rose-50 text-rose-700 px-3 py-2 text-sm">
            {errMsg}
          </div>
        )}

        {/* 제출 버튼: 딥블루 계열로 대비 강화 */}
        <button
          type="submit"
          disabled={!formValid || loading}
          className={`relative w-full py-3 rounded-lg font-semibold transition
            ${!formValid || loading
              ? "bg-slate-200 text-slate-500 cursor-not-allowed"
              : "bg-gradient-to-r from-indigo-600 to-sky-600 text-white hover:brightness-110 shadow"
            }`}
        >
          {loading ? "로그인 중..." : "로그인"}
          {!(!formValid || loading) && (
            <span className="pointer-events-none absolute inset-0 rounded-lg opacity-25 bg-[radial-gradient(120px_60px_at_30%_0%,white,transparent)]" />
          )}
        </button>

        {/* 링크 */}
        <div className="mt-5 flex items-center justify-between text-sm">
          <Link to="/forgot-password" className="text-slate-600 hover:text-slate-800">
            비밀번호 찾기
          </Link>
          <Link to="/signup" className="text-indigo-700 font-semibold hover:text-indigo-800">
            회원가입
          </Link>
        </div>

        {/* 안내 */}
        <p className="mt-5 text-[11px] leading-5 text-slate-600">
          데모 환경에서는 액세스 토큰을 브라우저 저장소에 보관합니다.
          실제 운영에서는 <b className="text-slate-800">httpOnly 쿠키</b> 사용과{" "}
          <b className="text-slate-800">짧은 액세스 토큰 만료 + 리프레시 토큰 회전</b>을 권장합니다.
        </p>

        {/* 미세한 하이라이트 테두리 (너무 밝지 않게) */}
        <div className="pointer-events-none absolute -inset-[1px] rounded-[16px] bg-white/40" />
      </form>
    </div>
  );
}
