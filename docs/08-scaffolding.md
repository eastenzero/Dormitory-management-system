# 脚手架生成方案（尽量少手工）

## 1. 前端（Vue 3 + Vite + TS）
建议使用 Vite 脚手架生成：
- 生成：`npm create vite@latest dormitory-admin-web -- --template vue-ts`
- 安装依赖：`npm install`
- 增加依赖：`npm i element-plus axios pinia vue-router`

## 2. 后端（Spring Boot）
由于本机未安装 Spring CLI，建议使用 Start.Spring.io 生成：
- 下载 zip：使用 `curl` 请求 start.spring.io
- 解压到目录：`dormitory-server/`
- 依赖建议：web, security, validation, lombok, mysql, flyway, actuator

## 3. 本地数据库
- 使用真实 MySQL 8（系统服务方式安装与启动，不使用容器）

## 4. 执行方式
我可以给出一组命令并在你确认后执行（命令会联网下载依赖，默认不自动执行）。
