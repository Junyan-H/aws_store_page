public class Actor {
    private final String name;
    private final String dob;

    public Actor(String name, String dob){
        this.name = name;
        this.dob = dob;
    }

    public String getName() {
        return name;
    }

    public String getDob() { return dob; }

    public String toString() {
        return "Name: " + getName() + "; " + "DOB: " + getDob();
    }
}