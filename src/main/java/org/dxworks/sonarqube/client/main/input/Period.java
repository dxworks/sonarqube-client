package org.dxworks.sonarqube.client.main.input;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder
public class Period {
	private LocalDate start;
	private LocalDate end;

	public boolean contains(ZonedDateTime date) {
		LocalDate localDate = date.toLocalDate();
		return start.isBefore(localDate) && localDate.isBefore(end);
	}
}
