package com.sofiapi.taxes_api.domain;

public record Balance(int income, int charge) {
    public int profit() {
        return income - charge;
    }
}