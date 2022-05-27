import React from 'react';
import {
    Button, Form, FormInstance,
} from 'antd';
import { useIntl } from 'react-intl';
import {
    useModal, useImmutable, usePersistFn,
} from '@/common';
import MetricSelect from './MetricSelect';
import ExpectedValue from './ExpectedValue';
import VerifyConfigure from './VerifyConfigure';
import ActuatorConfigure from './ActuatorConfigure';

type InnerProps = {
    form: FormInstance
}

const Inner = ({ form }: InnerProps) => {
    const intl = useIntl();
    return (
        <Form form={form}>
            <MetricSelect />
            <ExpectedValue />
            <VerifyConfigure />
            <ActuatorConfigure form={form} />
        </Form>
    );
};

export const useMetricModal = () => {
    const [form] = Form.useForm();
    const intl = useIntl();
    const onSave = usePersistFn(async () => {

    });
    const onSaveRun = usePersistFn(async () => {

    });
    const { Render, ...rest } = useModal<any>({
        title: (
            <div className="dv-editor-flex-between">
                <span>
                    {'Metric '}
                    {
                        intl.formatMessage({ id: 'dv_config_text' })
                    }
                </span>
                <span style={{ marginRight: 20 }}>
                    <Button type="primary" onClick={onSave}>{intl.formatMessage({ id: 'dv_metric_save' })}</Button>
                    <Button type="primary" onClick={onSaveRun} style={{ marginLeft: 12 }}>{intl.formatMessage({ id: 'dv_metric_save_run' })}</Button>
                </span>
            </div>
        ),
        width: 900,
        maskClosable: false,
        footer: null,
    });
    return {
        Render: useImmutable(() => (<Render><Inner form={form} /></Render>)),
        ...rest,
    };
};
