export type TMetaDataFetchTaskTableItem = {
    id: string | number,

    type: string,

    databaseName: string,

    tableName: string,
    status: 'submitted' | 'running' | 'failure' | 'success' | 'kill',

    scheduleTime: string,

    submitTime: string,

    startTime: string,

    endTime: string
}

export type TMetaDataFetchTaskTableData = {
    list: TMetaDataFetchTaskTableItem[],
    total: number
};