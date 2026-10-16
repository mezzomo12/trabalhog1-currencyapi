package br.edu.atitus.currencyapi.servicesimpl;

import br.edu.atitus.currencyapi.dtos.CurrencyResponse;
import br.edu.atitus.currencyapi.repositories.CurrencyRepository;
import br.edu.atitus.currencyapi.services.CurrencyService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceJpa implements CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyServiceJpa.class);

    private final CurrencyRepository repository;
    private final BcbClientService bcbClientService;

    public CurrencyServiceJpa(CurrencyRepository repository, BcbClientService bcbClientService) {
        this.repository = repository;
        this.bcbClientService = bcbClientService;
    }

    @Value("${server.port:8100}")
    private String serverPort;

    @Value("${app.bcb.quotation-date:10-15-2026}")
    private String quotationDate;

    @Override
    public CurrencyResponse findBySourceCurrencyAndTargetCurrency(
            String sourceCurrency,
            String targetCurrency
    ) throws Exception {
        String environment = "Currency API running in Port: " + serverPort;

        Double rate = findRateInBcb(sourceCurrency, targetCurrency);
        if (rate != null) {
            log.info("Cotação {}->{} obtida do BCB: {}", sourceCurrency, targetCurrency, rate);
            return new CurrencyResponse(sourceCurrency, targetCurrency, rate, environment);
        }

        log.info("Usando fallback local para {}->{}", sourceCurrency, targetCurrency);
        var currency = repository.findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency)
                .orElseThrow(() -> new EntityNotFoundException("Cotação não encontrada"));

        return new CurrencyResponse(
                currency.getSourceCurrency(),
                currency.getTargetCurrency(),
                currency.getConversionRate(),
                environment
        );
    }

    private Double findRateInBcb(String sourceCurrency, String targetCurrency) {
        try {
            boolean sourceIsBrl = "BRL".equalsIgnoreCase(sourceCurrency);
            boolean targetIsBrl = "BRL".equalsIgnoreCase(targetCurrency);

            if (sourceIsBrl == targetIsBrl) {
                return null;
            }

            String foreignCurrency = (targetIsBrl ? sourceCurrency : targetCurrency).toUpperCase();
            var response = bcbClientService.getQuotation(foreignCurrency, quotationDate);

            if (response == null || response.value() == null || response.value().isEmpty()) {
                return null;
            }

            var quotation = response.value().get(response.value().size() - 1);
            Double sellRate = quotation.cotacaoVenda();
            if (sellRate == null || sellRate <= 0) {
                return null;
            }

            return targetIsBrl ? sellRate : 1 / sellRate;
        } catch (Exception e) {
            log.warn("Falha ao consultar o BCB: {}", e.toString());
            return null;
        }
    }
}