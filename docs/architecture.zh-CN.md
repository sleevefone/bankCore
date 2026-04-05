# 1. 架构目标

`bank-core` 的目标不是再做一个“支付服务”，而是建设一个独立的内部核心系统，专门承接支付成功后的账务确认与资金状态变更。

它应当解决这几个问题：

- 支付系统不能直接修改账户余额
- 重试请求不能导致重复记账
- 每一笔账务处理都要可追踪、可查询、可审计
- 需要支持同步返回和异步补偿并存
- 后续要能扩展退款、冻结、解冻、冲正、对账、结算

这意味着 `bank-core` 的定位更接近“账务核心 + 交易内核”，而不是“支付渠道聚合器”。

# 2. 系统边界

## 2.1 `pay-hello` 负责什么

支付系统保留以下职责：

- 接受商户或业务系统发起的支付请求
- 创建支付订单并维护支付主状态
- 路由支付渠道
- 处理渠道同步响应与异步回调
- 在渠道确认后触发核心记账
- 在核心状态未知时发起补偿查询

## 2.2 `bank-core` 负责什么

核心系统承接以下职责：

- 管理账户与余额
- 管理账号、账号序号与客户号映射
- 管理账务主单
- 生成账务分录
- 管理会计科目与账户挂接关系
- 执行余额增减、冻结、解冻
- 管理利息规则、利息明细与计提结果
- 保证账务请求幂等
- 记录交易历史与审计轨迹
- 提供记账结果查询能力

## 2.3 明确不放进第一版的内容

第一版先不把这些能力做进去：

- 渠道适配器
- 商户支付接口
- 风控规则引擎
- 清结算批处理
- 总账会计科目引擎
- 多币种汇率处理

这些内容未来都可以接进来，但不应阻塞第一版落地。

# 3. 总体架构

建议将 `bank-core` 设计为一个独立的 Spring Boot 服务，对支付系统暴露内部 API。

建议分层如下：

- `controller`
  - 对内 HTTP 接口
  - 参数校验
  - 错误码转换
- `application`
  - 编排记账命令
  - 管理事务边界
  - 协调领域对象与仓储
- `domain`
  - 账户模型
  - 客户与账户映射模型
  - 交易模型
  - 分录模型
  - 科目模型
  - 利息与计提模型
  - 余额规则
  - 幂等规则
- `infrastructure`
  - MyBatis/MyBatis-Plus
  - 数据库仓储实现
  - HTTP/MQ 适配器
  - 事件发布
- `integration`
  - 对 `pay-hello` 暴露的 DTO
  - 供未来其他内部系统复用的网关契约

可以用下面这条链路理解它和支付系统的关系：

1. `pay-hello` 渠道成功
2. `pay-hello` 调用 `bank-core`
3. `bank-core` 做幂等校验与账务处理
4. `bank-core` 返回处理结果
5. `pay-hello` 更新 `coreStatus`
6. 若超时或未知，则由 `pay-hello` 补偿查询核心结果

# 4. 领域模型

第一版建议至少定义以下核心对象。

## 4.1 Account

表示一个真实可记账账户。

关键字段建议：

- `accountNo`
- `accountSeqNo`
- `customerNo`
- `accountType`
- `subjectCode`
- `normalBalanceDirection`
- `ownerId`
- `currency`
- `availableBalance`
- `frozenBalance`
- `status`

职责：

- 校验账户是否允许记账
- 绑定客户号和账户序号等审计标识
- 维护账户所属科目和正常余额方向
- 管理可用余额与冻结余额
- 执行入账、出账、冻结、解冻

## 4.2 CoreTransaction

表示一笔账务主单，是核心系统对外暴露的交易单元。

关键字段建议：

- `coreTxnId`
- `requestId`
- `bizOrderId`
- `bizType`
- `txnType`
- `amount`
- `currency`
- `debitAccountNo`
- `creditAccountNo`
- `status`
- `failureCode`
- `failureMessage`

职责：

- 关联外部业务单
- 管理核心交易状态
- 作为幂等返回的载体

## 4.3 LedgerEntry

表示一笔核心交易拆分出来的账务分录。

关键字段建议：

- `entryId`
- `coreTxnId`
- `accountNo`
- `accountSeqNo`
- `customerNo`
- `subjectCode`
- `entryDirection`
- `dcDirection`
- `amount`
- `currency`
- `sequenceNo`
- `balanceBefore`
- `balanceAfter`

职责：

- 保存借贷方向明确的账务明细
- 绑定账号序号、客户号和科目，满足审计穿透要求
- 支撑对账、审计、重建余额

## 4.4 Subject

表示核心账务中的科目主数据。

关键字段建议：

- `subjectCode`
- `subjectName`
- `subjectLevel`
- `parentSubjectCode`
- `normalBalanceDirection`
- `interestBearing`
- `status`

职责：

- 定义账户归属的会计科目
- 约束分录借贷方向与余额方向
- 为利息计提和监管审计提供会计口径

## 4.5 InterestDetail

表示账户或科目维度下的利息信息。

关键字段建议：

- `interestDetailId`
- `accountNo`
- `accountSeqNo`
- `customerNo`
- `subjectCode`
- `interestRate`
- `interestBaseAmount`
- `accruedInterest`
- `settledInterest`
- `lastAccrualDate`

职责：

- 存储利率、计息基数和已计提利息
- 记录利息计算结果的来源与口径
- 支撑后续结息、冲销和审计复核

## 4.6 AccrualRecord

表示每日或周期性计提结果。

关键字段建议：

- `accrualId`
- `businessDate`
- `accountNo`
- `accountSeqNo`
- `customerNo`
- `subjectCode`
- `accrualType`
- `accrualAmount`
- `dcDirection`
- `status`

职责：

- 保存每次计提动作和结果
- 为利息结转、利润确认和审计抽样提供依据
- 支撑补计提和重跑校验

## 4.7 IdempotencyRecord

表示一次外部业务请求在核心系统中的幂等占位。

关键字段建议：

- `idempotencyKey`
- `requestId`
- `bizOrderId`
- `txnType`
- `status`
- `boundCoreTxnId`

职责：

- 防止相同请求被重复记账
- 在请求超时和重试时返回既有结果

## 4.8 TransactionHistory

表示核心交易状态流转历史。

职责：

- 记录每次状态迁移
- 保留审计信息
- 支撑问题排查

## 4.9 AuditLog

表示面向架构审计和运营追踪的审计日志。

关键字段建议：

- `auditId`
- `entityType`
- `entityId`
- `operationType`
- `operatorId`
- `traceId`
- `beforeSnapshot`
- `afterSnapshot`
- `createdAt`

职责：

- 记录主数据和账务数据的变更前后镜像
- 串联请求链路、人工操作和系统任务
- 为审计、排障和合规检查提供可追溯证据

# 5. 状态设计

第一版建议核心交易状态单独维护，不直接复用支付聚合状态，但要能和 `pay-hello` 的 `CoreStatus` 清晰映射。

建议状态：

- `INIT`
- `PROCESSING`
- `SUCCESS`
- `FAILED`
- `UNKNOWN`

状态含义：

- `INIT`
  - 请求刚进入系统，尚未完成实际账务动作
- `PROCESSING`
  - 正在执行账务处理
- `SUCCESS`
  - 已完成记账，结果最终确定
- `FAILED`
  - 已确定失败，业务上不可重试或需显式冲正
- `UNKNOWN`
  - 无法在当前请求内确认最终结果，需要查单或补偿

与 `pay-hello` 的映射关系建议保持简单：

- 核心 `SUCCESS` -> 支付侧 `coreStatus = SUCCESS`
- 核心 `FAILED` -> 支付侧 `coreStatus = FAILED`
- 核心 `INIT/PROCESSING/UNKNOWN` -> 支付侧 `coreStatus = UNKNOWN` 或 `SUBMITTING`

# 6. 交易类型

第一版不要一次做全，建议按最小闭环推进。

## 6.1 第一版必须支持

- `PAY_IN`
  - 支付成功后入账

## 6.2 第二版再加入

- `PAY_OUT`
  - 退款、提现或付款出账
- `FREEZE`
  - 冻结余额
- `UNFREEZE`
  - 解冻余额
- `REVERSE`
  - 冲正已入账交易

这样可以先把“支付成功 -> 核心入账成功”跑通，再逐步拓展账务能力。

# 7. 接口设计

第一版建议提供两个核心接口即可。

## 7.1 提交核心交易

`POST /core/transactions`

请求字段建议：

- `requestId`
- `bizOrderId`
- `txnType`
- `bizType`
- `amount`
- `currency`
- `debitAccountNo`
- `creditAccountNo`
- `occurredAt`
- `extension`

响应字段建议：

- `coreTxnId`
- `requestId`
- `bizOrderId`
- `status`
- `success`
- `retryable`
- `rawCode`
- `rawMessage`

语义要求：

- 相同幂等键重复请求必须返回同一笔结果
- 如果已成功记账，不能再次执行
- 如果系统处理结果不明确，应返回可查询标识

## 7.2 查询核心交易结果

`GET /core/transactions/{bizOrderId}`

或：

`GET /core/transactions?requestId=...`

用途：

- 供 `pay-hello` 在超时、失败重试、补偿任务里查最终状态

# 8. 事务边界

这个系统的关键不是“把逻辑写进一个事务”，而是把事务切对。

建议边界如下：

1. 接收请求并校验幂等键
2. 在本地事务中创建或锁定 `IdempotencyRecord`
3. 在本地事务中创建 `CoreTransaction`
4. 在同一事务中写入 `LedgerEntry`、更新 `Account`
5. 提交事务后返回结果

如果未来引入外部依赖，例如通知、消息发布、清算系统回执，则应使用：

- 本地事务 + outbox
- 异步投递

不要把外部调用和核心账务更新混在同一个长事务里。

# 9. 数据模型建议

第一版建议先建这几张表。

统一主键规范：

- 所有表都必须包含 `id` 作为数据库自增逻辑主键
- 所有业务标识都必须与逻辑主键分离，不能直接拿业务号充当表主键
- 业务主键、业务唯一键、外部关联键需要按业务语义单独设计
- `account_no`、`subject_code`、`core_txn_id`、`biz_order_id`、`request_id`、`idempotency_key` 都属于业务标识，不属于表逻辑主键
- 支付订单表如 `pay_order` 也统一遵循该规范，即 `id` 为逻辑主键，`order_id` 或 `biz_order_id` 为业务主键

## 9.1 `core_account`

用途：

- 存账户主数据与实时余额

主键与唯一键建议：

- `id` 为逻辑主键
- `account_no` 为账户业务主键
- `account_seq_no` 为账户序号业务标识
- `uk_core_account_account_no`
- `uk_core_account_account_seq_no`

建议字段：

- `id`
- `account_no`
- `account_seq_no`
- `customer_no`
- `account_type`
- `subject_code`
- `normal_balance_direction`
- `owner_id`
- `currency`
- `available_balance`
- `frozen_balance`
- `status`
- `interest_rate`
- `last_accrual_date`
- `created_at`
- `updated_at`

## 9.2 `core_subject`

用途：

- 存科目主数据与会计方向定义

主键与唯一键建议：

- `id` 为逻辑主键
- `subject_code` 为科目业务主键
- `uk_core_subject_subject_code`

建议字段：

- `id`
- `subject_code`
- `subject_name`
- `subject_level`
- `parent_subject_code`
- `normal_balance_direction`
- `interest_bearing`
- `status`
- `created_at`
- `updated_at`

## 9.3 `core_transaction`

用途：

- 存交易主单

主键与唯一键建议：

- `id` 为逻辑主键
- `core_txn_id` 为核心交易业务主键
- `request_id` 为幂等业务键
- `biz_order_id` 为支付侧业务订单号
- 逻辑主键、自身业务主键、外部业务键必须分离

建议字段：

- `id`
- `core_txn_id`
- `request_id`
- `biz_order_id`
- `biz_type`
- `txn_type`
- `amount`
- `currency`
- `debit_account_no`
- `credit_account_no`
- `status`
- `failure_code`
- `failure_message`
- `created_at`
- `updated_at`

关键约束建议：

- `uk_core_txn_core_txn_id`
- `uk_core_txn_request_id`
- `idx_core_txn_biz_order_id`
- `idx_core_txn_status`

## 9.4 `core_ledger_entry`

用途：

- 存账务分录

主键与唯一键建议：

- `id` 为逻辑主键
- `core_txn_id + entry_no` 为分录业务唯一键
- 分录流水号不能替代逻辑主键

建议字段：

- `id`
- `core_txn_id`
- `entry_no`
- `account_no`
- `account_seq_no`
- `customer_no`
- `subject_code`
- `entry_direction`
- `dc_direction`
- `amount`
- `currency`
- `balance_before`
- `balance_after`
- `sequence_no`
- `created_at`

## 9.5 `core_interest_detail`

用途：

- 存账户利息详情

主键与唯一键建议：

- `id` 为逻辑主键
- 账户标识与利息维度字段承担业务识别职责
- 如果一户一条利息汇总，可增加 `uk_account_no`
- 如果一户多条分期明细，应按业务日或利息期间增加唯一键

建议字段：

- `id`
- `account_no`
- `account_seq_no`
- `customer_no`
- `subject_code`
- `interest_rate`
- `interest_base_amount`
- `accrued_interest`
- `settled_interest`
- `last_accrual_date`
- `created_at`
- `updated_at`

## 9.6 `core_accrual_record`

用途：

- 存每日或周期计提明细

主键与唯一键建议：

- `id` 为逻辑主键
- `account_no + account_seq_no + business_date + accrual_type` 可作为计提业务唯一键候选
- 计提业务唯一键与逻辑主键必须分离

建议字段：

- `id`
- `business_date`
- `account_no`
- `account_seq_no`
- `customer_no`
- `subject_code`
- `accrual_type`
- `dc_direction`
- `accrual_amount`
- `status`
- `created_at`

## 9.7 `core_idempotency_record`

用途：

- 存幂等记录

主键与唯一键建议：

- `id` 为逻辑主键
- `idempotency_key` 为幂等业务主键
- `request_id` 与 `biz_order_id + txn_type` 为辅助防重键
- 幂等业务键不能替代表逻辑主键

建议字段：

- `id`
- `idempotency_key`
- `request_id`
- `biz_order_id`
- `txn_type`
- `status`
- `core_txn_id`
- `created_at`
- `updated_at`

## 9.8 `core_transaction_history`

用途：

- 存交易状态变化轨迹

主键与唯一键建议：

- `id` 为逻辑主键
- `core_txn_id + from_status + to_status + created_at` 为历史识别组合

建议字段：

- `id`
- `core_txn_id`
- `from_status`
- `to_status`
- `reason_code`
- `reason_message`
- `operator_id`
- `created_at`

## 9.9 `core_audit_log`

用途：

- 存面向架构审计的统一操作日志

主键与唯一键建议：

- `id` 为逻辑主键
- `entity_type + entity_id + operation_type + created_at` 为审计识别组合

建议字段：

- `id`
- `entity_type`
- `entity_id`
- `operation_type`
- `operator_id`
- `trace_id`
- `before_snapshot`
- `after_snapshot`
- `created_at`

# 10. 架构审计要求

如果这个系统要通过账务和合规视角的架构审计，第一版就应明确以下约束：

- 所有业务表都必须有自增逻辑主键 `id`
- 所有业务主键都必须与逻辑主键分离，禁止把业务号直接设计成表主键
- 所有表统一遵循“逻辑主键 + 业务主键/业务唯一键”的双层标识模型
- `pay_order`、`core_transaction`、`core_account`、`core_subject`、`core_idempotency_record` 等表都必须统一执行这条规范
- 每笔分录都必须能追溯到账号、账号序号、客户号、科目和借贷方向
- 每次余额变化都必须能从交易主单和分录重建
- 利息与计提必须可追溯到利率、基数、业务日和会计科目
- 主数据变更和账务变更都必须保留审计日志
- 关键唯一键必须覆盖 `requestId`、`accountSeqNo`、`subjectCode` 等核心标识
- 查询接口必须支持按交易号、账号、客户号和业务日回溯

# 11. 幂等设计

这是第一版里最不能省的能力。

建议幂等键优先级如下：

1. `requestId`
2. `bizOrderId + txnType`

处理原则：

- 首次请求占位成功后再执行记账
- 重复请求若已成功，直接返回历史结果
- 重复请求若处理中，返回处理中状态
- 重复请求若状态未知，允许查询，不允许直接重复落账

注意：

- 幂等防重不能只放在应用内存中
- 必须依赖数据库唯一约束或悲观锁/乐观锁保证并发安全

# 12. 对接支付系统的流程

建议第一版按下面的时序工作。

## 11.1 正常成功链路

1. `pay-hello` 发起支付
2. 渠道返回成功
3. `pay-hello` 调 `POST /core/transactions`
4. `bank-core` 幂等校验成功
5. `bank-core` 创建核心交易主单
6. `bank-core` 写入分录
7. `bank-core` 更新账户余额
8. `bank-core` 返回 `SUCCESS`
9. `pay-hello` 将 `coreStatus` 更新为 `SUCCESS`
10. `pay-hello` 聚合支付状态为 `SUCCESS`

## 11.2 超时或未知链路

1. `pay-hello` 调核心超时
2. `pay-hello` 将 `coreStatus` 标记为 `UNKNOWN`
3. 后台补偿任务按 `requestId` 或 `bizOrderId` 查核心结果
4. 若核心已成功，则支付侧补记 `SUCCESS`
5. 若核心失败，则支付侧补记 `FAILED`

这个模式和你当前 `pay-hello` 的三状态设计是兼容的。

# 13. 演进路线

建议按三期推进。

## 12.1 Phase 1

目标：

- 跑通最小闭环

范围：

- Spring Boot 工程骨架
- `Account/CoreTransaction/LedgerEntry`
- `POST /core/transactions`
- `GET /core/transactions`
- MySQL 初版表结构
- `PAY_IN`
- 幂等保证

## 12.2 Phase 2

目标：

- 提高可靠性和可运维性

范围：

- 交易历史表
- 失败码体系
- 补偿查询任务
- outbox 事件
- 与 `pay-hello` 的真实适配器联调

## 12.3 Phase 3

目标：

- 支撑更复杂的支付与账务场景

范围：

- 冻结/解冻
- 退款/冲正
- 清结算扩展
- 审计报表
- 运营修复工具
- 风控联动接口

# 14. 第一版落地建议

真正开始编码时，推荐先坚持这三个约束：

- 先做单体服务，不急着拆微服务
- 先做 MySQL 持久化，不急着引 MQ
- 先做 `PAY_IN`，不急着把所有交易类型铺满

只要这三个点守住，第一版就能很快从“概念设计”进入“可联调实现”。

# 15. 结论

`bank-core` 最合适的定位是：

- 支付系统下游的内部核心账务服务
- 通过稳定的核心交易接口为支付系统提供记账能力
- 用账户、交易主单、账务分录、幂等记录来构成最小可信闭环

这样设计的好处是边界清晰：

- 支付负责“这笔钱该不该付、渠道结果是什么”
- 核心负责“这笔钱在内部账上怎么记、有没有真正落账”

后面实现时，我们就可以围绕这个边界稳定往下做，而不会把支付编排和账务内核搅在一起。

# 16. 实施状态（截至 2026-04-05）

为避免文档与代码脱节，这里按“已完成/未完成”同步真实状态。

## 16.1 已完成

- [x] 工程骨架：Spring Boot + MyBatis-Plus + Flyway + H2 测试基建
- [x] 核心接口：`POST /core/transactions`、`GET /core/transactions/{bizOrderId}`、`GET /core/transactions?requestId=...`
- [x] 最小记账闭环：核心交易主单、分录、余额更新、审计日志
- [x] 交易历史：`core_transaction_history` 已接入创建主链路
- [x] 架构审计基础字段：账号序号、客户号、科目、借贷方向、余额快照
- [x] 全局异常处理与关键日志（含服务层上下文）
- [x] OpenAPI 文档入口与基础接口说明
- [x] 后管测试能力：开客户、开存款账号、开贷款账号、按客户查账号（静态测试页）

## 16.2 未完成

- [ ] 幂等内核：`core_idempotency_record` 独立占位与并发防重
- [ ] 利息与计提：`core_interest_detail`、`core_accrual_record` 业务流程
- [ ] 交易扩展：`PAY_OUT/FREEZE/UNFREEZE/REVERSE`
- [ ] 可靠性：outbox 事件、补偿任务、失败重放
- [ ] 联调：与 `pay-hello` 端到端回调补偿闭环
- [ ] 运营后管：查询、审计、异常处置正式页面与权限体系

## 16.3 Phase 进度

- [x] Phase 1：最小闭环已跑通（进行中，仍需补独立幂等占位）
- [ ] Phase 2：可靠性与可运维性（未开始）
- [ ] Phase 3：复杂交易与运营能力（未开始）
