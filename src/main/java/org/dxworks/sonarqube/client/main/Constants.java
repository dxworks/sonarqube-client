package org.dxworks.sonarqube.client.main;

import java.time.ZoneId;

public interface Constants {
    String DEFAULT_SONAR_URL = "http://localhost:9000";
    ZoneId ZERO_ZONE_ID = ZoneId.of("Z");

    interface Environment {
        String SONAR_CLIENT_OUTPUT_PATH = "SONAR_CLIENT_OUTPUT_PATH";
        String SONAR_CLIENT_PROFILES_PATH = "SONAR_CLIENT_PROFILES_PATH";
        String SONAR_CLIENT_PROJECTS = "SONAR_CLIENT_PROJECTS";
        String SONAR_CLIENT_SONAR_URL = "SONAR_CLIENT_SONAR_URL";
        String SONAR_CLIENT_PREFIX_OUTPUT = "SONAR_CLIENT_PREFIX_OUTPUT";
        String SONAR_CLIENT_TASKS = "SONAR_CLIENT_TASKS";
    }

    interface ConfigFile {
        String OUTPUT_PATH = "output.path";
        String OUTPUT_FILE_PREFIX = "output.file.prefix";
        String TASKS = "tasks";
        String SONAR_URL = "sonar.url";
        String PROJECTS = "projects";
        String PERIOD_START = "period.start";
        String PERIOD_END = "period.end";
        String PROFILES_PATH = "profiles.path";
        String AUTHENTICATION_TYPE = "authentication.type";
        String AUTHENTICATION_USERNAME = "authentication.username";
        String AUTHENTICATION_PASSWORD = "authentication.password";
        String AUTHENTICATION_COOKIE = "authentication.cookie";
        String AUTHENTICATION_BEARER_TOKEN = "authentication.bearerToken";
    }

    interface Arguments {
        String PERIOD = "period";
        String CONFIG = "config";
    }

    interface AuthenticationType {
        String NONE = "none";
        String BASIC = "basic";
        String COOKIE = "cookie";
        String BEARER = "bearer";
    }
}
