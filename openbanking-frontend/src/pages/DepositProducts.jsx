import { useEffect, useState } from "react";
import axiosInstance from "../api/axiosInstance";
import { useNavigate } from "react-router-dom";

export default function DepositProducts() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const res = await axiosInstance.get("/deposit/products");
        setProducts(res.data);
      } catch (err) {
        console.error("상품 조회 실패:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen text-xl">
        상품 불러오는 중...
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">예금 상품 목록</h1>

      {products.length === 0 && (
        <div className="text-gray-500">등록된 상품이 없습니다.</div>
      )}

      <div className="space-y-4">
        {products.map((p) => (
          <div
            key={p.id}
            className="border p-4 rounded-md shadow-sm hover:shadow cursor-pointer"
            onClick={() => navigate(`/deposits/${p.id}`)}
          >
            <h2 className="text-xl font-semibold">{p.name}</h2>

            <div className="mt-2 text-sm text-gray-700">
              <p>이자율: {p.interestRate}%</p>
              <p>이자 방식: {p.interestType}</p>
              <p>예치 기간: {p.periodMonths}개월</p>
              <p>최소가입금액: {p.minAmount.toLocaleString()}원</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
