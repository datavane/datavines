import React, { useState } from 'react';
import { Table, Form } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import queryString from 'querystring';
import { TJobsInstanceTableData, TJobsInstanceTableItem } from '@/type/JobsInstance';
import { Title, SearchForm } from '@/component';
import { useMount } from '@/common';
import { $http } from '@/http';
import { defaultRender } from '@/utils/helper';

const JobsInstance = () => {
    const intl = useIntl();
    const form = Form.useForm()[0];
    const [loading, setLoading] = useState(false);
    const [tableData, setTableData] = useState<TJobsInstanceTableData>({ list: [], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNumber: 1,
        pageSize: 10,
    });
    const [qs] = useState(queryString.parse(window.location.href.split('?')[1] || ''));
    const getData = async (values: any = null) => {
        try {
            setLoading(true);
            const res = (await $http.get('/task/page', {
                jobId: qs.jobId,
                ...pageParams,
                ...(values || form.getFieldsValue()),
            })) || [];
            setTableData({
                list: res?.records || [],
                total: res.total || 0,
            });
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    useMount(() => {
        getData();
    });
    const onSearch = (_values: any) => {
        setPageParams({ ...pageParams, pageNumber: 1 });
        getData({
            ..._values,
            pageNumber: 1,
        });
    };
    const onChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNumber: current,
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
            key: 'name',
            width: 240,
            render: (text) => defaultRender(text, 240),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_type' }),
            dataIndex: 'jobType',
            key: 'jobType',
            width: 140,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_status' }),
            dataIndex: 'status',
            key: 'status',
            width: 140,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_update_time' }),
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: 160,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 200,
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
            <Title isBack>{intl.formatMessage({ id: 'jobs_list' })}</Title>
            <div style={{ paddingTop: '20px' }}>
                <div className="dv-flex-between">
                    <SearchForm form={form} onSearch={onSearch} placeholder={intl.formatMessage({ id: 'common_search' })} />
                </div>
            </div>
            <Table<TJobsInstanceTableItem>
                size="middle"
                loading={loading}
                rowKey="id"
                columns={columns}
                dataSource={tableData.list || []}
                onChange={onChange}
                pagination={{
                    size: 'small',
                    total: tableData.total,
                    showSizeChanger: true,
                    defaultPageSize: 20,
                    current: pageParams.pageNumber,
                    pageSize: pageParams.pageSize,
                }}
            />
        </div>
    );
};

export default JobsInstance;
