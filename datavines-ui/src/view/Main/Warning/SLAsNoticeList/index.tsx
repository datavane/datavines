import React, { useState } from 'react';
import { Tabs } from 'antd';
import { useIntl } from 'react-intl';
import SLAsList from './SLAsList';
import NoticeList from './NoticeList';

const { TabPane } = Tabs;

export default () => {
    const [activeKey, setActiveKey] = useState('SLAS');
    const intl = useIntl();
    return (
        <div>
            <Tabs activeKey={activeKey} type="card" onChange={(key) => (setActiveKey(key))}>
                <TabPane tab="SLAs" key="SLAS">
                    <SLAsList />
                </TabPane>
                <TabPane tab={intl.formatMessage({ id: 'common_notice' })} key="Notice">
                    <NoticeList />
                </TabPane>
            </Tabs>
        </div>
    );
};
