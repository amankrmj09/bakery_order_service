CREATE TABLE order_items
(
    id                       UUID           NOT NULL,
    order_id                 UUID           NOT NULL,
    product_id               UUID           NOT NULL,
    product_sku              VARCHAR(50)    NOT NULL,
    product_name             VARCHAR(200)   NOT NULL,
    product_category         VARCHAR(100),
    quantity                 INTEGER        NOT NULL,
    unit_price               DECIMAL(10, 2) NOT NULL,
    discount_per_item        DECIMAL(10, 2),
    special_instructions     TEXT,
    product_description      TEXT,
    product_image_url        VARCHAR(255),
    preparation_time_minutes INTEGER,
    created_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id                            UUID           NOT NULL,
    order_number                  VARCHAR(20)    NOT NULL,
    user_id                       UUID           NOT NULL,
    customer_name                 VARCHAR(200)   NOT NULL,
    customer_email                VARCHAR(255)   NOT NULL,
    customer_phone                VARCHAR(20),
    status                        VARCHAR(255)   NOT NULL,
    delivery_type                 VARCHAR(255)   NOT NULL,
    delivery_address              TEXT,
    delivery_date                 TIMESTAMP WITHOUT TIME ZONE,
    special_instructions          TEXT,
    subtotal                      DECIMAL(10, 2) NOT NULL,
    tax_amount                    DECIMAL(10, 2) NOT NULL,
    discount_amount               DECIMAL(10, 2),
    delivery_fee                  DECIMAL(10, 2),
    total_amount                  DECIMAL(10, 2) NOT NULL,
    discount_code                 VARCHAR(50),
    discount_percentage           DECIMAL(5, 2),
    created_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                    TIMESTAMP WITHOUT TIME ZONE,
    confirmed_at                  TIMESTAMP WITHOUT TIME ZONE,
    completed_at                  TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at                  TIMESTAMP WITHOUT TIME ZONE,
    cancellation_reason           TEXT,
    estimated_preparation_minutes INTEGER,
    estimated_ready_time          TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

ALTER TABLE orders
    ADD CONSTRAINT uc_orders_order_number UNIQUE (order_number);

CREATE INDEX idx_order_date ON orders (created_at);

CREATE INDEX idx_order_delivery_date ON orders (delivery_date);

CREATE INDEX idx_order_item_product ON order_items (product_id);

CREATE INDEX idx_order_number ON orders (order_number);

CREATE INDEX idx_order_status ON orders (status);

CREATE INDEX idx_order_user ON orders (user_id);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

CREATE INDEX idx_order_item_order ON order_items (order_id);
