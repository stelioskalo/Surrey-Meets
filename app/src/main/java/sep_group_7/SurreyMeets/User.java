package sep_group_7.SurreyMeets;

/**
 * User.java
 *
 * This class is used with other actions to retrieve the user information from firebase
 *
 * @author Rizwan
 */
public class User {
    //name of user
    private String name = null;

    //age of user.
    // NOTE: this is stored as a string but when the user registers or changes this,
    // the input is for integer values only
    private String age = null;

    //bio of user
    private String bio = null;
    private String image = null;

    //email of user
    private String email = null;
    private double latitude = 0;
    private double longitude = 0;
    private String id = null;
    private int events_joined = 0;
    private int events_hosted = 0;

    /**
     * empty default constructor
     */
    public User(){
        super();
    }

    /**
     * Parameterised constructor
     * @param name: name of user
     * @param age: age of user
     * @param bio: bio of user
     * @param email: email of user
     */
    public User(String name, String age, String bio, String email, double latitude, double longitude, String id, int events_joined, int events_hosted, String image) {
        super();
        this.name = name;
        this.age = age;
        this.bio = bio;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.events_joined = events_joined;
        this.events_hosted = events_hosted;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getId() {
        return this.id;
    }

    public int getEvents_joined() {
        return this.events_joined;
    }

    public int getEvents_hosted() {
        return this.events_hosted;
    }

    public String getImage() {return this.image;}
}
