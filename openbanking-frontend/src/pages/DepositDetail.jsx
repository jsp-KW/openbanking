import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axiosInstance from "../api/axiosInstance";

export default function DepositDetail() {
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const res = await axiosInstance.get(`/deposit/products/${productId}`);
        setProduct(res.data);
      } catch (err) {
        console.error("상품 상세 조회 실패:", err);
      }
    };

    fetchDetail();
  }, [productId]);

  if (!product) {
    return (
      <div className="flex justify-center items-center h-screen text-xl">
        불러오는 중...
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto p-4">
      <h1 className="text-2xl font-bold">{product.name}</h1>

      <div className="mt-4 space-y-2 text-gray-700">
        <p>이자율: {product.interestRate}%</p>
        <p>이자 방식: {product.interestType}</p>
        <p>예치 기간: {product.periodMonths}개월</p>
        <p>최소 금액: {product.minAmount.toLocaleString()}원</p>
      </div>

      <button
        className="mt-6 bg-blue-600 text-white px-4 py-2 rounded"
        onClick={() => navigate(`/deposits/${product.id}/subscribe`)}
      >
        가입하기
      </button>
    </div>
  );
}
