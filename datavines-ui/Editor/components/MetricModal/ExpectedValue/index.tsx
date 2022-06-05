import React, { useState } from 'react';
import { useIntl } from 'react-intl';
import {
    Row, Col, Form, Input, FormInstance,
} from 'antd';
import { CustomSelect, useMount } from '../../../common';
import Title from '../Title';
import { layoutItem } from '../helper';
import useRequest from '../../../hooks/useRequest';
import useRequiredRule from '../../../hooks/useRequiredRule';

type InnerProps = {
    form: FormInstance
}

const Index = ({ form }: InnerProps) => {
    const intl = useIntl();
    const { $http } = useRequest();
    const [expectedTypeList, setExpectedTypeList] = useState([]);
    const requiredRules = useRequiredRule();
    useMount(async () => {
        try {
            const res = await $http.get('metric/expectedValue/list');
            setExpectedTypeList(res || []);
        } catch (error) {
        }
    });

    return (
        <Title title={intl.formatMessage({ id: 'dv_metric_title_expected_value' })}>
            <Row>
                <Col span={12}>
                    <Form.Item
                        {...layoutItem}
                        rules={requiredRules}
                        // rules={
                        //     [
                        //         { required: true, message: '123' },
                        //     ]
                        // }
                        label={intl.formatMessage({ id: 'dv_metric_expected_value_type' })}
                        name="expectedType"
                    >
                        <CustomSelect allowClear source={expectedTypeList} sourceValueMap="key" />
                    </Form.Item>
                </Col>

                <Col span={12}>
                    <Form.Item noStyle dependencies={['expectedType']}>
                        {() => {
                            const value = form.getFieldValue('expectedType');
                            if (value !== 'fix_value') {
                                return null;
                            }
                            return (
                                <Form.Item
                                    {...layoutItem}
                                    rules={requiredRules}
                                    label={intl.formatMessage({ id: 'dv_metric_expected_value' })}
                                    name="expected_value"
                                >
                                    <Input allowClear />
                                </Form.Item>
                            );
                        }}
                    </Form.Item>

                </Col>
            </Row>
        </Title>
    );
};

export default Index;
