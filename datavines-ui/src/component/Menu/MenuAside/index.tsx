/* eslint-disable array-callback-return */
import React, { useEffect, useCallback, useState } from 'react';
import { Menu } from 'antd';
import { RouteComponentProps, withRouter } from 'react-router-dom';
import useWatch from '@Editor/common/useWatch';

export type MenuItem = {
    label: React.ReactNode,
    key: string,
    path: string,
    icon?: React.ReactNode,
    children?: MenuItem[],
    menuHide?: boolean,
    exact?: boolean,
    [key: string]: any,
}

interface TMenuAside extends RouteComponentProps {
    menus: MenuItem[]
}

const MenuAside: React.FC<TMenuAside> = ({ menus, history }) => {
    const [activeKeys, setActiveKeys] = useState<string[]>(['']);
    const setActiveKeyRedirect = (redirect = false) => {
        const url = window.location.href;
        const match = menus.find((item) => {
            if (item.path.includes(':id')) {
                const pathArr = item.path.split(':id');
                return url.includes(pathArr[0]) && url.includes(pathArr[1]);
            }
            if (url.includes(item.path)) {
                return true;
            }
        });
        const currItem = (match || menus[0]) as MenuItem;
        if (!currItem) {
            return;
        }
        if (!activeKeys.includes(currItem.key as string)) {
            setActiveKeys([currItem.key as string]);
        }
        if (!match && redirect) {
            history.replace(currItem.path);
        }
    };
    useWatch(menus, () => setActiveKeyRedirect(true), { immediate: true });
    const clickMenuItem = useCallback((obj) => {
        setActiveKeys(obj.key);
        history.push(obj.key);
    }, []);

    const $munus = menus.map((item) => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { exact, ...rest } = item;
        return {
            ...rest,
            style: {
                paddingLeft: 15,
                paddingRight: 15,
                fontSize: 14,
            },
        };
    }).filter((item) => (!item.menuHide)) as MenuItem[];

    return (
        <div>
            <Menu
                selectedKeys={activeKeys}
                mode="inline"
                onClick={clickMenuItem}
                // theme="dark"
                items={$munus}
            />
        </div>
    );
};

export default withRouter(MenuAside);
