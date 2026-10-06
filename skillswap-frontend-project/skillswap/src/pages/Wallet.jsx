import { useCallback, useEffect, useState } from "react";

import api, { getErrorMessage } from "../api/axios";

import { useAuth } from "../context/useAuth";

import { formatCredits, formatDateTime } from "../utils/formatters";

const Wallet = () => {
  const { refreshCredits } = useAuth();

  const [transactions, setTransactions] = useState([]);
  const [balance, setBalance] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadWallet = useCallback(async () => {
    try {
      setError("");

      // Get wallet information from backend
      const walletResponse = await api.get("/wallet");

      // Backend response:
      // {
      //   balance: 100,
      //   transactions: [...]
      // }

      setBalance(walletResponse.data?.balance ?? 0);

      setTransactions(
        Array.isArray(walletResponse.data?.transactions)
          ? walletResponse.data.transactions
          : [],
      );

      // Refresh credits in AuthContext
      if (refreshCredits) {
        await refreshCredits();
      }
    } catch (err) {
      console.error("Failed to load wallet:", err);

      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [refreshCredits]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadWallet();
    }, 0);

    return () => clearTimeout(timer);
  }, [loadWallet]);

  if (loading) {
    return (
      <div className="page">
        <p>Loading wallet...</p>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Wallet</h1>

        <p>Manage your SkillSwap credits and view transaction history.</p>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="wallet-balance">
        <h2>Current Balance</h2>

        <div className="credit-badge">{formatCredits(balance)}</div>
      </div>

      <div className="transaction-list">
        <h2>Transaction History</h2>

        {transactions.length === 0 ? (
          <div className="empty-state">
            <h3>No transactions yet</h3>

            <p>Your credit transactions will appear here.</p>
          </div>
        ) : (
          transactions.map((transaction) => {
            const amount = Number(transaction.amount ?? 0);

            const isPositive = amount > 0 || transaction.type === "EARN";

            return (
              <div className="transaction-item" key={transaction.id}>
                <div>
                  <strong>
                    {transaction.description ||
                      transaction.type ||
                      "Transaction"}
                  </strong>

                  <p className="muted">
                    {formatDateTime(transaction.createdAt)}
                  </p>
                </div>

                <span
                  className={isPositive ? "credit-positive" : "credit-negative"}
                >
                  {isPositive ? "+" : ""}

                  {formatCredits(Math.abs(amount))}
                </span>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default Wallet;
