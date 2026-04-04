# 时间字段约定

`bank-core` 里的时间字段需要在 API、应用、持久化、审计这几层保持一致，否则后面做对账、计提、补偿和审计时会非常容易出错。

## 1. Java 类型约定

- 业务日期使用 `LocalDate`
- 业务时间点使用 `LocalDateTime`
- 不在领域模型和 DTO 中直接使用 `java.util.Date`
- 不在领域模型和 DTO 中直接使用 `Timestamp`

适用场景：

- `lastAccrualDate`
  - 使用 `LocalDate`
- `occurredAt / createdAt / updatedAt`
  - 使用 `LocalDateTime`

## 2. 数据库字段约定

- `LocalDate` 对应数据库 `DATE`
- `LocalDateTime` 对应数据库 `DATETIME` 或测试库中的 `TIMESTAMP`
- 不在业务表中混用字符串时间字段

这样做的目标是：

- 降低时区歧义
- 保持 MyBatis Plus 映射清晰
- 便于对账和审计按业务时间口径查询

## 3. API 序列化约定

HTTP JSON 一律使用 ISO-8601 字符串，不使用时间戳。

示例：

```json
{
  "occurredAt": "2026-04-04T20:15:30",
  "lastAccrualDate": "2026-04-04"
}
```

当前项目已显式配置：

- `spring.jackson.serialization.write-dates-as-timestamps=false`
- `spring.jackson.deserialization.adjust-dates-to-context-time-zone=false`

工具类侧统一走：

- `com.payhub.bankcore.common.JacksonUtils`

## 4. MyBatis Plus 约定

当前版本下，MyBatis Plus 已可直接处理常见 Java 8 时间类型，但工程层面仍要求：

- DO 层字段显式使用 `LocalDate` / `LocalDateTime`
- 不手写多余的字符串时间中转字段
- 通过测试验证关键表的时间字段读写

当前已覆盖：

- `core_account.last_accrual_date`
- `core_transaction.occurred_at`
- `core_transaction.created_at`

## 5. 审计日志约定

审计快照中的时间字段统一按 JSON 字符串存储，不拼接文本。

要求：

- 审计快照必须为结构化 JSON
- 时间字段通过 `JacksonUtils.toJson(...)` 输出
- 审计中保留业务发生时间和系统创建时间

这样做的好处是：

- 后续可以按 JSON 字段抽取
- 时间格式在审计和 API 间保持一致
- 便于问题排查和运营查询
