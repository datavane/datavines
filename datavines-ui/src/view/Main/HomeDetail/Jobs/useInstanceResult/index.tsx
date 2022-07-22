/* eslint-disable react/no-danger */
import React, { useRef, useState } from 'react';
import { ModalProps, Row, Col } from 'antd';
import {
    useModal, useImmutable, useMount,
} from 'src/common';
import { useIntl } from 'react-intl';
import { $http } from '@/http';
import { useSelector } from '@/store';

type ResultProps = {
    checkResult: string,
    checkSubject: string,
    expectedType: string,
    metricName: string,
    resultFormulaFormat: string,
}
const Inner = (props: any) => {
    const { locale } = useSelector((r) => r.commonReducer);
    const [result, setResult] = useState<{key: string, value: string}[]>([]);
    const intl = useIntl();
    const getIntl = (id: any) => intl.formatMessage({ id });
    const getData = async () => {
        try {
            const res = (await $http.get<ResultProps>(`task/result/${props.record.id}`)) || {};
            setResult([
                { key: 'jobs_task_check_subject', value: res.checkSubject },
                { key: 'jobs_task_check_rule', value: res.metricName },
                { key: 'jobs_task_check_result', value: res.checkResult },
                { key: 'jobs_task_check_expectVal_type', value: res.expectedType },
                { key: 'jobs_task_check_formula', value: res.resultFormulaFormat },
                { key: 'jobs_task_check_explain', value: getIntl('jobs_task_check_explain_text') },
            ]);
        } catch (error) {
            console.log(error);
        } finally {
        }
    };

    const getItem = (key: string, value: string) => (
        <Row style={{ marginBottom: key === 'jobs_task_check_formula' ? 30 : 10 }}>
            <Col span={locale === 'zh_CN' ? 4 : 7} style={{ textAlign: 'right' }}>
                {getIntl(key)}
                ：
            </Col>
            <Col span={locale === 'zh_CN' ? 20 : 17}>{value}</Col>
        </Row>
    );
    useMount(() => {
        getData();
    });
    return (
        <div style={{ fontSize: 14, minHeight: 260 }}>
            {
                result.map((item) => getItem(item.key, item.value))
            }
        </div>
    );
};

export const useInstanceResult = (options: ModalProps) => {
    const intl = useIntl();
    const recordRef = useRef<any>();
    const {
        Render, show, ...rest
    } = useModal<any>({
        title: `${intl.formatMessage({ id: 'jobs_task_check_result' })}`,
        footer: null,
        width: '600px',
        ...(options || {}),
        afterClose() {
            recordRef.current = null;
        },
    });
    return {
        Render: useImmutable(() => (<Render><Inner record={recordRef.current} /></Render>)),
        show(record: any) {
            recordRef.current = record;
            show(record);
        },
        ...rest,
    };
};
