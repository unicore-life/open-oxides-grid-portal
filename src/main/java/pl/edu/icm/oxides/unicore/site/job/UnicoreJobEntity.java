package pl.edu.icm.oxides.unicore.site.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UnicoreJobEntity implements Serializable {
    private final String uri;
    private final String fullName;
    private final String status;
    private final Calendar submissionTime;
    private final String queue;

    public UnicoreJobEntity(EndpointReferenceType uri,
                            String name,
                            String status,
                            Calendar submissionTime,
                            String queue) {
        this.fullName = name;
        this.status = status;
        this.submissionTime = submissionTime;
        this.queue = queue;
        this.uri = uri.getAddress().getStringValue();
    }

    public String getUri() {
        return uri;
    }

    public String getUuid() {
        // FIXME: remove it - redundant with uri
        return uri.substring(uri.length() - 36);
    }

    public String getName() {
        return fullName.substring("_OpenOxides__".length());
    }

    public String getStatus() {
        return status;
    }

    public String getSubmissionTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(submissionTime.getTimeZone());
        return dateFormat.format(submissionTime.getTime());
    }

    public String getQueue() {
        return queue;
    }

    @JsonIgnore
    public String getFullName() {
        return fullName;
    }

    @JsonIgnore
    public long getTimestamp() {
        return submissionTime.getTimeInMillis();
    }

    @JsonIgnore
    public EndpointReferenceType getEpr() {
        EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
        epr.addNewAddress().setStringValue(this.uri);
        return epr;
    }
}
