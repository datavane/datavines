## Design

每个engine代表一个执行引擎，每个执行引擎都有自己的执行模式，但是它们必须包括以下组件
- ConfigurationParser
    - 负责将标准的datavinesConfiguration转换成每个引擎自己特有的Configuration
- Execution
    - 负责解析Configuration,执行具体的逻辑
- Connector
    - 每个引擎都有自己独有的Connector,需要保证不同的引擎能支撑多种Connector
