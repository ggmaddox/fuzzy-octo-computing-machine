DROP PROCEDURE IF EXISTS add_movie;
DELIMITER $$
CREATE PROCEDURE add_movie (

	IN movieTitle VARCHAR(100),
    IN movieYear INT,
    IN movieDirector VARCHAR(100),
    IN starName VARCHAR(100),
    IN genreName VARCHAR(32))
	
BEGIN

	set @movieId = '';
    set @genreId = 0;
    set @starId = '';
    
	/*
		get all ids and parse into ints to increment.
        These will be used if movie, star, or genre
        don't exist and a new entry is to be made in 
        the db.
    */
    
    -- star id
	select max(cast(substring(id, 3) as unsigned))  
    into @maxStarId
    from stars;

    set @maxStarId = @maxStarId + 1;
    set @newMaxStarId = concat('nm', lpad(cast(@maxStarId as char(7)), 7, '0'));

    
    -- movie id
	select max(cast(substring(id, 3) as unsigned))  
	into @maxMovieId
	from movies;

	set @maxMovieId = @maxMovieId + 1;
	set @newMaxMovieId = concat('tt', lpad(cast(@maxMovieId as char(7)), 7, '0'));

    
    -- genre id
    select max(id)
	into @maxGenreId
	from genres;
    
    set @newMaxGenreId = @maxGenreId + 1;
    
    /*
		set new generic rating so that queries will work.
        Otherwise, search, browse, in website will not
        return anything because the ratings table is joined in the query.
    */
    set @newRating = 5.0;
    set @newNumVotes = 1;
    
    -- ----------------------------------------------
    
	/*
		set boolean flags to know whether we need 
        to create new entries for stars/genres
        or just link them to movies through the 
        genres_in_movies/stars_in_movies tables.
    */
    
    -- if @starExists = 1, star exists.
    select count(*)
	into @starExists
	from stars 
	where `name` = starName;
    
	IF @starExists = 0 THEN
		-- insert star into star as a new star
        insert into stars (id, `name`) values (@newMaxStarId, starName);
        select concat("Star doesn't exist. New ID is ", @newMaxStarId);
        
        else
			-- get starId
			select id 
            into @starId 
            from stars
            where `name` = starName;
            
			select concat("Star exists. ID is ", @starId);
	end if;
    
    -- if genreExists = 1, genre exists
	select count(*)
	into @genreExists
	from genres 
	where `name` = genreName;

	IF @genreExists = 0 THEN
		-- insert genre into genres as a new genre
        insert into genres values (@newMaxGenreId, genreName);
        
        select concat("Genre doesn't exist. New ID is ", @newMaxGenreId);
        
        else 
			-- get genreId
            select id 
            into @genreId 
            from genres
            where `name` = genreName;
            
			select concat("Genre exists. ID is ", @genreId);
	end if;    
    
    /*
		get movieId, if it exists, so the query for movies.id
        isn't run multiple times
    */
    select count(*)
    into @movieExists
    from movies
    where title = movieTitle and 
    `year` = movieYear and 
	director = movieDirector;
    
    if @movieExists >= 1 THEN
		select movies.id
        into @movieId
        from movies
        where title = movieTitle and
        `year` = movieYear and 
        director = movieDirector;
        
        select concat("Movie exists. ID is ", @movieId);
        
        else
			select concat("Movie does not exist. New ID is ", @newMaxMovieId);
    end if;

	/*
		First query checks to see if there is an exact match in the database. 
    */
	IF (select count(*)
		from movies, genres, stars, genres_in_movies, stars_in_movies
		where 
		movies.id = genres_in_movies.movieId and 
		movies.id = stars_in_movies.movieId and
		genres.id = genres_in_movies.genreId and 
		stars.id = stars_in_movies.starId and 
		title = movieTitle and
		`year` = movieYear and
		director = movieDirector and
		stars.`name` = starName and
		genres.`name` = genreName) > 0 THEN
	
		SELECT "Movie is already in database. No changes made." AS answer;

	-- title, year, director exists, but not genre
	ELSEIF (select count(*)
		from movies, stars, stars_in_movies
		where 
		movies.id = stars_in_movies.movieId and
		stars.id = stars_in_movies.starId and 
		title = movieTitle and
		`year` = movieYear and
		director = movieDirector and
		stars.`name` = starName) > 0 THEN
        
        SELECT "Movie is in database, but genre is not linked." AS answer;
        -- link genre to movie (insert into genres_in_movies)
        if @genreExists != 0 then
			insert into genres_in_movies values(@genreId, @movieId);
            select "Linking existing genre with movie...";
            
            else
				insert into genres_in_movies values(@newMaxGenreId, @movieId); 
                select "Linking movie with new genre....";
        end if;
        
    -- title, year, director, genre exists, but not star
	ELSEIF (select count(*)
		from movies, genres, genres_in_movies
		where 
		movies.id = genres_in_movies.movieId and
		genres.id = genres_in_movies.genreId and 
		title = movieTitle and
		`year` = movieYear and
		director = movieDirector and
		genres.`name` = genreName) > 0 THEN
        
        SELECT "Movie is in database, but star is not linked." as answer;
        -- link star to movie
        if @starExists != 0 THEN
			insert into stars_in_movies values(@starId, @movieId);
            select "Linking existing star with movie...";
            else
				insert into stars_in_movies values(@newMaxStarId, @movieId);
				select "Linking movie with new star...";
		end if;
        
        -- title, year, director exist, but not star or genre
        ELSEIF (select count(*)
		from movies
		where 
		title = movieTitle and
		`year` = movieYear and
		director = movieDirector) > 0 THEN
        
        SELECT "Movie is in database, but star and genre aren't. Linking star and genre to movie..." as answer;
        
        -- link star and genre to movie
        insert into stars_in_movies values(@newMaxStarId, @movieId);
        insert into genres_in_movies values(@newMaxGenreId, @movieId); 
        
	/*
		otherwise it's going to be a new movie, so we just need to check
        whether to create new star/genre or just link them to the movie.
    */
    else
        -- add title, year, director
        insert into movies values(@newMaxMovieId, movieTitle, movieYear, movieDirector);
        
        -- add a new default rating
        INSERT INTO ratings VALUES(@newMaxMovieId, @newRating, @newNumVotes);
        
        if @starId != '' THEN
			select "Linking existing star to new movie...";
            insert into stars_in_movies values(@starId, @newMaxMovieId);
            
            else
				select "Linking new star to new movie...";
                insert into stars_in_movies values(@newMaxStarId, @newMaxMovieId);
            
		end if;
        
        if @genreId != 0 THEN
			select "Linking existing genre to new movie...";
            -- link old genre w/new movie
            insert into genres_in_movies values(@genreId, @newMaxMovieId);
			
            else
				select "Linking new genre to new movie...";
                insert into genres_in_movies values(@newMaxGenreId, @newMaxMovieId);
		end if;
                SELECT "Database successfully updated." AS answer;
    END IF;

END $$
DELIMITER ;