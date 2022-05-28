import React, { useState } from 'react';
import { Table, Popconfirm } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { TJobsTableData, TJobsTableItem } from '@/type/Jobs';
import { useScheduleModal } from './hooks/useScheduleModal';

const Jobs = () => {
    const { Render: RenderSchedule, show: showSchedule } = useScheduleModal({});
    const intl = useIntl();
    const history = useHistory();
    const match = useRouteMatch();
    const [tableData, setTableData] = useState<TJobsTableData>({ list: [{ id: 1 }], total: 0 });
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
    const onRun = (record: TJobsTableItem) => {
        console.log(record);
    };
    const onSchedule = (record: TJobsTableItem) => {
        console.log(record);
        showSchedule({ record });
    };
    const onEdit = (record: TJobsTableItem) => {
        console.log(record);
    };
    const onDelete = (record: TJobsTableItem) => {
        console.log(record);
    };
    const onViewInstance = (record: TJobsTableItem) => {
        console.log(record, match);
        history.push(`${match.url}/instance?id=${record.id}`);
    };
    const columns: ColumnsType<TJobsTableItem> = [
        {
            title: intl.formatMessage({ id: 'jobs_name' }),
            dataIndex: 'name',
            fixed: 'left',
            key: 'name',
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_type' }),
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
            width: 160,
            render: (text: string, record: TJobsTableItem) => (
                <div className="dv-jobs">
                    <a style={{ marginRight: 5 }} onClick={() => { onRun(record); }}>{intl.formatMessage({ id: 'jobs_run' })}</a>
                    <a style={{ marginRight: 5 }} onClick={() => { onSchedule(record); }}>{intl.formatMessage({ id: 'jobs_schedule' })}</a>
                    <a style={{ marginRight: 5 }} onClick={() => { onEdit(record); }}>{intl.formatMessage({ id: 'common_edit' })}</a>
                    <Popconfirm
                        title={intl.formatMessage({ id: 'common_delete_tip' })}
                        onConfirm={() => { onDelete(record); }}
                        okText={intl.formatMessage({ id: 'common_Ok' })}
                        cancelText={intl.formatMessage({ id: 'common_Cancel' })}
                    >
                        <a>{intl.formatMessage({ id: 'common_delete' })}</a>
                    </Popconfirm>
                    <div>
                        <a onClick={() => { onViewInstance(record); }}>{intl.formatMessage({ id: 'jobs_view_instance' })}</a>
                    </div>
                </div>
            ),
        },
    ];
    return (
        <div className="dv-page-paddinng">
            <Table<TJobsTableItem>
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
            <RenderSchedule />
        </div>
    );
};

export default Jobs;
