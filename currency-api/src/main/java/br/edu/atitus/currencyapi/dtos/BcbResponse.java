package br.edu.atitus.currencyapi.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcbResponse(List<Quotation> value) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Quotation(
            Double cotacaoCompra,
            Double cotacaoVenda,
            String dataHoraCotacao
    ) {
    }
}
