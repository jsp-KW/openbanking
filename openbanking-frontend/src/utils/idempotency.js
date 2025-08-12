// src/utils/idempotency.js
export function newIdemKey() {
  if (window.crypto?.randomUUID) return window.crypto.randomUUID();
  // fallback (구형 브라우저)
  return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
    (c ^ (crypto.getRandomValues(new Uint8Array(1))[0] & 15) >> (c / 4)).toString(16)
  );
}

// 폼 내용 기반 지문 (같은 이체면 같은 문자열)
export function makeFingerprint({ fromBankId, toBankId, fromAccountNumber, toAccountNumber, amount }) {
  const canon = {
    fromBankId: Number(fromBankId),
    toBankId: Number(toBankId),
    fromAccountNumber: String(fromAccountNumber || ''),
    toAccountNumber: String(toAccountNumber || ''),
    amount: Number(amount),
  };
  return JSON.stringify(canon);
}

export function getOrCreateKey(userId, fingerprint) {
  const storageKey = `idem.transfer.${userId}.${fingerprint}`;
  let k = localStorage.getItem(storageKey);
  if (!k) {
    k = newIdemKey();
    localStorage.setItem(storageKey, k);
  }
  return { key: k, storageKey };
}

export function clearKey(storageKey) {
  localStorage.removeItem(storageKey);
}
