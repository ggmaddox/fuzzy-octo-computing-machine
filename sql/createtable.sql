set foreign_key_checks=0;

CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;

DROP TABLE IF EXISTS movies;
CREATE TABLE movies (
	id VARCHAR(10) NOT NULL DEFAULT '',
	title VARCHAR(100) NOT NULL DEFAULT '',
	`year` INT NOT NULL,
	director VARCHAR(100) NOT NULL DEFAULT '',
	PRIMARY KEY (id));
  
DROP TABLE IF EXISTS stars;
CREATE TABLE stars (
	id VARCHAR(10) NOT NULL DEFAULT '',
    `name` VARCHAR(100) NOT NULL DEFAULT '',
    birthYear INT,
    PRIMARY KEY (id));
    
DROP TABLE IF EXISTS stars_in_movies;
CREATE TABLE stars_in_movies (
	starId VARCHAR(10) NOT NULL DEFAULT '',
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id));
    
DROP TABLE IF EXISTS genres;
CREATE TABLE genres (
	id INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(32) NOT NULL DEFAULT '',
    PRIMARY KEY (id));
    
DROP TABLE IF EXISTS genres_in_movies;
CREATE TABLE genres_in_movies (
	genreId INT NOT NULL,
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id));
    
DROP TABLE IF EXISTS customers;
CREATE TABLE customers (
	id INT NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    ccId VARCHAR(20) NOT NULL DEFAULT '',
    address VARCHAR(200) NOT NULL DEFAULT '',
    email VARCHAR(50) NOT NULL DEFAULT '',
    `password` VARCHAR(20) NOT NULL DEFAULT '',
	PRIMARY KEY (id),
    FOREIGN KEY (ccId) REFERENCES creditcards(id));
    
DROP TABLE IF EXISTS sales;
CREATE TABLE sales (
	id INT NOT NULL,
    customerId INT NOT NULL REFERENCES customers(id),
    movieId VARCHAR(10) NOT NULL DEFAULT '',
    saleDate DATE NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (movieId) REFERENCES movies(id));
    
DROP TABLE IF EXISTS creditcards;
CREATE TABLE creditcards (
	id VARCHAR(20) NOT NULL DEFAULT '',
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    expiration DATE NOT NULL,
    PRIMARY KEY (id));
    
DROP TABLE IF EXISTS ratings;
CREATE TABLE ratings (
	movieId VARCHAR(10) NOT NULL DEFAULT '',
    rating FLOAT NOT NULL,
    numVotes INT NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies(id));

-- add employee information, project 3
DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
	email VARCHAR(50) PRIMARY KEY,
    `password` VARCHAR(20) NOT NULL,
    fullname VARCHAR(100));
    
INSERT INTO employees VALUES('classta@email.edu', 'classta', 'TA CS122B');

-- UPDATE employees SET `password`='1' WHERE email='classta@email.edu';