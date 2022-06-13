import React, { useImperativeHandle, useRef } from 'react';
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
    innerRef: {
        current: FormInstance
    }
}

const Inner = ({ innerRef }: InnerProps) => {
    const [form] = Form.useForm();
    useImperativeHandle(innerRef, () => (form));
    return (
        <Form form={form}>
            <MetricSelect form={form} />
            <ExpectedValue form={form} />
            <VerifyConfigure />
            <ActuatorConfigure form={form} />
        </Form>
    );
};

export const useMetricModal = () => {
    const innerRef:InnerProps['innerRef'] = useRef<any>();
    const intl = useIntl();
    const onSave = usePersistFn(async () => {
        innerRef.current.validateFields().then(async (values) => {
            console.log('values', values);
        }).catch(() => {});
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
        Render: useImmutable(() => (<Render><Inner innerRef={innerRef} /></Render>)),
        ...rest,
    };
};
