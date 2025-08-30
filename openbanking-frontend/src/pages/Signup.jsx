// src/pages/Signup.jsx
import { useMemo, useState } from "react";
import axios from "../api/axiosInstance";
import { useNavigate, Link } from "react-router-dom";
import {
  AiOutlineUser,
  AiOutlineMail,
  AiOutlineLock,
  AiOutlinePhone,
} from "react-icons/ai";

/**
 * Signup — Sky Balanced Theme
 * - 산뜻한 파스텔 배경 + 높은 가독성 카드/텍스트
 * - 기능: 클라이언트 유효성 검사, 실패 원인별 메시지, 중복 전송 방지, 접근성
 */
export default function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    phone: "",
    role: "USER",
  });
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState("");
  const [okMsg, setOkMsg] = useState("");

  const isEmailValid = /\S+@\S+\.\S+/.test(form.email);
  const isPwValid = form.password.length >= 8;
  const isNameValid = form.name.trim().length >= 2;
  const isPhoneValid = /^\d{9,13}$/.test(form.phone.replace(/[^0-9]/g, ""));
  const formValid = isEmailValid && isPwValid && isNameValid && isPhoneValid;

  const pwStrength = useMemo(() => {
    const p = form.password;
    let score = 0;
    if (p.length >= 8) score++;
    if (/[A-Z]/.test(p)) score++;
    if (/[a-z]/.test(p)) score++;
    if (/\d/.test(p)) score++;
    if (/[^A-Za-z0-9]/.test(p)) score++;
    return Math.min(score, 4); // 0~4
  }, [form.password]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setErrMsg("");
    setOkMsg("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formValid || loading) return;
    setErrMsg("");
    setOkMsg("");
    setLoading(true);
    try {
      await axios.post("/auth/signup", {
        ...form,
        phone: form.phone.replace(/[^0-9]/g, ""),
      });
      setOkMsg("회원가입 성공! 잠시 후 로그인 화면으로 이동합니다.");
      // UX: 메시지 잠깐 보여주고 이동
      setTimeout(() => navigate("/login"), 900);
    } catch (err) {
      if (err.response) {
        const { status, data } = err.response;
        if (status === 409) setErrMsg("이미 가입된 이메일입니다.");
        else setErrMsg(data?.message || "회원가입 중 오류가 발생했습니다.");
      } else if (err.request) {
        setErrMsg("서버에 연결할 수 없습니다. 네트워크를 확인해주세요.");
      } else {
        setErrMsg("예상치 못한 오류가 발생했습니다. 다시 시도해주세요.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen flex items-center justify-center bg-gradient-to-br from-sky-100 via-indigo-100 to-slate-100 overflow-hidden px-4">
      {/* 아주 은은한 블롭 */}
      <div className="pointer-events-none absolute inset-0 opacity-40">
        <div className="absolute -top-24 -left-24 h-64 w-64 rounded-full bg-white/50 blur-3xl" />
        <div className="absolute bottom-[-60px] right-[-40px] h-72 w-72 rounded-full bg-white/40 blur-3xl" />
      </div>

      <form
        onSubmit={handleSubmit}
        aria-busy={loading}
        className="relative w-[min(480px,92vw)] rounded-2xl border border-slate-200 bg-white/90 backdrop-blur-md shadow-xl p-8"
      >
        <h2 className="text-2xl font-extrabold text-slate-800 text-center">
          회원가입
        </h2>
        <p className="mt-1 mb-6 text-sm text-slate-600 text-center">
          안전한 디지털 뱅킹을 시작해 보세요.
        </p>

        {/* 이름 */}
        <label className="block text-sm font-medium text-slate-800 mb-2" htmlFor="name">
          이름
        </label>
        <div className="relative mb-4">
          <AiOutlineUser className="absolute left-3 top-2.5 text-slate-500" aria-hidden="true" />
          <input
            id="name"
            name="name"
            type="text"
            value={form.name}
            onChange={handleChange}
            placeholder="홍길동"
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white text-slate-900 placeholder-slate-400 border border-slate-200
                       focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
            required
            aria-invalid={!isNameValid && form.name !== ""}
          />
          {!isNameValid && form.name !== "" && (
            <p className="mt-1 text-xs text-rose-600">이름은 2자 이상 입력하세요.</p>
          )}
        </div>

        {/* 이메일 */}
        <label className="block text-sm font-medium text-slate-800 mb-2" htmlFor="email">
          이메일
        </label>
        <div className="relative mb-4">
          <AiOutlineMail className="absolute left-3 top-2.5 text-slate-500" aria-hidden="true" />
          <input
            id="email"
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            placeholder="you@example.com"
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white text-slate-900 placeholder-slate-400 border border-slate-200
                       focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
            required
            aria-invalid={!isEmailValid && form.email !== ""}
            autoComplete="email"
          />
          {!isEmailValid && form.email !== "" && (
            <p className="mt-1 text-xs text-rose-600">올바른 이메일 형식을 입력하세요.</p>
          )}
        </div>

        {/* 비밀번호 */}
        <label className="block text-sm font-medium text-slate-800 mb-2" htmlFor="password">
          비밀번호
        </label>
        <div className="relative mb-1">
          <AiOutlineLock className="absolute left-3 top-2.5 text-slate-500" aria-hidden="true" />
          <input
            id="password"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            placeholder="영문/숫자/기호 조합 8자 이상"
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white text-slate-900 placeholder-slate-400 border border-slate-200
                       focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
            required
            aria-invalid={!isPwValid && form.password !== ""}
            autoComplete="new-password"
          />
        </div>
        {/* 비밀번호 강도 표시 */}
        <div className="mb-3">
          <div className="h-1.5 w-full bg-slate-200 rounded-full overflow-hidden">
            <div
              className={`h-full transition-all duration-300 ${
                pwStrength <= 1
                  ? "bg-rose-400 w-1/4"
                  : pwStrength === 2
                  ? "bg-amber-400 w-2/4"
                  : pwStrength === 3
                  ? "bg-sky-400 w-3/4"
                  : "bg-emerald-500 w-full"
              }`}
            />
          </div>
          <p className="mt-1 text-xs text-slate-500">
            {pwStrength <= 1
              ? "보안 수준 낮음"
              : pwStrength === 2
              ? "보안 수준 보통"
              : pwStrength === 3
              ? "보안 수준 좋음"
              : "보안 수준 매우 좋음"}
          </p>
        </div>

        {/* 전화번호 */}
        <label className="block text-sm font-medium text-slate-800 mb-2" htmlFor="phone">
          전화번호
        </label>
        <div className="relative mb-5">
          <AiOutlinePhone className="absolute left-3 top-2.5 text-slate-500" aria-hidden="true" />
          <input
            id="phone"
            name="phone"
            type="tel"
            inputMode="numeric"
            value={form.phone}
            onChange={handleChange}
            placeholder="01012345678"
            className="w-full pl-10 pr-3 py-3 rounded-lg bg-white text-slate-900 placeholder-slate-400 border border-slate-200
                       focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
            required
            aria-invalid={!isPhoneValid && form.phone !== ""}
            autoComplete="tel"
          />
          {!isPhoneValid && form.phone !== "" && (
            <p className="mt-1 text-xs text-rose-600">
              숫자만 9~13자리로 입력하세요 (예: 01012345678)
            </p>
          )}
        </div>

        {/* 역할 선택 (선택 사항) */}
        <label className="block text-sm font-medium text-slate-800 mb-2" htmlFor="role">
          권한
        </label>
        <select
          id="role"
          name="role"
          value={form.role}
          onChange={handleChange}
          className="mb-6 w-full py-3 px-3 rounded-lg bg-white text-slate-900 border border-slate-200
                     focus:outline-none focus:ring-2 focus:ring-sky-300 focus:border-transparent"
        >
          <option value="USER">일반 사용자</option>
          <option value="ADMIN">관리자</option>
        </select>

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

        {/* 제출 버튼 */}
        <button
          type="submit"
          disabled={!formValid || loading}
          className={`w-full py-3 rounded-lg font-semibold transition
            ${!formValid || loading
              ? "bg-slate-200 text-slate-500 cursor-not-allowed"
              : "bg-gradient-to-r from-indigo-600 to-sky-600 text-white hover:brightness-110 shadow"
            }`}
        >
          {loading ? "가입 처리 중..." : "회원가입"}
        </button>

        <p className="mt-5 text-sm text-center text-slate-600">
          이미 계정이 있으신가요?{" "}
          <Link to="/login" className="text-indigo-700 font-semibold hover:text-indigo-800">
            로그인
          </Link>
        </p>
      </form>
    </div>
  );
}
