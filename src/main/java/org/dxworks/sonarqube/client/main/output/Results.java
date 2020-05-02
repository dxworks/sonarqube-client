package org.dxworks.sonarqube.client.main.output;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Results {
    private List<Result> open;
    private List<Result> closed;
    private Long totalEffort;
}
