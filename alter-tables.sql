USE moviedb;

CREATE FULLTEXT INDEX ft_title
ON movies(title);

CREATE INDEX movie_index
ON movies (title, year, director);

CREATE INDEX star_index
ON stars (name);

alter table sales add column qty int not null default 0;