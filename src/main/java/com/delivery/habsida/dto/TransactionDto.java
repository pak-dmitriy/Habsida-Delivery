package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Transaction;
import com.delivery.habsida.entity.TransactionStatus;

import java.math.BigDecimal;

public record TransactionDto(
        Long id,
        TransactionStatus status,
        BigDecimal amount
) {
    public static TransactionDto from(Transaction transaction) {
      return new TransactionDto(
              transaction.getId(),
        transaction.getStatus(),
        transaction.getAmount()
      );
    }
}
