# Straw Bed Modify

一个 Minecraft Fabric 模组，增强原版稻草床的行为，添加**行军稻草床**方块和可配置的游戏规则。

## 功能

###  行军稻草床（Marching Straw Bed）

新增一种不会在睡醒后消失的稻草床。与原版稻草床不同，它会在使用后**保持放置状态**，适合长期使用，但会受到本模组`explode`和`spawnpoint`命令影响。

###  `/strawbed` 命令

通过三个游戏规则灵活控制稻草床行为：

| 子命令 | 默认值 | 说明 |
|--------|--------|------|
| `/strawbed spawnpoint <true\|false>` | `false` | 睡醒后是否设置玩家出生点 |
| `/strawbed explode <true\|false>` | `false` | 在下界/末地点击时是否爆炸 |
| `/strawbed vanish <true\|false>` | `true` | 睡醒后稻草床是否消失 |

###  维度适配

- **主世界**：正常使用，夜晚睡觉
- **下界/末地**：点击床会触发破坏（爆炸与否由 `explode` 规则控制）
- 无论在哪个维度，**永远不会**在危险维度设置出生点

## 技术实现

- **Mod 加载器**：Fabric Loader ≥ 0.19.3
- **Minecraft 版本**：≥ 26.3-alpha（1.21.11+）
- **核心机制**：通过 Mixin 注入 `AbstractBedBlock.getBedRule()` 和 `StrawBedBlock` 的破坏逻辑
- **自定义方块**：`MarchingStrawBedBlock` 继承 `StrawBedBlock`，重写关键方法实现持久化

## 项目结构

```
src/
├── main/java/com/yourname/straw_bed_modify/
│   ├── Straw_bed_modify.java          # Mod 入口，注册方块、物品、命令
│   ├── StrawBedGameRules.java         # 三个自定义游戏规则
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