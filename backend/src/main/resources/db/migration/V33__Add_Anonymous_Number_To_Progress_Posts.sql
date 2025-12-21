-- Add anonymous_number field to progress_posts table
-- This enables anonymous posting with sequential numbers per organization (e.g., #0001, #0002)

ALTER TABLE progress_posts
ADD COLUMN anonymous_number INT NULL;

-- Create index for efficient anonymous number assignment
CREATE INDEX idx_progress_posts_org_anon_num
ON progress_posts(organization_id, anonymous_number);

-- Update existing posts with sequential anonymous numbers per organization
-- This ensures backward compatibility for existing data
-- H2-compatible MERGE statement
MERGE INTO progress_posts p1
USING (
    SELECT
        p2.id,
        (SELECT COUNT(*) + 1
         FROM progress_posts p3
         WHERE p3.organization_id = p2.organization_id
         AND p3.id < p2.id) AS new_anon_num
    FROM progress_posts p2
    WHERE p2.anonymous_number IS NULL
) AS temp_nums
ON p1.id = temp_nums.id
WHEN MATCHED THEN UPDATE SET p1.anonymous_number = temp_nums.new_anon_num;

-- Make the column NOT NULL after populating existing data
-- H2 uses ALTER TABLE ... ALTER COLUMN instead of MODIFY COLUMN
ALTER TABLE progress_posts
ALTER COLUMN anonymous_number INT NOT NULL;
