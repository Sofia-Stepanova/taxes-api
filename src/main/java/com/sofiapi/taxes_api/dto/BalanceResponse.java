package com.sofiapi.taxes_api.dto;

import com.sofiapi.taxes_api.domain.Balance;

public record BalanceResponse(int income, int charge, int profit) {
    public static BalanceResponse from(Balance b) {
        return new BalanceResponse(b.income(), b.charge(), b.profit());
    }
}
