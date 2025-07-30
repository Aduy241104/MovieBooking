import axios from "axios";

const BASE_URL = "http://localhost:8081/api/admin/payment-transactions";

const PaymentTransactionService = {
    getAllTransactions: () => axios.get(BASE_URL),
};

export default PaymentTransactionService;
