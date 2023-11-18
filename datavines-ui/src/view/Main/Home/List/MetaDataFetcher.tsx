import React, {useEffect, useState} from 'react';
import {
   Table
} from 'antd';
import { useIntl } from 'react-intl';
import Schedule from '@/view/Main/HomeDetail/Jobs/components/Schedule';
import {Tabs} from "antd/lib";
import TabPane = Tabs.TabPane;
import {TJobsInstanceTableData, TJobsInstanceTableItem} from "@/type/JobsInstance";
import {$http} from "@/http";
import {IF, useMount} from "@Editor/common";
import {ColumnsType} from "antd/lib/table";
import {defaultRender} from "utils/helper";

type IndexProps = {
    datasourceId: number | string,
    onSavaEnd: any,
}

const Index: React.FC<IndexProps> = ({datasourceId, onSavaEnd}) => {
    const intl = useIntl();
    const [loading, setLoading] = useState(false);
    const [tableData, setTableData] = useState<TJobsInstanceTableData>({ list: [], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNumber: 1,
        pageSize: 10,
    });

    const getData = async (values?: any, $pageParams?: any) => {
        try {
            setLoading(true);
            const res = (await $http.get('/catalog/metadata/task/page', {
                datasourceId: datasourceId,
                ...($pageParams || pageParams),
            })) || [];
            setTableData({
                list: res?.records || [],
                total: res.total || 0,
            });
        } catch (error) {
            console.log("get execution page : {}", error)
        } finally {
            setLoading(false);
        }
    };
    useMount(() => {
        getData();
    });

    useEffect(() =>{
        getData();
    })

    const onChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNumber: current,
            pageSize,
        });
        getData(null, {
            pageNumber: current,
            pageSize,
        });
    };

    const columns: ColumnsType<TJobsInstanceTableItem> = [
        {
            title: intl.formatMessage({ id: 'jobs_task_type' }),
            dataIndex: 'type',
            key: 'type',
            width: 100,
            render: (text) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_schema_name' }),
            dataIndex: 'databaseName',
            key: 'databaseName',
            width: 100,
            render: (text) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_table_name' }),
            dataIndex: 'tableName',
            key: 'tableName',
            width: 200,
            render: (text) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_status' }),
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_execute_host' }),
            dataIndex: 'executeHost',
            key: 'executeHost',
            width: 140,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_schedule_time' }),
            dataIndex: 'scheduleTime',
            key: 'scheduleTime',
            width: 200,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_submit_time' }),
            dataIndex: 'submitTime',
            key: 'submitTime',
            width: 200,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_start_time' }),
            dataIndex: 'startTime',
            key: 'startTime',
            width: 200,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_end_time' }),
            dataIndex: 'endTime',
            key: 'endTime',
            width: 200,
            render: (text: string) => <div>{text || '--'}</div>,
        }
    ];

    return (
        <>
            <Tabs
                type="line"
                size="small"
            >
                <TabPane tab='调度配置' key = "1">

                    <Schedule
                        onSavaEnd={() => {
                            onSavaEnd();
                        }}
                        width="100%"
                        style={{ height: 'auto' }}
                        jobId={datasourceId}
                        isShowPush
                        api="catalog/metadata"
                    />
                </TabPane>
                <TabPane tab='执行记录' key = "2">

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
                            current: pageParams.pageNumber,
                            pageSize: pageParams.pageSize,
                        }}
                    />
                </TabPane>
            </Tabs>
        </>
    )
};

export default Index;