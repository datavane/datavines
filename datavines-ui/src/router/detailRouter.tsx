/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { lazy } from 'react';
import { DatabaseOutlined, UnorderedListOutlined, HistoryOutlined } from '@ant-design/icons';
import { TRouter } from './type';

const detailRouter: TRouter = {
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
    },
    // 'dv-detail-tasks': {
    //     path: '/main/detail/:id/tasks',
    //     key: '/main/detail/:id/tasks',
    //     label: '',
    //     exact: true,
    //     icon: <HistoryOutlined />,
    //     component: lazy(() => import(/* webpackChunkName: 'view-detail-tasks' */ '@/view/Main/HomeDetail/Tasks')),
    // },
};

export default detailRouter;
