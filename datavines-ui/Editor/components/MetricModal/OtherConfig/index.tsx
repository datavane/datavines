import React from 'react';
import { useIntl } from 'react-intl';
import {
    Row, Col, Form, InputNumber, FormInstance,
} from 'antd';
import { CustomSelect } from '../../../common';
import Title from '../Title';
import { layoutItem } from '../helper';

type InnerProps = {
    form: FormInstance
}

const Index = ({ form }: InnerProps) => {
    const intl = useIntl();

    return (
        <Title title={intl.formatMessage({ id: 'dv_metric_other_config' })}>
            <Row gutter={20}>
                <Col span={12}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_other_retry_number' })}
                        name="retryTimes"
                        initialValue={0}
                    >
                        <InputNumber min={0} />
                    </Form.Item>
                </Col>

                <Col span={12}>
                    <Form.Item
                        {...layoutItem}
                        initialValue={1}
                        label={intl.formatMessage({ id: 'dv_metric_other_retry_interval' })}
                        name="retryInterval"
                    >
                        <InputNumber min={0} />
                    </Form.Item>
                </Col>

                <Col span={12}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_other_timeout' })}
                        name="timeout"
                        initialValue={36000}
                    >
                        <InputNumber min={0} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_other_timeout_strategy' })}
                        name="timeoutStrategy"
                        initialValue={0}
                    >
                        <CustomSelect
                            allowClear
                            source={[
                                { label: intl.formatMessage({ id: 'dv_metric_other_timeout_strategy_retry' }), value: 0 },
                                { label: intl.formatMessage({ id: 'dv_metric_other_timeout_strategy_alert' }), value: 1 }]}
                        />
                    </Form.Item>
                </Col>
            </Row>
        </Title>
    );
};

export default Index;
