import React, {
    forwardRef, useEffect, useImperativeHandle, useState,
} from 'react';
import { Button, Col, DatePicker, Dropdown, Form, Input, Row, Select, Table, Tabs } from 'antd';
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
    const transDateArrFormat = (updateTimeArr: any[], format: string) => {
        let res = {'startTime' : '', 'endTime' : ''}
        if (updateTimeArr){
            if (updateTimeArr.length == 2) {
                console.log(updateTimeArr[0])
                console.log(updateTimeArr[1])
                const startDay = dayjs(updateTimeArr[0])
                const endDay = dayjs(updateTimeArr[1])

                if (startDay.isValid()) {
                    res['startTime'] = startDay.format(format)
                    console.log("start " + startDay.format(format))
                }
                if (endDay.isValid()) {
                    res['endTime'] = endDay.format(format)
                    console.log("end " + endDay.format(format))
                }
            }
        }
        return res
    }

    const Date = <DatePicker showTime />;

    const jobExecutionColumns: ColumnsType<TJobExecutionLosTableItem> = [
        {
            title: intl.formatMessage({ id: 'jobs_task_name' }),
            dataIndex: 'name',
            key: 'name',
            width: 300,
            render: (text: string) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_schema_name' }),
            dataIndex: 'schemaName',
            key: 'schemaName',
            width: 150,
            render: (text: string) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_table_name' }),
            dataIndex: 'tableName',
            key: 'tableName',
            width: 150,
            render: (text: string) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_column_name' }),
            dataIndex: 'columnName',
            key: 'columnName',
            width: 150,
            render: (text: string) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_metric_type' }),
            dataIndex: 'metricType',
            key: 'metricType',
            width: 150,
            render: (text: string) => defaultRender(text, 300),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_type' }),
            dataIndex: 'jobType',
            key: 'jobType',
            width: 150,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_status' }),
            dataIndex: 'status',
            key: 'status',
            width: 150,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_check_status' }),
            dataIndex: 'checkState',
            key: 'checkState',
            width: 150,
            render: (text: string) => <div>{text}</div>,
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
            const updateTimeRes = transDateArrFormat(formValue.updateTime, 'YYYY-MM-DD HH:mm:ss')
            formValue.startTime = updateTimeRes['startTime']
            formValue.endTime = updateTimeRes['endTime']
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
            <div style={{ paddingTop: '20px' }}>
                <div className="dv-flex-between">
                    <div className="dv-datasource__search">
                        <Form form={form}>
                            <Row gutter={[16, 16]} style={{width: '100%'}}>
                                <Col xs={24} sm={12} md={8} lg={6} xl={6}>
                                    <Form.Item
                                        label={intl.formatMessage({id: "jobs_task_name"})}
                                        name="searchVal"
                                    >
                                        <Input
                                            autoComplete="off"
                                            onPressEnter={() => {
                                                getData();
                                            }}
                                        />
                                    </Form.Item>
                                </Col>

                                <Col xs={24} sm={12} md={8} lg={6} xl={4}>
                                    <Form.Item
                                        label={intl.formatMessage({id: "job_database"})}
                                        name="schemaSearch"
                                    >
                                        <Input
                                            autoComplete="off"
                                            onPressEnter={() => {
                                                getData();
                                            }}
                                        />
                                    </Form.Item>
                                </Col>

                                <Col xs={24} sm={12} md={8} lg={6} xl={4}>
                                    <Form.Item
                                        label={intl.formatMessage({id: "job_table"})}
                                        name="tableSearch"
                                    >
                                        <Input
                                            autoComplete="off"
                                            onPressEnter={() => {
                                                getData();
                                            }}
                                        />
                                    </Form.Item>
                                </Col>

                                <Col xs={24} sm={12} md={8} lg={6} xl={4}>
                                    <Form.Item
                                        label={intl.formatMessage({id: "job_column"})}
                                        name="columnSearch"
                                    >
                                        <Input
                                            autoComplete="off"
                                            onPressEnter={() => {
                                                getData();
                                            }}
                                        />
                                    </Form.Item>
                                </Col>
                                <Col xs={24} sm={12} md={8} lg={6} xl={6}>
                                    <Form.Item
                                        label={intl.formatMessage({id: "jobs_task_status"})}
                                        name="status"
                                    >
                                        <Select
                                            allowClear
                                            onSelect={() => {
                                                getData();
                                            }}
                                            onClear={() => {
                                                form.setFieldsValue({ status: undefined });
                                                getData();
                                            }}
                                        >
                                            <Select.Option value="0">
                                                {intl.formatMessage({id: "jobs_task_status_submitted_success"})}
                                            </Select.Option>
                                            <Select.Option value="1">
                                                {intl.formatMessage({id: "jobs_task_status_running_execution"})}
                                            </Select.Option>
                                            <Select.Option value="2">
                                                {intl.formatMessage({id: "jobs_task_status_ready_pause"})}
                                            </Select.Option>
                                            <Select.Option value="3">
                                                {intl.formatMessage({id: "jobs_task_status_pause"})}
                                            </Select.Option>
                                            <Select.Option value="4">
                                                {intl.formatMessage({id: "jobs_task_status_ready_stop"})}
                                            </Select.Option>
                                            <Select.Option value="5">
                                                {intl.formatMessage({id: "jobs_task_status_stop"})}
                                            </Select.Option>
                                            <Select.Option value="6">
                                                {intl.formatMessage({id: "jobs_task_status_failure"})}
                                            </Select.Option>
                                            <Select.Option value="7">
                                                {intl.formatMessage({id: "jobs_task_status_success"})}
                                            </Select.Option>
                                            <Select.Option value="8">
                                                {intl.formatMessage({id: "jobs_task_status_need_fault_tolerance"})}
                                            </Select.Option>
                                            <Select.Option value="9">
                                                {intl.formatMessage({id: "jobs_task_status_kill"})}
                                            </Select.Option>
                                            <Select.Option value="10">
                                                {intl.formatMessage({id: "jobs_task_status_waiting_thread"})}
                                            </Select.Option>
                                            <Select.Option value="11">
                                                {intl.formatMessage({id: "jobs_task_status_waiting_summit"})}
                                            </Select.Option>
                                        </Select>
                                    </Form.Item>
                                </Col>

                                <Col xs={24} sm={24} md={16} lg={12} xl={8}>
                                    <Form.Item
                                        label={intl.formatMessage({ id: 'jobs_update_time' })}
                                        name='updateTime'
                                    >
                                        <DatePicker.RangePicker
                                            allowEmpty={[true, true]}
                                            style={{ width: '100%' }}
                                            showTime
                                        />
                                    </Form.Item>
                                </Col>
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
