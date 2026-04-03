INSERT INTO users (name, age)
SELECT 'Ana', 26
WHERE NOT EXISTS (
  SELECT 1
  FROM users
  WHERE name = 'Ana' AND age = 26
);

