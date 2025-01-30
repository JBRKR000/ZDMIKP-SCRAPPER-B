
package org.scrapper.zdmik_scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class DeparturesService {
    private final WebClient webClient;

    public DeparturesService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://odjazdy.zdmikp.bydgoszcz.pl")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    public Flux<Departures> getDepartures(String stopId) {
        String URL = "/mobile/panel.aspx?stop=" + stopId;

        return webClient
                .get()
                .uri(URL)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(this::parseDepartures)
                .onErrorResume(e -> Flux.empty()); // Reactive error handling
    }

    private Flux<Departures> parseDepartures(String html) {
        return Flux.defer(() -> {
            Document document = Jsoup.parse(html);
            Elements rows = document.select("table.tablePanel tbody tr");
            return Flux.fromStream(rows.stream().map(row -> {
                String line = row.select("td:nth-child(1)").text().trim();
                String destination = row.select("td:nth-child(2)").text().trim();
                String time = row.select("td:nth-child(3)").text().trim();
                return new Departures(line, destination, time);
            }));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<Departures> getDeparturesForStops(List<String> stopIds) {
        return Flux.fromIterable(stopIds)
                .flatMap(this::getDepartures)
                .onErrorResume(e -> Flux.empty());
    }
}