import React, { useState } from 'react';
import {
    Row, Col, Form, Input, InputNumber, FormInstance,
} from 'antd';
import { useIntl } from 'react-intl';
import './index.less';
import { layoutItem } from '../helper';
import useRequest from '../../../hooks/useRequest';
import useRequiredRule from '../../../hooks/useRequiredRule';
import { TMetricModal } from '../type';
import {
    CustomSelect, useMount, useContextModal, usePersistFn, IF,
} from '../../../common';

type InnerProps = {
    form: FormInstance
}

const Index = ({ form }: InnerProps) => {
    const intl = useIntl();
    const { $http } = useRequest();
    const { data } = useContextModal<TMetricModal>();
    const [metricList, setMetricList] = useState([]);
    const requiredRules = useRequiredRule();
    const [databases, setDataBases] = useState([]);
    const [tables, setTables] = useState([]);
    const [columns, setColumns] = useState([]);
    // [ "comparator","length", "enum_list", "regexp", "min", "max", "datetime_format", "begin_time", "deadline_time"]
    // ["min", "max"]
    const [configsName, setConfigsName] = useState<string[]>([]);
    const getTable = async (databaseName: string) => {
        try {
            const res = await $http.get(`/datasource/${data.id}/${databaseName}/tables`);
            setTables(res || []);
        } catch (error) {
        }
    };
    const getCloumn = async (databaseName: string, tableName:string) => {
        try {
            const res = await $http.get(`/datasource/${data.id}/${databaseName}/${tableName}/columns`);
            setColumns(res?.columns || []);
        } catch (error) {
        }
    };
    console.log('data', data);
    useMount(async () => {
        const $metricList = await $http.get('metric/list');
        const $databases = await $http.get(`/datasource/${data.id}/databases`);
        if (data.databaseName) {
            await getTable(data.databaseName);
        }
        if (data.databaseName && data.tableName) {
            await getCloumn(data.databaseName, data.tableName);
        }
        setDataBases($databases || []);
        setMetricList($metricList || []);
        form.setFieldsValue({
            database: data.databaseName,
            table: data.tableName,
            column: data.columnName,
        });
    });

    const getConfigsName = usePersistFn(async (val) => {
        try {
            const res = await $http.get(`metric/configs/${val}`);
            setConfigsName(res || []);
        } catch (error) {
        }
    });
    return (
        <Row gutter={30}>
            <Col span={12}>
                <Form.Item
                    {...layoutItem}
                    label="Metric"
                    name="metricType"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_placeholder' }) }]}
                >
                    <CustomSelect
                        allowClear
                        source={metricList}
                        sourceValueMap="key"
                        onChange={getConfigsName}
                    />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_database' })}
                    name="database"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_table' }) }]}
                >
                    <CustomSelect allowClear source={databases} sourceValueMap="name" />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_table' })}
                    name="table"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_table' }) }]}
                >
                    <CustomSelect allowClear source={tables} sourceValueMap="name" />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_column' })}
                    name="column"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_column' }) }]}
                >
                    <CustomSelect allowClear source={columns} sourceValueMap="name" />
                </Form.Item>
                <IF visible={configsName.includes('max')}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_maxValue' })}
                        name="max"
                        rules={[...requiredRules]}
                    >
                        <InputNumber style={{ width: '50%' }} />
                    </Form.Item>
                </IF>

                <IF visible={configsName.includes('min')}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_minValue' })}
                        name="min"
                        rules={[...requiredRules]}
                    >
                        <InputNumber style={{ width: '50%' }} />
                    </Form.Item>
                </IF>
            </Col>
            <Col span={12}>
                <div style={{ paddingTop: 60 }} />
                <Form.Item
                    className="dv-editor__condition"
                    colon={false}
                    label={(
                        <div>
                            {intl.formatMessage({ id: 'dv_metric_condition' })}
                            :
                        </div>
                    )}
                    // rules={[...requiredRules]}
                    name="filter"
                >
                    <Input.TextArea style={{ marginLeft: -60 }} rows={5} />
                </Form.Item>
            </Col>
        </Row>
    );
};

export default Index;
