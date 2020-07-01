package org.dxworks.sonarqube.client.main.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
public class Period {
    private ZonedDateTime start;
    private ZonedDateTime end;

    public boolean contains(ZonedDateTime date) {
        if (start == null && end == null) {
            return true;
        }
        if (start == null) {
            return isBeforeOrEqualToEnd(date);
        }
        if (end == null) {
            return start.isBefore(date);
        }
        return start.isBefore(date) && isBeforeOrEqualToEnd(date);
    }

    private boolean isBeforeOrEqualToEnd(ZonedDateTime date) {
        return end.isAfter(date) || end.isEqual(date);
    }
}
