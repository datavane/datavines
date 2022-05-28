import React, { useState } from 'react';
import { Table } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { TJobsInstanceTableData, TJobsInstanceTableItem } from '@/type/JobsInstance';

const JobsInstance = () => {
    console.log('jobs instance');
    const intl = useIntl();
    const [tableData, setTableData] = useState<TJobsInstanceTableData>({ list: [{ id: 1 }], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNo: 1,
        pageSize: 10,
    });
    const onChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNo: current,
            pageSize,
        });
    };
    const onStop = (record: TJobsInstanceTableItem) => {
        console.log(record);
    };
    const onLog = (record: TJobsInstanceTableItem) => {
        console.log(record);
    };
    const onResult = (record: TJobsInstanceTableItem) => {
        console.log(record);
    };
    const columns: ColumnsType<TJobsInstanceTableItem> = [
        {
            title: intl.formatMessage({ id: 'jobs_task_name' }),
            dataIndex: 'name',
            fixed: 'left',
            key: 'name',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_type' }),
            dataIndex: 'type',
            key: 'type',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_dataSource' }),
            dataIndex: 'dataSource',
            key: 'dataSource',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_status' }),
            dataIndex: 'status',
            key: 'status',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_founder' }),
            dataIndex: 'founder',
            key: 'founder',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_create_time' }),
            dataIndex: 'createTime',
            key: 'createTime',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 120,
            render: (text: string, record: TJobsInstanceTableItem) => (
                <>
                    <a style={{ marginRight: 5 }} onClick={() => { onStop(record); }}>{intl.formatMessage({ id: 'jobs_task_stop_btn' })}</a>
                    <a style={{ marginRight: 5 }} onClick={() => { onLog(record); }}>{intl.formatMessage({ id: 'jobs_task_log_btn' })}</a>
                    <a style={{ marginRight: 5 }} onClick={() => { onResult(record); }}>{intl.formatMessage({ id: 'jobs_task_result_btn' })}</a>
                </>
            ),
        },
    ];
    return (
        <div className="dv-page-paddinng">
            <Table<TJobsInstanceTableItem>
                size="middle"
                rowKey="id"
                columns={columns}
                dataSource={tableData.list || []}
                onChange={onChange}
                pagination={{
                    size: 'small',
                    total: tableData.total,
                    showSizeChanger: true,
                    defaultPageSize: 20,
                    current: pageParams.pageNo,
                    pageSize: pageParams.pageSize,
                }}
            />
        </div>
    );
};

export default JobsInstance;
