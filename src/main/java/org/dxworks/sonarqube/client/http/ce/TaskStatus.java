package org.dxworks.sonarqube.client.http.ce;

public enum TaskStatus {
    SUCCESS, FAILED, CANCELED, PENDING, IN_PROGRESS;

    public boolean isFinished() {
        return this == SUCCESS || this == FAILED || this == CANCELED;
    }

    public boolean isSuccessful() {
        return this == SUCCESS;
    }

    public boolean hasFailed() {
        return !isSuccessful();
    }
}
