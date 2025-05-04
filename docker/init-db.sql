CREATE TABLE IF NOT EXISTS contacts (
    id SERIAL PRIMARY KEY,
    firstName VARCHAR(255),
    lastName VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_fts_contacts ON contacts
    USING GIN (
        to_tsvector(
            'english',
            coalesce(firstName,'') || ' ' ||
            coalesce(lastName,'')  || ' ' ||
            coalesce(address,'')   || ' ' ||
            coalesce(phone,'')
        )
    );