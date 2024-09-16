-- Task 1

CREATE TABLE IF NOT EXISTS worker (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	name VARCHAR(1000) NOT NULL CHECK (length(name) BETWEEN 2 AND 1000),
	birthday DATE CHECK (EXTRACT(YEAR FROM birthday) BETWEEN 1901 AND EXTRACT(YEAR FROM CURRENT_DATE)),
    email VARCHAR(1000) UNIQUE,
	level VARCHAR(10) NOT NULL CHECK (level IN('trainee', 'junior', 'middle', 'senior')),
	salary INTEGER NOT NULL CHECK (salary BETWEEN 100 AND 100000)
);

CREATE TABLE IF NOT EXISTS client (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	name VARCHAR(1000) NOT NULL UNIQUE CHECK (length(name) BETWEEN 2 AND 1000)
);

CREATE TABLE IF NOT EXISTS project (
	id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
	name VARCHAR(1000) UNIQUE,
	client_id INTEGER NOT NULL,
	start_date DATE,
	finish_date DATE CHECK (finish_date >= start_date),
	CONSTRAINT fk_client
    FOREIGN KEY (client_id)
    REFERENCES client(id)
);

CREATE TABLE IF NOT EXISTS project_worker (
	project_id INTEGER NOT NULL,
	worker_id INTEGER NOT NULL,
	PRIMARY KEY (project_id, worker_id),
	FOREIGN KEY (project_id) REFERENCES project (id),
	FOREIGN KEY (worker_id) REFERENCES worker (id)
);