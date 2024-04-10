INSERT INTO account (number_account, balance, username)
VALUES ('PL61109010140000071219812874', 10000.00, 'User1'),
       ('PL61109010140000071219812875', 15000.00, 'User2'),
       ('PL61109010140000071219812876', 15000.00, 'User3');


INSERT INTO transaction (source_account_id, destination_account_id, amount, transaction_date, title)
VALUES (1, 2, 100.00, '2023-01-04 12:00', 'Przelew 1'),
       (2, 1, 200.00, '2023-01-03 13:00', 'Przelew 2'),
       (3, 1, 200.00, '2023-01-02 13:00', 'Przelew 3'),
       (3, 1, 250.00, '2023-01-02 13:00', 'Przelew 4');
