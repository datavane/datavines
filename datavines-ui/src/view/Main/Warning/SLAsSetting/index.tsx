import React, { useState } from 'react';
import { Tabs } from 'antd';
import { useIntl } from 'react-intl';
import { GoBack } from '@/component';
import BasicInfo from './BasicInfo';
import Notification from './Notification';

const { TabPane } = Tabs;

export default () => {
    const [activeKey, setActiveKey] = useState('basicInfo');
    const intl = useIntl();
    return (
        <div>
            <div className="dv-flex-between" style={{ marginBottom: 20 }}>
                <GoBack />
                <span />
            </div>
            <div style={{ padding: '0 20px' }}>
                <Tabs activeKey={activeKey} type="card" onChange={(key) => (setActiveKey(key))}>
                    <TabPane tab={intl.formatMessage({ id: 'warn_setting_basic_info' })} key="basicInfo">
                        <BasicInfo />
                    </TabPane>
                    <TabPane tab={intl.formatMessage({ id: 'common_notice' })} key="notification">
                        <Notification />
                    </TabPane>
                </Tabs>
            </div>

        </div>
    );
};
