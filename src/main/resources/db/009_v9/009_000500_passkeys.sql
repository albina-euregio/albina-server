-- liquibase formatted sql
-- changeset albina:009_000500 failOnError:true

CREATE TABLE passkeys (
    id               varchar(36)  NOT NULL,
    owner_email      varchar(191) NOT NULL,
    credential_id    varchar(191) NOT NULL,
    public_key_cose  varchar(1024) NOT NULL,
    sign_count       bigint       NOT NULL,
    name             varchar(191),
    created_at       datetime(6)  NOT NULL,
    last_used_at     datetime(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_passkeys_credential_id UNIQUE (credential_id),
    CONSTRAINT fk_passkeys_owner FOREIGN KEY (owner_email) REFERENCES users (email)
);
