import React, {
    useRef, useImperativeHandle, useState, useMemo,
} from 'react';
import {
    ModalProps, Tabs, Spin,
} from 'antd';
import {
    useModal, useImmutable, usePersistFn, useContextModal, useMount,
} from 'src/common';
import { useIntl } from 'react-intl';
import { DvEditor } from '@Editor/index';
import { useSelector } from '@/store';
import { $http } from '@/http';
import Schedule from '../components/Schedule';

const { TabPane } = Tabs;

type InnerProps = {
    innerRef: any
}

const Inner = ({ innerRef }: InnerProps) => {
    const [activeKey, setActiveKey] = useState('metric');
    const [loading, setLoading] = useState(true);
    const intl = useIntl();
    const { data } = useContextModal();
    const [metricDetail, setMetricDetail] = useState({});
    const metricConfigRef = useRef<any>();
    const { loginInfo } = useSelector((r) => r.userReducer);
    const editorParams = useMemo(() => ({
        baseURL: '/api/v1',
        headers: {
            Authorization: `Bearer ${loginInfo.token}`,
        },
    }), []);
    useImperativeHandle(innerRef, () => ({
        getData() {
            return {};
        },
    }));
    useMount(async () => {
        if (!data.record?.id) {
            setLoading(false);
            return;
        }
        try {
            const res = await $http.get(`/job/${data.record.id}`);
            res.parameterItem = res.parameter ? JSON.parse(res.parameter)[0] : {};
            setMetricDetail(res);
        } catch (error) {
        } finally {
            setLoading(false);
        }
    });
    if (loading) {
        return <Spin spinning={loading} />;
    }
    return (
        <div>
            <Tabs activeKey={activeKey} onChange={(key) => (setActiveKey(key))}>
                <TabPane tab={intl.formatMessage({ id: 'jobs_tabs_config' })} key="metric">
                    <DvEditor {...editorParams} innerRef={metricConfigRef} id={data.id} showMetricConfig detail={metricDetail} />
                </TabPane>
                <TabPane tab={intl.formatMessage({ id: 'jobs_tabs_schedule' })} key="schedule">
                    <Schedule />
                </TabPane>
                <TabPane tab={intl.formatMessage({ id: 'jobs_tabs_SLA' })} key="SLA">
                    <div>3</div>
                </TabPane>
            </Tabs>
        </div>
    );
};

export const useAddEditJobsModal = (options: ModalProps) => {
    const innerRef = useRef();
    const onOk = usePersistFn(() => {
        console.log('connfirm ok');
    });
    const { Render, ...rest } = useModal<any>({
        title: 'Schedule Manage',
        width: 640,
        ...(options || {}),
        onOk,
        footer: null,
        className: 'dv-modal-fullscreen',
    });
    return {
        Render: useImmutable(() => (<Render><Inner innerRef={innerRef} /></Render>)),
        ...rest,
    };
};
