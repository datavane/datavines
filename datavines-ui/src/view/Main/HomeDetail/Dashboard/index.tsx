import React, {useState} from 'react';
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
import {SearchForm, Title} from "@/component";
import {useIntl} from "react-intl";
import * as echarts from 'echarts';
import {useHistory, useRouteMatch} from "react-router-dom";
import {$http} from "@/http";
import {IF, Popconfirm, useMount} from "@Editor/common";
import {ColumnsType} from "antd/lib/table";
import {TJobsInstanceTableItem} from "@/type/JobsInstance";
import {defaultRender} from "utils/helper";
import {useInstanceErrorDataModal} from "view/Main/HomeDetail/Jobs/useInstanceErrorDataModal";
import {useInstanceResult} from "view/Main/HomeDetail/Jobs/useInstanceResult";
import {useLogger} from "view/Main/HomeDetail/Jobs/useLogger";

type TJobs = {
    datasourceId?: any,
}

interface Option {
    value?: string | number | null;
    label: React.ReactNode;
    children?: Option[];
    isLeaf?: boolean;
}

const pieOption = {
    tooltip: {
        trigger: 'item'
    },
    legend: {
        top: '5%',
        left: 'center'
    },
    series: [
        {
            name: 'Access From',
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
            data: [
                { value: 1048, name: 'Success' },
                { value: 735, name: 'Failure' },
            ]
        }
    ]
};

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

app.config = {
    rotate: 90,
    align: 'left',
    verticalAlign: 'middle',
    position: 'insideBottom',
    distance: 15,
    onChange: function () {
        const labelOption: BarLabelOption = {
            rotate: app.config.rotate as BarLabelOption['rotate'],
            align: app.config.align as BarLabelOption['align'],
            verticalAlign: app.config
                .verticalAlign as BarLabelOption['verticalAlign'],
            position: app.config.position as BarLabelOption['position'],
            distance: app.config.distance as BarLabelOption['distance']
        };
    }
};

type BarLabelOption = NonNullable<echarts.BarSeriesOption['label']>;

const labelOption: BarLabelOption = {
    show: true,
    position: app.config.position as BarLabelOption['position'],
    distance: app.config.distance as BarLabelOption['distance'],
    align: app.config.align as BarLabelOption['align'],
    verticalAlign: app.config.verticalAlign as BarLabelOption['verticalAlign'],
    rotate: app.config.rotate as BarLabelOption['rotate'],
    formatter: '{c}  {name|{a}}',
    fontSize: 16,
    rich: {
        name: {}
    }
};

const barOption = {
    tooltip: {
        trigger: 'axis',
        axisPointer: {
            type: 'shadow'
        }
    },
    legend: {
        data: ['Forest', 'Steppe', 'Desert', 'Wetland']
    },
    toolbox: {
        show: true,
        orient: 'vertical',
        left: 'right',
        top: 'center',
        feature: {
            mark: { show: true },
            dataView: { show: true, readOnly: false },
            magicType: { show: true, type: ['line', 'bar', 'stack'] },
            restore: { show: true },
            saveAsImage: { show: true }
        }
    },
    xAxis: [
        {
            type: 'category',
            axisTick: { show: false },
            data: ['2012', '2013', '2014', '2015', '2016']
        }
    ],
    yAxis: [
        {
            type: 'value'
        }
    ],
    series: [
        {
            name: 'Forest',
            type: 'bar',
            barGap: 0,
            label: labelOption,
            emphasis: {
                focus: 'series'
            },
            data: [320, 332, 301, 334, 390]
        },
        {
            name: 'Steppe',
            type: 'bar',
            label: labelOption,
            emphasis: {
                focus: 'series'
            },
            data: [220, 182, 191, 234, 290]
        },
        {
            name: 'Desert',
            type: 'bar',
            label: labelOption,
            emphasis: {
                focus: 'series'
            },
            data: [150, 232, 201, 154, 190]
        },
        {
            name: 'Wetland',
            type: 'bar',
            label: labelOption,
            emphasis: {
                focus: 'series'
            },
            data: [98, 77, 101, 99, 40]
        }
    ]
};



const Dashboard = ({ datasourceId }: TJobs) => {
    const [dqBarOption, setDqBarOption] = useState<any>(barOption);
    const [dqPieOption, setDqPieOption] = useState<any>(pieOption);

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
    // const optionLists: Option[] = [];
    // const [options, setOptions] = useState<Option[]>(optionLists);
    const [databases, setDataBases] = useState<Option[]>([]);
    const [metricList, setMetricList] = useState([]);

    const getJobExecutionData = async () => {
        try {
            setLoading(true);
            const res = (await $http.post('/job/execution/page', {
                schemaName : entityParam.schemaName,
                tableName : entityParam.tableName,
                columnName : entityParam.columnName,
                metricType : metricType,
                datasourceId : datasourceId || (match.params as any).id,
                pageNumber : pageParam.pageNumber,
                pageSize : 5,
                status:  2,
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

    useMount(async () => {

        const $metricList = await $http.get('metric/list/DATA_QUALITY');
        let $reMetricList = $metricList ? JSON.parse(JSON.stringify($metricList)) : [];
        const $reMetricList1 : ((prevState: never[]) => never[]) | { value: any; label: any;}[]=[];
        $reMetricList.forEach((item: { key: any; label: any; }) => {
            return $reMetricList1?.push({value: item.key, label: item.label});
        })
        // @ts-ignore
        setMetricList($reMetricList1);
        console.log("metricList: ", $reMetricList )
        const $datasourceId = datasourceId || (match.params as any).id
        const $databases = await $http.get(`/datasource/${$datasourceId}/databases`);
        let $reDatabases = $databases ? JSON.parse(JSON.stringify($databases)) : [];
        const $reDatabases1: ((prevState: never[]) => never[]) | { value: any; label: any; isLeaf:any;}[]=[];
        $reDatabases.forEach((item: { name: any; }) => {
            return $reDatabases1.push({value: item.name, label: item.name, isLeaf: false});
        })
        // @ts-ignore
        setDataBases($reDatabases1);
        getJobExecutionData();
    });
    const onPageChange = ({ current, pageSize }: any) => {

        setPageParam({
            pageNumber : current,
            pageSize : pageSize
        })

        getJobExecutionData();
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
        console.log(value)
        setMetricType(value)
    };

    const onStartTimeChange : DatePickerProps['onChange'] = (date, dateString) => {
        setStartTime(dateString)
    };

    const onEndTimeChange : DatePickerProps['onChange'] = (date, dateString) => {
        setEndTime(dateString)
    };

    const onQueryClick = () => {
        getJobExecutionData();
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
            width: 240,
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
        <div className="dv-page-padding" style={{ height: 'calc(100vh - 75px)' }}>
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
            <div>
                <Row style = {{marginTop: '20px'}}>
                    <Col span={12}>
                        <Title>
                            {intl.formatMessage({ id: 'config_title' })}
                        </Title>
                        <DashBoard option={dqPieOption} id={"1"} style={{height:'350px'}}/>
                    </Col>
                    <Col span={12}>
                        <Title>
                            {intl.formatMessage({ id: 'config_title' })}
                        </Title>
                        <DashBoard option={dqBarOption} id={"2"} style={{height:'350px'}}/>
                    </Col>
                </Row>
                <Row>
                    <Col span={24} >
                        <Title>
                            {intl.formatMessage({ id: 'config_title' })}
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
