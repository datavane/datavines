import React, { lazy } from 'react';
import { DatabaseOutlined, WarningOutlined } from '@ant-design/icons';
import { TRouter } from './type';

const router: TRouter = {
    'dv-home': {
        path: '/main/home',
        key: '/main/home',
        label: '数据源',
        icon: <DatabaseOutlined />,
        component: lazy(() => import(/* webpackChunkName: 'view-home' */ '@/view/Main/Home')),
    },
    'dv-warning': {
        path: '/main/warning',
        key: '/main/warning',
        label: '告警',
        icon: <WarningOutlined />,
        component: lazy(() => import(/* webpackChunkName: 'view-warning' */ '@/view/Main/Warning')),
    },
    'dv-home-detail': {
        path: '/main/detail/:id',
        key: '/main/detail',
        menuHide: true,
        label: '',
        exact: false,
        icon: null,
        component: lazy(() => import(/* webpackChunkName: 'view-home-detail' */ '@/view/Main/HomeDetail')),
    },
};

export default router;
