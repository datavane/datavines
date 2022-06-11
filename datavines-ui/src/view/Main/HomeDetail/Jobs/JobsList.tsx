import React, { useState } from 'react';
import {
    Table, Form, Button, Popconfirm,
} from 'antd';
// import { PlayCircleOutlined, EditOutlined } from '@ant-design/icons';
import { ColumnsType } from 'antd/es/table';
import { useIntl } from 'react-intl';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { TJobsTableData, TJobsTableItem } from '@/type/Jobs';
import { useScheduleModal } from './hooks/useScheduleModal';
import { Title, SearchForm } from '@/component';
import { useMount } from '@/common';
import { $http } from '@/http';
import { defaultRender } from '@/utils/helper';

const Jobs = () => {
    const { Render: RenderSchedule, show: showSchedule } = useScheduleModal({});
    const intl = useIntl();
    const form = Form.useForm()[0];
    const [loading, setLoading] = useState(false);
    const history = useHistory();
    const match = useRouteMatch();
    const [tableData, setTableData] = useState<TJobsTableData>({ list: [], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNumber: 1,
        pageSize: 10,
    });
    const getData = async (values: any = null) => {
        try {
            setLoading(true);
            const res = (await $http.get('/job/page', {
                datasourceId: (match.params as any).id,
                ...pageParams,
                ...(values || form.getFieldsValue),
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
        getData(_values);
    };
    const onChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNumber: current,
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
            key: 'name',
            width: 160,
            render: (text) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_type' }),
            dataIndex: 'type',
            key: 'type',
            width: 200,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_updater' }),
            dataIndex: 'updater',
            key: 'updater',
            width: 100,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_update_time' }),
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: 180,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 240,
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
                    <a style={{ marginLeft: 5 }} onClick={() => { onViewInstance(record); }}>{intl.formatMessage({ id: 'jobs_view' })}</a>
                </div>
            ),
        },
    ];
    return (
        <div className="dv-page-paddinng">
            <Title>{intl.formatMessage({ id: 'jobs_list' })}</Title>
            <div style={{ padding: '20px 0px' }}>
                <div className="dv-flex-between">
                    <SearchForm form={form} onSearch={onSearch} placeholder={intl.formatMessage({ id: 'common_search' })} />
                    <div>
                        <Button
                            type="primary"
                            style={{ marginRight: 15 }}
                            onClick={() => {
                                console.log('点击');
                                // show(null);
                            }}
                        >
                            {intl.formatMessage({ id: 'jobs_add' })}

                        </Button>
                    </div>
                </div>
            </div>
            <Table<TJobsTableItem>
                loading={loading}
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
                    current: pageParams.pageNumber,
                    pageSize: pageParams.pageSize,
                }}
            />
            <RenderSchedule />
        </div>
    );
};

export default Jobs;
