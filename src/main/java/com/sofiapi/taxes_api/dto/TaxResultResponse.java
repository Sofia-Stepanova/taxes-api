package com.sofiapi.taxes_api.dto;

import com.sofiapi.taxes_api.domain.TaxResult;

public record TaxResultResponse(String system, int amount) {
    public static TaxResultResponse from(TaxResult result) {
        return new TaxResultResponse(result.system().name(), result.amount()); //фронту здесь проще работать со строкой, чем с объектом
    }
}
