package models;

/**
 * Created by Bruno on 3/15/2015.
 */
public class ValueAndTimeStamp {

    Integer value;
    Long timeStamp;

    public ValueAndTimeStamp(Integer value, Long timeStamp) {
        this.value = value;
        this.timeStamp = timeStamp;
    }

    public Integer getValue() {
        return value;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
