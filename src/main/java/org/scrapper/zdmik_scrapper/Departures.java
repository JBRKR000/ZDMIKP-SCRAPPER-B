package org.scrapper.zdmik_scrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Departures {
    private String line;
    private String destination;
    private String departureTime;
}
