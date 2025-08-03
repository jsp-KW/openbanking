import axios from 'axios';

const instance = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true, 
  headers: {
    'Content-Type': 'application/json',
  },
});

instance.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('jwtToken');

    const excludedPaths = ['/auth/login', '/auth/refresh'];
    const isExcluded = excludedPaths.some(path => config.url?.includes(path));

    if (!isExcluded && accessToken && !config.headers['Authorization']) {
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

    if (error.response?.status === 403 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error(' refreshToken 없음');

        const refreshInstance = axios.create({
          baseURL: 'http://localhost:8080/api',
        });

        const res = await refreshInstance.post(
          '/auth/refresh',
          {},
          {
            headers: {
              Authorization: `Bearer ${refreshToken}`,
            },
          }
        );

        const newAccessToken = res.data.accessToken || res.data.token;
        const newRefreshToken = res.data.refreshToken;

        if (!newAccessToken) throw new Error(' 새 accessToken 없음');

        localStorage.setItem('jwtToken', newAccessToken);
        if (newRefreshToken) {
          localStorage.setItem('refreshToken', newRefreshToken);
        }

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return instance(originalRequest);
      } catch (refreshError) {
        console.error('[axios] 토큰 재발급 실패:', refreshError);
        alert('세션이 만료되었습니다. 다시 로그인해주세요.');
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default instance;
