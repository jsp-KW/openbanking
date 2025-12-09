import axios from 'axios';

const instance = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

// ⛔ 토큰 제외 경로(여기 매우 중요함)
const excludedPaths = [
  '/auth/login',
  '/auth/signup',
  '/auth/check-email',
  '/auth/refresh',
];

instance.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('jwtToken');

    const isExcluded = excludedPaths.some((path) =>
      config.url?.includes(path)
    );

    if (!isExcluded && accessToken) {
      config.headers['Authorization'] = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

instance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 🛑 변경: 401일 때만 refresh 시도
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error('RefreshToken 없음');

        const res = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          {},
          {
            headers: {
              Authorization: `Bearer ${refreshToken}`,
            },
          }
        );

        const newAccessToken = res.data.accessToken;
        const newRefreshToken = res.data.refreshToken;

        if (newAccessToken) {
          localStorage.setItem('jwtToken', newAccessToken);
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }
        if (newRefreshToken) localStorage.setItem('refreshToken', newRefreshToken);

        return instance(originalRequest);
      } catch (refreshError) {
        console.error('refresh 실패 → 강제 로그아웃');
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default instance;
