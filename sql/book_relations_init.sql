-- 图书实验：保留一对一、多对一、一对多、双向多对多结构。
-- 先执行 book_init.sql；本脚本可重复执行。
SET NAMES utf8mb4;
CREATE SCHEMA IF NOT EXISTS mybatisdb CHARACTER SET utf8mb4;
USE mybatisdb;

CREATE TABLE IF NOT EXISTS publisher (
    publisher_id INT NOT NULL AUTO_INCREMENT COMMENT '出版社编号',
    publisher_name VARCHAR(50) NOT NULL COMMENT '出版社名称',
    city VARCHAR(50) NULL COMMENT '所在城市',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    CONSTRAINT pk_publisher PRIMARY KEY (publisher_id),
    CONSTRAINT uk_publisher_name UNIQUE (publisher_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO publisher (publisher_name, city)
SELECT samples.publisher_name, samples.city FROM (
    SELECT '星河科技出版社' AS publisher_name, '北京' AS city
    UNION ALL SELECT '知行教育出版社', '上海'
    UNION ALL SELECT '远山科学出版社', '杭州'
    UNION ALL SELECT '清风文艺出版社', '南京'
) samples
WHERE NOT EXISTS (SELECT 1 FROM publisher p WHERE p.publisher_name = samples.publisher_name);

SET @publisher_column = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'book' AND column_name = 'publisher_id');
SET @publisher_column_ddl = IF(@publisher_column = 0,
    'ALTER TABLE book ADD COLUMN publisher_id INT NULL COMMENT ''出版社编号（外键）''', 'DO 0');
PREPARE statement_to_run FROM @publisher_column_ddl;
EXECUTE statement_to_run;
DEALLOCATE PREPARE statement_to_run;

SET @publisher_fk = (SELECT COUNT(*) FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE() AND table_name = 'book' AND column_name = 'publisher_id'
      AND referenced_table_name = 'publisher');
SET @publisher_fk_ddl = IF(@publisher_fk = 0,
    'ALTER TABLE book ADD CONSTRAINT fk_book_publisher FOREIGN KEY (publisher_id) REFERENCES publisher(publisher_id)', 'DO 0');
PREPARE statement_to_run FROM @publisher_fk_ddl;
EXECUTE statement_to_run;
DEALLOCATE PREPARE statement_to_run;

UPDATE book b JOIN publisher p ON p.publisher_name = b.publisher
SET b.publisher_id = p.publisher_id WHERE b.publisher_id IS NULL;

CREATE TABLE IF NOT EXISTS book_detail (
    detail_id INT NOT NULL AUTO_INCREMENT COMMENT '详情编号',
    book_id INT NOT NULL COMMENT '图书编号，唯一外键',
    isbn VARCHAR(30) NOT NULL COMMENT '实验用ISBN标识',
    print_date DATE NULL COMMENT '印刷日期',
    CONSTRAINT pk_book_detail PRIMARY KEY (detail_id),
    CONSTRAINT uk_book_detail_book UNIQUE (book_id),
    CONSTRAINT fk_book_detail_book FOREIGN KEY (book_id) REFERENCES book (book_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ISBN仅作为实验样例标识，不代表真实出版物书号。
INSERT INTO book_detail (book_id, isbn, print_date)
SELECT b.book_id, CONCAT('LAB-BOOK-', LPAD(b.book_id, 6, '0')), b.publish_date FROM book b
WHERE NOT EXISTS (SELECT 1 FROM book_detail d WHERE d.book_id = b.book_id);

CREATE TABLE IF NOT EXISTS author (
    author_id INT NOT NULL AUTO_INCREMENT COMMENT '作者编号',
    author_name VARCHAR(50) NOT NULL COMMENT '作者姓名',
    birth_date DATE NULL COMMENT '出生日期',
    CONSTRAINT pk_author PRIMARY KEY (author_id),
    CONSTRAINT uk_author_name UNIQUE (author_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 书名、作者、出版社均为虚构的教学样例。
INSERT INTO author (author_name, birth_date)
SELECT samples.author_name, samples.birth_date FROM (
    SELECT '林知远' AS author_name, CAST('1982-06-18' AS DATE) AS birth_date
    UNION ALL SELECT '陈书言', '1988-03-12'
    UNION ALL SELECT '周明川', '1985-09-25'
    UNION ALL SELECT '许清和', '1990-11-08'
    UNION ALL SELECT '沈知夏', '1987-05-16'
    UNION ALL SELECT '顾南星', '1992-02-20'
) samples
WHERE NOT EXISTS (SELECT 1 FROM author a WHERE a.author_name = samples.author_name);

CREATE TABLE IF NOT EXISTS book_author (
    book_id INT NOT NULL COMMENT '图书编号',
    author_id INT NOT NULL COMMENT '作者编号',
    CONSTRAINT pk_book_author PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_author_book FOREIGN KEY (book_id) REFERENCES book (book_id),
    CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES author (author_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO book_author (book_id, author_id)
SELECT b.book_id, a.author_id FROM book b JOIN author a ON (
       (a.author_name = '林知远' AND b.book_title IN ('MyBatis从入门到实践', 'Spring框架实验教程', 'Java程序设计基础'))
    OR (a.author_name = '陈书言' AND b.book_title IN ('MyBatis从入门到实践', '数据库原理与SQL实践', '软件工程案例选编'))
    OR (a.author_name = '周明川' AND b.book_title IN ('数据结构与算法实验', '计算机网络图解'))
    OR (a.author_name = '许清和' AND b.book_title IN ('Python数据分析入门', '人工智能通识'))
    OR (a.author_name = '沈知夏' AND b.book_title IN ('数据库原理与SQL实践', '数据结构与算法实验'))
    OR (a.author_name = '顾南星' AND b.book_title IN ('人工智能通识', '阅读与写作练习'))
)
WHERE NOT EXISTS (SELECT 1 FROM book_author linked
    WHERE linked.book_id = b.book_id AND linked.author_id = a.author_id);
