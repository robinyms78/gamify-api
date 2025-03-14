-- First, disable foreign key constraints temporarily
SET session_replication_role = 'replica';

-- 1. Alter users table
ALTER TABLE "users"
    ALTER COLUMN "id" TYPE varchar(36) USING id::varchar,
    ADD COLUMN IF NOT EXISTS "role" varchar(50) NOT NULL DEFAULT 'USER',
    ADD COLUMN IF NOT EXISTS "earned_points" bigint NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS "available_points" bigint NOT NULL DEFAULT 0,
    ALTER COLUMN "created_at" TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN "updated_at" TYPE timestamp with time zone USING updated_at AT TIME ZONE 'UTC';

-- If there's a single points column, migrate data to the new columns
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'points') THEN
        UPDATE "users" SET "earned_points" = "points", "available_points" = "points";
        ALTER TABLE "users" DROP COLUMN "points";
    END IF;
END $$;

-- 2. Alter points_transactions table
ALTER TABLE "points_transactions"
    ALTER COLUMN "transaction_id" TYPE varchar(36) USING transaction_id::varchar,
    ALTER COLUMN "user_id" TYPE varchar(36) USING user_id::varchar,
    ALTER COLUMN "points" TYPE bigint USING points::bigint,
    ALTER COLUMN "timestamp" TYPE timestamp with time zone USING timestamp AT TIME ZONE 'UTC',
    ALTER COLUMN "created_at" TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC',
    ADD COLUMN IF NOT EXISTS "metadata" jsonb NOT NULL DEFAULT '{}';

-- 3. Alter rewards table
-- First check if it's called rewards or reward
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'reward') THEN
        ALTER TABLE "reward" RENAME TO "rewards";
    END IF;
END $$;

ALTER TABLE "rewards"
    ALTER COLUMN "id" TYPE varchar(36) USING id::varchar,
    ALTER COLUMN "cost_in_points" TYPE bigint USING cost_in_points::bigint,
    ALTER COLUMN "created_at" TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN "updated_at" TYPE timestamp with time zone USING updated_at AT TIME ZONE 'UTC';

-- Rename costInPoints to cost_in_points if needed
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'rewards' AND column_name = 'costinpoints') THEN
        ALTER TABLE "rewards" RENAME COLUMN "costinpoints" TO "cost_in_points";
    END IF;
END $$;

-- 4. Handle redemptions table (consolidate reward_redemptions if needed)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'reward_redemptions') THEN
        -- If reward_redemptions exists but redemptions doesn't, rename it
        IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'redemptions') THEN
            ALTER TABLE "reward_redemptions" RENAME TO "redemptions";
        ELSE
            -- Both tables exist, would need more complex migration
            RAISE NOTICE 'Both redemptions and reward_redemptions tables exist. Manual migration needed.';
        END IF;
    END IF;
END $$;

-- Now alter the redemptions table
ALTER TABLE "redemptions"
    ALTER COLUMN "id" TYPE varchar(36) USING id::varchar,
    ALTER COLUMN "user_id" TYPE varchar(36) USING user_id::varchar,
    ALTER COLUMN "reward_id" TYPE varchar(36) USING reward_id::varchar,
    ADD COLUMN IF NOT EXISTS "status" varchar(50) NOT NULL DEFAULT 'COMPLETED',
    ALTER COLUMN "created_at" TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN "updated_at" TYPE timestamp with time zone USING updated_at AT TIME ZONE 'UTC';

-- 5. Alter ladder_levels table
ALTER TABLE "ladder_levels"
    ALTER COLUMN "level" TYPE bigint USING level::bigint,
    ADD COLUMN IF NOT EXISTS "label" varchar(255) NOT NULL DEFAULT 'Level',
    ALTER COLUMN "points_required" TYPE bigint USING points_required::bigint,
    ALTER COLUMN "created_at" TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC';

-- 6. Alter user_ladder_status table
ALTER TABLE "user_ladder_status"
    ALTER COLUMN "user_id" TYPE varchar(36) USING user_id::varchar,
    ALTER COLUMN "current_level" TYPE bigint USING current_level::bigint,
    ALTER COLUMN "earned_points" TYPE bigint USING earned_points::bigint,
    ALTER COLUMN "points_to_next_level" TYPE bigint USING points_to_next_level::bigint,
    ADD COLUMN IF NOT EXISTS "achievements" varchar(255),
    ALTER COLUMN "updated_at" TYPE timestamp with time zone USING updated_at AT TIME ZONE 'UTC';

-- Rename points to earned_points if needed
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'user_ladder_status' AND column_name = 'points') THEN
        ALTER TABLE "user_ladder_status" RENAME COLUMN "points" TO "earned_points";
    END IF;
END $$;

-- 7. Alter leaderboard table
ALTER TABLE "leaderboard"
    ALTER COLUMN "user_id" TYPE varchar(36) USING user_id::varchar,
    ALTER COLUMN "earned_points" TYPE bigint USING earned_points::bigint,
    ALTER COLUMN "current_level" TYPE bigint USING current_level::bigint,
    ALTER COLUMN "rank" TYPE bigint USING rank::bigint,
    ALTER COLUMN "created_at" TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC';

-- Rename points to earned_points if needed
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'leaderboard' AND column_name = 'points') THEN
        ALTER TABLE "leaderboard" RENAME COLUMN "points" TO "earned_points";
    END IF;
END $$;

-- 8. Alter task_events table
ALTER TABLE "task_events"
    ALTER COLUMN "event_id" TYPE varchar(36) USING event_id::varchar,
    ALTER COLUMN "user_id" TYPE varchar(36) USING user_id::varchar,
    ALTER COLUMN "task_id" TYPE varchar(36) USING task_id::varchar,
    ADD COLUMN IF NOT EXISTS "status" varchar(50) NOT NULL DEFAULT 'COMPLETED',
    ADD COLUMN IF NOT EXISTS "assigned_at" timestamp with time zone,
    ADD COLUMN IF NOT EXISTS "due_date" timestamp with time zone,
    ALTER COLUMN "completion_time" TYPE timestamp with time zone USING completion_time AT TIME ZONE 'UTC',
    ADD COLUMN IF NOT EXISTS "metadata" jsonb DEFAULT '{}',
    ALTER COLUMN "created_at" TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC',
    ADD COLUMN IF NOT EXISTS "updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP;

-- 9. Create or alter achievements table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'achievements') THEN
        CREATE TABLE "achievements" (
            "achievement_id" varchar(36) NOT NULL,
            "name" varchar(255) NOT NULL,
            "description" text,
            "criteria" jsonb NOT NULL,
            "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY ("achievement_id")
        );
    ELSE
        -- If table exists, alter it to match our schema
        ALTER TABLE "achievements"
            ALTER COLUMN "achievement_id" TYPE varchar(36) USING achievement_id::varchar,
            ALTER COLUMN "created_at" TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC',
            ALTER COLUMN "criteria" TYPE jsonb USING criteria::jsonb;
    END IF;
END $$;

-- 10. Create or alter user_achievements table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_achievements') THEN
        CREATE TABLE "user_achievements" (
            "user_id" varchar(36) NOT NULL,
            "achievement_id" varchar(36) NOT NULL,
            "earned_at" timestamp with time zone NOT NULL,
            "metadata" jsonb,
            PRIMARY KEY ("user_id", "achievement_id"),
            FOREIGN KEY ("user_id") REFERENCES "users"("id"),
            FOREIGN KEY ("achievement_id") REFERENCES "achievements"("achievement_id")
        );
    ELSE
        -- If table exists, alter it to match our schema
        ALTER TABLE "user_achievements"
            ALTER COLUMN "user_id" TYPE varchar(36) USING user_id::varchar,
            ALTER COLUMN "achievement_id" TYPE varchar(36) USING achievement_id::varchar,
            ALTER COLUMN "earned_at" TYPE timestamp with time zone USING earned_at AT TIME ZONE 'UTC',
            ALTER COLUMN "metadata" TYPE jsonb USING metadata::jsonb;
    END IF;
END $$;

-- Re-enable foreign key constraints
SET session_replication_role = 'origin';
