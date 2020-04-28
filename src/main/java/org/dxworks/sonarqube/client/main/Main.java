package org.dxworks.sonarqube.client.main;

import lombok.SneakyThrows;
import org.dxworks.sonarqube.client.http.issue.Issue;
import org.dxworks.sonarqube.client.http.issue.SonarIssueService;
import org.dxworks.sonarqube.client.main.input.Period;
import org.dxworks.sonarqube.client.main.input.Profile;
import org.dxworks.sonarqube.client.main.input.ProjectInput;
import org.dxworks.sonarqube.client.main.output.Result;
import org.dxworks.sonarqube.client.main.output.Results;
import org.dxworks.sonarqube.client.main.output.ResultsGenerator;
import org.dxworks.sonarqube.client.main.output.Summary;
import org.dxworks.utils.java.rest.client.utils.JsonMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final JsonMapper jsonMapper = new JsonMapper();
	public static final String DEFAULT_CONFIG_FILE = "./conf.properties";

	public static void main(String[] args) {
		String configFile = getArg(args, "-config=").orElse(DEFAULT_CONFIG_FILE);
		Properties properties = getProperties(configFile);
		Period period = getPeriod(args, properties);
		Path pathToOutput = Paths.get(properties.getProperty("output.path"));
		String outputFilesPrefix = properties.getProperty("output.file.prefix");
		Optional<String> baseUrl = Optional.ofNullable(properties.getProperty("sonar.url"));
		List<ProjectInput> projectInputs = getProjectInputs(properties);
		Profile profile = readProfile(properties);

		List<Issue> issues = new SonarIssueService(baseUrl.orElse("http://localhost:9000"))
				.getAllIssuesAndComponentsForProjects(
						projectInputs.stream().map(ProjectInput::getKey).collect(Collectors.toList()));

		Results results = new ResultsGenerator(issues, projectInputs, period).getResults(profile);
		pathToOutput.toFile().mkdirs();
		writeOutput(pathToOutput, outputFilesPrefix, results);
	}

	private static Optional<String> getArg(String[] args, String argName) {
		return Stream.of(args).filter(arg -> arg.startsWith(argName)).findFirst()
				.map(arg -> arg.substring(argName.length()));
	}

	@SneakyThrows
	private static void writeOutput(Path pathToOutput, String outputFilesPrefix, Results results) {
		File openFile = pathToOutput.resolve(getPrefixedFilename(outputFilesPrefix, "open.json")).toFile();
		File closedFile = pathToOutput.resolve(getPrefixedFilename(outputFilesPrefix, "closed.json")).toFile();
		jsonMapper.writeJSON(new FileWriter(openFile), results.getOpen());
		jsonMapper.writeJSON(new FileWriter(closedFile), results.getClosed());
		printSummary(pathToOutput, outputFilesPrefix, results);
	}

	@SneakyThrows
	private static void printSummary(Path pathToOutput, String outputFilesPrefix, Results results) {
		File summaryFile = pathToOutput.resolve(getPrefixedFilename(outputFilesPrefix, "summary.json")).toFile();
		jsonMapper.writeJSON(new FileWriter(summaryFile),
				Summary.builder().openEffort(getSumOfValues(results.getOpen()))
						.closedEffort(getSumOfValues(results.getClosed())).build());
	}

	private static long getSumOfValues(List<Result> resultList) {
		return resultList.stream().mapToLong(Result::getValue).sum();
	}

	private static String getPrefixedFilename(String prefix, String name) {
		return Stream.concat(Stream.of(prefix).filter(Objects::nonNull), Stream.of(name))
				.collect(Collectors.joining("-"));
	}

	@SneakyThrows
	private static Profile readProfile(Properties properties) {
		Path pathToProfiles = Paths.get(properties.getProperty("profiles.path"));
		return jsonMapper.readJSONfromFile(pathToProfiles.toFile(), Profile.class);
	}

	private static List<ProjectInput> getProjectInputs(Properties properties) {
		String projectInputsString = properties.getProperty("projects");
		boolean prefixOutputs = Boolean.parseBoolean(properties.getProperty("prefixOutput"));

		List<String> projectInputsList = Arrays.asList(projectInputsString.split(","));
		return projectInputsList.stream().map(inputString -> {
			List<String> keyAndPrefix = new ArrayList<>(Arrays.asList(inputString.split(">")));
			String key = keyAndPrefix.remove(0);
			String prefix = prefixOutputs ? keyAndPrefix.stream().findFirst().orElse(null) : null;
			return ProjectInput.builder().key(key).prefix(prefix).build();
		}).collect(Collectors.toList());
	}

	private static Period getPeriod(String[] args, Properties properties) {
		String argName = "-period=";
		Optional<String> datesString = getArg(args, argName);
		String startDate = datesString.map(it -> getDate(it, 0)).orElse(properties.getProperty("period.start"));
		String endDate = datesString.map(it -> getDate(it, 1)).orElse(properties.getProperty("period.end"));
		if (startDate == null || endDate == null)
			return null;
		LocalDate start = LocalDate.parse(startDate, dateFormatter);
		LocalDate end = LocalDate.parse(endDate, dateFormatter).plusDays(1);
		return Period.builder().start(start).end(end).build();
	}

	private static String getDate(String datesString, int index) {
		try {
			return datesString.split(":")[index];
		} catch (Exception e) {
			System.out.println("Wrong period format: " + datesString);
		}
		return null;
	}

	@SneakyThrows
	private static Properties getProperties(String configFile) {
		Properties properties = new Properties();
		File file = Paths.get(configFile).toFile();
		properties.load(new FileInputStream(file));
		return properties;
	}
}
