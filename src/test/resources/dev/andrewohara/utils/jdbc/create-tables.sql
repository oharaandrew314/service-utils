CREATE TABLE IF NOT EXISTS cats (
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR,
    lives INT,
    born TIMESTAMP,
    trills BOOLEAN,
    weight FLOAT
)