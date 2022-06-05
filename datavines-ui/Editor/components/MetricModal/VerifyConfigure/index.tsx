import React, { useState } from 'react';
import { useIntl } from 'react-intl';
import {
    Row, Col, Form, Input,
} from 'antd';
import { CustomSelect, useMount } from '../../../common';
import { layoutItem } from '../helper';
import Title from '../Title';
import useRequest from '../../../hooks/useRequest';
import useRequiredRule from '../../../hooks/useRequiredRule';

const Index = () => {
    const intl = useIntl();
    const { $http } = useRequest();
    const [operatorList, setOperatorList] = useState([]);
    const [resultFormula, setResultFormula] = useState([]);
    const requiredRule = useRequiredRule();
    useMount(async () => {
        try {
            const $resultFormula = await $http.get('metric/resultFormula/list');
            const $operatorList = await $http.get('metric/operator/list');
            setOperatorList($operatorList || []);
            setResultFormula($resultFormula || []);
        } catch (error) {
        }
    });
    return (
        <Title title={intl.formatMessage({ id: 'dv_metric_title_verify_configure' })}>
            <Row gutter={10}>
                <Col span={8}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_verify_formula' })}
                        name="result_formula"
                        rules={[...requiredRule]}
                    >
                        <CustomSelect
                            allowClear
                            source={resultFormula}
                            sourceValueMap="key"
                        />
                    </Form.Item>
                </Col>
                <Col span={8}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_verify_compare' })}
                        name="operator"
                        rules={[...requiredRule]}
                    >
                        <CustomSelect
                            allowClear
                            source={operatorList}
                            sourceValueMap="key"
                        />
                    </Form.Item>
                </Col>
                <Col span={8}>
                    <Form.Item
                        {...layoutItem}
                        rules={[...requiredRule]}
                        label={intl.formatMessage({ id: 'dv_metric_verify_threshold' })}
                        name="threshold"
                    >
                        <Input />
                    </Form.Item>
                </Col>
            </Row>
        </Title>
    );
};

export default Index;
