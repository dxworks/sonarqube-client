package org.dxworks.sonarqube.client.main.input;

import com.google.api.client.util.Key;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class Profile {
    @Key
    private String category;
    @Key
    private List<Axis> axes;

    public List<String> getAllRules() {
        return axes.stream()
                .flatMap(axis -> axis.getRules().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
