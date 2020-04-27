package org.dxworks.sonarqube.client.main;

import lombok.SneakyThrows;
import org.dxworks.sonarqube.client.http.issue.Issue;
import org.dxworks.sonarqube.client.http.issue.SonarIssueService;
import org.loose.fis.project.proofing.tool.utils.JsonMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final JsonMapper jsonMapper = new JsonMapper();

	@SneakyThrows
	public static void main(String[] args) {
		Properties properties = getProperties();
		Period period = getPeriod(properties);
		Path pathToOutput = Paths.get(properties.getProperty("output.path"));
		Optional<String> baseUrl = Optional.ofNullable(properties.getProperty("sonar.url"));
		List<ProjectInput> projectInputs = getProjectInputs(properties);
		Profile profile = readProfile(properties);

		List<Issue> issues = new SonarIssueService(baseUrl.orElse("http://localhost:9000"))
				.getAllIssuesAndComponentsForProjects(
						projectInputs.stream().map(ProjectInput::getKey).collect(Collectors.toList()));

		Results results = new ResultsGenerator(issues, projectInputs, period).getResults(profile);
		pathToOutput.toFile().mkdirs();
		File openFile = pathToOutput.resolve("open.json").toFile();
		File closedFile = pathToOutput.resolve("closed.json").toFile();
		jsonMapper.writeJSON(new FileWriter(openFile), results.getOpen());
		jsonMapper.writeJSON(new FileWriter(closedFile), results.getClosed());
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

	private static Period getPeriod(Properties properties) {
		String startDate = properties.getProperty("period.start");
		String endDate = properties.getProperty("period.end");
		if (startDate == null || endDate == null)
			return null;
		LocalDate start = LocalDate.parse(startDate, dateFormatter);
		LocalDate end = LocalDate.parse(endDate, dateFormatter).plusDays(1);
		return Period.builder().start(start).end(end).build();
	}

	@SneakyThrows
	private static Properties getProperties() {
		Properties properties = new Properties();
		File file = Paths.get("./conf.properties").toFile();
		properties.load(new FileInputStream(file));
		return properties;
	}
}
