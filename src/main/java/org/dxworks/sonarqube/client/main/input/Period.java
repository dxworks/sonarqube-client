package org.dxworks.sonarqube.client.main.input;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class Period {
    private ZonedDateTime start;
    private ZonedDateTime end;

    public boolean contains(ZonedDateTime date) {
        return start.isBefore(date) && date.isBefore(end);
    }
}
