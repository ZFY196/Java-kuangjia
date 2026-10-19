-- 图书实验：基础表及中文样例，可重复执行，不覆盖已有图书。
SET NAMES utf8mb4;
CREATE SCHEMA IF NOT EXISTS mybatisdb CHARACTER SET utf8mb4;
USE mybatisdb;

CREATE TABLE IF NOT EXISTS book (
    book_id INT NOT NULL AUTO_INCREMENT COMMENT '图书编号',
    book_title VARCHAR(50) NOT NULL COMMENT '书名',
    price DECIMAL(10, 2) NULL COMMENT '定价（元）',
    publish_date DATE NULL COMMENT '出版日期',
    status TINYINT DEFAULT 1 COMMENT '销售状态，1在售，0下架',
    publisher VARCHAR(50) NULL COMMENT '出版社名称，供单表实验使用',
    category VARCHAR(50) NULL COMMENT '图书分类',
    binding CHAR(1) DEFAULT '平' COMMENT '装帧，平为平装，精为精装',
    create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记，0正常，1已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    CONSTRAINT pk_book PRIMARY KEY (book_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO book (book_title, price, publish_date, status, publisher, category, binding)
SELECT samples.title, samples.price, samples.published_on, samples.sale_status,
       samples.publisher_name, samples.category, samples.binding
FROM (
    SELECT 'MyBatis从入门到实践' AS title, 69.80 AS price, CAST('2025-03-15' AS DATE) AS published_on,
           1 AS sale_status, '星河科技出版社' AS publisher_name, 'Java开发' AS category, '平' AS binding
    UNION ALL SELECT 'Spring框架实验教程', 89.00, '2025-06-20', 1, '星河科技出版社', 'Java开发', '精'
    UNION ALL SELECT 'Java程序设计基础', 59.80, '2024-09-01', 1, '星河科技出版社', '程序设计', '平'
    UNION ALL SELECT '数据库原理与SQL实践', 79.00, '2025-01-12', 1, '知行教育出版社', '数据库', '精'
    UNION ALL SELECT '数据结构与算法实验', 66.00, '2024-08-18', 1, '知行教育出版社', '算法设计', '平'
    UNION ALL SELECT '计算机网络图解', 72.50, '2025-07-08', 1, '知行教育出版社', '计算机基础', '精'
    UNION ALL SELECT 'Python数据分析入门', 68.00, '2025-10-10', 1, '远山科学出版社', '数据分析', '平'
    UNION ALL SELECT '人工智能通识', 88.00, '2026-02-15', 1, '远山科学出版社', '人工智能', '精'
    UNION ALL SELECT '软件工程案例选编', 49.80, '2023-05-06', 0, '星河科技出版社', '软件工程', '平'
    UNION ALL SELECT '阅读与写作练习', 39.90, '2024-04-23', 1, '清风文艺出版社', '文学', '平'
) AS samples
WHERE NOT EXISTS (SELECT 1 FROM book existing WHERE existing.book_title = samples.title);
