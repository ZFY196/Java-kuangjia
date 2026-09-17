-- =====================================================
--  MyBatis Demo 数据库初始化脚本
--  数据库名：test（与 db.properties 中的 jdbc.url 一致）
--  MySQL 8.x 语法
-- =====================================================

-- 1. 创建数据库（如已存在则忽略）
CREATE DATABASE IF NOT EXISTS `test`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 2. 使用数据库
USE `test`;

-- 3. 创建用户表 user
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(100) DEFAULT NULL            COMMENT '密码',
    `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `create_time` DATETIME     DEFAULT NULL            COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '用户表';

-- 4. 插入示例数据
INSERT INTO `user` (`username`, `password`, `email`, `create_time`) VALUES
    ('zhangsan', '123456', 'zhangsan@example.com', NOW()),
    ('lisi',     '123456', 'lisi@example.com',     NOW()),
    ('wangwu',   '123456', 'wangwu@example.com',   NOW()),
    ('zhaoliu',  '123456', 'zhaoliu@example.com',  NOW());
