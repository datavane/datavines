import React from 'react';
import {
    Row, Col, Form, Input, InputNumber,
} from 'antd';
import { useIntl } from 'react-intl';
import CustomSelect from '../../../common/CustomSelect';
import './index.less';
import { layoutItem } from '../helper';

const Index = () => {
    const intl = useIntl();
    return (
        <Row gutter={30}>
            <Col span={12}>
                <Form.Item
                    {...layoutItem}
                    label="Metric"
                    name="metricType"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_placeholder' }) }]}
                >
                    <CustomSelect allowClear source={[{ labe: '1', value: '1' }]} />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_table' })}
                    name="table"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_table' }) }]}
                >
                    <CustomSelect allowClear source={[{ labe: '1', value: '1' }]} />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_column' })}
                    name="column"
                    rules={[{ required: true, message: intl.formatMessage({ id: 'editor_dv_metric_select_column' }) }]}
                >
                    <CustomSelect allowClear source={[{ labe: '1', value: '1' }]} />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_maxValue' })}
                    name="metric"
                >
                    <InputNumber style={{ width: '50%' }} />
                </Form.Item>
                <Form.Item
                    {...layoutItem}
                    label={intl.formatMessage({ id: 'dv_metric_minValue' })}
                    name="metric"
                >
                    <InputNumber style={{ width: '50%' }} />
                </Form.Item>
            </Col>
            <Col span={12}>
                <div style={{ paddingTop: 60 }} />
                <Form.Item
                    className="dv-editor__condition"
                    colon={false}
                    label={(
                        <div style={{ marginTop: -60 }}>
                            {intl.formatMessage({ id: 'dv_metric_condition' })}
                            :
                        </div>
                    )}
                    name="condition"
                >
                    <Input.TextArea style={{ marginLeft: -60 }} rows={5} />
                </Form.Item>
            </Col>
        </Row>
    );
};

export default Index;
