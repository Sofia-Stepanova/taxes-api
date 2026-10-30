package com.sofiapi.taxes_api.domain;

import lombok.Data;

@Data
public class Income {
    private int income;

    public void sumIncome(int amount) {
        this.income += amount;
    }

    }
