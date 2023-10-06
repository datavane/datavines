/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { lazy } from 'react';
import { DatabaseOutlined, UnorderedListOutlined, HistoryOutlined ,BarChartOutlined} from '@ant-design/icons';
import { TRouter } from './type';

const detailRouter: TRouter = {
    'dv-detail-dashboard': {
        path: '/main/detail/:id/dashboard',
        key: '/main/detail/:id/dashboard',
        label: '',
        exact: false,
        icon: <BarChartOutlined/>,
        component: lazy(() => import(/* webpackChunkName: 'view-detail-dashboard' */ '@/view/Main/HomeDetail/Dashboard')),
    },
    'dv-detail-editor': {
        path: '/main/detail/:id/editor',
        key: '/main/detail/:id/editor',
        label: '',
        exact: true,
        icon: <DatabaseOutlined />,
        component: lazy(() => import(/* webpackChunkName: 'view-detail-editor' */ '@/view/Main/HomeDetail/EditorData')),
    },
    'dv-detail-jobs': {
        path: '/main/detail/:id/jobs',
        key: '/main/detail/:id/jobs',
        label: '',
        exact: false,
        icon: <UnorderedListOutlined />,
        component: lazy(() => import(/* webpackChunkName: 'view-detail-jobs' */ '@/view/Main/HomeDetail/Jobs')),
    }
};

export default detailRouter;
