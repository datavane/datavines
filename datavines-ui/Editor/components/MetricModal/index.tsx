import React, { useImperativeHandle, useRef, useState } from 'react';
import {
    Button, Form, FormInstance, message,
} from 'antd';
import { useIntl } from 'react-intl';
import {
    useModal, useImmutable, usePersistFn, useLoading, useMount,
} from '@/common';
import useRequest from '../../hooks/useRequest';
import MetricSelect from './MetricSelect';
import ExpectedValue from './ExpectedValue';
import VerifyConfigure from './VerifyConfigure';
import ActuatorConfigure from './ActuatorConfigure';
import RunEvnironment from './RunEvnironment';
import OtherConfig from './OtherConfig';
import { pickProps } from './helper';
import { TDetail } from './type';

type InnerProps = {
    innerRef?: {
        current: {
            form: FormInstance,
            getValues: (...args: any[]) => any
        }
    },
    id: string,
    detail: TDetail
}
const keys = [
    'engineType',
    'retryTimes',
    'retryInterval',
    'timeout',
    'timeoutStrategy',
    'tenantCode',
    'env',
];
export const MetricConfig = ({ innerRef, id, detail }: InnerProps) => {
    const [form] = Form.useForm();
    const metricSelectRef = useRef<any>();
    useImperativeHandle(innerRef, () => ({
        form,
        getValues() {
            return new Promise((resolve, reject) => {
                innerRef?.current.form.validateFields().then((values) => {
                    console.log('values', values);
                    const params: any = {
                        type: 'DATA_QUALITY',
                        dataSourceId: id,
                        ...(pickProps(values, [...keys])),
                    };
                    if (values.engineType === 'spark') {
                        params.engineParameter = {
                            programType: 'JAVA',
                            ...pickProps(values, ['deployMode', 'driverCores', 'driverMemory', 'numExecutors', 'executorMemory', 'executorCores', 'others']),
                        };
                    }
                    const parameter: any = {
                        ...(pickProps(values, ['metricType', 'expectedType', 'result_formula', 'operator', 'threshold'])),
                        metricParameter: {
                            ...(pickProps(values, ['database', 'table', 'column', 'filter'])),
                            ...(metricSelectRef.current.getDynamicValues()),
                        },
                    };
                    if (values.expectedType === 'fix_value') {
                        parameter.expectedParameter = {
                            expected_value: values.expected_value,
                        };
                    }
                    params.parameter = JSON.stringify([parameter]);
                    resolve(params);
                }).catch((error) => {
                    reject(error);
                });
            });
        },
    }));
    return (
        <Form form={form}>
            <MetricSelect detail={detail} id={id} form={form} metricSelectRef={metricSelectRef} />
            <ExpectedValue detail={detail} form={form} />
            <VerifyConfigure detail={detail} form={form} />
            <ActuatorConfigure detail={detail} form={form} />
            <RunEvnironment form={form} />
            <OtherConfig detail={detail} form={form} />
        </Form>
    );
};

export const useMetricModal = () => {
    const innerRef:InnerProps['innerRef'] = useRef<any>();
    const intl = useIntl();
    const { $http } = useRequest();
    const [id, setId] = useState('');
    const idRef = useRef(id);
    idRef.current = id;
    const [detail, setDetail] = useState<TDetail>(null);
    const detailRef = useRef(detail);
    detailRef.current = detail;
    const setLoading = useLoading();
    const onJob = usePersistFn(async (runningNow = 0) => {
        try {
            setLoading(true);
            const params = await innerRef.current.getValues();
            console.log('params', params);
            const res = await $http.post('/job', { ...params, runningNow });
            console.log('res', res);
            message.success('Success!');
        } catch (error) {
            console.log('error', error);
        } finally {
            setLoading(false);
        }
    });
    const onSave = usePersistFn(async () => {
        onJob();
    });
    const onSaveRun = usePersistFn(async () => {
        onJob(1);
    });
    const { Render, show, ...rest } = useModal<any>({
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
        afterClose() {
            setId('');
            setDetail(null);
        },
        maskClosable: false,
        footer: null,
    });
    return {
        Render: useImmutable(() => (<Render><MetricConfig id={idRef.current} detail={detailRef.current} innerRef={innerRef} /></Render>)),
        show($id: string, $detail: TDetail) {
            setId($id);
            setDetail($detail);
            show({});
        },
        ...rest,
    };
};
