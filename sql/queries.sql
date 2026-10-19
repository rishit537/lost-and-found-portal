-- all queries used in the app
USE lost_and_found_portal;

-- \\\ UserDAO ///

-- Find a user by their email address
SELECT user_id, name, email, phone_number
FROM users
WHERE email = ?;

-- Insert a new user and return the generated user_id
INSERT INTO users (name, email, phone_number)
VALUES (?, ?, ?);


-- \\\ CategoryDAO ///

-- Fetch all categories ordered by their ID
SELECT category_id, category_name
FROM categories
ORDER BY category_id;


-- \\\ ItemDAO ///

-- Insert a new lost/found item
INSERT INTO items (title, description, location, status, image_data,
                   category_id, reported_by_user_id)
VALUES (?, ?, ?, ?, ?, ?, ?);

-- Search for unclaimed items with optional keyword, category, and status filters
-- this is only the base query. where clauses are added when needed in java
SELECT i.*, c.category_name,
       u.name AS reporter_name, u.email AS reporter_email
FROM items i
JOIN categories c ON i.category_id = c.category_id
JOIN users u ON i.reported_by_user_id = u.user_id
WHERE i.status != 'Claimed'
  -- AND (i.title LIKE ? OR i.description LIKE ?)   -- optional keyword filter
  -- AND i.category_id = ?                           -- optional category filter
  -- AND i.status = ?                                -- optional status filter
ORDER BY i.date_reported DESC;

-- Fetch all items reported by a specific user (identified by email)
SELECT i.*, c.category_name,
       u.name AS reporter_name, u.email AS reporter_email
FROM items i
JOIN categories c ON i.category_id = c.category_id
JOIN users u ON i.reported_by_user_id = u.user_id
WHERE u.email = ?
ORDER BY i.date_reported DESC;

-- Fetch a single item by its item_id (pk)
SELECT i.*, c.category_name,
       u.name AS reporter_name, u.email AS reporter_email
FROM items i
JOIN categories c ON i.category_id = c.category_id
JOIN users u ON i.reported_by_user_id = u.user_id
WHERE i.item_id = ?;

-- Update an item's details (with a new image)
UPDATE items
SET title = ?, description = ?, location = ?, status = ?,
    image_data = ?, category_id = ?
WHERE item_id = ?;

-- Update an item's details (without changing the image)
UPDATE items
SET title = ?, description = ?, location = ?, status = ?,
    category_id = ?
WHERE item_id = ?;

-- Delete an item by its primary key
DELETE FROM items
WHERE item_id = ?;

-- Mark a lost item as found (only if current status is 'Lost')
UPDATE items
SET status = 'Found'
WHERE item_id = ? AND status = 'Lost';

-- Mark an item as claimed (only if it is not already claimed). This is a part of claim transaction
UPDATE items
SET status = 'Claimed'
WHERE item_id = ? AND status != 'Claimed';

-- Record a new claim entry. The other part of claim transaction
INSERT INTO claims (item_id, claimed_by_user_id)
VALUES (?, ?);


-- \\\ ClaimDAO ///

-- Fetch the full claims log (joins claims → items → categories → reporter → claimer)
SELECT cl.claim_id, cl.claim_date,
       i.item_id, i.title, i.location, i.date_reported,
       cat.category_name,
       reporter.name  AS reporter_name,  reporter.email AS reporter_email,
       claimer.name   AS claimer_name,   claimer.email  AS claimer_email
FROM claims cl
JOIN items i          ON cl.item_id             = i.item_id
JOIN categories cat   ON i.category_id          = cat.category_id
JOIN users reporter   ON i.reported_by_user_id  = reporter.user_id
JOIN users claimer    ON cl.claimed_by_user_id  = claimer.user_id
ORDER BY cl.claim_date DESC;
