# Straw Bed Modify

一个 Minecraft Fabric 模组，增强原版稻草床的行为，添加**行军稻草床**方块和可配置的游戏规则。

## 功能

###  行军稻草床（Marching Straw Bed）

新增一种不会在睡醒后消失的稻草床。与原版稻草床不同，它会在使用后**保持放置状态**，适合长期使用，但会受到本模组`explode`、`equiv`和`spawnpoint`命令影响。

###  `/strawbed` 命令

通过四个游戏规则灵活控制稻草床行为：

| 子命令 | 默认值 | 说明 |
|--------|--------|------|
| `/strawbed spawnpoint <true\|false>` | `false` | 睡醒后是否设置玩家出生点 |
| `/strawbed explode <true\|false>` | `false` | 在下界/末地点击时是否爆炸 |
| `/strawbed vanish <true\|false>` | `true` | 睡醒后稻草床是否消失 |
| `/strawbed equiv <default\|0~32>` | `default` | 爆炸力度，仅在 `explode` 为 `true` 时生效 |

### 爆炸力度（`equiv`）

`default` 表示沿用**原版的爆炸力度 5.0**（即 `BedBlock` 里 `level.explode(..., 5.0F, ...)` 的取值），这也是该项的默认值；玩家可改为 `0`~`64` 之间的任意整数。

- `0` —— 仍有爆炸的音效和粒子，但半径为 0，不破坏方块、不造成伤害
- `default` —— 5.0，与原版床一致
- 数值越大，破坏范围和伤害越高（原版 TNT 为 4，床/重生锚为 5，凋灵为 7）
- **破坏半径 = 数值 × 2**，上限 `32` 即半径 64 格。实测更大的值会明显拖慢服务端，故设此上限；超出范围的输入会被拒绝，读取时也会被收敛到 `32`

> 旧版本存档里已存的更大值（例如 `64`）**仍可正常加载**，只是按上限 `32` 生效 —— 上限是靠输入校验 + 读取时收敛实现的，没有收窄存档解码范围（收窄会让整份 `game_rules.dat` 解码失败，把该存档所有游戏规则重置）。

> 内部实现上 `default` 存为哨兵值 `-1`，因此 `/gamerule` 里会显示 `-1`（已在规则名中标注）；用 `/strawbed equiv default` 复位即可，无需手输 -1。

###  维度适配

- **主世界**：正常使用，夜晚睡觉
- **下界/末地**：点击床会触发破坏（爆炸与否由 `explode` 规则控制）
- 无论在哪个维度，**永远不会**在危险维度设置出生点

### 与原版规则的优先级

自 MC 26.3 起，原版稻草床的行为改由数据包控制 —— 在 `dimension_type` 的 `minecraft:gameplay/straw_bed_rule` 里配置（字段：`can_set_spawn`、`can_sleep`、`destroy_on_use`、`destroy_on_leave`）。主世界默认 `destroy_on_leave: true`（睡醒即消失），下界/末地为 `destroy_on_use: true`。

**本模组的游戏规则优先级高于原版数据包**，会影响原版稻草床：

| 模组规则 | 覆盖的原版字段 | 说明 |
|---------|--------------|------|
| `/strawbed spawnpoint` | `straw_bed_rule.can_set_spawn` | 原版固定为 `never`，模组可放开（危险维度仍然禁止） |
| `/strawbed vanish` | `straw_bed_rule.destroy_on_leave` | 模组规则始终生效，数据包设置会被忽略 |

> ⚠️ 安装本模组后，数据包对上述两个字段的配置**将不再生效**。`can_sleep` 与 `destroy_on_use` 不受影响，仍由原版数据包决定；`explode` 规则作用于破坏时的爆炸效果，不改动原版字段。

## 技术实现

- **Mod 加载器**：Fabric Loader ≥ 0.19.5
- **Minecraft 版本**：≥ 26.3（1.21.11+）
- **核心机制**：通过 Mixin 注入 `AbstractBedBlock.getBedRule()` 和 `StrawBedBlock` 的破坏逻辑
- **自定义方块**：`MarchingStrawBedBlock` 继承 `StrawBedBlock`，重写关键方法实现持久化

## 项目结构

```
src/
├── main/java/com/yourname/straw_bed_modify/
│   ├── Straw_bed_modify.java          # Mod 入口，注册方块、物品、命令
│   ├── StrawBedGameRules.java         # 四个自定义游戏规则
│   ├── block/
│   │   └── MarchingStrawBedBlock.java # 行军稻草床方块逻辑
│   ├── command/
│   │   └── StrawBedCommand.java       # /strawbed 命令
│   └── mixin/
│       ├── AbstractBedBlockMixin.java # 注入床规则，应用游戏规则
│       └── StrawBedBlockMixin.java    # 注入破坏/消失行为
├── main/resources/
│   ├── fabric.mod.json
│   ├── straw_bed_modify.mixins.json
│   └── assets/straw_bed_modify/
│       ├── blockstates/
│       ├── models/block/
│       ├── items/
│       └── textures/
└── client/java/...                    # 客户端入口 & 数据生成
```

## 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/`。

## 许可

本项目代码采用 [MIT License](LICENSE.txt) 开源。

### Mojang 版权声明

本模组为 Minecraft Java 版非盈利衍生创作，依托 Fabric Loader 开发。

Minecraft 原版游戏、方块逻辑、原生资源版权归属 **Mojang Studios / Microsoft**。

本项目仅包含原创扩展代码，未分发 MC 原版源码。