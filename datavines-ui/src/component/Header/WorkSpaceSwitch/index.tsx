import React, { useState } from 'react';
import {
    Dropdown, Menu, Popconfirm,
} from 'antd';
import { useIntl } from 'react-intl';
import {
    EditOutlined, EllipsisOutlined, PlusSquareOutlined, DeleteOutlined,
} from '@ant-design/icons';
import { useAddSpace } from './useAddSpace';
import { useSelector, useWorkSpaceActions } from '@/store';
import { CustomSelect } from '@/common';
import { $http } from '@/http';
import { getWorkSpaceList } from '@/action/workSpace';
import './index.less';

export default React.memo(() => {
    const intl = useIntl();
    const [visible, setVisible] = useState(false);
    const { Render: RenderSpace, show } = useAddSpace({});
    const { workspaceId, spaceList } = useSelector((r) => r.workSpaceReducer);
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
        } catch (error: any) {
        }
    };
    const onChangeSpace = (id: number) => {
        setCurrentSpace(id);
    };
    return (
        <div className="dv-header__work-space">
            <CustomSelect
                showSearch
                style={{
                    width: 150,
                    height: 26,
                    fontSize: 12,
                    verticalAlign: 'middle',
                }}
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
            <Dropdown
                visible={visible}
                onVisibleChange={($visible: boolean) => {
                    console.log('visible', $visible);
                    if (!$visible) {
                        setVisible(false);
                    }
                }}
                overlay={(
                    <Menu
                        selectable={false}
                        items={[
                            {
                                icon: <PlusSquareOutlined />,
                                label: <span onClick={() => (onShowSpace(undefined))}>{intl.formatMessage({ id: 'workspace_add' })}</span>,
                                key: 'add',
                            },
                            {
                                icon: <EditOutlined />,
                                label: <span onClick={() => (onShowSpace(workspaceId))}>{intl.formatMessage({ id: 'workspace_edit' })}</span>,
                                key: 'edit',
                            },
                            spaceList.length !== 1 && {
                                icon: <DeleteOutlined style={{ color: '#f81d22' }} />,
                                label: (
                                    <Popconfirm
                                        title={intl.formatMessage({ id: 'common_delete_tip' })}
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
                // trigger={['hover']}
            >
                <EllipsisOutlined onClick={onEllips} style={{ fontSize: 14, cursor: 'pointer', color: '#000' }} />
            </Dropdown>
            <RenderSpace />
        </div>
    );
});
