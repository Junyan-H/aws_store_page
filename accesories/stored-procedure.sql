-- ~~~~~~~~ ADD MOVIE PROCEDURE ~~~~~~~~
DELIMITER //

CREATE PROCEDURE add_movie 
(IN movieTitle VARCHAR(100), IN movieYear INT, IN movieDirector VARCHAR(100),
 IN starName VARCHAR(100), IN starBirthYear INT, 
 IN genreName VARCHAR(32))
BEGIN 
	DECLARE movieExists INT;
	DECLARE starExists INT;
    DECLARE genreExists INT;
    DECLARE newGenreID INT;
    DECLARE starIDToInsert VARCHAR(10);
    DECLARE genreIDToInsert INT;
    
    DECLARE lastStarID VARCHAR(10);
    DECLARE newStarID VARCHAR(10);
    
    DECLARE lastMovieID VARCHAR(10);
	DECLARE newMovieID VARCHAR(10);
    
    SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO lastStarID FROM stars;
    SET newStarID = CONCAT('nm', LPAD(lastStarID + 1, 7, '0'));
    
	SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO lastMovieID FROM movies;
	SET newMovieID = CONCAT('tt', LPAD(lastMovieID + 1, 7, '0'));
    
    SELECT COUNT(*) INTO movieExists FROM movies WHERE title = movieTitle 
		AND year = movieYear AND director = movieDirector;
    SELECT COUNT(*) INTO starExists FROM stars WHERE name = starName;
    SELECT COUNT(*) INTO genreExists FROM genres WHERE name = genreName;
    
    IF movieExists > 0 THEN
		SELECT CONCAT("Error! Movie already exists.") AS answer;
    ELSEIF movieExists = 0 THEN
		INSERT INTO movies VALUES (newMovieID, movieTitle, movieYear, movieDirector);
		-- ~~~~~~~~~~ CASE 1: ~~~~~~~~~~
		-- Star does not exists, create star first
		IF starExists = 0 THEN
            IF starBirthYear IS NULL THEN
				INSERT INTO stars (id, name) VALUES (newStarID, starName);
			ELSE 
				INSERT INTO stars VALUES (newStarID, starName, starBirthYear);
			END IF;
		END IF;
        
		-- Then, get starID of star. Finally, insert star into stars_in_movies
		IF starBirthYear IS NULL THEN
			SELECT id INTO starIDToInsert FROM stars WHERE name = starName LIMIT 1;
            INSERT INTO stars_in_movies (starId, movieId) VALUES (starIDToInsert, newMovieID);
		ELSE
			SELECT id INTO starIDToInsert FROM stars WHERE name = starName AND birthYear = starBirthYear LIMIT 1;
			INSERT INTO stars_in_movies (starId, movieId) VALUES (starIDToInsert, newMovieID);
        END IF;
		
        -- ~~~~~~~~~~ CASE 2: ~~~~~~~~~~
		-- Genre does not exists, create genre first
		IF genreExists = 0 THEN
			SELECT MAX(id) INTO newGenreID FROM genres;
			SET newGenreID = newGenreID + 1;
            INSERT INTO genres VALUES (newGenreID, genreName);
		END IF;
        
		-- Then, get genreID of genre
		SELECT id INTO genreIDToInsert FROM genres WHERE name = genreName;
        
		-- Finally, insert genre into genres_in_movies
		INSERT INTO genres_in_movies (genreId, movieId) VALUES (genreIDToInsert, newMovieID);
        
        -- ~~~~~~~~~~ CASE 3: ~~~~~~~~~~
        -- Add N/A rating to ratings table with movie ID
        INSERT INTO ratings VALUES (newMovieID, -1, -1);
        
    END IF;
    
	SELECT CONCAT("Success! Movie added (Movie ID: ", newMovieID, 
    ", Star ID: ", starIDToInsert, 
    ", Genre ID: ", genreIDToInsert, ")") AS answer;
END
//
 
DELIMITER ;