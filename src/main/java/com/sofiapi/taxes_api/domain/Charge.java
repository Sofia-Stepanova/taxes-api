package com.sofiapi.taxes_api.domain;

import lombok.Data;

@Data
public class Charge {
    private int charge;

    public void sumCharge(int amount) {
        this.charge += amount;
    }
}
