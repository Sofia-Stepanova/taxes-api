package com.sofiapi.taxes_api.dto;

import com.sofiapi.taxes_api.domain.TaxResult;

public record TaxResponse(String system, String label, int tax) {
    public static TaxResponse from(TaxResult r) {
        return new TaxResponse(r.system().name(), r.system().label(), r.amount());
    }
}
