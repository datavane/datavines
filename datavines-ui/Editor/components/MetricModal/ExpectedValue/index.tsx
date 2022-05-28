import React from 'react';
import { useIntl } from 'react-intl';
import {
    Row, Col, Form, Input,
} from 'antd';
import CustomSelect from '../../../common/CustomSelect';
import Title from '../Title';
import { layoutItem } from '../helper';

const Index = () => {
    const intl = useIntl();
    return (
        <Title title={intl.formatMessage({ id: 'dv_metric_title_expected_value' })}>
            <Row>
                <Col span={12}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_expected_value_type' })}
                        name="expectType"
                    >
                        <CustomSelect allowClear source={[{ labe: '1', value: '1' }]} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_expected_value' })}
                        name="expectValue"
                    >
                        <Input allowClear />
                    </Form.Item>
                </Col>
            </Row>
        </Title>
    );
};

export default Index;
