import java.util.ArrayList;
import java.util.Set;

public class Cast {
    private final String id;
    private final String title;
    private final Set<String> actor;

    public Cast(String id, String title, Set<String> actor){
        // params
        this.id = id;
        this.title = title;
        this.actor = actor;
    }
    public String getID(){return id;}
    public String getTitle(){return title;}
    public Set<String> getActor(){return actor;}


    public String toString(){
        String actors = "";
        for(String actorStr: getActor()){
            actors += actorStr + ", ";
        }
        return  "ID: "+ getID() +", " +
                "Title: " + getTitle() + ", " +
                "Actors: " + actors;
    }


}