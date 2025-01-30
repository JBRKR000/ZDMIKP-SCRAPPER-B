package org.scrapper.zdmik_scrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainDeparture {
    private String departureTime;
    private String trainNumber;
    private String destination;
}
