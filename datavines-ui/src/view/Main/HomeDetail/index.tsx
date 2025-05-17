import React, { useEffect, useState } from 'react';
import useRoute from 'src/router/useRoute';
import { useIntl } from 'react-intl';
import {
    Route, Switch, useHistory, useLocation, useParams, useRouteMatch,
} from 'react-router-dom';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import { CustomSelect } from '@Editor/common';
import { useSelector } from 'react-redux';
import { MenuItem } from '@/component/Menu/MenuAside';
import MenuLayout from '@/component/Menu/Layout';
import { $http } from '@/http';
import store from '@/store';
import EditorData from '@/view/Main/HomeDetail/EditorData';
import Jobs from '@/view/Main/HomeDetail/Jobs';
import Dashboard from "view/Main/HomeDetail/Dashboard";
import JobExecutionLogs from "view/Main/HomeDetail/JobExecutionLogs";

type DataSource = {
    id:number,
    name:string
}
const DetailMain = () => {
    const { isDetailPage } = useSelector((r:any) => r.commonReducer);
    const { workspaceId } = useSelector((r:any) => r.workSpaceReducer);
    const { editType } = useSelector((r:any) => r.datasourceReducer);
    const location = useLocation();
    const [dataSourceList, setDataSourceList] = useState<DataSource[]>([]);
    const params = useParams<{ id: string}>();
    useEffect(() => {
        if (isDetailPage) {
            try {
                $http.get('/datasource/page', {
                    workSpaceId: workspaceId,
                    pageNumber: 1,
                    pageSize: 9999,
                }).then((res) => {
                    setDataSourceList(res?.records || []);
                });
            } catch (error) {
                console.log('error', error);
            }
        }
    }, [isDetailPage]);
    const match = useRouteMatch();
    const intl = useIntl();
    const { detailRoutes, visible } = useRoute();
    if (!visible || !detailRoutes.length) {
        return null;
    }
    const detailMenus = detailRoutes.map((item) => ({
        ...item,
        key: item.path.replace(/:id/, (match.params as any).id || ''),
        label: intl.formatMessage({ id: item.path as any }),
    })) as MenuItem[];

    const history = useHistory();
    const goBack = () => {
        // eslint-disable-next-line no-unused-expressions
        location.pathname.includes('jobs/instance') ? history.goBack() : history.push('/main/home');
    };
    const onChangeDataSource = (id: string) => {
        const url = `${match.path}`.replace(/:id/, id);
        history.push(`${url}/dashboard`);
    };
    const changeType = () => {
        store.dispatch({
            type: 'save_datasource_editType',
            payload: !editType,
        });
    };
    const renderDataSourceSelect = () => (
        <CustomSelect
            showSearch
            style={{
                width: 240,
            }}
            placeholder={intl.formatMessage({ id: 'header_top_search_msg' })}
            optionFilterProp="children"
            filterOption={(input, option: any) => option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
            source={dataSourceList}
            value={params?.id ? +params.id : undefined}
            onChange={onChangeDataSource}
            sourceLabelMap="name"
            sourceValueMap="id"
        />
    );
    const renderTopContent = () => (
        <div
            className="dv-title-edit"
            style={{
                paddingTop: '20px',
            }}
        >
            <div onClick={goBack} style={{ cursor: 'pointer' }}>
                <ArrowLeftOutlined />
            </div>
            {renderDataSourceSelect()}
            {!location.pathname.includes('jobs') && !location.pathname.includes('dashboard') && !location.pathname.includes('jobExecutionLogs') ? (
                <Button style={{ marginLeft: '10px' }} onClick={changeType}>
                    { editType ? intl.formatMessage({ id: 'jobs_directory' }) : intl.formatMessage({ id: 'jobs_editor' })}
                </Button>
            ) : ''}

        </div>
    );
    return (

        <MenuLayout menus={detailMenus}>
            {renderTopContent()}
            <div style={{
                display: location.pathname.includes('dashboard') ? 'block' : 'none',
            }}
            >
                {
                    location.pathname.includes('dashboard') ? <div>
                        <Dashboard />
                    </div>: ''
                }
            </div>

            <div style={{
                display: location.pathname.includes('editor') ? 'block' : 'none',
            }}
            >
                {
                    location.pathname.includes('editor') ? <div>
                        <EditorData />
                    </div>: ''
                }
            </div>

            <div style={{
                display: location.pathname.includes('jobs') ? 'block' : 'none',
            }}
            >
                {
                    location.pathname.includes('jobs') ? <div>
                        <Jobs />
                    </div>: ''
                }
            </div>

            <div style={{
                display: location.pathname.includes('jobExecutionLogs') ? 'block' : 'none',
            }}
            >
                {
                    location.pathname.includes('jobExecutionLogs') ? <div>
                        <JobExecutionLogs />
                    </div>: ''
                }
            </div>
        </MenuLayout>

    );
};

export default DetailMain;
