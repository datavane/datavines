/* eslint-disable react/no-danger */
import React, { useRef, useState, useImperativeHandle } from 'react';
import { ModalProps, Spin } from 'antd';
import { SyncOutlined } from '@ant-design/icons';
import {
    useModal, useContextModal, useImmutable, usePersistFn, useMount, IF,
} from 'src/common';
import { useIntl } from 'react-intl';
import { $http } from '@/http';
import {CopyBlock, github} from "react-code-blocks";
type InnerProps = {
    innerRef: any
}


const Inner = ({ innerRef }: InnerProps) => {
    const [loading, setLoading] = useState(false);
    const { data } = useContextModal();
    const [jobExecutionConfig, setJobExecutionConfig] = useState("");
    const getData = async () => {
        try {
            setLoading(true);
            const res = (await $http.get(`/job/execute/config/${data.id}`)) || "";
            const json = JSON.stringify(JSON.parse(res), null, 4);
            setJobExecutionConfig(json);
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    useMount(async () => {
        getData();
    });
    useImperativeHandle(innerRef, () => ({
        onRefresh() {
            getData();
        },
    }));
    return (
        <Spin spinning={loading}>
            <div style={{ minHeight: 300 }}>
                <CopyBlock
                    language="javascript"
                    text={jobExecutionConfig}
                    codeBlock
                    theme={github}
                    showLineNumbers={true}
                />
                <div />
            </div>
        </Spin>
    );
};

export const useJobExecutionConfigPreview = (options: ModalProps) => {
    const intl = useIntl();
    const innerRef = useRef<any>();
    const recordRef = useRef<any>();
    const onOk = usePersistFn(() => {
        hide();
    });

    const {
        Render, hide, show, ...rest
    } = useModal<any>({
        title: (
            <div className="dv-flex-between">
                <span>{intl.formatMessage({ id: 'jobs_preview' })}</span>
                <div style={{ marginRight: 30 }}>
                    <a
                        style={{ marginRight: 10 }}
                        onClick={() => {
                            innerRef.current.onRefresh();
                        }}
                    >
                        <SyncOutlined style={{ marginRight: 5 }} />
                        {intl.formatMessage({ id: 'job_log_refresh' })}
                    </a>
                </div>
            </div>
        ),
        className: 'dv-modal-fullscreen',
        footer: null,
        width: '90%',
        ...(options || {}),
        afterClose() {
            recordRef.current = null;
        },
        onOk,
    });
    return {
        Render: useImmutable(() => (<Render><Inner innerRef={innerRef} /></Render>)),
        show(record: any) {
            recordRef.current = record;
            show(record);
        },
        ...rest,
    };
}

