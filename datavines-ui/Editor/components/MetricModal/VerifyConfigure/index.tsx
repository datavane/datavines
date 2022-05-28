import React from 'react';
import { useIntl } from 'react-intl';
import {
    Row, Col, Form, Input,
} from 'antd';
import CustomSelect from '../../../common/CustomSelect';
import { layoutItem } from '../helper';
import Title from '../Title';

const Index = () => {
    const intl = useIntl();
    return (
        <Title title={intl.formatMessage({ id: 'dv_metric_title_verify_configure' })}>
            <Row gutter={10}>
                <Col span={8}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_verify_formula' })}
                        name="formula"
                    >
                        <CustomSelect allowClear source={[{ labe: '1', value: '1' }]} />
                    </Form.Item>
                </Col>
                <Col span={8}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_verify_compare' })}
                        name="compare"
                    >
                        <CustomSelect allowClear source={[{ labe: '1', value: '1' }]} />
                    </Form.Item>
                </Col>
                <Col span={8}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_verify_threshold' })}
                        name="verifyValue"
                    >
                        <Input allowClear />
                    </Form.Item>
                </Col>
            </Row>
        </Title>
    );
};

export default Index;
