import { useMemo, useState } from "react";
import axios from "../api/axiosInstance";
import { useNavigate, Link } from "react-router-dom";
import {
  AiOutlineUser,
  AiOutlineMail,
  AiOutlineLock,
  AiOutlinePhone,
} from "react-icons/ai";

export default function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    phone: "",
  });

  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState("");
  const [okMsg, setOkMsg] = useState("");

  // 이메일 중복 체크
  const [emailAvailable, setEmailAvailable] = useState(null); 
  const [checkingEmail, setCheckingEmail] = useState(false);

  // Validation
  const isEmailValid = /\S+@\S+\.\S+/.test(form.email);
  const isPwValid = form.password.length >= 8;
  const isNameValid = form.name.trim().length >= 2;
  const isPhoneValid = /^\d{9,13}$/.test(form.phone.replace(/[^0-9]/g, ""));

  const formValid =
    isEmailValid &&
    isPwValid &&
    isNameValid &&
    isPhoneValid &&
    emailAvailable === true;

  // Password Strength
  const pwStrength = useMemo(() => {
    const p = form.password;
    let score = 0;
    if (p.length >= 8) score++;
    if (/[A-Z]/.test(p)) score++;
    if (/[a-z]/.test(p)) score++;
    if (/\d/.test(p)) score++;
    if (/[^A-Za-z0-9]/.test(p)) score++;
    return Math.min(score, 4);
  }, [form.password]);

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((prev) => ({ ...prev, [name]: value }));
    setErrMsg("");
    setOkMsg("");

    if (name === "email") {
      setEmailAvailable(null); // 이메일 바뀌면 초기화
    }
  };

  // 중복 체크 버튼 클릭 시 실행
  const handleCheckEmail = async () => {
    if (!isEmailValid) {
      setErrMsg("올바른 이메일 형식을 입력하세요.");
      return;
    }

    try {
      setCheckingEmail(true);
      const res = await axios.get(`/auth/check-email?email=${form.email}`);
      const exists = res.data; // true = 이미 존재함

      if (exists) {
        setEmailAvailable(false);
      } else {
        setEmailAvailable(true);
      }
    } catch (err) {
      setErrMsg("중복 검사 실패 (서버 또는 네트워크 오류)");
    } finally {
      setCheckingEmail(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formValid || loading) return;

    setErrMsg("");
    setOkMsg("");
    setLoading(true);

    try {
      await axios.post("/auth/signup", {
        name: form.name,
        email: form.email,
        password: form.password,
        phone: form.phone.replace(/[^0-9]/g, ""),
      });

      setOkMsg("회원가입 성공! 잠시 후 로그인 화면으로 이동합니다.");
      setTimeout(() => navigate("/login"), 900);
    } catch (err) {
      if (err.response)
        setErrMsg(err.response.data?.message || "회원가입 실패");
      else setErrMsg("네트워크 오류");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen flex items-center justify-center bg-gradient-to-br from-sky-100 via-indigo-100 to-slate-100 px-4">
      <form
        onSubmit={handleSubmit}
        className="relative w-[min(480px,92vw)] rounded-2xl border border-slate-200 bg-white/90 backdrop-blur-md shadow-xl p-8"
      >
        <h2 className="text-2xl font-extrabold text-slate-800 text-center">
          회원가입
        </h2>

        <p className="mt-1 mb-6 text-sm text-slate-600 text-center">
          안전한 디지털 뱅킹을 시작해 보세요.
        </p>

        {/* 이름 */}
        <label className="block text-sm font-medium mb-2">이름</label>
        <div className="relative mb-4">
          <AiOutlineUser className="absolute left-3 top-2.5 text-slate-500" />
          <input
            name="name"
            value={form.name}
            onChange={handleChange}
            required
            placeholder="홍길동"
            className="w-full pl-10 pr-3 py-3 rounded-lg border border-slate-200 focus:ring-2 focus:ring-sky-300"
          />
        </div>

        {/* 이메일 */}
        <label className="block text-sm font-medium mb-2">이메일</label>
        <div className="relative mb-2 flex gap-2">
          <div className="relative flex-1">
            <AiOutlineMail className="absolute left-3 top-2.5 text-slate-500" />
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              required
              placeholder="you@example.com"
              className="w-full pl-10 pr-3 py-3 rounded-lg border border-slate-200 focus:ring-2 focus:ring-sky-300"
            />
          </div>

          <button
            type="button"
            onClick={handleCheckEmail}
            className="px-3 py-2 rounded-lg bg-sky-600 text-white text-sm hover:bg-sky-700"
          >
            중복확인
          </button>
        </div>

        {/* 이메일 중복 결과 */}
        {checkingEmail && <p className="text-xs text-slate-500 mb-2">확인 중...</p>}
        {emailAvailable === true && <p className="text-xs text-emerald-600 mb-2">사용 가능한 이메일입니다!</p>}
        {emailAvailable === false && <p className="text-xs text-rose-600 mb-2">이미 사용 중인 이메일입니다.</p>}

        {/* 비밀번호 */}
        <label className="block text-sm font-medium mb-2">비밀번호</label>
        <div className="relative mb-4">
          <AiOutlineLock className="absolute left-3 top-2.5 text-slate-500" />
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            required
            placeholder="8자 이상"
            className="w-full pl-10 pr-3 py-3 rounded-lg border border-slate-200 focus:ring-2 focus:ring-sky-300"
          />
        </div>

        {/* 전화번호 */}
        <label className="block text-sm font-medium mb-2">전화번호</label>
        <div className="relative mb-5">
          <AiOutlinePhone className="absolute left-3 top-2.5 text-slate-500" />
          <input
            name="phone"
            value={form.phone}
            onChange={handleChange}
            required
            placeholder="01012345678"
            className="w-full pl-10 pr-3 py-3 rounded-lg border border-slate-200 focus:ring-2 focus:ring-sky-300"
          />
        </div>

        {/* 메시지 */}
        {errMsg && (
          <div className="mb-4 px-3 py-2 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-sm">
            {errMsg}
          </div>
        )}
        {okMsg && (
          <div className="mb-4 px-3 py-2 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm">
            {okMsg}
          </div>
        )}

        {/* 버튼 */}
        <button
          type="submit"
          disabled={!formValid || loading}
          className={`w-full py-3 rounded-lg font-semibold transition ${
            formValid && !loading
              ? "bg-gradient-to-r from-indigo-600 to-sky-600 text-white hover:brightness-110"
              : "bg-slate-200 text-slate-500 cursor-not-allowed"
          }`}
        >
          {loading ? "가입 중..." : "회원가입"}
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
