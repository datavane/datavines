import React, { useState } from 'react';
import { Tree, message } from 'antd';
import {
    DatabaseOutlined, CopyOutlined, DownOutlined, EditOutlined, ReloadOutlined,
} from '@ant-design/icons';
import type { TreeProps } from 'antd/lib/tree';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import { useMetricModal } from '../MetricModal';
import { useEditorContextState } from '../../store/editor';
import { usePersistFn } from '../../common';
import { IDvDataBaseItem } from '../../type';
import useTableCloumn from './useTableCloumn';
import './index.less';

type TIndexProps = {
    getDatabases: (...args: any[]) => void;
}

const Index = ({ getDatabases }: TIndexProps) => {
    const { Render: RenderModal, show } = useMetricModal();
    const [{ databases, id }] = useEditorContextState();
    const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
    const $setExpandedKeys = usePersistFn((key: React.Key, isCancel?: boolean) => {
        if (isCancel) {
            setExpandedKeys(expandedKeys.filter((item) => (item !== key)));
            return;
        }
        setExpandedKeys([...expandedKeys, key]);
    });
    const { onRequestTable, onRequestCloumn } = useTableCloumn({ $setExpandedKeys });

    const onFieldClick = (database: string, table: string, column?: string) => {
        show(id as string, {
            parameterItem: {
                metricParameter: {
                    database,
                    table,
                    column: column || '',
                },
            },
        });
    };

    const renderSingle = (item: IDvDataBaseItem) => ({
        title: <span className="dv-editor-tree-title">{item.name}</span>,
        key: item.name,
        dataName: item.name,
        type: item.type,
        icon: <DatabaseOutlined />,
        children: (item.children || []).map((tableItem) => ({
            title: <span className="dv-editor-tree-title">{tableItem.name}</span>,
            key: `${item.name}_${tableItem.name}`,
            dataName: tableItem.name,
            parentName: item.name,
            type: tableItem.type,
            icon: (
                <span
                    onClick={(e) => {
                        e.stopPropagation();
                    }}
                >
                    <CopyToClipboard
                        text={`select * from ${item.name}.${tableItem.name}`}
                        onCopy={() => {
                            message.success('Copy success');
                        }}
                    >
                        <CopyOutlined />
                    </CopyToClipboard>
                </span>
            ),
            children: (tableItem.children || []).map((fieldItem) => ({
                title: (
                    <span className="dv-editor-tree-title">
                        <EditOutlined
                            onClick={(e) => {
                                e.stopPropagation();
                                onFieldClick(item.name, tableItem.name, fieldItem.name);
                            }}
                        />
                        {' '}
                        {fieldItem.name}
                    </span>
                ),
                className: 'dv-editor-tree-field',
                key: `${item.name}_${tableItem.name}_${fieldItem.name}`,
                dataName: fieldItem.name,
                type: fieldItem.type,
            })),
        })),
    });
    const onSelect: TreeProps['onSelect'] = (selectedKeys, e: any) => {
        if (e.node.children?.length >= 1) {
            if (e.selected) {
                $setExpandedKeys(e.node.key);
            } else {
                $setExpandedKeys(e.node.key, true);
            }
        }
        if (e.node.type === 'DATABASE') {
            onRequestTable(e.node.dataName, e.node.key);
        } else if (e.node.type === 'TABLE') {
            onRequestCloumn(e.node.parentName, e.node.dataName, e.node.key);
        }
    };

    const onExpand = (expandedKeysValue: React.Key[]) => {
        setExpandedKeys(expandedKeysValue);
    };

    return (
        <div className="dv-editor-tree">
            <div className="dv-editor-flex-between">
                <span />
                <span style={{ marginRight: 15 }}>
                    <ReloadOutlined
                        onClick={() => {
                            getDatabases();
                        }}
                    />

                </span>
            </div>
            <div className="dv-editor-tree_list">
                <Tree
                    showIcon
                    switcherIcon={<DownOutlined />}
                    onSelect={onSelect}
                    onExpand={onExpand}
                    expandedKeys={expandedKeys}
                    treeData={databases.map((item) => renderSingle(item))}
                />
            </div>

            <RenderModal />
        </div>
    );
};

export default Index;
