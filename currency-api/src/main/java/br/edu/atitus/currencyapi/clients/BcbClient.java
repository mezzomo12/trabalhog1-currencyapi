package br.edu.atitus.currencyapi.clients;

import br.edu.atitus.currencyapi.dtos.BcbResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.PathVariable;

@HttpExchange
public interface BcbClient {

    @GetExchange("/olinda/servico/PTAX/versao/v1/odata/CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)?@moeda='{moeda}'&@dataCotacao='{data}'&$format=json")
    BcbResponse getQuotation(
            @PathVariable("moeda") String moeda,
            @PathVariable("data") String data
    );
}
