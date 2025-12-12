-- =============================================
-- V2__create_alert_preferences.sql
-- Create table to store user alert preferences
-- =============================================

CREATE TABLE alert_preferences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(30) NOT NULL,  -- EMAIL, SMS, TELEGRAM (enum no backend)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),

    -- FK para tabela users
    CONSTRAINT fk_alert_pref_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- Um usuário pode ter vários tipos de alerta,
-- mas não pode ter o mesmo tipo duplicado
CREATE UNIQUE INDEX ux_user_alert_type
    ON alert_preferences(user_id, alert_type);

-- Index para acelerar buscas por user_id
CREATE INDEX idx_alert_pref_user_id
    ON alert_preferences(user_id);
