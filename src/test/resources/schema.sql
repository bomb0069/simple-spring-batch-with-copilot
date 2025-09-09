-- H2 Compatible Schema for Testing
CREATE TABLE price_calculations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_price DECIMAL(10,2) NOT NULL,
    vat_rate DECIMAL(5,4) NOT NULL,
    vat_amount DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
