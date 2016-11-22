package model.times;

import de.unihd.dbs.uima.types.heideltime.Timex3Interval;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import model.times.UnsureDateWeightConfiguration;

public class HeidelIntervalString {
    private int begin;
    private int end;
    private Timex3Interval timex3interval;
    private String stringRepresentation;
    private String completeIntervalStringRepresentation;
    private Date startTime;
    private Date endTime;
    private Double weight;
    private int sentenceNumber;
    private String coveredText;

    public HeidelIntervalString(Timex3Interval timex3interval, String stringRepresentation) {
        this.begin = timex3interval.getBegin();
        this.end = timex3interval.getEnd();
        this.timex3interval = timex3interval;
        this.stringRepresentation = stringRepresentation;
    }

    public HeidelIntervalString(String completeIntervalStringRepresentation, Date startTime, Date endTime) {
        this.completeIntervalStringRepresentation = completeIntervalStringRepresentation;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getBegin() {
        return this.begin;
    }

    public int getEnd() {
        return this.end;
    }

    public String getStringRepresentation() {
        return this.stringRepresentation;
    }

    public Timex3Interval getTimex3interval() {
        return this.timex3interval;
    }

    public String getCompleteIntervalStringRepresentation() {
        return this.completeIntervalStringRepresentation;
    }

    public void setCompleteIntervalStringRepresentation(String completeIntervalStringRepresentation) {
        this.completeIntervalStringRepresentation = completeIntervalStringRepresentation;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public double getWeight() {
        if (this.weight == null) {
            this.weight = 0.5;
            long days = TimeUnit.DAYS.convert(this.endTime.getTime() - this.startTime.getTime(), TimeUnit.MILLISECONDS);
            this.weight = UnsureDateWeightConfiguration.getInstance().getWeight(days, false);
        }
        return this.weight;
    }

    public void resetWeight() {
        this.weight = null;
    }

    public int getSentenceNumber() {
        return this.sentenceNumber;
    }

    public void setSentenceNumber(int sentenceNumber) {
        this.sentenceNumber = sentenceNumber;
    }

    public String getCoveredText() {
        return this.coveredText;
    }

    public void setCoveredText(String coveredText) {
        this.coveredText = coveredText;
    }
}

