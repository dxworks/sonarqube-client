package org.dxworks.sonarqube.client.main;

import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.dxworks.sonarqube.client.main.input.Period;
import org.dxworks.sonarqube.client.main.input.Profile;
import org.dxworks.sonarqube.client.main.input.ProjectInput;
import org.dxworks.utils.java.rest.client.utils.JsonMapper;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.dxworks.sonarqube.client.main.Constants.Arguments.CONFIG;
import static org.dxworks.sonarqube.client.main.Constants.Arguments.PERIOD;
import static org.dxworks.sonarqube.client.main.Constants.ConfigFile.*;
import static org.dxworks.sonarqube.client.main.Constants.DEFAULT_SONAR_URL;
import static org.dxworks.sonarqube.client.main.Constants.Environment.*;
import static org.dxworks.sonarqube.client.main.Constants.ZERO_ZONE_ID;

@Data
public class SonarClientConfig {
    private static final String DEFAULT_CONFIG_FILE = "./config/config.properties";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String configFile;
    private Period period;
    private Path pathToOutput;
    private String outputFilesPrefix;
    private List<ProjectInput> projectInputs;
    private Profile profile;
    private List<String> tasks;
    private String baseUrl;

    public SonarClientConfig(String... args) {
        initialize(args);
    }

    @SneakyThrows
    private Properties loadProperties(String... args) {
        String configFile = getArg(args, CONFIG).orElse(DEFAULT_CONFIG_FILE);

        Properties properties = new Properties();
        loadEnvironmentVariables(properties);
        Path path = Paths.get(configFile);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            properties.load(new FileInputStream(path.toFile()));
        } else {
            System.out.println("WARNING: configuration file not found!");
        }
        return properties;
    }

    private void loadEnvironmentVariables(Properties properties) {
        replaceWithEnvironmentVariable(properties, SONAR_CLIENT_OUTPUT_PATH, OUTPUT_PATH);
        replaceWithEnvironmentVariable(properties, SONAR_CLIENT_SONAR_URL, SONAR_URL);
        replaceWithEnvironmentVariable(properties, SONAR_CLIENT_PREFIX_OUTPUT, OUTPUT_FILE_PREFIX);
        replaceWithEnvironmentVariable(properties, SONAR_CLIENT_PROFILES_PATH, PROFILES_PATH);
        replaceWithEnvironmentVariable(properties, SONAR_CLIENT_PROJECTS, PROJECTS);
    }

    private void replaceWithEnvironmentVariable(Properties properties, String envVariableName, String propertyName) {
        String envValue = System.getenv(envVariableName);
        if (envValue != null)
            properties.setProperty(propertyName, envValue);
    }

    public void initialize(String... args) {
        Properties properties = loadProperties(args);

        period = getPeriod(args, properties);
        pathToOutput = Paths.get(properties.getProperty(OUTPUT_PATH));
        outputFilesPrefix = properties.getProperty(OUTPUT_FILE_PREFIX);
        projectInputs = getProjectInputs(properties);
        profile = readProfile(properties);

        tasks = Arrays.stream(getArg(args, TASKS).orElse("").split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        Optional<String> baseUrlOption = Optional.ofNullable(properties.getProperty(SONAR_URL));
        baseUrl = baseUrlOption.orElse(DEFAULT_SONAR_URL);
    }

    private Optional<String> getArg(String[] args, String argName) {
        return Stream.of(args).filter(arg -> arg.startsWith(String.format("-%s=", argName))).findFirst()
                .map(arg -> arg.substring(argName.length() + 2));
    }

    private String getDate(String datesString, int index) {
        try {
            return datesString.split(":")[index];
        } catch (Exception e) {
            System.out.println("Wrong period format: " + datesString);
        }
        return null;
    }

    private List<ProjectInput> getProjectInputs(Properties properties) {
        String projectInputsString = properties.getProperty(PROJECTS);

        List<String> projectInputsList = Arrays.asList(projectInputsString.split(","));
        return projectInputsList.stream()
                .map(inputString -> {
                    List<String> keyAndPrefix = new ArrayList<>(Arrays.asList(inputString.split(">")));
                    String key = keyAndPrefix.remove(0);
                    String prefix = keyAndPrefix.stream().findFirst().orElse(null);
                    return ProjectInput.builder().key(key).prefix(prefix).build();
                })
                .collect(Collectors.toList());
    }

    private Period getPeriod(String[] args, Properties properties) {
        Optional<String> datesString = getArg(args, PERIOD);
        String startDate = datesString.map(it -> getDate(it, 0)).orElse(properties.getProperty(PERIOD_START));
        String endDate = datesString.map(it -> getDate(it, 1)).orElse(properties.getProperty(PERIOD_END));
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Start date or end date not provided correctly!");

        ZonedDateTime start = LocalDate.parse(startDate, dateFormatter).atStartOfDay(ZERO_ZONE_ID);
        ZonedDateTime end = LocalDate.parse(endDate, dateFormatter).atTime(LocalTime.MAX).atZone(ZERO_ZONE_ID);

        if (!start.isBefore(end))
            throw new IllegalArgumentException("Start date is after end date!");

        return Period.builder().start(start).end(end).build();
    }

    @SneakyThrows
    private Profile readProfile(Properties properties) {
        Path pathToProfiles = Paths.get(properties.getProperty(PROFILES_PATH));
        return new JsonMapper().readJSONfromFile(pathToProfiles.toFile(), Profile.class);
    }
}