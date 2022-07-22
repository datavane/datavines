import React, { useState } from 'react';
import {
    Dropdown, Menu, Popconfirm, Button,
} from 'antd';
import { useIntl } from 'react-intl';
import {
    EditOutlined, EllipsisOutlined, DeleteOutlined,
} from '@ant-design/icons';
import { useHistory } from 'react-router-dom';
import { useAddSpace } from './useAddSpace';
import { useSelector, useWorkSpaceActions } from '@/store';
import { CustomSelect, IF } from '@/common';
import { $http } from '@/http';
import { getWorkSpaceList } from '@/action/workSpace';
import './index.less';

export default React.memo(() => {
    const intl = useIntl();
    const history = useHistory();
    const [visible, setVisible] = useState(false);
    const { Render: RenderSpace, show } = useAddSpace({});
    const { workspaceId, spaceList } = useSelector((r) => r.workSpaceReducer);
    const { isDetailPage } = useSelector((r) => r.commonReducer);
    const { setCurrentSpace } = useWorkSpaceActions();
    const onEllips = (e: any) => {
        e.stopPropagation();
        setVisible(true);
    };
    const onShowSpace = (val: any) => {
        setVisible(false);
        show(val);
    };
    const onDelete = async () => {
        try {
            await $http.delete(`/workspace/${workspaceId}`);
            getWorkSpaceList();
        } catch (error) {
        }
    };
    const goBack = () => {
        history.push('/main/home');
    };
    const onChangeSpace = (id: number) => {
        setCurrentSpace(id);
        if (!window.location.href.includes('/main/home')) {
            goBack();
        }
    };
    const renderSelect = () => (
        <CustomSelect
            showSearch
            style={{
                width: 150,
                height: 30,
                display: 'flex',
                // lineHeight: '30px',
                // fontSize: 12,
                // position: 'relative',
                // verticalAlign: 'middle',
            }}
            // disabled={disabled}
            size="small"
            placeholder={intl.formatMessage({ id: 'header_top_search_msg' })}
            optionFilterProp="children"
            filterOption={(input, option: any) => option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
            source={spaceList}
            value={workspaceId}
            onChange={onChangeSpace}
            sourceLabelMap="name"
            sourceValueMap="id"
        />
    );
    return (
        <>
            <span className="main-color" style={{ fontSize: 18, fontWeight: 700, marginRight: 20 }}>DataVines</span>
            <IF visible={isDetailPage}>
                <a onClick={goBack} style={{ fontSize: 14, marginLeft: 20 }}>{intl.formatMessage({ id: 'common_back' })}</a>
                <div className="dv-header__work-space" style={{ paddingRight: 0 }}>
                    {renderSelect()}
                </div>
            </IF>
            <IF visible={!isDetailPage}>
                {/* <span className="main-color" style={{ fontSize: 14, marginLeft: 20 }}>{intl.formatMessage({ id: 'datasource' })}</span> */}
                <div className="dv-header__work-space">
                    {renderSelect()}
                    <Dropdown
                        visible={visible}
                        onVisibleChange={($visible: boolean) => {
                            if (!$visible) {
                                setVisible(false);
                            }
                        }}
                        overlay={(
                            <Menu
                                selectable={false}
                                items={[
                                    {
                                        icon: <EditOutlined />,
                                        label: <span onClick={() => (onShowSpace(workspaceId))}>{intl.formatMessage({ id: 'workspace_edit' })}</span>,
                                        key: 'edit',
                                    },
                                    spaceList.length !== 1 && {
                                        icon: <DeleteOutlined style={{ color: '#f81d22' }} />,
                                        label: (
                                            <Popconfirm
                                                title={intl.formatMessage({ id: 'workspace_delete_tip' })}
                                                onConfirm={() => { onDelete(); }}
                                                okText={intl.formatMessage({ id: 'common_Ok' })}
                                                cancelText={intl.formatMessage({ id: 'common_Cancel' })}
                                            >
                                                <span>{intl.formatMessage({ id: 'workspace_delete' })}</span>
                                            </Popconfirm>
                                        ),
                                        key: 'delete',
                                    },
                                ].filter(Boolean) as any}
                            />
                        )}
                    >
                        <EllipsisOutlined
                            onClick={onEllips}
                            style={{
                                fontSize: 14,
                                cursor: 'pointer',
                                color: '#000',
                                right: 5,
                                position: 'absolute',
                                top: '50%',
                                transform: 'translateY(-50%)',
                            }}
                        />
                    </Dropdown>
                </div>
                <Button
                    type="primary"
                    onClick={() => {
                        onShowSpace(undefined);
                    }}
                    style={{
                        marginLeft: 10,
                        height: 32,
                        borderRadius: '16px',
                        display: 'inline-block',
                        verticalAlign: 'middle',
                        marginTop: 10,
                    }}
                >
                    {intl.formatMessage({ id: 'workspace_add' })}
                </Button>
            </IF>
            <RenderSpace />
        </>
    );
});
