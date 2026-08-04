# Flyway 数据库迁移

将 PostgreSQL 迁移脚本放入此目录，并使用以下格式命名：

```text
V{version}__{description}.sql
```

示例：

```text
V1__create_audio_uploads.sql
V2__add_audio_upload_status.sql
```

不要修改已在共享环境中执行过的迁移文件。每次 Schema 变更都应创建一个新的迁移文件。
