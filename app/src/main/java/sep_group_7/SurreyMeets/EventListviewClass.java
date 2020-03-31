package sep_group_7.SurreyMeets;

/**
 * Created by Stelios on 07/04/2019.
 */

public class EventListviewClass {
    private String name;
    private String category;
    private String eventId;
    private String desc;
    private int current_participants;
    private String dateAndTime;
    private double latitude;
    private double longitude;
    private String owner;
    private int participants;


    public EventListviewClass(){}

    public EventListviewClass(String name, String category, String eventId, String desc, int current_participants, String dateAndTime, double latitude, double longitude, String owner, int participants) {
        this.name = name;
        this.category = category;
        this.eventId = eventId;
        this.desc = desc;
        this.current_participants = current_participants;
        this.dateAndTime = dateAndTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.owner = owner;
        this.participants = participants;
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public String getEventId() {
        return this.eventId;
    }

    public String getDesc() {
        return this.desc;
    }

    public int getCurrent_participants() {
        return this.current_participants;
    }

    public String getDateAndTime() {
        return this.dateAndTime;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getOwner() {
        return this.owner;
    }

    public int getParticipants() {
        return this.participants;
    }
}
