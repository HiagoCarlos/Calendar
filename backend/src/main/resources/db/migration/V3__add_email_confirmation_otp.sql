-- V3 - add a dedicated OTP field for email confirmation

-- otp / otp_expiration (from V1) become exclusive to the forgot-password flow.
-- Email confirmation gets its own pair of columns instead of sharing those.

ALTER TABLE users
    ADD COLUMN email_confirmation_otp varchar(8);

ALTER TABLE users
    ADD COLUMN email_confirmation_otp_expiration timestamp;
