# AGENTS.md

## 代码风格
- 命名风格遵循 Kotlin 规范
- 单行代码块需要用花括号包裹，即不能出现 `if (condition) action()` 类似的形式，除非代码很短，例如 `val result = if (conditional) "abc" else "def"`。但 `return`、`continue`、`break` 等控制语句不允许不加花括号
- Composable 之间要空行，不只是函数声明，布局作用域内的 Composable 函数调用之间也要空行
- **严禁**出现代码挤在一行加分号的写法
- 代码合理加空行、合理换行保证可读性。但换行位置不能太少，一般控制在 160 个字符左右即可
- 少写只用一次的工具函数。只用一次的尽量内联

## 代码提交
- 提交信息格式：`<action>: <message>`，其中 `action` 建议取值如下：
  | `action` | 含义 |
  | - | - |
  | feat | 新功能 |
  | fix | 问题修复 |
  | refactor | 重构 |
  | opt | 优化 |
  | chore | 其他修改（不影响功能，例如文档、构建过程、版本号、依赖等） |
  | doc | 文档修改 |

  `message` 是对修改内容的简要描述，建议使用英文，且首字母小写。

  综合以上要求，一个符合规范的提交信息示例为 `feat: add support for multiple languages`。

## 测试
- 以人工测试为主
- 禁止写单元测试