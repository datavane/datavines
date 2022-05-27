import React from 'react';
import { useIntl } from 'react-intl';
import {
    Row, Col, Form, Input, Radio, FormInstance,
} from 'antd';
import Title from '../Title';
import CustomSelect from '../../../common/CustomSelect';
import {
    layoutItem, layoutActuatorItem, layoutActuatorLineItem,
} from '../helper';

type InnerProps = {
    form: FormInstance
}

const Index = ({ form }: InnerProps) => {
    const intl = useIntl();
    const renderSpark = () => (
        <>
            <Form.Item
                dependencies={['actuatorType']}
                {...layoutActuatorLineItem}
                label={intl.formatMessage({ id: 'dv_metric_expected_value' })}
                name="expectValue"
            >
                <Radio.Group>
                    <Radio value="cluster">cluster</Radio>
                    <Radio value="client">client</Radio>
                    <Radio value="local">local</Radio>
                </Radio.Group>
            </Form.Item>
            <Row gutter={20}>
                <Col span={12}>
                    <Form.Item
                        {...layoutActuatorItem}
                        label={intl.formatMessage({ id: 'dv_metric_actuator_driver_cores' })}
                        name="driverCores"
                    >
                        <Input allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        {...layoutActuatorItem}
                        label={intl.formatMessage({ id: 'dv_metric_actuator_driver_memory' })}
                        name="driverMemory"
                    >
                        <Input allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        {...layoutActuatorItem}
                        label={intl.formatMessage({ id: 'dv_metric_actuator_executor_numbers' })}
                        name="executorNumber"
                    >
                        <Input allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        {...layoutActuatorItem}
                        label={intl.formatMessage({ id: 'dv_metric_actuator_executor_memory' })}
                        name="executorMemory"
                    >
                        <Input allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        {...layoutActuatorItem}
                        label={intl.formatMessage({ id: 'dv_metric_actuator_executor_cores' })}
                        name="executorCores"
                    >
                        <Input allowClear />
                    </Form.Item>
                </Col>
            </Row>
            <Form.Item
                {...layoutActuatorLineItem}
                label={intl.formatMessage({ id: 'dv_metric_actuator_executor_options' })}
                name="excutorOptions"
            >
                <Input.TextArea rows={3} />
            </Form.Item>
        </>
    );
    return (
        <Title title={intl.formatMessage({ id: 'dv_metric_title_actuator_configure' })}>
            <Row>
                <Col span={24} style={{ display: 'flex' }}>
                    <Form.Item
                        {...layoutItem}
                        label=""
                        name="actuatorType"
                        initialValue="Spark"
                    >
                        <CustomSelect
                            source={
                                [
                                    { labe: 'JDBC', value: 'JDBC' },
                                    { labe: 'Spark', value: 'Spark' },
                                ]
                            }
                            style={{ width: 200 }}
                        />
                    </Form.Item>
                    <span style={{ paddingTop: 5, marginLeft: 15 }}>{intl.formatMessage({ id: 'dv_metric_actuator_tip' })}</span>
                </Col>
            </Row>
            <Form.Item noStyle dependencies={['actuatorType']}>
                {() => {
                    const value = form.getFieldValue('actuatorType');
                    if (value !== 'Spark') {
                        return null;
                    }
                    return renderSpark();
                }}
            </Form.Item>

        </Title>
    );
};

export default Index;
