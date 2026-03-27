import React, {useState} from 'react';
import type {MenuProps} from 'antd';
import {Button, DatePicker, Dropdown, Form, Input, Menu, message, Popconfirm, Row, Table, Tabs, TabsProps,} from 'antd';
import {ColumnsType} from 'antd/lib/table';
import {useIntl} from 'react-intl';
import {useHistory, useRouteMatch} from 'react-router-dom';
import {TJobsTableData, TJobsTableItem} from '@/type/Jobs';
import {useWatch} from '@/common';
import {$http} from '@/http';
import {defaultRender} from '@/utils/helper';
import {useAddEditJobsModal} from './useAddEditJobsModal';
import {useSelectSLAsModal} from './useSelectSLAsModal';
import { useJobExecutionConfigPreview } from '@/view/Main/HomeDetail/Jobs/useJobExecutionConfigPreview';
import store from '@/store';
import dayjs from "dayjs";

type TJobs = {
    datasourceId?: any,
}

const Jobs = ({ datasourceId }: TJobs) => {
    const intl = useIntl();
    const form = Form.useForm()[0];
    const [loading, setLoading] = useState(false);
    const history = useHistory();
    const match = useRouteMatch();
    const [addType, setAddType] = useState('');
    const { Render: RenderJobPreviewModal, show: showJobPreviewModal } = useJobExecutionConfigPreview({
        afterClose() {
            getData();
        }
    });

    const { Render: RenderJobsModal, show: showJobsModal } = useAddEditJobsModal({
        title: intl.formatMessage({ id: addType === 'quality' ? 'jobs_tabs_title' : 'jobs_tabs_comparison_title' }),
        afterClose() {
            getData();
        },
    });

    const { Render: RenderSLAsModal, show: showSLAsModal } = useSelectSLAsModal({
        afterClose() {
            getData();
        },
    });

    const [tableData, setTableData] = useState<TJobsTableData>({ list: [], total: 0 });

    const [pageParams, setPageParams] = useState({
        pageNumber: 1,
        pageSize: 10,
    });

    const handleMenuClick: MenuProps['onClick'] = (e) => {
        setAddType(e.key);
        store.dispatch({
            type: 'save_datasource_modeType',
            payload: e.key,
        });
        store.dispatch({
            type: 'save_datasource_entityUuid',
            payload: '',
        });
        store.dispatch({
            type: 'save_datasource_dsiabledEdit',
            payload: null,
        });
        showJobsModal({
            id: (match.params as any).id,
            record: {
                modeType: e.key,
            },
        });
    };

    const menu = (
        <Menu
            onClick={handleMenuClick}
            items={[
                {
                    key: 'quality',
                    label: intl.formatMessage({ id: 'jobs_tabs_title' }),
                },
                {
                    key: 'comparison',
                    label: intl.formatMessage({ id: 'jobs_tabs_comparison_title' }),
                },
            ]}
        />
    );

    const transDateFormat = (datetime: any, format: string) => {
        if (datetime){
            const day = dayjs(datetime)
            if(day.isValid()){
                return day.format(format)
            }
        }
    }

    // eslint-disable-next-line default-param-last
    const getData = async (values: any = null, typeData?:number) => {
        try {
            setLoading(true);
            let formValue = form.getFieldsValue();
            formValue.startTime = transDateFormat(formValue.startTime, 'YYYY-MM-DD HH:mm:ss')
            formValue.endTime = transDateFormat(formValue.endTime, 'YYYY-MM-DD HH:mm:ss')
            const res = (await $http.get('/job/page', {
                datasourceId: datasourceId || (match.params as any).id,
                ...pageParams,
                ...(values || formValue),
                type: typeData !== undefined ? typeData : type,
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

    const onSearch = (_values: any) => {
        setPageParams({ ...pageParams, pageNumber: 1 });
    };

    const onChange = ({ current, pageSize }: any) => {
        setPageParams({
            pageNumber: current,
            pageSize,
        });
    };

    const onRun = async (record: TJobsTableItem) => {
        try {
            setLoading(true);
            await $http.post(`/job/execute/${record.id}`);
            message.success('Run Success');
            getData();
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };

    const onEdit = (record: TJobsTableItem) => {
        const type = record.type === 'DATA_RECONCILIATION' ? 'comparison' : 'quality';
        store.dispatch({
            type: 'save_datasource_modeType',
            payload: type,
        });
        setAddType(type);
        showJobsModal({
            id: (match.params as any).id,
            record,
        });
    };

    const onDelete = async (record: TJobsTableItem) => {
        try {
            setLoading(true);
            await $http.delete(`/job/${record.id}`);
            message.success('Delete Success');
            getData();
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };

    const onSLAs = (record: TJobsTableItem) => {
        showSLAsModal(record);
    };

    const onViewInstance = (record: TJobsTableItem) => {
        history.push(`${match.url}/instance?jobId=${record.id}`);
    };

    const onPreviewInstance = async (record: TJobsTableItem) => {
        showJobPreviewModal(record.id);
    };
    // @ts-ignore
    const columns: ColumnsType<TJobsTableItem> = [
        {
            title: intl.formatMessage({ id: 'jobs_id' }),
            dataIndex: 'id',
            key: 'id',
            width: 160,
            render: (text: any) => defaultRender(text, 200),
        },
        {
            title: intl.formatMessage({ id: 'jobs_name' }),
            dataIndex: 'name',
            key: 'name',
            width: 160,
            render: (text: any) => defaultRender(text, 200),
        },
        {
            title: intl.formatMessage({ id: 'dv_metric_database' }),
            dataIndex: 'schemaName',
            key: 'schemaName',
            width: 200,
            render: (text: string) => defaultRender(text, 200),
        },
        {
            title: intl.formatMessage({ id: 'dv_metric_table' }),
            dataIndex: 'tableName',
            key: 'tableName',
            width: 200,
            render: (text: string) => defaultRender(text, 200),
        },
        {
            title: intl.formatMessage({ id: 'dv_metric_column' }),
            dataIndex: 'columnName',
            key: 'columnName',
            width: 200,
            render: (text: string) => defaultRender(text, 200),
        },
        {
            title: intl.formatMessage({ id: 'jobs_schedule_express' }),
            dataIndex: 'cronExpression',
            key: 'cronExpression',
            width: 150,
            render: (text: string) => defaultRender(text, 200),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_success_count' }),
            dataIndex: 'successCount',
            key: 'successCount',
            width: 100,
            render: (text: number) => defaultRender(text, 200),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_fail_count' }),
            dataIndex: 'failCount',
            key: 'failCount',
            width: 100,
            render: (text: number) => defaultRender(text, 200),
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_last_time' }),
            dataIndex: 'lastJobExecutionTime',
            key: 'lastJobExecutionTime',
            width: 200,
            render: (text: string) => defaultRender(text, 200),
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
            key: 'action',
            dataIndex: 'action',
            width: 300,
            render: (text: string, record: TJobsTableItem) => {
                const editComp = <a style={{ marginRight: 5 }} onClick={() => { onEdit(record); }}>{intl.formatMessage({ id: 'common_edit' })}</a>;
                const deleteComp = (
                    <Popconfirm
                        title={intl.formatMessage({ id: 'common_delete_tip' })}
                        onConfirm={() => { onDelete(record); }}
                        okText={intl.formatMessage({ id: 'common_Ok' })}
                        cancelText={intl.formatMessage({ id: 'common_Cancel' })}
                    >
                        <a>{intl.formatMessage({ id: 'common_delete' })}</a>
                    </Popconfirm>
                );
                return (
                    <div className="dv-jobs">
                        <a style={{ marginRight: 5 }} onClick={() => { onRun(record); }}>{intl.formatMessage({ id: 'jobs_run' })}</a>
                        {editComp}
                        {deleteComp}
                        {/* <a style={{ marginLeft: 5 }} onClick={() => { onSLAs(record); }}>SLAs</a> */}
                        <a style={{ marginLeft: 5 }} onClick={() => { onPreviewInstance(record); }}>{intl.formatMessage({ id: 'jobs_preview' })}</a>
                        <a style={{ marginLeft: 5 }} onClick={() => { onViewInstance(record); }}>{intl.formatMessage({ id: 'jobs_view' })}</a>
                    </div>
                );
            },
        },
    ];

    const [type, setType] = useState(0);

    const onChangeTab = (key: string) => {
        setType(+key);
        setPageParams({
            pageNumber: 1,
            pageSize: 10,
        });
    };

    const items: TabsProps['items'] = [
        {
            key: '0',
            label: intl.formatMessage({ id: 'jobs_tabs_title' }),
            children: '',
        },
        {
            key: '2',
            label: intl.formatMessage({ id: 'jobs_tabs_comparison_title' }),
            children: '',
        },
    ];

    const Date = <DatePicker showTime />;

    return (
        <div className="dv-page-padding" style={{ height: 'calc(100vh - 73px)' }}>
            <Tabs defaultActiveKey="0" items={items} onChange={onChangeTab} />
            <div style={{ paddingTop: '0px' }}>
                <div className="dv-flex-between">
                    {/*<SearchForm form={form} onSearch={onSearch} placeholder={intl.formatMessage({ id: 'common_search' })} />*/}
                    <div className="dv-datasource__search">
                        <Form form={form}>

                            <Row style={{width: '100%'}}>
                                <Form.Item style={{width: '18%'}}
                                    label={intl.formatMessage({id: "jobs_name"})}
                                    name="searchVal"
                                >
                                    <Input
                                        style={{ width: '100%' }}
                                        autoComplete="off"
                                    />
                                </Form.Item>

                                <Form.Item style={{width: '15%',marginLeft: '10px'}}
                                    label={intl.formatMessage({id: "job_database"})}
                                    name="schemaSearch"
                                >
                                    <Input
                                        style={{ width: '100%' }}
                                        autoComplete="off"
                                    />
                                </Form.Item>

                                <Form.Item style={{width: '15%',marginLeft: '10px'}}
                                    label={intl.formatMessage({id: "job_table"})}
                                    name="tableSearch"
                                >
                                    <Input
                                        style={{ width: '100%' }}
                                        autoComplete="off"
                                    />
                                </Form.Item>

                                <Form.Item style={{width: '15%',marginLeft: '10px'}}
                                    label={intl.formatMessage({id: "job_column"})}
                                    name="columnSearch"
                                >
                                    <Input
                                        style={{ width: '100%' }}
                                        autoComplete="off"
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
                        <Dropdown overlay={menu}>
                            <Button
                                type="primary"
                                style={{ marginRight: 15 }}
                            >
                                {intl.formatMessage({ id: 'jobs_add' })}
                            </Button>
                        </Dropdown>
                    </div>
                </div>
            </div>
            <Table<TJobsTableItem>
                // virtual
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
                    current: pageParams.pageNumber,
                    pageSize: pageParams.pageSize,
                }}
            />
            <RenderJobsModal />
            <RenderSLAsModal />
            <RenderJobPreviewModal />
        </div>
    );
};

export default Jobs;
