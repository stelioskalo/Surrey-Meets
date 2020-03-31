package sep_group_7.SurreyMeets;

import java.util.Random;
import java.util.UUID;

/**
 * @author Rizwan
 *
 * Random Number Generator class to generate random numbers without duplicate methods in each activity
 */
public class RNG {
    /**
     * empty constructor
     */
    public RNG(){
        super();
    }

    /**
     * static method to allow users to instantiate method without object.
     * this method is used to generate a random number to set a value in firebase to allow us to change/view data
     * @return: a random number
     */
    public static int generateNumber(){
        Random rand = new Random();
        int num = rand.nextInt(1000000) + 1;
        return num;
    }

    /**
     * static method to allow users to instantiate method without object.
     * this method is used to generate a random id string
     * @return: a random id string
     */
    public static String generateUID(){
        return UUID.randomUUID().toString();
    }
}
