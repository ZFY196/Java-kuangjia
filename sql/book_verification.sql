-- 图书实验观察用查询，仅查询，不修改数据。
USE mybatisdb;

-- 1. 初始样例：10本图书，其中9本在售，合计定价681.80元。
SELECT COUNT(*) AS book_count, SUM(status = 1) AS on_sale_count, SUM(price) AS total_price
FROM book WHERE deleted = 0;

-- 2. 一对一：书籍及详情；多对一：书籍及出版社。
SELECT b.book_id, b.book_title, b.price, p.publisher_name, d.isbn, d.print_date
FROM book b
LEFT JOIN publisher p ON p.publisher_id = b.publisher_id AND p.deleted = 0
LEFT JOIN book_detail d ON d.book_id = b.book_id
WHERE b.deleted = 0 ORDER BY b.book_id;

-- 3. 多对多：按书籍展示合著作者。
SELECT b.book_id, b.book_title, GROUP_CONCAT(a.author_name ORDER BY a.author_id SEPARATOR '、') AS authors
FROM book b
LEFT JOIN book_author ba ON ba.book_id = b.book_id
LEFT JOIN author a ON a.author_id = ba.author_id
WHERE b.deleted = 0 GROUP BY b.book_id, b.book_title ORDER BY b.book_id;

-- 4. 分组聚合，包括下架但未逻辑删除的图书。
SELECT publisher, COUNT(*) AS book_count, AVG(price) AS average_price
FROM book WHERE deleted = 0 GROUP BY publisher ORDER BY publisher;

-- 5. 外键关系应完整；三个 orphan_count 均为0。
SELECT '图书-出版社' AS relation_name, COUNT(*) AS orphan_count
FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id
WHERE b.publisher_id IS NOT NULL AND p.publisher_id IS NULL
UNION ALL
SELECT '详情-图书', COUNT(*) FROM book_detail d LEFT JOIN book b ON b.book_id = d.book_id WHERE b.book_id IS NULL
UNION ALL
SELECT '图书-作者', COUNT(*) FROM book_author ba
LEFT JOIN book b ON b.book_id = ba.book_id LEFT JOIN author a ON a.author_id = ba.author_id
WHERE b.book_id IS NULL OR a.author_id IS NULL;
