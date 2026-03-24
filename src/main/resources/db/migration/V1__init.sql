CREATE TABLE trouble_tickets
(
    id          VARCHAR(36)  NOT NULL,
    tenant_id   VARCHAR(255) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    service_id  BIGINT       NOT NULL,
    description TEXT         NOT NULL,
    status      VARCHAR(50)  NOT NULL,

    CONSTRAINT pk_trouble_tickets PRIMARY KEY (id),
    CONSTRAINT uq_tenant_external_id UNIQUE (tenant_id, external_id)
);

CREATE TABLE notes
(
    id                VARCHAR(36)  NOT NULL,
    trouble_ticket_id VARCHAR(36)  NOT NULL,
    text              TEXT         NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_notes PRIMARY KEY (id),
    CONSTRAINT fk_notes_trouble_ticket
        FOREIGN KEY (trouble_ticket_id)
            REFERENCES trouble_tickets (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_trouble_tickets_tenant_id
    ON trouble_tickets (tenant_id);

CREATE INDEX idx_notes_trouble_ticket_id
    ON notes (trouble_ticket_id);