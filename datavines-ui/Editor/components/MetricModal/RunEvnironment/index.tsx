import React, { useState } from 'react';
import { useIntl } from 'react-intl';
import {
    Row, Col, Form, FormInstance,
} from 'antd';
import { SettingOutlined } from '@ant-design/icons';
import { CustomSelect, useMount } from '../../../common';
import { layoutItem } from '../helper';
import Title from '../Title';
import useRequest from '../../../hooks/useRequest';
import useRequiredRule from '../../../hooks/useRequiredRule';
import { useTenantModal } from './useTenantModal';
import { useEnvModal } from './useEnvModal';

type InnerProps = {
    form: FormInstance
}

const Setting = ({ ...rest }) => <div style={{ paddingTop: 5, cursor: 'pointer' }} {...rest}><SettingOutlined /></div>;

const Index = ({ form }: InnerProps) => {
    const intl = useIntl();
    const { $http } = useRequest();
    const [tenantCodeList, setTenantCodeList] = useState([]);
    const [envList, setEnvList] = useState([]);
    const requiredRule = useRequiredRule();
    const { Render: RenderTenantModal, show: showTenant } = useTenantModal({
        title: intl.formatMessage({ id: 'dv_metric_linux_user' }),
        afterClose() {
            getTenantList();
        },
    });
    const { Render: RenderEnvModal, show: showEnv } = useEnvModal({
        title: intl.formatMessage({ id: 'dv_metric_env_config' }),
        afterClose() {
            getEnvList();
        },
    });
    const getTenantList = async () => {
        try {
            const tenantListOptions = await $http.get('tenant/listOptions');
            setTenantCodeList(tenantListOptions || []);
        } catch (error) {
        }
    };
    const getEnvList = async () => {
        try {
            const envListOptions = await $http.get('env/listOptions');
            setEnvList(envListOptions || []);
        } catch (error) {
        }
    };
    useMount(async () => {
        getTenantList();
        getEnvList();
    });
    const render = () => (
        <Title title={intl.formatMessage({ id: 'dv_metric_run_env_config' })}>
            <Row gutter={10}>
                <Col span={10}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_linux_user' })}
                        name="tenantCode"
                        rules={[...requiredRule]}
                    >
                        <CustomSelect
                            allowClear
                            source={tenantCodeList}
                            sourceValueMap="key"
                        />
                    </Form.Item>
                </Col>
                <Col span={2}>
                    <Setting
                        onClick={() => {
                            showTenant({
                                currentValue: form.getFieldValue('tenantCode'),
                            });
                        }}
                    />
                </Col>
                <Col span={10}>
                    <Form.Item
                        {...layoutItem}
                        label={intl.formatMessage({ id: 'dv_metric_env_config' })}
                        name="env"
                        rules={[...requiredRule]}
                    >
                        <CustomSelect
                            allowClear
                            source={envList}
                            sourceValueMap="key"
                        />
                    </Form.Item>
                </Col>
                <Col span={2}>
                    <Setting
                        onClick={() => {
                            showEnv({
                                currentValue: form.getFieldValue('env'),
                            });
                        }}
                    />

                </Col>
            </Row>
            <RenderTenantModal />
            <RenderEnvModal />
        </Title>
    );
    return (
        <Form.Item noStyle dependencies={['engineType']}>
            {() => {
                const value = form.getFieldValue('engineType');
                if (value !== 'spark') {
                    return null;
                }
                return render();
            }}
        </Form.Item>
    );
};

export default Index;
