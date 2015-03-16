package models;

/**
 * Structure that keeps track of a value (on the key-value system) and its corresponding timestamp, a long-type number
 * that makes reference to the exact moment the value was updated.
 *
 * @author Bruno, Cassio, Marco
 * @version 1.0
 */
public class ValueAndTimeStamp {

    private Integer value;
    private Long timeStamp;

    /**
     * Creates a new object that contains an integer value and its timestamp.
     *
     * @param value     an integer corresponding to the current value of a key.
     * @param timeStamp a long-type number corresponding to the timestamp of the last update.
     */
    public ValueAndTimeStamp(Integer value, Long timeStamp) {
        this.value = value;
        this.timeStamp = timeStamp;
    }

    /**
     * Gets the value stored in this object.
     *
     * @return an integer corresponding to a value of a key.
     */
    public Integer getValue() {
        return value;
    }

    /**
     * Gets the timestamp of this value's last update.
     *
     * @return a long number corresponding to the last update's instant.
     */
    public Long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets a new value for this object.
     *
     * @param value the new value that the key must receive.
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * Sets a new timestamp for this object, subject to change along with its value.
     *
     * @param timeStamp the new long value corresponding to an updated timestamp.
     */
    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
