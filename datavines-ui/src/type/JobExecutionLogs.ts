export type TJobExecutionLosTableItem = {
    id: string | number,
    jobType: string,
    name: string,
    status: 'submitted' | 'running' | 'failure' | 'success' | 'kill',
    updateTime: string,
}

export type TJobExecutionLosTableData = {
    list: TJobExecutionLosTableItem[],
    total: number
};
