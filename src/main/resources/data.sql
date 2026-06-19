-- Insertar Clientes
INSERT INTO customer (name, email, segment) VALUES ('Ada Lovelace', 'ada@example.com', 'STANDARD');
INSERT INTO customer (name, email, segment) VALUES ('Alan Turing', 'alan@example.com', 'VIP');
INSERT INTO customer (name, email, segment) VALUES ('Grace Hopper', 'grace@example.com', 'PREMIUM');

-- Insertar Transacciones para Ada (customer_id = 1)
INSERT INTO transaction (amount, status, credit_card_number, customer_id) 
VALUES (1500.50, 'APPROVED', '4532-1111-2222-3333', 1);

INSERT INTO transaction (amount, status, credit_card_number, customer_id) 
VALUES (350.00, 'APPROVED', '4532-1111-2222-3333', 1);

-- Insertar Transacciones para Alan (customer_id = 2)
INSERT INTO transaction (amount, status, credit_card_number, customer_id) 
VALUES (5000.00, 'REJECTED', '5555-4444-3333-2222', 2);
