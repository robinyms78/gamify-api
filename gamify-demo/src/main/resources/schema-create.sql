-- Drop tables in reverse order of dependencies
DROP TABLE IF EXISTS user_achievements;
DROP TABLE IF EXISTS achievements;
DROP TABLE IF EXISTS task_events;
DROP TABLE IF EXISTS leaderboard;
DROP TABLE IF EXISTS user_ladder_status;
DROP TABLE IF EXISTS ladder_levels;
DROP TABLE IF EXISTS redemptions;
DROP TABLE IF EXISTS rewards;
DROP TABLE IF EXISTS points_transactions;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE IF NOT EXISTS "users" (
    "id" varchar(36) NOT NULL,
    "username" varchar(255) NOT NULL UNIQUE,
    "email" varchar(255) NOT NULL UNIQUE,
    "password_hash" varchar(255) NOT NULL,
    "role" varchar(50) NOT NULL,
    "department" varchar(255),
    "earned_points" bigint NOT NULL DEFAULT 0,
    "available_points" bigint NOT NULL DEFAULT 0,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);

-- Points transactions table
CREATE TABLE IF NOT EXISTS "points_transactions" (
    "transaction_id" varchar(36) NOT NULL,
    "user_id" varchar(36) NOT NULL,
    "event_type" varchar(255) NOT NULL,
    "points" bigint NOT NULL,
    "timestamp" timestamp with time zone,
    "metadata" jsonb,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("transaction_id"),
    FOREIGN KEY ("user_id") REFERENCES "users"("id")
);

-- Rewards table
CREATE TABLE IF NOT EXISTS "rewards" (
    "id" varchar(36) NOT NULL,
    "name" varchar(255) NOT NULL,
    "description" text NOT NULL,
    "cost_in_points" bigint NOT NULL,
    "available" boolean NOT NULL DEFAULT true,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);

-- Redemptions table
CREATE TABLE IF NOT EXISTS "redemptions" (
    "id" varchar(36) NOT NULL,
    "user_id" varchar(36) NOT NULL,
    "reward_id" varchar(36) NOT NULL,
    "status" varchar(50) NOT NULL,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    FOREIGN KEY ("user_id") REFERENCES "users"("id"),
    FOREIGN KEY ("reward_id") REFERENCES "rewards"("id")
);

-- Ladder levels table
CREATE TABLE IF NOT EXISTS "ladder_levels" (
    "level" bigint NOT NULL,
    "label" varchar(255) NOT NULL,
    "points_required" bigint NOT NULL,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("level")
);

-- User ladder status table
CREATE TABLE IF NOT EXISTS "user_ladder_status" (
    "user_id" varchar(36) NOT NULL,
    "current_level" bigint NOT NULL,
    "earned_points" bigint NOT NULL,
    "points_to_next_level" bigint NOT NULL,
    "achievements" varchar(255),
    "updated_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("user_id"),
    FOREIGN KEY ("user_id") REFERENCES "users"("id"),
    FOREIGN KEY ("current_level") REFERENCES "ladder_levels"("level")
);

-- Leaderboard table
CREATE TABLE IF NOT EXISTS "leaderboard" (
    "user_id" varchar(36) NOT NULL,
    "username" varchar(255) NOT NULL,
    "department" varchar(255),
    "earned_points" bigint NOT NULL,
    "current_level" bigint NOT NULL,
    "rank" bigint NOT NULL,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("user_id"),
    FOREIGN KEY ("user_id") REFERENCES "users"("id"),
    FOREIGN KEY ("current_level") REFERENCES "ladder_levels"("level")
);

-- Task events table
CREATE TABLE IF NOT EXISTS "task_events" (
    "event_id" varchar(36) NOT NULL,
    "user_id" varchar(36) NOT NULL,
    "task_id" varchar(36) NOT NULL,
    "event_type" varchar(255) NOT NULL,
    "status" varchar(50) NOT NULL,
    "assigned_at" timestamp with time zone,
    "due_date" timestamp with time zone,
    "completion_time" timestamp with time zone,
    "metadata" jsonb,
    "points_earned" bigint,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("event_id"),
    FOREIGN KEY ("user_id") REFERENCES "users"("id")
);

-- Achievements table
CREATE TABLE IF NOT EXISTS "achievements" (
    "achievement_id" varchar(36) NOT NULL,
    "name" varchar(255) NOT NULL,
    "description" text,
    "criteria" jsonb NOT NULL,
    "created_at" timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("achievement_id")
);

-- User achievements table
CREATE TABLE IF NOT EXISTS "user_achievements" (
    "user_id" varchar(36) NOT NULL,
    "achievement_id" varchar(36) NOT NULL,
    "earned_at" timestamp with time zone NOT NULL,
    "metadata" jsonb,
    PRIMARY KEY ("user_id", "achievement_id"),
    FOREIGN KEY ("user_id") REFERENCES "users"("id"),
    FOREIGN KEY ("achievement_id") REFERENCES "achievements"("achievement_id")
);