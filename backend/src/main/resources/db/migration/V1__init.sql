CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    display_name  VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL
);

CREATE TABLE boards (
    id          UUID PRIMARY KEY,
    name        VARCHAR(120)  NOT NULL,
    description VARCHAR(1000),
    owner_id    UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  TIMESTAMP     NOT NULL
);
CREATE INDEX idx_boards_owner ON boards (owner_id);

CREATE TABLE board_members (
    id         UUID PRIMARY KEY,
    board_id   UUID        NOT NULL REFERENCES boards (id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    CONSTRAINT uq_board_member UNIQUE (board_id, user_id)
);
CREATE INDEX idx_board_members_board ON board_members (board_id);
CREATE INDEX idx_board_members_user ON board_members (user_id);

CREATE TABLE task_lists (
    id         UUID PRIMARY KEY,
    name       VARCHAR(120)     NOT NULL,
    board_id   UUID             NOT NULL REFERENCES boards (id) ON DELETE CASCADE,
    position   DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP        NOT NULL
);
CREATE INDEX idx_task_lists_board ON task_lists (board_id);

CREATE TABLE cards (
    id          UUID PRIMARY KEY,
    title       VARCHAR(250)     NOT NULL,
    description VARCHAR(4000),
    list_id     UUID             NOT NULL REFERENCES task_lists (id) ON DELETE CASCADE,
    position    DOUBLE PRECISION NOT NULL,
    assignee_id UUID             REFERENCES users (id) ON DELETE SET NULL,
    labels      VARCHAR(500),
    due_date    TIMESTAMP,
    created_at  TIMESTAMP        NOT NULL,
    updated_at  TIMESTAMP        NOT NULL
);
CREATE INDEX idx_cards_list ON cards (list_id);

CREATE TABLE activities (
    id         UUID PRIMARY KEY,
    board_id   UUID          NOT NULL REFERENCES boards (id) ON DELETE CASCADE,
    actor_id   UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    message    VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP     NOT NULL
);
CREATE INDEX idx_activities_board ON activities (board_id);
