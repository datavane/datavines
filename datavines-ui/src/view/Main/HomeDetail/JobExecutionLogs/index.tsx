import React, {
    forwardRef, useEffect, useImperativeHandle, useState,
} from 'react';
import {Button, DatePicker, Dropdown, Form, Input, Row, Table, Tabs} from 'antd';
import { useIntl } from 'react-intl';
import { ColumnsType } from 'antd/es/table';
import { $http } from '@/http';
import {IF, useWatch} from '@/common';
import { useInstanceErrorDataModal } from '@/view/Main/HomeDetail/Jobs/useInstanceErrorDataModal';
import { useInstanceResult } from '@/view/Main/HomeDetail/Jobs/useInstanceResult';
import { useLogger } from '@/view/Main/HomeDetail/Jobs/useLogger';
import {defaultRender} from "utils/helper";
import {TJobExecutionLosTableItem} from "@/type/JobExecutionLogs";
import {useRouteMatch} from "react-router-dom";
import dayjs from "dayjs";

type TJobExecutionLogs = {
    datasourceId?: any,
}


const JobExecutionLogs = ({ datasourceId }: TJobExecutionLogs) => {

    const intl = useIntl();
    const form = Form.useForm()[0];
    const [loading, setLoading] = useState(false);
    const match = useRouteMatch();
    const { Render: RenderErrorDataModal, show: showErrorDataModal } = useInstanceErrorDataModal({});
    const { Render: RenderResultModal, show: showResultModal } = useInstanceResult({});
    const { Render: RenderLoggerModal, show: showLoggerModal } = useLogger({});

    const [tableData, setTableData] = useState({ list: [], total: 0 });
    const [pageParams, setPageParams] = useState({
        pageNumber: 1,
        pageSize: 10,
    });

    const onLog = (record: TJobExecutionLosTableItem) => {
        showLoggerModal(record);
    };
    const onResult = (record: TJobExecutionLosTableItem) => {
        showResultModal(record);
    };
    const onErrorData = (record: TJobExecutionLosTableItem) => {
        showErrorDataModal(record);
    };

    const transDateFormat = (datetime: any, format: string) => {
        if (datetime){
            const day = dayjs(datetime)
            if(day.isValid()){
                return day.format(format)
            }
        }
    }
    const Date = <DatePicker showTime />;

    const jobExecutionColumns: ColumnsType<TJobExecutionLosTableItem> = [
        {
            title: intl.formatMessage({ id: 'jobs_task_name' }),
            dataIndex: 'name',
            key: 'name',
            width: 300,
            render: (text) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_schema_name' }),
            dataIndex: 'schemaName',
            key: 'schemaName',
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
            title: intl.formatMessage({ id: 'jobs_task_column_name' }),
            dataIndex: 'columnName',
            key: 'columnName',
            width: 200,
            render: (text) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_metric_type' }),
            dataIndex: 'metricType',
            key: 'metricType',
            width: 300,
            render: (text) => defaultRender(text, 300),
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
            title: intl.formatMessage({ id: 'jobs_task_start_time' }),
            dataIndex: 'startTime',
            key: 'startTime',
            width: 160,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_end_time' }),
            dataIndex: 'endTime',
            key: 'endTime',
            width: 160,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_update_time' }),
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: 160,
            render: (text: string) => <div>{text || '--'}</div>
        },
        {
            title: intl.formatMessage({ id: 'common_action' }),
            fixed: 'right',
            key: 'right',
            dataIndex: 'right',
            width: 240,
            render: (text: string, record: any) => (
                <>
                    <a style={{ marginRight: 5 }} onClick={() => { onLog(record); }}>{intl.formatMessage({ id: 'jobs_task_log_btn' })}</a>
                    <a style={{ marginRight: 5 }} onClick={() => { onResult(record); }}>{intl.formatMessage({ id: 'jobs_task_result' })}</a>
                    <a style={{ marginRight: 5 }} onClick={() => { onErrorData(record); }}>{intl.formatMessage({ id: 'jobs_task_error_data' })}</a>
                </>
            ),
        }
    ]

    const onSearch = (_values: any) => {
        setPageParams({ ...pageParams, pageNumber: 1 });
    };

    const onChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNumber: current,
            pageSize,
        });
    };

    const getData = async (values: any = null, typeData?:number) => {
        try {
            setLoading(true);
            let formValue = form.getFieldsValue();
            formValue.startTime = transDateFormat(formValue.startTime, 'YYYY-MM-DD HH:mm:ss')
            formValue.endTime = transDateFormat(formValue.endTime, 'YYYY-MM-DD HH:mm:ss')
            const res = (await $http.post('/job/execution/page', {
                datasourceId: datasourceId || (match.params as any).id,
                ...pageParams,
                ...(values || formValue)
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

    useWatch([pageParams], () => {
        getData();
    }, { immediate: true });

    return (
        <div className="dv-page-padding" style={{ height: 'calc(100vh - 73px)' }}>
            <div style={{ paddingTop: '0px' }}>
                <div className="dv-flex-between">
                    <div className="dv-datasource__search">
                        <Form form={form}>
                            <Row style={{width: '100%'}}>
                                <Form.Item style={{width: '18%'}}
                                           label={intl.formatMessage({id: "jobs_task_name"})}
                                           name="searchVal"
                                >
                                    <Input
                                        style={{ width: '100%' }}
                                        autoComplete="off"
                                        onPressEnter={() => {
                                            getData();
                                        }}
                                    />
                                </Form.Item>

                                <Form.Item style={{width: '15%',marginLeft: '10px'}}
                                           label={intl.formatMessage({id: "job_database"})}
                                           name="schemaSearch"
                                >
                                    <Input
                                        style={{ width: '100%' }}
                                        autoComplete="off"
                                        onPressEnter={() => {
                                            getData();
                                        }}
                                    />
                                </Form.Item>

                                <Form.Item style={{width: '15%',marginLeft: '10px'}}
                                           label={intl.formatMessage({id: "job_table"})}
                                           name="tableSearch"
                                >
                                    <Input
                                        style={{ width: '100%' }}
                                        autoComplete="off"
                                        onPressEnter={() => {
                                            getData();
                                        }}
                                    />
                                </Form.Item>

                                <Form.Item style={{width: '15%',marginLeft: '10px'}}
                                           label={intl.formatMessage({id: "job_column"})}
                                           name="columnSearch"
                                >
                                    <Input
                                        style={{ width: '100%' }}
                                        autoComplete="off"
                                        onPressEnter={() => {
                                            getData();
                                        }}
                                    />
                                </Form.Item>

                                <Form.Item style={{width: '15%',marginLeft: '10px'}}
                                           label={intl.formatMessage({ id: 'jobs_update_time' })}
                                           name="startTime"
                                >
                                    {Date}
                                </Form.Item>

                                <span style={{ margin: '5px 10px 0px' }}>
                                {intl.formatMessage({ id: 'jobs_schedule_time_to' })}
                                </span>

                                <Form.Item
                                    label=""
                                    name="endTime"
                                    style={{ display: 'inline-block', width: '15%',marginLeft: '10px'}}
                                >
                                    {Date}
                                </Form.Item>
                            </Row>
                        </Form>
                    </div>
                    <div>
                        <Button
                            type="default"
                            style={{ marginRight: 15 }}
                            onClick={onSearch}
                        >
                            {intl.formatMessage({ id: 'common_search' })}
                        </Button>
                    </div>
                </div>
            </div>
            <Table<TJobExecutionLosTableItem>
                loading={loading}
                size="middle"
                rowKey="id"
                bordered
                columns={jobExecutionColumns}
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
            <RenderLoggerModal />
            <RenderErrorDataModal />
            <RenderResultModal />
        </div>
    );



}
export default JobExecutionLogs;