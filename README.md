# Asaka (Codegen Frame)

一个类似 GraalVM 上 Truffle 的东西

## 注意

以下为2024.3时编写的README, 4月时因为个人原因项目搁浅了, 目前仍在继续开发.

**Asaka 目前只是一个半成品! 不建议投入生产环境, 甚至不保证可以正常使用!**

## 有什么用?

让你5分钟就能设计一个语言原型, 并可以直接编译到 JVM 字节码, 无运行时开销地运行.  
具体怎么做?
你只需要学会:

- ANTLR4 语法
- 一个小小的转译器用法

## 思想

见知乎 [Why Concrete Syntax Doesnt Matter](https://zhuanlan.zhihu.com/p/24756198)  
Unified abstract syntax for all concrete syntax

来自 [mh04](https://github.com/KouyouX) 的评价:

- 做一个类似**LLVM IR**的东西 然后把后端到**JVM Target**的东西写好
- 中端转译器(Transpiler)手写 (ANTLR4 AST -> Asahi AST)
- 前端ANTLR4 codegen

### 结构

- 一棵统一的抽象语法树(AST) (✅)
- 一个完整的语法分析器 (✅/todo)
- 一门完整的代码生成器 (todo)
- 一套优雅无比的标准库 (todo)

### 未完待续...