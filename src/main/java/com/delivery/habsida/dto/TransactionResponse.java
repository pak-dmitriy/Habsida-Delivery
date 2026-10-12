package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Transaction;
import com.delivery.habsida.entity.TransactionStatus;

import java.math.BigDecimal;

public record TransactionResponse(
        Long id,
        TransactionStatus status,
        BigDecimal amount
) {
    public static TransactionResponse from(Transaction transaction) {
      return new TransactionResponse(
              transaction.getId(),
        transaction.getStatus(),
        transaction.getAmount()
      );
    }
}
