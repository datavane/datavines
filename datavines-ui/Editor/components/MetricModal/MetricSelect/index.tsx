import React, { useState, useImperativeHandle } from 'react';
import {
    Row, Col, Form, Input, InputNumber, FormInstance,
} from 'antd';
import { useIntl } from 'react-intl';
import './index.less';
import { layoutItem } from '../helper';
import useRequest from '../../../hooks/useRequest';
import useRequiredRule from '../../../hooks/useRequiredRule';
import { TMetricModal, TDetail } from '../type';
import {
    CustomSelect, useMount, useContextModal, usePersistFn, IF,
} from '../../../common';

type InnerProps = {
    form: FormInstance,
    metricSelectRef: any,
    id: string,
    detail: TDetail
}

type dynamicConfigItem = {
    label: string,
    key: string,
}

const Index = ({
    form, metricSelectRef, id, detail,
}: InnerProps) => {
    const intl = useIntl();
    const { $http } = useRequest();
    const [metricList, setMetricList] = useState([]);
    const requiredRules = useRequiredRule();
    const [databases, setDataBases] = useState([]);
    const [tables, setTables] = useState([]);
    const [columns, setColumns] = useState([]);
    const [configsName, setConfigsName] = useState<dynamicConfigItem[]>([]);

    useImperativeHandle(metricSelectRef, () => ({
        getDynamicValues() {
            const values = form.getFieldsValue();
            return configsName.reduce((prev: Record<string, any>, cur) => {
                prev[cur.key] = values[cur.key];
                return prev;
            }, {});
        },
    }));
    const getTable = async (databaseName: string) => {
        try {
            const res = await $http.get(`/datasource/${id}/${databaseName}/tables`);
            setTables(res || []);
        } catch (error) {
        }
    };
    const getCloumn = async (databaseName: string, tableName:string) => {
        try {
            const res = await $http.get(`/datasource/${id}/${databaseName}/${tableName}/columns`);
            setColumns(res?.columns || []);
        } catch (error) {
        }
    };
    console.log('detail', detail);
    useMount(async () => {
        const $metricList = await $http.get('metric/list');
        const $databases = await $http.get(`/datasource/${id}/databases`);
        if (detail?.databaseName) {
            await getTable(detail.databaseName);
        }
        if (detail?.databaseName && detail.tableName) {
            await getCloumn(detail.databaseName, detail.tableName);
        }
        setDataBases($databases || []);
        setMetricList($metricList || []);
        form.setFieldsValue({
            database: detail?.databaseName,
            table: detail?.tableName,
            column: detail?.columnName,
        });
    });

    const getConfigsName = usePersistFn(async (val) => {
        try {
            const res = await $http.get(`metric/configs/${val}`);
            setConfigsName(res || []);
        } catch (error) {
        }
    });
    const databasesChange = (val: string) => {
        form.setFieldsValue({
            table: undefined,
        });
        getTable(val);
    };
    const tableChange = () => {
        form.setFieldsValue({
            column: undefined,
        });
        const values = form.getFieldsValue();
        getCloumn(values.database, values.table);
    };
    const renderColumn = () => (
        <Form.Item
            {...layoutItem}
            label={intl.formatMessage({ id: 'dv_metric_column' })}
            name="column"
            rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_column' }) }]}
        >
            <CustomSelect allowClear source={columns} sourceValueMap="name" />
        </Form.Item>
    );
    const dynamicRender = (item: dynamicConfigItem) => (
        <Form.Item
            {...layoutItem}
            label={item.label}
            name={item.key}
            rules={[...requiredRules]}
        >
            <Input style={{ width: '100%' }} />
        </Form.Item>
    );
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
                        source={metricList}
                        sourceValueMap="key"
                        onChange={getConfigsName}
                    />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_database' })}
                    name="database"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_databases' }) }]}
                >
                    <CustomSelect onChange={databasesChange} allowClear source={databases} sourceValueMap="name" />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_table' })}
                    name="table"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_table' }) }]}
                >
                    <CustomSelect onChange={tableChange} allowClear source={tables} sourceValueMap="name" />
                </Form.Item>
                <Form.Item noStyle dependencies={['table']}>
                    {() => {
                        const value = form.getFieldValue('table');
                        if (!value) {
                            return null;
                        }
                        return renderColumn();
                    }}
                </Form.Item>
                {
                    configsName.map((item) => <React.Fragment key={item.key}>{dynamicRender(item)}</React.Fragment>)
                }
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
