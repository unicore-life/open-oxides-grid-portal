package pl.edu.icm.oxides.unicore.site.job;

public class UnicoreJobDetailsEntity {
    private final String uri;
    private final String name;
    private final String log;
    private final String submissionTime;
    private final String terminationTime;

    public UnicoreJobDetailsEntity(String uri, String name, String log,
                                   String submissionTime, String terminationTime) {
        this.uri = uri;
        this.name = name;
        this.log = log;
        this.submissionTime = submissionTime;
        this.terminationTime = terminationTime;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getLog() {
        return log;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public String getTerminationTime() {
        return terminationTime;
    }
}
