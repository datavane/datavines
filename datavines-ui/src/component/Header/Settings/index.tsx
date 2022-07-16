import React from 'react';
import { Dropdown, Menu } from 'antd';
import { LoginOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router-dom';
import { SwitchLanguage } from '@/component';
import { useSelector } from '@/store';
import { DV_STORAGE_LOGIN } from '@/utils/constants';

const HeaderRight = () => {
    const history = useHistory();
    const { loginInfo } = useSelector((r) => r.userReducer);
    const handleMenuClick = () => {
        window.localStorage.removeItem(DV_STORAGE_LOGIN);
        history.push('/login');
    };
    return (
        <div className="dv-header-right">
            <div className="dv-header__switch-language"><SwitchLanguage /></div>
            <span style={{ margin: '0 15px' }}>{loginInfo.username}</span>
            <Dropdown
                overlay={(
                    <Menu
                        onClick={handleMenuClick}
                        items={[
                            {
                                label: 'Log out',
                                key: '0',
                            },
                        ]}
                    />
                )}
            >
                <LoginOutlined style={{ fontSize: 14, cursor: 'pointer' }} />
            </Dropdown>
        </div>
    );
};

export default React.memo(HeaderRight);
