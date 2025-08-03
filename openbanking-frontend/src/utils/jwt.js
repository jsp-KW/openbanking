export const parseJwt = (token) => {
  if (!token || typeof token !== 'string') return null;

  try {
    const parts = token.split('.');
    if (parts.length !== 3) throw new Error('Invalid token format');

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64).split('').map(c =>
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      ).join('')
    );
    return JSON.parse(jsonPayload);
  } catch (err) {
    console.error('❌ JWT 파싱 실패:', err);
    return null;
  }
};
