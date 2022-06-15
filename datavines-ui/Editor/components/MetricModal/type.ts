export type TMetricModal = {
    id?: number | string | null,
    databaseName?: string,
    tableName?: string,
    columnName?: string,
}

export type TMetricParameter = {
    database: string;
    table: string;
    column: string;
    filter?: string;
    [key: string]: any;
}

export type TParameterItem = {
    metricType?: string;
    expectedType?: string;
    expected_value?: string;
    result_formula?: string;
    operator?: string;
    threshold?: string;
    metricParameter: TMetricParameter;
}

export type TEngineParameter = {
    programType:string, // JAVA
    deployMode:string,
    driverCores: number,
    driverMemory: string,
    numExecutors: number,
    executorMemory:string,
    executorCores: number,
    others: string
}

export type TDetail = null | {
    id?: number;
    name?: string;
    type?: string;
    dataSourceId?: number;
    executePlatformType?: string;
    executePlatformParameter?: string;
    engineType?: string;
    engineParameter?: TEngineParameter,
    parameter?: string;
    parameterItem?: TParameterItem
    retryTimes?: number;
    retryInterval?: number;
    timeout?: number;
    timeoutStrategy?: string;
    tenantCode?: string;
    env?: string;
    createBy?: number | string;
    createTime?: Date;
    updateBy?: number | string;
    updateTime?: Date;
}
