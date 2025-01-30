package org.scrapper.zdmik_scrapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class DeparturesController {
    private final TrainDeparturesService trainDeparturesService;
    private final DeparturesService departuresService;

    public DeparturesController(TrainDeparturesService trainDeparturesService, DeparturesService departuresService) {
        this.trainDeparturesService = trainDeparturesService;
        this.departuresService = departuresService;
    }
    @GetMapping("/departures")
    public Flux<Departures> getDepartures(@RequestParam String stopId){
        return departuresService.getDepartures(stopId);
    }
    @GetMapping("/departures/multiple")
    public Flux<Departures> getDeparturesForMultipleStops(@RequestParam List<String> stopIds) {
        return departuresService.getDeparturesForStops(stopIds);
    }
    @GetMapping("/train/departures")
    public Flux<TrainDeparture> getTrainDepartures(@RequestParam String station) {
        return trainDeparturesService.getTrainDepartures(station);
    }
    @GetMapping("/train/departures/multiple")
    public Flux<TrainDeparture> getTrainDeparturesForMultipleStations(@RequestParam List<String> stations) {
        return trainDeparturesService.getTrainDeparturesForStations(stations);
    }

}
