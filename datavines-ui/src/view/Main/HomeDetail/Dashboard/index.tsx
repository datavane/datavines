import React, {useEffect, useState} from 'react';
import {
    Button,
    Cascader,
    Checkbox,
    Col,
    DatePicker, DatePickerProps,
    Dropdown,
    Form,
    Input,
    MenuProps,
    message,
    Row,
    Select,
    Table
} from "antd";
import DashBoard from "@Editor/components/Database/Detail/dashBoard";
import {Title} from "@/component";
import {useIntl} from "react-intl";
import * as echarts from 'echarts';
import {useRouteMatch} from "react-router-dom";
import {$http} from "@/http";
import {useMount} from "@Editor/common";
import {ColumnsType} from "antd/lib/table";
import {TJobsInstanceTableItem} from "@/type/JobsInstance";
import {defaultRender} from "utils/helper";
import {useInstanceErrorDataModal} from "view/Main/HomeDetail/Jobs/useInstanceErrorDataModal";
import {useInstanceResult} from "view/Main/HomeDetail/Jobs/useInstanceResult";
import {useLogger} from "view/Main/HomeDetail/Jobs/useLogger";
import { useWatch } from '@/common';

type TJobs = {
    datasourceId?: any,
}

interface Option {
    value?: string | number | null;
    label: React.ReactNode;
    children?: Option[];
    isLeaf?: boolean;
}

const app: any = {};
const posList = [
    'left',
    'right',
    'top',
    'bottom',
    'inside',
    'insideTop',
    'insideLeft',
    'insideRight',
    'insideBottom',
    'insideTopLeft',
    'insideTopRight',
    'insideBottomLeft',
    'insideBottomRight'
] as const;

app.configParameters = {
    rotate: {
        min: -90,
        max: 90
    },
    align: {
        options: {
            left: 'left',
            center: 'center',
            right: 'right'
        }
    },
    verticalAlign: {
        options: {
            top: 'top',
            middle: 'middle',
            bottom: 'bottom'
        }
    },
    position: {
        options: posList.reduce(function (map, pos) {
            map[pos] = pos;
            return map;
        }, {} as Record<string, string>)
    },
    distance: {
        min: 0,
        max: 100
    }
};

const Dashboard = ({ datasourceId }: TJobs) => {
    const [dqBarOption, setDqBarOption] = useState<any>();
    const [dqPieOption, setDqPieOption] = useState<any>();

    const intl = useIntl();
    const match = useRouteMatch();

    const [entityParam, setEntityParam] = useState<any>({
        schemaName: null,
        tableName: null,
        columnName: null
    });
    const [pageParam, setPageParam] = useState<any>({
        pageNumber : 1,
        pageSize : 5
    });
    const [metricType, setMetricType] = useState<any>();
    const [startTime, setStartTime] = useState<any>();
    const [endTime, setEndTime] = useState<any>();

    const onEntitySelectChange = async (value: (string | number)[], selectedOptions: Option[]) => {

        if (value) {
            if (value.length == 1) {
                setEntityParam({
                    schemaName:value[0]
                })

            } else if (value.length == 2) {
                setEntityParam({
                    schemaName : value[0],
                    tableName : value[1]
                })
            } else if (value.length == 3) {
                setEntityParam({
                    schemaName : value[0],
                    tableName : value[1],
                    columnName : value[2]
                })
            }
        } else {
            setEntityParam({
                schemaName : null,
                tableName : null,
                columnName : null
            })
        }
    };

    const loadData = (selectedOptions: Option[]) => {
        console.log(selectedOptions);
        setTimeout(async () => {
            const targetOption = selectedOptions[selectedOptions.length - 1];
            if (selectedOptions.length == 1) {
                const tables = await $http.get(`/datasource/${(match.params as any).id}/${selectedOptions[0].value}/tables`);
                let $reTables = tables ? JSON.parse(JSON.stringify(tables)) : [];
                const $reTables1: ((prevState: never[]) => never[]) | { value: any; label: any; isLeaf:any;}[] = [];
                $reTables.forEach((item: { name: any; }) => {
                    $reTables1.push({value: item.name, label: item.name,isLeaf:false})
                })
                targetOption.children = $reTables1;
                setDataBases([...databases])
            } else if (selectedOptions.length == 2) {

                const columns = await $http.get(`/datasource/${(match.params as any).id}/${selectedOptions[0].value}/${selectedOptions[1].value}/columns`);
                let $reColumns = columns ? JSON.parse(JSON.stringify(columns)) : [];
                const $reColumns1: ((prevState: never[]) => never[]) | { value: any; label: any; isLeaf:any;}[] = [];
                $reColumns.forEach((item: { name: any; }) => {
                    $reColumns1.push({value: item.name, label: item.name, isLeaf:true})
                })
                targetOption.children = $reColumns1;
                setDataBases([...databases])
            }
        },1000);
    };

    const [loading, setLoading] = useState(false);

    const [tableData, setTableData] = useState<{ list: TJobsInstanceTableItem[], total: number}>({ list: [], total: 0 });

    const { Render: RenderErrorDataModal, show: showErrorDataModal } = useInstanceErrorDataModal({});
    const { Render: RenderResultModal, show: showResultModal } = useInstanceResult({});
    const { Render: RenderLoggerModal, show: showLoggerModal } = useLogger({});

    const [databases, setDataBases] = useState<Option[]>([]);
    const [metricList, setMetricList] = useState([]);

    const getJobExecutionData = async (pageParam1 :any) => {
        try {
            setLoading(true);
            const res = (await $http.post('/job/execution/page', {
                schemaName : entityParam.schemaName,
                tableName : entityParam.tableName,
                columnName : entityParam.columnName,
                metricType : metricType,
                datasourceId : datasourceId || (match.params as any).id,
                pageNumber : pageParam1.pageNumber,
                pageSize : pageParam1.pageSize,
                status:  6,
                startTime : startTime,
                endTime : endTime
                },
            )) || [];
            setTableData({
                list: res?.records || [],
                total: res.total || 0,
            });
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };

    const getJobExecutionAggPie = async () => {
        try {
            setLoading(true);
            const res = (await $http.post('/job/execution/agg-pie', {
                    schemaName : entityParam.schemaName,
                    tableName : entityParam.tableName,
                    columnName : entityParam.columnName,
                    metricType : metricType,
                    datasourceId : datasourceId || (match.params as any).id,
                    startTime : startTime,
                    endTime : endTime
                },
            )) || [];
            console.log("agg pie res : ", res)

            const pieOption = {
                tooltip: {
                    trigger: 'item'
                },
                legend: {
                    top: '5%',
                    left: 'center'
                },
                color: ['#ef6567', '#91cd77'],
                series: [
                    {
                        type: 'pie',
                        radius: ['40%', '70%'],
                        avoidLabelOverlap: false,
                        itemStyle: {
                            borderRadius: 10,
                            borderColor: '#fff',
                            borderWidth: 2
                        },
                        label: {
                            show: false,
                            position: 'center'
                        },
                        emphasis: {
                            label: {
                                show: true,
                                fontSize: 40,
                                fontWeight: 'bold'
                            }
                        },
                        labelLine: {
                            show: false
                        },
                        data: res
                    }
                ]
            };
            setDqPieOption(pieOption)

        } catch (error) {
        } finally {
            setLoading(false);
        }
    };

    const getJobExecutionTrendBar = async () => {
        try {
            setLoading(true);
            const res = (await $http.post('/job/execution/trend-bar', {
                    schemaName : entityParam.schemaName,
                    tableName : entityParam.tableName,
                    columnName : entityParam.columnName,
                    metricType : metricType,
                    datasourceId : datasourceId || (match.params as any).id,
                    startTime : startTime,
                    endTime : endTime
                },
            )) || [];
            console.log("agg bar res : ", res)

            const barOption = {
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data: ['All', 'Success', 'Failure']
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                toolbox: {
                    feature: {
                        saveAsImage: {}
                    }
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: res.dateList
                },
                yAxis: {
                    type: 'value'
                },
                series: [
                    {
                        name: 'All',
                        type: 'line',
                        data: res.allList
                    },
                    {
                        name: 'Success',
                        type: 'line',
                        data: res.successList
                    },
                    {
                        name: 'Failure',
                        type: 'line',
                        color: 'red',
                        data: res.failureList
                    }
                ]
            };
            setDqBarOption(barOption)

        } catch (error) {
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {

    }, []);

    useWatch([(match.params as any).id], () =>{
        refreshData()
    })

    useMount(async () => {
        refreshData()
    });

    const refreshData = async () => {
        setEntityParam({
            schemaName: null,
            tableName: null,
            columnName: null
        })

        setMetricType(null)
        setStartTime(null)
        setEndTime(null)

        const $metricList = await $http.get('metric/list/DATA_QUALITY');
        let $reMetricList = $metricList ? JSON.parse(JSON.stringify($metricList)) : [];
        const $reMetricList1: ((prevState: never[]) => never[]) | { value: any; label: any; }[] = [];
        $reMetricList.forEach((item: { key: any; label: any; }) => {
            return $reMetricList1?.push({value: item.key, label: item.label});
        })
        // @ts-ignore
        setMetricList($reMetricList1);
        console.log("metricList: ", $reMetricList)
        const $datasourceId = datasourceId || (match.params as any).id
        const $databases = await $http.get(`/datasource/${$datasourceId}/databases`);
        let $reDatabases = $databases ? JSON.parse(JSON.stringify($databases)) : [];
        const $reDatabases1: ((prevState: never[]) => never[]) | { value: any; label: any; isLeaf: any; }[] = [];
        $reDatabases.forEach((item: { name: any; }) => {
            return $reDatabases1.push({value: item.name, label: item.name, isLeaf: false});
        })
        // @ts-ignore
        setDataBases($reDatabases1);
        getJobExecutionData(pageParam);
        getJobExecutionAggPie();
        getJobExecutionTrendBar();
    }

    const onPageChange = ({ current, pageSize }: any) => {
        setPageParam({
            pageNumber : current,
            pageSize : pageSize
        })

        getJobExecutionData({
            pageNumber : current,
            pageSize : pageSize
        });
    };

    const onLog = (record: TJobsInstanceTableItem) => {
        showLoggerModal(record);
    };
    const onResult = (record: TJobsInstanceTableItem) => {
        showResultModal(record);
    };
    const onErrorData = (record: TJobsInstanceTableItem) => {
        showErrorDataModal(record);
    };

    const onMetricSelectChange  = (value: string) => {
        setMetricType(value)
    };

    const onStartTimeChange : DatePickerProps['onChange'] = (date, dateString) => {
        setStartTime(dateString)
    };

    const onEndTimeChange : DatePickerProps['onChange'] = (date, dateString) => {
        setEndTime(dateString)
    };

    const onQueryClick = () => {
        getJobExecutionData(pageParam);
        getJobExecutionAggPie();
        getJobExecutionTrendBar();
    }

    const columns: ColumnsType<TJobsInstanceTableItem> = [
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
            width: 200,
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
            title: intl.formatMessage({ id: 'jobs_task_check_status' }),
            dataIndex: 'checkState',
            key: 'checkState',
            width: 140,
            render: (text: string) => <div>{text}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_start_time' }),
            dataIndex: 'startTime',
            key: 'startTime',
            width: 180,
            render: (text: string) => <div>{text || '--'}</div>,
        },
        {
            title: intl.formatMessage({ id: 'jobs_task_end_time' }),
            dataIndex: 'endTime',
            key: 'endTime',
            width: 180,
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
                    <a style={{ marginRight: 5 }} onClick={() => { onLog(record); }}>{intl.formatMessage({ id: 'jobs_task_log_btn' })}</a>
                    <a style={{ marginRight: 5 }} onClick={() => { onResult(record); }}>{intl.formatMessage({ id: 'jobs_task_result' })}</a>
                    <a style={{ marginRight: 5 }} onClick={() => { onErrorData(record); }}>{intl.formatMessage({ id: 'jobs_task_error_data' })}</a>
                </>
            ),
        },
    ];
    return (
        <div className="dv-page-padding" style={{height:'calc(100vh - 100px)'}} >
            <div>
                <Row style = {{marginTop: '20px'}}>
                    <Col span={24}>
                        <Cascader options={databases} loadData={loadData} onChange={onEntitySelectChange} changeOnSelect style = {{width:500}}/>
                        <Select
                            onChange={onMetricSelectChange}
                            style = {{marginLeft: '20px', width: 200}}
                            options={metricList}
                            allowClear
                        />
                        <span style = {{marginLeft: '20px'}}>开始时间</span> <DatePicker onChange={onStartTimeChange} style = {{marginLeft: '10px'}} showTime ></DatePicker>
                        <span style = {{marginLeft: '20px'}}>结束时间</span> <DatePicker style = {{marginLeft: '10px'}} showTime onChange={onEndTimeChange}></DatePicker>
                        <Button style = {{marginLeft: '20px'}} onClick={onQueryClick} >查询</Button>
                    </Col>
                </Row>
            </div>
            <div  style={{height:'calc(100vh - 160px)',overflow: 'auto'}}>
                <Row style = {{marginTop: '20px'}}>
                    <Col span={12}>
                        <Title>
                            {intl.formatMessage({ id: 'quality_dashboard_profile' })}
                        </Title>
                        <DashBoard option={dqPieOption} id={"1"} style={{height:'350px',width:'calc(50vw - 100px)'}}/>
                    </Col>
                    <Col span={12}>
                        <Title>
                            {intl.formatMessage({ id: 'quality_dashboard_trend' })}
                        </Title>
                        <DashBoard option={dqBarOption} id={"2"} style={{height:'350px',width:'calc(50vw - 100px)'}}/>
                    </Col>
                </Row>
                <Row>
                    <Col span={24} >
                        <Title>
                            {intl.formatMessage({ id: 'quality_dashboard_failure_execution' })}
                        </Title>
                        <Table<TJobsInstanceTableItem>
                            size="middle"
                            loading={loading}
                            rowKey="id"
                            columns={columns}
                            dataSource={tableData.list || []}
                            onChange={onPageChange}
                            pagination={{
                                size: 'small',
                                total: tableData.total,
                                showSizeChanger: true,
                                current: pageParam.pageNumber,
                                pageSize: pageParam.pageSize,
                                pageSizeOptions: [5, 10, 20, 50, 100],
                            }}
                        />
                        <RenderLoggerModal />
                        <RenderErrorDataModal />
                        <RenderResultModal />
                    </Col>
                </Row>
            </div>
        </div>
    );
};

export default Dashboard;
