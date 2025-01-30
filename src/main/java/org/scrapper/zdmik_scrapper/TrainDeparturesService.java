package org.scrapper.zdmik_scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TrainDeparturesService {
    private final WebClient webClient;

    public TrainDeparturesService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://rozklad-pkp.pl")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    public Flux<TrainDeparture> getTrainDepartures(String station) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("dd.MM.yy"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        String URL = "/pl/sq?maxJourneys=10&input=" + station +
                "&REQStationS0F=excludeStationAttribute%3BM-" +
                "&disableEquivs=yes" +
                "&date=" + date + "&dateStart=" + date +
                "&REQ0JourneyDate=" + date +
                "&time=" + time + "&boardType=dep" +
                "&GUIREQProduct_0=on&GUIREQProduct_1=on" +
                "&GUIREQProduct_2=on&GUIREQProduct_3=on" +
                "&maxJourneys=10&dateEnd=" + date +
                "&advancedProductMode=&start=#focus";

        return webClient
                .get()
                .uri(URL)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(this::parseTrainDepartures)
                .onErrorResume(e -> Flux.empty()); // Reactive error handling
    }

    private Flux<TrainDeparture> parseTrainDepartures(String html) {
        return Flux.defer(() -> {
            Document document = Jsoup.parse(html);
            Elements rows = document.select("tbody tr.even, tbody tr.odd");

            return Flux.fromStream(rows.stream().map(row -> {
                // Extracting departure time
                Element timeElement = row.selectFirst("td .time");
                String departureTime = timeElement != null ? timeElement.text().trim() : "";

                // Extracting train number
                Element trainElement = row.selectFirst("td.sqResMOT .train-name");
                String trainNumber = trainElement != null ? trainElement.text().trim() : "";

                // Extracting direction (concatenated stations)
                Elements directionElements = row.select("td .bold");
                System.out.println(directionElements);
                String direction = directionElements.eachText().stream()
                        .reduce((a, b) -> a + " -> " + b)
                        .orElse("");

                return new TrainDeparture(departureTime, trainNumber, direction);
            }));
        }).subscribeOn(Schedulers.boundedElastic());
    }


    public Flux<TrainDeparture> getTrainDeparturesForStations(List<String> stations) {
        return Flux.fromIterable(stations)
                .flatMap(this::getTrainDepartures)
                .onErrorResume(e -> Flux.empty());
    }
}
