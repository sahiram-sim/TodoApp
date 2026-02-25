ALTER TABLE refresh_tokens
  ADD COLUMN IF NOT EXISTS token varchar(512);

ALTER TABLE refresh_tokens
  ADD COLUMN IF NOT EXISTS revoked boolean NOT NULL DEFAULT false;

-- If table is empty, it's safe to enforce NOT NULL + UNIQUE immediately:
ALTER TABLE refresh_tokens
  ALTER COLUMN token SET NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uq_refresh_tokens_token'
  ) THEN
    ALTER TABLE refresh_tokens ADD CONSTRAINT uq_refresh_tokens_token UNIQUE (token);
  END IF;
END $$;