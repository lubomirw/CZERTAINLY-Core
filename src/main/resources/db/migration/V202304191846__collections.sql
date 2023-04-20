CREATE TABLE "collection" (
    "uuid" uuid NOT NULL,
    "i_author" text NOT NULL,
    "i_cre" TIMESTAMP NOT NULL,
    "i_upd" TIMESTAMP NOT NULL,
    "name" text NOT NULL,
    "description" text,
    "content_type" text NOT NULL,
    "system_collection" bool NOT NULL
)
;

CREATE TABLE "collection_item" (
    "uuid" uuid NOT NULL,
    "description" text,
    "default_item" bool NOT NULL,
    "content" text NOT NULL,
    "collection_uuid" uuid NOT NULL
)
;

CREATE TABLE "collection_source" (
    "uuid" uuid NOT NULL,
    "type" text NOT NULL,
    "name" text,
    "url" text,
    "collection_uuid" uuid NOT NULL,
    "credential_uuid" uuid
)
;

-- -----------------
-- Primary Keys
-- -----------------
ALTER TABLE "collection" ADD CONSTRAINT "pk_collection" PRIMARY KEY ("uuid");
ALTER TABLE "collection_item" ADD CONSTRAINT "pk_collection_item" PRIMARY KEY ("uuid");
ALTER TABLE "collection_source" ADD CONSTRAINT "pk_collection_source" PRIMARY KEY ("uuid");

-- -----------------
-- Foreign Keys
-- -----------------
ALTER TABLE "collection_item"
    ADD CONSTRAINT "fk_collection_item_to_collection"
    FOREIGN KEY ("collection_uuid")
    REFERENCES "collection" ("uuid")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "collection_source"
    ADD CONSTRAINT "fk_collection_source_to_collection"
    FOREIGN KEY ("collection_uuid")
    REFERENCES "collection" ("uuid")
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "collection_source"
    ADD CONSTRAINT "fk_collection_source_to_credential"
    FOREIGN KEY ("credential_uuid")
    REFERENCES "credential" ("uuid")
    ON DELETE SET NULL ON UPDATE CASCADE;
