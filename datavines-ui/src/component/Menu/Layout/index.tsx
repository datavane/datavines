import React, { useState } from 'react';
import { Layout } from 'antd';
import { MenuUnfoldOutlined, MenuFoldOutlined } from '@ant-design/icons';
import AsideMenu, { MenuItem } from '../MenuAside';
import HeaderTop from '../../Header';
import './index.less';

const { Header, Content, Sider } = Layout;

type TMainProps = {
    menus: MenuItem[],
    visible?: boolean,
};

const MenuLayout: React.FC<TMainProps> = ({ children, menus, visible = true }) => {
    const [collapsed, setCollapsed] = useState(false);
    const onCollapse = (bool: boolean) => {
        setCollapsed(bool);
    };
    if (!visible) {
        return <>{children}</>;
    }
    return (
        <Layout>
            <Header className="dv-header-layout">
                <HeaderTop />
            </Header>
            <Layout>
                <Sider
                    theme="light"
                    style={{ height: 'calc(100vh - 60px)', overflow: 'auto', borderTop: '1px solid #f0f0f0' }}
                    width={180}
                    collapsedWidth={45}
                    trigger={(
                        <div style={{ position: 'absolute', right: 15, fontSize: 16 }}>
                            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                        </div>
                    )}
                    collapsible
                    collapsed={collapsed}
                    onCollapse={onCollapse}
                >
                    <AsideMenu menus={menus} />
                </Sider>
                <Layout style={{ padding: '10px' }}>
                    <Content>
                        {children}
                    </Content>
                </Layout>
            </Layout>
        </Layout>
    );
};

export default MenuLayout;
