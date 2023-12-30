import java.util.ArrayList;

public class Movie {
    private final String id;
    private final String title;
    private final String year;
    private final String director;
    private final ArrayList<String> genre;

    public Movie(String id, String title, String year, String director, ArrayList<String> genre){
        //required
        this.title = title;
        //required
        this.year = year;
        //required
        this.director = director;
        //required
        this.genre = genre;
        //required
        this.id = id;
    }

    public String getID(){return id;}
    public String getTitle(){return title;}
    public String getYear(){return year;}
    public String getDirector(){return director;}
    public ArrayList<String> getGenre(){return genre;}

    public String toString(){
        String genres = "";
        for(int i = 0; i < getGenre().size(); i++ ){
            genres += getGenre().get(i) + " ";
        }
        return  "ID: " + getID() + "; " +
                "Title: " + getTitle() + "; " +
                "Director: " + getDirector() + "; " +
                "Year: " + getYear() + "; " +
                "Genre: " + genres;
    }
}