import React, {
    useRef, useImperativeHandle, useState, useMemo,
} from 'react';
import {
    ModalProps, Tabs, Spin, Button, message,
} from 'antd';
import {
    useModal, useImmutable, usePersistFn, useContextModal, useMount, useLoading, IF,
} from 'src/common';
import { useIntl } from 'react-intl';
import { DvEditor } from '@Editor/index';
import { useSelector } from '@/store';
import { $http } from '@/http';
import Schedule from '../components/Schedule';
import PageContainer from './PageContainer';
import { SelectSLAsComponent } from '../useSelectSLAsModal';

const { TabPane } = Tabs;

type InnerProps = {
    innerRef: any
}

const Inner = ({ innerRef }: InnerProps) => {
    const [activeKey, setActiveKey] = useState('metric');
    const [loading, $setLoading] = useState(true);
    const setLoading = useLoading();
    const intl = useIntl();
    const { data } = useContextModal();
    const [jobId, setJobId] = useState(data.record?.id);
    const [metricDetail, setMetricDetail] = useState(data.record?.parameterItem ? data.record : {});
    console.log('metricDetail', metricDetail);
    const metricConfigRef = useRef<any>();
    const { loginInfo } = useSelector((r) => r.userReducer);
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const { locale } = useSelector((r) => r.commonReducer);
    const editorParams = useMemo(() => ({
        baseURL: '/api/v1',
        workspaceId,
        headers: {
            Authorization: `Bearer ${loginInfo.token}`,
        },
    }), [workspaceId]);
    useImperativeHandle(innerRef, () => ({
        getData() {
            return {};
        },
    }));
    const getData = async (id: string, showLoading = true) => {
        if (showLoading) {
            setLoading(true);
        }
        const res = await $http.get(`/job/${id}`);
        res.parameterItem = res.parameter ? JSON.parse(res.parameter)[0] : {};
        if (showLoading) {
            setMetricDetail(res);
            setLoading(false);
        } else {
            return res;
        }
    };
    useMount(async () => {
        if (!data.record?.id) {
            $setLoading(false);
            return;
        }
        try {
            const res = await getData(data.record.id, false);
            setMetricDetail(res);
        } catch (error) {
        } finally {
            setTimeout(() => {
                $setLoading(false);
            }, 100);
        }
    });
    const onJob = usePersistFn(async (runningNow = 0) => {
        try {
            setLoading(true);
            const params = await metricConfigRef.current.getValues();
            if (data.record?.id) {
                await $http.put('/job', { ...params, id: data.record?.id, runningNow });
            } else {
                const res = await $http.post('/job', { ...params, runningNow });
                console.log('----', res);
                setJobId(res);
            }
            message.success('Success!');
        } catch (error) {
            console.log('error', error);
        } finally {
            setLoading(false);
        }
    });
    if (loading) {
        return <Spin spinning={loading} />;
    }
    const slaId = (data?.record?.slaList || [])[0]?.id;
    return (
        <div>
            <Tabs
                activeKey={activeKey}
                onChange={(key) => {
                    if (!jobId) {
                        message.info(intl.formatMessage({ id: 'jobs_add_tip' }));
                        return null;
                    }
                    setActiveKey(key);
                }}
            >
                <TabPane tab={intl.formatMessage({ id: 'jobs_tabs_config' })} key="metric">
                    <PageContainer
                        footer={(
                            <>
                                <Button type="primary" onClick={() => onJob()}>保存</Button>
                                <Button style={{ marginLeft: 10 }} type="primary" onClick={() => onJob(1)}>保存并运行</Button>
                            </>
                        )}
                    >
                        <div style={{ width: 'calc(100vw - 80px)' }}>
                            <DvEditor {...editorParams} locale={locale} innerRef={metricConfigRef} id={data.id} showMetricConfig detail={metricDetail} />
                        </div>
                    </PageContainer>
                </TabPane>
                <TabPane tab={intl.formatMessage({ id: 'jobs_tabs_schedule' })} key="schedule">
                    <IF visible={jobId}><Schedule jobId={jobId} /></IF>
                </TabPane>
                <TabPane tab={intl.formatMessage({ id: 'jobs_tabs_SLA' })} key="SLA">
                    <IF visible={jobId}><SelectSLAsComponent jobId={jobId} id={slaId} /></IF>
                </TabPane>
            </Tabs>
        </div>
    );
};

export const useAddEditJobsModal = (options: ModalProps) => {
    const innerRef = useRef();
    const onOk = usePersistFn(() => {
    });
    const { Render, ...rest } = useModal<any>({
        title: 'Schedule Manage',
        width: 640,
        ...(options || {}),
        bodyStyle: {
            overflow: 'hidden',
        },
        onOk,
        footer: null,
        className: 'dv-modal-fullscreen',
    });
    return {
        Render: useImmutable(() => (<Render><Inner innerRef={innerRef} /></Render>)),
        ...rest,
    };
};
